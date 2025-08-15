# US-101-003: 로그아웃

**As a** 로그인된 사용자  
**I want to** 안전하게 세션을 종료하여  
**So that** 내 계정을 보호할 수 있다.

**Scenarios:**

**✅ 성공 케이스 - 로그아웃 수행**
```gherkin
Given 사용자가 로그인된 상태에서 대시보드에 있고
When 헤더의 로그아웃 버튼을 클릭하면
Then JWT 토큰이 로컬 스토리지에서 삭제되고
And 랜딩 페이지로 리다이렉트되고
And 보호된 페이지에 접근할 수 없게 된다
```

**Priority:** High | **Story Points:** 2
