# US-101-007: 언어 설정 변경 `[P0]`

**As a** 사용자  
**I want to** 사용자 인터페이스 언어를 변경하여  
**So that** 내가 선호하는 언어로 서비스를 이용할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 언어 설정 변경
```gherkin
Given 사용자가 프로필 설정 페이지에 있을 때
When 언어 옵션(한국어/영어)을 선택하고
And 저장 버튼을 클릭하면
Then 언어 설정이 저장되고
And UI가 즉시 선택한 언어로 변경되고
And 로컬 스토리지에 언어 설정이 저장되고
And 활동 내역에 "언어 설정 변경" 기록이 남는다
```

### ✅ 성공 케이스 - 초기 언어 자동 감지
```gherkin
Given 신규 사용자가 처음 서비스에 접속하고
And 브라우저의 Accept-Language가 한국어로 설정되어 있을 때
When 서비스에 접속하면
Then UI가 한국어로 표시되고
And 언어 설정이 로컬 스토리지에 'ko'로 저장된다
```

**Priority:** Medium | **Story Points:** 3
