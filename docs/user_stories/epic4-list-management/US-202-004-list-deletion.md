# US-202-004: 리스트 삭제 `[P0]`

**As a** 사용자  
**I want to** 불필요한 리스트를 삭제하여  
**So that** 보드를 깔끔하게 정리할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 리스트 삭제
```gherkin
Given 사용자가 보드에 있고
And 리스트가 존재하고
And 사용자가 'content:delete' 권한을 가지고 있을 때
When 리스트 메뉴에서 삭제 옵션을 선택하고
And 삭제 확인 대화상자에서 확인하면
Then 리스트와 포함된 모든 카드가 삭제되고
And 다른 리스트들의 position이 자동으로 조정되고
And "리스트가 삭제되었습니다" 메시지가 표시되고
And 활동 내역에 "리스트 삭제" 기록이 남는다
```

### ❌ 실패 케이스 - 권한 없는 사용자의 리스트 삭제 시도
```gherkin
Given 사용자가 'content:delete' 권한이 없을 때
When 리스트 삭제를 시도하면
Then HTTP 403 Forbidden 응답이 반환되고
And "리스트를 삭제할 권한이 없습니다" 오류 메시지가 표시되고
And 삭제가 진행되지 않는다
```

**Priority:** Medium | **Story Points:** 3
