# US-701-002: 다국어 메시지 `[P0]`

**As a** 사용자  
**I want to** 내가 선택한 언어로 모든 시스템 메시지를 확인하고  
**So that** 시스템을 정확히 이해할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 활동 내역 다국어 표시
```gherkin
Given 사용자가 한국어로 언어를 설정하고
And 활동 내역을 조회할 때
When 활동 목록이 표시되면
Then 모든 활동 메시지가 한국어 템플릿을 사용하여 표시되고
And 사용자 이름이 한국어 형식(성+이름)으로 표시되고
And "{{actorLastName}}{{actorFirstName}}님이" 패턴이 적용된다
```

### ✅ 성공 케이스 - 에러 메시지 다국어 표시
```gherkin
Given 사용자가 영어로 언어를 설정하고
And 잘못된 작업을 수행하여 에러가 발생할 때
When 에러 메시지가 표시되면
Then 에러 메시지가 영어로 표시되고
And Spring의 MessageSource에서 로드된 메시지가 사용되고
And HTTP 응답의 Accept-Language를 기반으로 처리된다
```

### ✅ 성공 케이스 - 폼 검증 메시지 다국어
```gherkin
Given 사용자가 한국어로 언어를 설정하고
And 폼 입력에서 검증 오류가 발생할 때
When 검증 메시지가 표시되면
Then ValidationMessages_ko.properties에서 로드된 한국어 메시지가 표시되고
And 필드명도 현지화되어 표시된다
```

### ✅ 성공 케이스 - 날짜/시간 현지화
```gherkin
Given 사용자가 언어를 설정했을 때
When 카드의 마감일이나 활동 시간이 표시되면
Then 해당 언어/지역에 맞는 날짜/시간 형식으로 표시되고
And 상대적 시간(2시간 전, 3일 전 등)도 해당 언어로 표시되고
And 시간대는 사용자의 로컬 시간대로 변환된다
```

**Priority:** Medium | **Story Points:** 4
