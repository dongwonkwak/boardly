# US-UX-003: 키보드 단축키 `[P0]`

**As a** 파워 유저  
**I want to** 마우스 없이도 빠르게 작업을 수행하고  
**So that** 효율성을 극대화할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 카드 생성 단축키
```gherkin
Given 사용자가 보드에 있을 때
When Ctrl+Enter (또는 Cmd+Enter) 키를 누르면
Then 현재 포커스된 리스트에 새 카드 생성 모달이 열리고
And 제목 입력 필드에 포커스가 설정된다
```

### ✅ 성공 케이스 - 검색 단축키
```gherkin
Given 사용자가 보드나 대시보드에 있을 때
When Ctrl+K (또는 Cmd+K) 키를 누르면
Then 검색창에 포커스가 설정되고
And 기존 검색어가 있다면 전체 선택된다
```

### ✅ 성공 케이스 - 카드 상세 닫기
```gherkin
Given 사용자가 카드 상세 모달을 보고 있을 때
When ESC 키를 누르면
Then 모달이 닫히고
And 변경사항이 있다면 저장 확인 대화상자가 표시된다
```

### ✅ 성공 케이스 - 키보드 네비게이션
```gherkin
Given 사용자가 보드에 있을 때
When 화살표 키를 사용하면
Then 카드 간에 포커스를 이동할 수 있고
And Enter 키로 포커스된 카드를 열 수 있고
And Tab 키로 인터페이스 요소 간 이동이 가능하다
```

### ✅ 성공 케이스 - 단축키 도움말
```gherkin
Given 사용자가 서비스를 이용하고 있을 때
When ? 키를 누르면
Then 사용 가능한 키보드 단축키 목록이 모달로 표시되고
And 각 단축키의 기능 설명이 제공된다
```

**Priority:** Medium | **Story Points:** 6
