# US-202-002: 리스트 편집 `[P0]`

**As a** 사용자  
**I want to** 프로젝트 진행에 따라 리스트 이름을 변경하고  
**So that** 시각적 구분을 위해 색상을 설정할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 리스트 이름 변경
```gherkin
Given 사용자가 보드에 있고
And 리스트가 존재하고
And 사용자가 'content:write' 권한을 가지고 있을 때
When 리스트 헤더를 클릭하고
And 새로운 리스트 이름을 입력하고
And Enter 키를 누르거나 외부 클릭하면
Then 리스트 이름이 업데이트되고
And 변경사항이 즉시 반영되고
And 활동 내역에 "리스트 이름 변경" 기록이 남는다
```

### ✅ 성공 케이스 - 리스트 색상 변경
```gherkin
Given 사용자가 보드에 있고
And 리스트가 존재하고
And 사용자가 'content:write' 권한을 가지고 있을 때
When 리스트 메뉴를 클릭하고
And 색상 변경 옵션을 선택하고
And 새로운 색상을 선택하면
Then 리스트 헤더가 새로운 색상으로 변경되고
And 변경사항이 즉시 반영되고
And 활동 내역에 "리스트 색상 변경" 기록이 남는다
```

### ❌ 실패 케이스 - 빈 리스트 이름으로 변경
```gherkin
Given 사용자가 리스트 이름을 편집하고 있고
And 리스트 이름을 비워두었을 때
When Enter 키를 누르면
Then HTTP 400 Bad Request 응답이 반환되고
And "리스트 이름을 입력해주세요" 오류 메시지가 표시되고
And 이전 이름으로 되돌아간다
```

**Priority:** Medium | **Story Points:** 3
