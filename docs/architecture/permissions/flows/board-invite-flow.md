# 보드 멤버 초대 권한 검증 플로우

```mermaid
flowchart TD
    A[사용자가 보드 멤버 초대 요청] --> B[JWT 토큰 검증]
    B -->|실패| C[401 Unauthorized]
    B -->|성공| D[사용자 ID 추출]
    
    D --> E[워크스페이스 소유자인가?]
    E -->|예| F[✅ 초대 허용]
    E -->|아니오| G[보드 멤버십 확인]
    
    G --> H[보드 멤버 테이블 조회]
    H --> I{보드 OWNER인가?}
    I -->|예| F
    I -->|아니오| J[❌ 403 Forbidden - 보드 소유자만 초대 가능]
    
    F --> K[초대 대상 사용자 검증]
    K --> L{이미 보드 멤버인가?}
    L -->|예| M[❌ 409 Conflict - 이미 멤버임]
    L -->|아니오| N[초대할 역할 검증]
    
    N --> O{유효한 역할인가?<br/>EDITOR/VIEWER}
    O -->|아니오| P[❌ 400 Bad Request - 잘못된 역할]
    O -->|예| Q[초대 처리 분기]
    
    Q --> R{기존 사용자인가?}
    R -->|예| S[직접 보드 멤버 추가]
    R -->|아니오| T[초대 링크/이메일 발송]
    
    S --> U[워크스페이스 멤버십 확인]
    U --> V{워크스페이스 멤버인가?}
    V -->|아니오| W[BOARD_ONLY 타입으로 워크스페이스 멤버 추가]
    V -->|예| X[기존 워크스페이스 멤버십 유지]
    
    W --> Y[보드 멤버 추가]
    X --> Y
    T --> Z[초대 테이블에 초대 정보 저장]
    Z --> AA[초대 이메일 발송]
    
    Y --> BB[활동 로그 기록]
    AA --> BB
    BB --> CC[200 OK 응답]
```

## 보드 멤버 초대 검증 코드

```javascript
async function inviteBoardMemberWithPermissionCheck(inviterId, boardId, inviteData) {
  // 1. 초대자 권한 검증
  const board = await boardRepository.findById(boardId);
  if (!board) {
    throw new NotFoundException('보드를 찾을 수 없습니다');
  }
  
  const workspace = await workspaceRepository.findById(board.workspaceId);
  const boardMember = await boardMemberRepository.findByUserAndBoard(inviterId, boardId);
  
  const isWorkspaceOwner = workspace.ownerId === inviterId;
  const isBoardOwner = boardMember?.role === 'OWNER';
  
  if (!isWorkspaceOwner && !isBoardOwner) {
    throw new ForbiddenException('보드에 멤버를 초대할 권한이 없습니다');
  }
  
  // 2. 초대 대상 검증
  const targetUser = await userRepository.findByEmail(inviteData.email);
  
  if (targetUser) {
    // 기존 사용자인 경우
    const existingBoardMember = await boardMemberRepository.findByUserAndBoard(targetUser.id, boardId);
    if (existingBoardMember) {
      throw new ConflictException('이미 보드의 멤버입니다');
    }
    
    // 직접 멤버 추가
    return await addBoardMemberDirectly(targetUser.id, boardId, inviteData.role, inviterId);
  } else {
    // 신규 사용자인 경우 - 초대 링크/이메일 발송
    return await createBoardInvitation(inviteData.email, boardId, inviteData.role, inviterId);
  }
}

async function addBoardMemberDirectly(userId, boardId, role, invitedBy) {
  // 워크스페이스 멤버십 확인 및 설정
  const board = await boardRepository.findById(boardId);
  const workspaceMember = await workspaceMemberRepository.findByUserAndWorkspace(userId, board.workspaceId);
  
  if (!workspaceMember) {
    // BOARD_ONLY 타입으로 워크스페이스 멤버 추가
    await workspaceMemberRepository.create({
      userId,
      workspaceId: board.workspaceId,
      role: 'MEMBER',
      type: 'BOARD_ONLY',
      invitedBy,
      joinedAt: new Date()
    });
  }
  
  // 보드 멤버 추가
  await boardMemberRepository.create({
    userId,
    boardId,
    role,
    invitedBy,
    invitationType: 'INDIVIDUAL',
    joinedAt: new Date()
  });
  
  // 활동 로그 기록
  await activityRepository.create({
    type: 'BOARD_ADD_MEMBER',
    userId: invitedBy,
    boardId,
    payload: {
      memberId: userId,
      memberRole: role
    }
  });
}
```