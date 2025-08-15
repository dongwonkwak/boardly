# US-201-001: 보드 생성 `[P0]`

**As a** PM  
**I want to** 새로운 프로젝트를 시작할 때 해당 프로젝트를 위한 보드를 생성하고  
**So that** 프로젝트 작업을 체계적으로 관리할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 보드 생성
```gherkin
Given 사용자가 워크스페이스에 접근하고
And 워크스페이스당 보드 수가 50개 미만이고
And 사용자가 'board:create' 권한을 가지고 있을 때
When 보드 생성 버튼을 클릭하고
And 보드 이름과 설명을 입력하고
And 생성 버튼을 클릭하면
Then 새로운 보드가 생성되고
And 사용자가 보드 소유자(OWNER) 역할을 가지게 되고
And 보드 상세 페이지로 이동하고
And 활동 내역에 "보드 생성" 기록이 남는다
```

### ❌ 실패 케이스 - 보드 수 제한 초과
```gherkin
Given 워크스페이스에 이미 50개의 보드가 있을 때
When 새로운 보드를 생성하려고 시도하면
Then HTTP 422 Unprocessable Entity 응답이 반환되고
And "워크스페이스당 최대 50개의 보드만 생성할 수 있습니다" 오류 메시지가 표시되고
And 보드 생성이 진행되지 않는다
```

### ❌ 실패 케이스 - 빈 보드 이름
```gherkin
Given 사용자가 보드 생성 모달에 있고
And 보드 이름을 입력하지 않았을 때
When 생성 버튼을 클릭하면
Then HTTP 400 Bad Request 응답이 반환되고
And "보드 이름을 입력해주세요" 오류 메시지가 표시되고
And 보드 생성이 진행되지 않는다
```

### ❌ 실패 케이스 - 권한 없는 사용자의 보드 생성 시도
```gherkin
Given 사용자가 'board:create' 권한이 없을 때
When 보드 생성을 시도하면
Then HTTP 403 Forbidden 응답이 반환되고
And "보드를 생성할 권한이 없습니다" 오류 메시지가 표시되고
And 보드 생성이 진행되지 않는다
```

**Priority:** High | **Story Points:** 5
