# US-601-001: 활동 내역 기록 `[P0]`

**As a** 사용자  
**I want to** 보드에서 누가 언제 무엇을 했는지 추적하여  
**So that** 업무 진행 상황을 파악할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 활동 자동 기록
```gherkin
Given 사용자가 보드에서 작업을 수행할 때 (카드 생성, 이동, 수정, 댓글 작성 등)
When 작업이 완료되면
Then 해당 활동이 activities 테이블에 자동으로 기록되고
And 활동 타입, 수행자, 시간, 페이로드가 저장되고
And JSON 형태의 payload에 상세 정보가 포함되고
And 다국어 메시지 템플릿이 적용된다
```

### ✅ 성공 케이스 - 복잡한 활동 기록 (카드 이동)
```gherkin
Given 사용자가 카드를 다른 리스트로 이동할 때
When 이동이 완료되면
Then 활동 내역에 "CARD_MOVE" 타입으로 기록되고
And payload에 cardTitle, sourceListName, destListName이 포함되고
And 현재 언어 설정에 맞는 메시지로 표시되고
And actor 정보에 firstName, lastName이 분리되어 저장된다
```

### ✅ 성공 케이스 - 사용자 이름 다국어 처리
```gherkin
Given 활동 내역이 기록될 때
And 사용자의 언어 설정이 한국어일 때
When 활동 메시지가 생성되면
Then 한국어 형식으로 "{{actorLastName}}{{actorFirstName}}님이" 형태로 표시되고
And 영어 설정일 때는 "{{actorFirstName}} {{actorLastName}}" 형태로 표시된다
```

**Priority:** High | **Story Points:** 6
