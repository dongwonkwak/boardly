# US-203-002: 카드 편집 `[P0]`

**As a** 사용자  
**I want to** 업무 진행에 따라 카드의 제목과 상세 내용을 수정하고  
**So that** 최신 정보를 유지할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 카드 제목 편집
```gherkin
Given 사용자가 보드에 있고
And 카드가 존재하고
And 사용자가 'content:write' 권한을 가지고 있을 때
When 카드를 클릭하여 상세 모달을 열고
And 제목을 편집하고
And 저장하면
Then 카드 제목이 업데이트되고
And 변경사항이 즉시 반영되고
And 활동 내역에 "카드 제목 변경" 기록이 남는다
```

### ✅ 성공 케이스 - 카드 설명 편집 (마크다운 지원)
```gherkin
Given 사용자가 카드 상세 모달에 있고
And 사용자가 'content:write' 권한을 가지고 있을 때
When 설명 편집 버튼을 클릭하고
And 마크다운 형식으로 설명을 작성하고 (최대 10,000자)
And 저장하면
Then 설명이 마크다운으로 렌더링되어 표시되고
And XSS 방지를 위한 sanitization이 적용되고
And 변경사항이 저장되고
And 활동 내역에 "카드 설명 수정" 기록이 남는다
```

### ❌ 실패 케이스 - 빈 카드 제목으로 변경
```gherkin
Given 사용자가 카드 제목을 편집하고 있고
And 카드 제목을 비워두었을 때
When 저장을 시도하면
Then HTTP 400 Bad Request 응답이 반환되고
And "카드 제목을 입력해주세요" 오류 메시지가 표시되고
And 이전 제목으로 되돌아간다
```

### ❌ 실패 케이스 - 설명 길이 초과
```gherkin
Given 사용자가 10,000자를 초과하는 카드 설명을 입력했을 때
When 저장을 시도하면
Then HTTP 400 Bad Request 응답이 반환되고
And "카드 설명은 10,000자 이하여야 합니다" 오류 메시지가 표시되고
And 저장이 진행되지 않는다
```

**Priority:** High | **Story Points:** 4
