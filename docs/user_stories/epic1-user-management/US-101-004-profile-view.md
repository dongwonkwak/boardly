# US-101-004: 프로필 조회 `[P0]`

**As a** 사용자  
**I want to** 내 프로필 정보를 확인하여  
**So that** 계정 상태를 파악할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 프로필 정보 조회
```gherkin
Given 사용자가 로그인된 상태에서
When 프로필 페이지에 접근하면
Then 이메일, 성, 이름이 표시되고
And 현재 언어 설정이 표시되고
And 계정 생성일이 표시되고
And 편집 버튼이 제공된다
```

**Priority:** Medium | **Story Points:** 2
