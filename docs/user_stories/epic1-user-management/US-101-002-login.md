# US-101-002: 로그인 `[P0]`

**As a** 가입한 사용자  
**I want to** 내 계정으로 로그인하여  
**So that** 개인화된 서비스를 이용할 수 있다.

## Scenarios

### ✅ 성공 케이스 - 올바른 인증 정보로 로그인
```gherkin
Given 사용자가 로그인 페이지에 있고
And 등록된 이메일과 올바른 비밀번호를 입력했을 때
When 로그인 버튼을 클릭하면
Then JWT 토큰이 발급되고
And 로컬 스토리지에 토큰이 저장되고
And 대시보드 페이지로 리다이렉트되고
And 사용자 세션이 활성화된다
```

### ❌ 실패 케이스 - 잘못된 인증 정보
```gherkin
Given 사용자가 로그인 페이지에 있고
And 존재하지 않는 이메일 또는 잘못된 비밀번호를 입력했을 때
When 로그인 버튼을 클릭하면
Then HTTP 401 Unauthorized 응답이 반환되고
And "이메일 또는 비밀번호가 올바르지 않습니다" 오류 메시지가 표시되고
And 로그인이 진행되지 않는다
```

### ❌ 실패 케이스 - Rate Limiting 초과
```gherkin
Given 사용자가 짧은 시간 내에 5회 이상 로그인 실패를 했을 때
When 추가 로그인을 시도하면
Then HTTP 429 Too Many Requests 응답이 반환되고
And "잠시 후 다시 시도해주세요" 메시지가 표시되고
And 일정 시간 동안 로그인이 차단된다
```

**Priority:** High | **Story Points:** 4
