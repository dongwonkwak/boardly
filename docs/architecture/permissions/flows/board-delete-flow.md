# 보드 삭제 권한 검증 플로우

```mermaid
flowchart TD
    A[사용자가 보드 삭제 요청] --> B[JWT 토큰 검증]
    B -->|실패| C[401 Unauthorized]
    B -->|성공| D[사용자 ID 추출]
    
    D --> E[워크스페이스 소유자인가?]
    E -->|예| F[✅ 삭제 허용]
    E -->|아니오| G[보드 멤버십 확인]
    
    G --> H[보드 멤버 테이블 조회]
    H --> I{보드 OWNER인가?}
    I -->|예| F
    I -->|아니오| J[❌ 403 Forbidden - 보드 소유자만 삭제 가능]
    
    F --> K[삭제 확인 대화상자 검증]
    K --> L{보드명 입력 확인됨?}
    L -->|아니오| M[❌ 400 Bad Request - 보드명 불일치]
    L -->|예| N[보드 소프트 삭제 처리]
    
    N --> O[보드 상태를 'DELETED'로 변경]
    O --> P[30일 후 영구 삭제 스케줄 설정]
    P --> Q[모든 보드 멤버 접근 권한 제거]
    Q --> R[활동 로그 기록]
    R --> S[보드 멤버들에게 삭제 알림]
    S --> T[200 OK 응답]
```

## 보드 삭제 검증 코드

```javascript
async function deleteBoardWithPermissionCheck(userId, boardId, confirmationData) {
  // 1. 권한 검증
  const board = await boardRepository.findById(boardId);
  if (!board) {
    throw new NotFoundException('보드를 찾을 수 없습니다');
  }
  
  // 2. 워크스페이스 소유자 또는 보드 소유자만 삭제 가능
  const workspace = await workspaceRepository.findById(board.workspaceId);
  const boardMember = await boardMemberRepository.findByUserAndBoard(userId, boardId);
  
  const isWorkspaceOwner = workspace.ownerId === userId;
  const isBoardOwner = boardMember?.role === 'OWNER';
  
  if (!isWorkspaceOwner && !isBoardOwner) {
    throw new ForbiddenException('보드를 삭제할 권한이 없습니다. 보드 소유자만 삭제할 수 있습니다');
  }
  
  // 3. 삭제 확인 검증
  if (confirmationData.boardName !== board.name) {
    throw new BadRequestException('보드명이 일치하지 않습니다');
  }
  
  // 4. 소프트 삭제 처리
  return await boardDeletionService.softDelete(boardId, userId);
}
```