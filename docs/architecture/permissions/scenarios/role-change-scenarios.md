# 워크스페이스 멤버 역할 변경 시나리오

## 시나리오 1: MEMBER → OWNER 승격

### 상황
- 김대표(현재 OWNER)가 이부장(현재 MEMBER)을 OWNER로 승격
- 이부장은 여러 보드에 참여 중

### 변경 과정
```javascript
async function promoteToWorkspaceOwner(currentOwnerId, targetUserId, workspaceId) {
  // 1. 권한 검증 - 현재 소유자만 가능
  if (currentOwnerId !== workspace.ownerId) {
    throw new ForbiddenException('워크스페이스 소유자만 다른 멤버를 승격시킬 수 있습니다');
  }
  
  // 2. 대상이 워크스페이스 정식 멤버인지 확인
  const targetMember = await workspaceMemberRepository.findByUserAndWorkspace(targetUserId, workspaceId);
  if (!targetMember || targetMember.type !== 'MEMBER') {
    throw new BadRequestException('워크스페이스 정식 멤버만 소유자로 승격할 수 있습니다');
  }
  
  // 3. 역할 변경
  await workspaceMemberRepository.updateRole(targetUserId, workspaceId, 'OWNER');
  
  // 4. 기존 소유자를 MEMBER로 변경
  await workspaceMemberRepository.updateRole(currentOwnerId, workspaceId, 'MEMBER');
  
  // 5. 워크스페이스 소유자 필드 업데이트
  await workspaceRepository.updateOwner(workspaceId, targetUserId);
}
```

### 결과
- 이부장: 워크스페이스의 모든 보드에 대한 절대 권한 획득
- 김대표: 기존 보드 권한은 유지하되 워크스페이스 관리 권한 상실
- 모든 공개 보드에 대한 접근 권한은 계속 유지 (MEMBER 권한으로)

---

## 시나리오 2: BOARD_ONLY → MEMBER 승격

### 상황
- 외부 협력업체 직원이 BOARD_ONLY 타입으로 특정 보드에만 참여
- 정식 팀원으로 합류하여 MEMBER로 승격

### 변경 과정
```javascript
async function promoteToWorkspaceMember(workspaceOwnerId, targetUserId, workspaceId) {
  // 1. 권한 검증
  const workspace = await workspaceRepository.findById(workspaceId);
  if (workspaceOwnerId !== workspace.ownerId) {
    throw new ForbiddenException('워크스페이스 소유자만 멤버 타입을 변경할 수 있습니다');
  }
  
  // 2. 현재 BOARD_ONLY 타입인지 확인
  const targetMember = await workspaceMemberRepository.findByUserAndWorkspace(targetUserId, workspaceId);
  if (!targetMember || targetMember.type !== 'BOARD_ONLY') {
    throw new BadRequestException('BOARD_ONLY 타입의 사용자만 MEMBER로 승격할 수 있습니다');
  }
  
  // 3. MEMBER 타입으로 변경
  await workspaceMemberRepository.updateType(targetUserId, workspaceId, 'MEMBER');
  
  // 4. 모든 공개 보드에 자동 권한 부여
  const publicBoards = await boardRepository.findPublicByWorkspace(workspaceId);
  for (const board of publicBoards) {
    const existingAccess = await boardMemberRepository.findByUserAndBoard(targetUserId, board.id);
    if (!existingAccess) {
      await boardMemberRepository.create({
        userId: targetUserId,
        boardId: board.id,
        role: 'EDITOR',
        invitationType: 'WORKSPACE_AUTO',
        joinedAt: new Date()
      });
    }
  }
}
```

### 결과
- 기존 개별 초대받은 보드: 권한 유지
- 새로운 공개 보드: 자동 EDITOR 권한 부여
- 워크스페이스 정보 접근 가능
- 새로운 보드 생성 권한 획득

---

## 시나리오 3: 역할 변경 시 권한 충돌 해결

### 상황
- BOARD_ONLY 사용자가 특정 보드에 VIEWER 권한
- MEMBER로 승격 시 해당 보드가 공개 보드인 경우

### 권한 우선순위 적용
```javascript
// 워크스페이스 MEMBER는 공개 보드에 최소 EDITOR 권한을 가져야 함
if (board.isPublic && targetMember.type === 'MEMBER') {
  const currentBoardAccess = await boardMemberRepository.findByUserAndBoard(targetUserId, board.id);
  if (currentBoardAccess && currentBoardAccess.role === 'VIEWER') {
    // VIEWER → EDITOR로 자동 승격
    await boardMemberRepository.updateRole(targetUserId, board.id, 'EDITOR');
    await boardMemberRepository.updateInvitationType(targetUserId, board.id, 'WORKSPACE_AUTO');
  }
}
```

### 결과
- 워크스페이스 권한이 보드별 권한보다 우선
- VIEWER → EDITOR로 자동 승격
- 개별 초대 상태에서 워크스페이스 자동 권한으로 변경