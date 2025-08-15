# US-SEC-003: XSS/CSRF 방어 `[P0]`

**As a** 사용자  
**I want to** 악의적인 공격으로부터 내 데이터가 보호되기를 원하고  
**So that** 안전하게 서비스를 이용할 수 있다.

## Scenarios

### ✅ 성공 케이스 - XSS 방어
```gherkin
Given 사용자가 카드 설명에 HTML 태그나 스크립트를 포함한 텍스트를 입력할 때
When 마크다운 렌더링이 수행되면
Then 위험한 HTML 태그가 sanitize되고
And 허용된 마크다운만 렌더링되고
And 스크립트 실행이 차단된다
```

### ✅ 성공 케이스 - CSRF 토큰 검증
```gherkin
Given 사용자가 상태 변경 작업(카드 생성, 수정 등)을 수행할 때
When 서버에서 요청을 처리하면
Then CSRF 토큰이 검증되고
And 유효한 토큰이 있는 경우에만 작업이 수행되고
And 토큰이 없거나 잘못된 경우 요청이 거부된다
```

### ✅ 성공 케이스 - Content Security Policy
```gherkin
Given 사용자가 웹 페이지에 접근할 때
When 브라우저가 페이지를 로드하면
Then 적절한 CSP 헤더가 설정되어 있고
And 인라인 스크립트 실행이 제한되고
And 외부 리소스 로드가 허용된 도메인으로 제한된다
```

### ❌ 실패 케이스 - 악성 스크립트 차단
```gherkin
Given 공격자가 XSS 공격을 시도할 때
When 악성 스크립트가 포함된 데이터를 입력하면
Then 스크립트가 실행되지 않고
And 안전한 텍스트로 변환되어 표시되고
And 보안 로그에 공격 시도가 기록된다
```

**Priority:** High | **Story Points:** 7
