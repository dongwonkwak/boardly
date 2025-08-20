# 보드 멤버 제거 권한 검증 플로우

```mermaid
flowchart TD
    A[사용자가 보드 멤버 제거 요청] --> B[JWT 토큰 검증]
    B -->|실패| C[401 Unauthorized]
    B -->|성공| D[사용자 ID 추출]
    
    D --> E[워크스페이스 소유자인가?]
    E -->|예| F[✅ 제거 허용]
    E -->|아니오| G[보드 멤버십 확인]
    
    G --> H[보드 멤버 테이블 조회]
    H --> I{보드 OWNER인가?}
    I -->|예| F
    I -->|아니오| J[❌ 403 Forbidden - 보드 소유자만 멤버 제거 가능]
    
    F --> K[제거 대상 멤버 검증]
    K --> L{대상이 보드 멤버인가?}
    L -->|아니오| M[❌ 404 Not Found - 멤버가 아님]
    L -->|예| N{자기 자신을 제거하려는가?}
    
    N -->|예| O{보드 OWNER가 1명뿐인가?}
    N -->|아니오| P[다른 멤버 제거 처리]
    
    O -->|예| Q[❌ 400 Bad Request - 마지막 소유자는 제거 불가]
    O -->|아니오| R[자기 자신 제거 처리]
    
    P --> S[대상 멤버 보드 접근 권한 제거]
    R --> S
    
    S --> T[워크스페이스 멤버십 확인]
    T --> U{BOARD_ONLY 타입인가?}
    U -->|예| V[다른 보드 접근 권한 확인]
    U -->|아니오| W[워크스페이스 멤버십 유지]
    
    V --> X{다른 보드 접근 권한이 있는가?}
    X -->|아니오| Y[워크스페이스 멤버십도 제거]
    X -->|예| W
    
    Y --> Z[카드 담당자에서 제거]
    W --> Z
    Z --> AA[활동 로그 기록]
    AA --> BB[제거된 멤버에게 알림]
    BB --> CC[200 OK 응답]
```

## 보드 멤버 제거 검증 코드

```javascript
async function removeBoardMemberWithPermissionCheck(removerId, boardId, targetUserId) {
  // 1. 제거자 권한 검증
  const board = await boardRepository.findById(boardId);
  if (!board) {
    throw new NotFoundException('보드를 찾을 수 없습니다');
  }
  
  const workspace = await workspaceRepository.findById(board.workspaceId);
  const removerBoardMember = await boardMemberRepository.findByUserAndBoard(removerId, boardId);
  
  const isWorkspaceOwner = workspace.ownerId === removerId;
  const isBoardOwner = removerBoardMember?.role === 'OWNER';
  
  if (!isWorkspaceOwner && !isBoardOwner) {
    throw new ForbiddenException('보드 멤버를 제거할 권한이 없습니다');
  }
  
  // 2. 제거 대상 검증
  const targetBoardMember = await boardMemberRepository.findByUserAndBoard(targetUserId, boardId);
  if (!targetBoardMember) {
    throw new NotFoundException('해당 사용자는 보드 멤버가 아닙니다');
  }
  
  // 3. 자기 자신 제거 시 추가 검증
  if (removerId === targetUserId) {
    const boardOwnerCount = await boardMemberRepository.countOwners(boardId);
    if (targetBoardMember.role === 'OWNER' && boardOwnerCount === 1) {
      throw new BadRequestException('마지막 보드 소유자는 보드를 떠날 수 없습니다');
    }
  }
  
  // 4. 멤버 제거 처리
  return await removeBoardMemberComplete(targetUserId, boardId, removerId);
}

async function removeBoardMemberComplete(userId, boardId, removedBy) {
  // 1. 보드 멤버십 제거
  await boardMemberRepository.deleteByUserAndBoard(userId, boardId);
  
  // 2. 카드 담당자에서 제거
  await cardMemberRepository.deleteByUserAndBoard(userId, boardId);
  
  // 3. 워크스페이스 멤버십 확인
  const board = await boardRepository.findById(boardId);
  const workspaceMember = await workspaceMemberRepository.findByUserAndWorkspace(userId, board.workspaceId);
  
  if (workspaceMember?.type === 'BOARD_ONLY') {
    // 다른 보드 접근 권한이 있는지 확인
    const otherBoardAccess = await boardMemberRepository.countByUserAndWorkspace(userId, board.workspaceId);
    
    if (otherBoardAccess === 0) {
      // 다른 보드 접근 권한이 없으면 워크스페이스 멤버십도 제거
      await workspaceMemberRepository.deleteByUserAndWorkspace(userId, board.workspaceId);
    }
  }
  
  // 4. 활동 로그 기록
  await activityRepository.create({
    type: 'BOARD_REMOVE_MEMBER',
    userId: removedBy,
    boardId,
    payload: {
      removedUserId: userId,
      removedBy: removedBy === userId ? 'self' : 'admin'
    }
  });
  
  // 5. 알림 발송
  if (removedBy !== userId) {
    await notificationService.sendBoardRemovalNotification(userId, boardId);
  }
}
```