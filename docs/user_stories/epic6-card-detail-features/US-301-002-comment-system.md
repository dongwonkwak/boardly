# US-301-002: 댓글 시스템 `[P0]`

**As a** 팀원  
**I want to** 특정 업무에 대해 의견을 교환하고  
**So that** 효과적으로 소통할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 댓글 작성
```gherkin
Given 사용자가 카드 상세 모달에 있고
And 사용자가 'content:write' 권한을 가지고 있을 때
When 댓글 입력 필드에 내용을 작성하고
And 댓글 등록 버튼을 클릭하면
Then 새로운 댓글이 댓글 목록에 추가되고
And 작성자, 작성 시간이 표시되고
And 마크다운 렌더링이 적용되고
And 활동 내역에 "댓글 추가" 기록이 남는다
```

### ✅ 성공 케이스 - 댓글 수정
```gherkin
Given 사용자가 자신이 작성한 댓글을 보고 있고
And 사용자가 'content:write' 권한을 가지고 있을 때
When 댓글 수정 버튼을 클릭하고
And 댓글 내용을 수정하고
And 저장하면
Then 댓글이 업데이트되고
And "수정됨" 표시가 나타나고
And updatedAt 시간이 업데이트되고
And 활동 내역에 "댓글 수정" 기록이 남는다
```

### ✅ 성공 케이스 - 댓글 삭제
```gherkin
Given 사용자가 자신이 작성한 댓글을 보고 있고
And 사용자가 'content:delete' 권한을 가지고 있을 때
When 댓글 삭제 버튼을 클릭하고
And 삭제를 확인하면
Then 댓글이 삭제되고
And 댓글 목록에서 제거되고
And 활동 내역에 "댓글 삭제" 기록이 남는다
```

### ❌ 실패 케이스 - 다른 사용자 댓글 수정/삭제 시도
```gherkin
Given 사용자가 다른 사용자의 댓글을 보고 있을 때
When 댓글 수정 또는 삭제를 시도하면
Then HTTP 403 Forbidden 응답이 반환되고
And 수정/삭제 버튼이 표시되지 않고
And "댓글을 수정/삭제할 권한이 없습니다" 메시지가 표시된다
```

### ❌ 실패 케이스 - 빈 댓글 작성
```gherkin
Given 사용자가 댓글 입력 필드에 아무것도 입력하지 않았을 때
When 댓글 등록 버튼을 클릭하면
Then HTTP 400 Bad Request 응답이 반환되고
And "댓글 내용을 입력해주세요" 오류 메시지가 표시되고
And 댓글이 등록되지 않는다
```

**Priority:** High | **Story Points:** 5
