# 보드 수정 권한 검증 플로우

```mermaid
flowchart TD
    A[사용자가 보드 수정 요청] --> B[JWT 토큰 검증]
    B -->|실패| C[401 Unauthorized]
    B -->|성공| D[사용자 ID 추출]
    
    D --> E[워크스페이스 소유자인가?]
    E -->|예| F[✅ 수정 허용]
    E -->|아니오| G[해당 보드 접근 권한 확인]
    
    G --> H[보드 멤버 테이블 조회]
    H --> I{보드 멤버인가?}
    I -->|아니오| J[❌ 403 Forbidden]
    I -->|예| K[보드에서의 역할 확인]
    
    K --> L{보드 OWNER인가?}
    L -->|예| F
    L -->|아니오| M{보드 EDITOR인가?}
    M -->|예| F
    M -->|아니오| N{보드 VIEWER인가?}
    N -->|예| J
    N -->|아니오| J
    
    F --> O[보드 정보 업데이트]
    O --> P[활동 로그 기록]
    P --> Q[200 OK 응답]
```

## 권한 검증 코드 예시

```javascript
async function checkBoardEditPermission(userId, boardId) {
  // 1. 보드 정보 조회
  const board = await boardRepository.findById(boardId);
  if (!board) {
    throw new NotFoundException('보드를 찾을 수 없습니다');
  }
  
  // 2. 워크스페이스 소유자 확인 (최고 권한)
  const workspace = await workspaceRepository.findById(board.workspaceId);
  if (workspace.ownerId === userId) {
    return { allowed: true, reason: 'workspace_owner' };
  }
  
  // 3. 보드 멤버십 확인
  const boardMember = await boardMemberRepository.findByUserAndBoard(userId, boardId);
  if (!boardMember) {
    throw new ForbiddenException('보드에 접근할 권한이 없습니다');
  }
  
  // 4. 보드 역할 확인
  if (boardMember.role === 'OWNER' || boardMember.role === 'EDITOR') {
    return { allowed: true, reason: `board_${boardMember.role.toLowerCase()}` };
  }
  
  throw new ForbiddenException('보드를 수정할 권한이 없습니다');
}
```