# 복잡한 권한 상황 해결 시나리오

## 시나리오 1: 워크스페이스 소유자 변경 시 기존 보드 권한 처리

### 상황
- A사 대표이사(김대표)가 CTO(이부장)에게 워크스페이스 소유권 이양
- 김대표가 개인적으로 소유한 보드들이 다수 존재
- 일부 보드는 이부장이 접근 권한이 없었던 비공개 보드

### 해결 과정
```javascript
async function transferWorkspaceOwnership(currentOwnerId, newOwnerId, workspaceId) {
  // 1. 기존 소유자의 개인 보드들 처리
  const ownerBoards = await boardRepository.findByOwnerAndWorkspace(currentOwnerId, workspaceId);
  
  for (const board of ownerBoards) {
    // 새 워크스페이스 소유자에게 보드 접근 권한 부여
    const existingAccess = await boardMemberRepository.findByUserAndBoard(newOwnerId, board.id);
    if (!existingAccess) {
      await boardMemberRepository.create({
        userId: newOwnerId,
        boardId: board.id,
        role: 'EDITOR', // 워크스페이스 소유자 권한으로 접근
        invitationType: 'WORKSPACE_AUTO',
        joinedAt: new Date()
      });
    }
  }
  
  // 2. 워크스페이스 소유권 이전
  await workspaceRepository.updateOwner(workspaceId, newOwnerId);
  await workspaceMemberRepository.updateRole(newOwnerId, workspaceId, 'OWNER');
  await workspaceMemberRepository.updateRole(currentOwnerId, workspaceId, 'MEMBER');
  
  // 3. 기존 소유자의 보드 소유권은 유지 (보드 OWNER 역할 유지)
  // 워크스페이스 권한과 보드 권한은 독립적으로 관리
}
```

### 결과
- 이부장: 모든 보드에 대한 워크스페이스 소유자 권한 획득
- 김대표: 자신이 만든 보드의 보드 OWNER 권한은 유지
- 김대표: 워크스페이스 MEMBER로 강등되지만 기존 보드 접근은 유지

---

## 시나리오 2: 보드 소유자가 워크스페이스를 떠날 때

### 상황
- 프리랜서 디자이너가 BOARD_ONLY 타입으로 특정 보드의 OWNER
- 프로젝트 종료로 워크스페이스에서 제거되어야 함
- 해당 보드에는 다른 팀원들도 참여 중

### 해결 과정
```javascript
async function handleBoardOwnerRemoval(boardId, leavingUserId, workspaceOwnerId) {
  // 1. 보드 소유권 이전 확인
  const otherBoardMembers = await boardMemberRepository.findByBoardExcludingUser(boardId, leavingUserId);
  const newOwnerCandidate = otherBoardMembers.find(member => 
    member.role === 'EDITOR' && member.invitationType === 'WORKSPACE_AUTO'
  );
  
  if (!newOwnerCandidate) {
    // 적절한 후계자가 없으면 워크스페이스 소유자가 보드 소유자가 됨
    await boardMemberRepository.create({
      userId: workspaceOwnerId,
      boardId: boardId,
      role: 'OWNER',
      invitationType: 'INDIVIDUAL',
      joinedAt: new Date()
    });
  } else {
    // 기존 EDITOR를 OWNER로 승격
    await boardMemberRepository.updateRole(newOwnerCandidate.userId, boardId, 'OWNER');
  }
  
  // 2. 떠나는 사용자의 모든 권한 제거
  await boardMemberRepository.deleteByUserAndBoard(leavingUserId, boardId);
  await workspaceMemberRepository.deleteByUserAndWorkspace(leavingUserId, workspaceId);
  
  // 3. 카드 담당자에서도 제거
  await cardMemberRepository.removeFromAllCards(leavingUserId, boardId);
}
```

### 결과
- 보드 연속성 보장 (새로운 소유자 확정)
- 떠나는 사용자의 모든 흔적 정리
- 프로젝트 진행에 지장 없음

---

## 시나리오 3: 순환 권한 참조 방지

### 상황
- 사용자 A가 보드 X의 OWNER
- 사용자 B가 보드 Y의 OWNER  
- A가 B를 보드 X에 OWNER로 초대하려 함
- B가 A를 보드 Y에 OWNER로 초대하려 함

### 해결 방안
```javascript
async function validateBoardOwnerInvitation(inviterId, boardId, targetUserId, targetRole) {
  if (targetRole !== 'OWNER') {
    return true; // OWNER가 아니면 순환 참조 우려 없음
  }
  
  // 다중 보드 소유자 허용 정책
  const inviterOwnedBoards = await boardMemberRepository.findOwnedBoardsByUser(inviterId);
  const targetOwnedBoards = await boardMemberRepository.findOwnedBoardsByUser(targetUserId);
  
  // 현재는 다중 보드 소유자를 허용하므로 순환 참조 검증 생략
  // 필요시 비즈니스 규칙에 따라 제한 가능
  
  return true;
}
```

### 결과
- 현재 MVP에서는 다중 보드 소유자 허용
- 순환 참조는 비즈니스 로직상 문제없음
- 향후 필요시 제한 정책 추가 가능

---

## 시나리오 4: 대량 권한 변경 시 성능 최적화

### 상황
- 100명 규모의 대기업 워크스페이스
- 50개의 보드가 있는 상황에서 공개 보드 하나를 비공개로 변경

### 최적화된 처리
```javascript
async function bulkUpdateBoardVisibility(boardId, isPublic, requesterId) {
  if (!isPublic) {
    // 공개 → 비공개: 기존 권한 타입만 일괄 변경
    await boardMemberRepository.bulkUpdateInvitationType(
      boardId, 
      'WORKSPACE_AUTO', 
      'INDIVIDUAL',
      requesterId
    );
  } else {
    // 비공개 → 공개: 워크스페이스 멤버들에게 일괄 권한 부여
    const workspaceMembers = await workspaceMemberRepository.findByWorkspaceAndType(
      workspace.id, 
      'MEMBER'
    );
    
    const newBoardMembers = workspaceMembers
      .filter(member => !hasBoardAccess(member.userId, boardId))
      .map(member => ({
        userId: member.userId,
        boardId: boardId,
        role: 'EDITOR',
        invitationType: 'WORKSPACE_AUTO',
        joinedAt: new Date()
      }));
    
    await boardMemberRepository.bulkCreate(newBoardMembers);
  }
  
  // 실시간 알림은 비동기로 처리
  setImmediate(() => {
    notifyBoardVisibilityChange(boardId, isPublic);
  });
}
```

### 결과
- 대량 데이터 처리 시 배치 처리로 성능 향상
- 실시간 알림은 비동기로 분리하여 응답 시간 최적화
- 트랜잭션 범위 최소화로 데드락 방지