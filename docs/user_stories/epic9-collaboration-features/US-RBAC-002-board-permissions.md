# US-RBAC-002: 보드 권한 관리 `[P0]`

**As a** 보드 소유자  
**I want to** 멤버별로 적절한 권한을 부여하여  
**So that** 보드를 안전하게 관리할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 보드 소유자 권한
```gherkin
Given 사용자가 보드 소유자(OWNER)일 때
When 보드에 접근하면
Then 'board:read', 'board:write', 'board:delete', 'board:manage', 'board:invite' 권한을 가지고
And 'content:read', 'content:write', 'content:delete' 권한을 가지고
And 모든 보드 설정을 변경할 수 있고
And 멤버를 초대하고 역할을 변경할 수 있다
```

### ✅ 성공 케이스 - 보드 편집자 권한
```gherkin
Given 사용자가 보드 편집자(EDITOR)일 때
When 보드에 접근하면
Then 'board:read', 'board:write' 권한을 가지고
And 'content:read', 'content:write', 'content:delete' 권한을 가지고
And 보드 정보를 수정할 수 있지만
And 보드 삭제나 멤버 관리는 할 수 없다
```

### ✅ 성공 케이스 - 보드 조회자 권한
```gherkin
Given 사용자가 보드 조회자(VIEWER)일 때
When 보드에 접근하면
Then 'board:read', 'content:read' 권한만 가지고
And 모든 카드와 리스트를 조회할 수 있고
And 댓글을 읽을 수 있지만
And 카드나 리스트의 수정/생성/삭제는 할 수 없다
```

### ❌ 실패 케이스 - VIEWER의 편집 시도
```gherkin
Given 사용자가 보드 조회자(VIEWER)이고
When 카드를 생성하거나 수정하려고 시도하면
Then HTTP 403 Forbidden 응답이 반환되고
And "이 작업을 수행할 권한이 없습니다" 오류 메시지가 표시되고
And 작업이 차단된다
```

**Priority:** High | **Story Points:** 6
