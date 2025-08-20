# 보드 공개/비공개 설정 변경 시나리오

## 시나리오 1: 비공개 → 공개 변경

### 상황
- 김팀장(워크스페이스 OWNER)이 "기밀 프로젝트" 비공개 보드를 공개로 변경
- 기존 멤버: 이개발자(EDITOR), 박디자이너(VIEWER)
- 워크스페이스 정식 멤버: 최신입사원, 정인턴

### 변경 과정
```javascript
// 1. 권한 검증
if (!isWorkspaceOwner && !isBoardOwner) {
  throw new ForbiddenException('보드 공개 설정을 변경할 권한이 없습니다');
}

// 2. 워크스페이스 정식 멤버들에게 자동 권한 부여
const workspaceMembers = await getWorkspaceMembers(workspaceId, 'MEMBER');
workspaceMembers.forEach(async member => {
  if (!hasBoardAccess(member.userId, boardId)) {
    await addBoardMember(boardId, member.userId, 'EDITOR', 'WORKSPACE_AUTO');
  }
});

// 3. 실시간 UI 업데이트
notifyWorkspaceMembers(workspaceId, {
  type: 'BOARD_MADE_PUBLIC',
  boardId: boardId,
  boardName: '기밀 프로젝트'
});
```

### 결과
- 최신입사원, 정인턴: 자동으로 EDITOR 권한 부여
- 이개발자: 기존 EDITOR 권한 유지
- 박디자이너: 기존 VIEWER 권한 유지
- 모든 워크스페이스 멤버의 보드 목록에 "기밀 프로젝트" 표시

---

## 시나리오 2: 공개 → 비공개 변경

### 상황
- 김팀장이 "마케팅 캠페인" 공개 보드를 비공개로 변경
- 기존 접근자: 모든 워크스페이스 멤버 (EDITOR 권한)

### 변경 과정
```javascript
// 1. 기존 WORKSPACE_AUTO 권한을 INDIVIDUAL로 변환
const autoMembers = await getBoardMembersByType(boardId, 'WORKSPACE_AUTO');
autoMembers.forEach(async member => {
  await updateBoardMember(member.userId, boardId, {
    invitationType: 'INDIVIDUAL',
    invitedBy: requesterId,
    invitedAt: new Date()
  });
});

// 2. 보드 공개 상태 변경
await updateBoard(boardId, { isPublic: false });
```

### 결과
- 모든 기존 멤버의 권한은 유지 (EDITOR → EDITOR)
- 권한 부여 방식만 변경 (WORKSPACE_AUTO → INDIVIDUAL)
- 새로운 워크스페이스 멤버는 개별 초대를 통해서만 접근 가능

---

## 시나리오 3: Personal 워크스페이스에서 공개 설정 시도

### 상황
- 개인 사용자가 Personal 워크스페이스의 보드 공개 설정 변경 시도

### 결과
```javascript
if (workspace.type === 'PERSONAL') {
  throw new BadRequestException('개인 워크스페이스에서는 공개 설정을 변경할 수 없습니다');
}
```

- UI에서 공개 설정 옵션 자체가 표시되지 않음
- API 호출 시 400 에러 반환