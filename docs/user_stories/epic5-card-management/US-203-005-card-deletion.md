# US-203-005: 카드 삭제 `[P0]`

**As a** 사용자  
**I want to** 불필요한 카드를 삭제하여  
**So that** 보드를 깔끔하게 정리할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 카드 삭제
```gherkin
Given 사용자가 카드 상세 모달에 있고
And 사용자가 'content:delete' 권한을 가지고 있을 때
When 삭제 버튼을 클릭하고
And 삭제 확인 대화상자에서 확인하면
Then 카드가 삭제되고
And 관련된 모든 데이터(댓글, 라벨, 담당자)가 함께 삭제되고
And 다른 카드들의 position이 자동으로 조정되고
And "카드가 삭제되었습니다" 메시지가 표시되고
And 활동 내역에 "카드 삭제" 기록이 남는다
```

### ❌ 실패 케이스 - 권한 없는 사용자의 카드 삭제 시도
```gherkin
Given 사용자가 'content:delete' 권한이 없을 때
When 카드 삭제를 시도하면
Then HTTP 403 Forbidden 응답이 반환되고
And "카드를 삭제할 권한이 없습니다" 오류 메시지가 표시되고
And 삭제가 진행되지 않는다
```

**Priority:** Medium | **Story Points:** 3
