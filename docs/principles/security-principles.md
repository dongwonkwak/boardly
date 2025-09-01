# Boardly - 보안 설계 원칙

## 인증 및 보안

### JWT 토큰 기반 인증

**인증 방식**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**JWT 토큰 정책**
- **알고리즘**: HS256 (개발), RS256 (운영 권장)
- **만료시간**: Access Token 15분, Refresh Token 7일
- **클레임 구조**:
  ```json
  {
    "sub": "usr_01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "email": "user@example.com",
    "displayName": "홍길동",
    "iat": 1609459200,
    "exp": 1609460100,
    "iss": "boardly-api",
    "aud": "boardly-client"
  }
  ```

**토큰 갱신 정책**
- Refresh Token은 HttpOnly 쿠키로 저장
- Access Token 만료 시 자동 갱신
- Refresh Token 로테이션 적용

### CORS 설정

**개발 환경**
```yaml
# application-dev.yml
cors:
  allowed-origins: 
    - http://localhost:3000
    - http://localhost:5173
  allowed-methods:
    - GET
    - POST
    - PUT
    - PATCH
    - DELETE
    - OPTIONS
  allowed-headers:
    - Authorization
    - Content-Type
    - X-Request-ID
    - X-Requested-With
  exposed-headers:
    - X-Request-ID
    - X-Rate-Limit-Remaining
    - X-Rate-Limit-Reset
  allow-credentials: true
  max-age: 3600
```

**운영 환경**
```yaml
# application-prod.yml
cors:
  allowed-origins: 
    - https://boardly.example.com
    - https://app.boardly.example.com
  allowed-methods:
    - GET
    - POST
    - PUT
    - PATCH
    - DELETE
    - OPTIONS
  allowed-headers:
    - Authorization
    - Content-Type
    - X-Request-ID
  exposed-headers:
    - X-Request-ID
    - X-Rate-Limit-Remaining
    - X-Rate-Limit-Reset
  allow-credentials: true
  max-age: 86400
```

### Rate Limiting

**API 요청 제한**
- **일반 사용자**: 100 requests/minute
- **인증된 사용자**: 1000 requests/minute
- **업로드 요청**: 10 requests/minute
- **인증 요청**: 5 requests/minute

**응답 헤더 예시**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1609459200
X-Request-ID: req-12345678-1234-1234-1234-123456789abc
```

**제한 초과 시 응답**
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.",
    "context": {
      "limit": 100,
      "window": "1 minute",
      "retryAfter": 60
    }
  },
  "meta": {
    "timestamp": "2025-01-01T12:34:56Z",
    "apiVersion": "v1",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

### 보안 헤더

**필수 보안 헤더**
```
# Content Security Policy
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'

# XSS 보호
X-XSS-Protection: 1; mode=block

# Content Type 스니핑 방지
X-Content-Type-Options: nosniff

# Frame 옵션
X-Frame-Options: DENY

# Referrer 정책
Referrer-Policy: strict-origin-when-cross-origin

# HSTS (HTTPS 전용)
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

### 입력 검증 및 보안

**SQL Injection 방지**
- Prepared Statement 사용
- JPA/QueryDSL을 통한 파라미터 바인딩
- 동적 쿼리 시 화이트리스트 검증

**XSS 방지**
- HTML 이스케이프 처리
- Content-Type 헤더 검증
- 사용자 입력 sanitization

**CSRF 방지**
- SameSite 쿠키 설정
- CSRF 토큰 (필요시)
- Origin/Referer 헤더 검증

**파일 업로드 보안**
- 파일 타입 검증 (MIME Type + 확장자)
- 파일 크기 제한
- 바이러스 스캔 (운영 환경)
- 안전한 저장 위치

### 데이터 보호

**민감 정보 처리**
- 비밀번호 해싱 (BCrypt, Scrypt, Argon2)
- 개인정보 암호화 (AES-256)
- 로그에서 민감 정보 마스킹

**데이터베이스 보안**
- 연결 문자열 암호화
- 최소 권한 원칙
- 감사 로그 활성화

### 로깅 및 모니터링

**보안 이벤트 로깅**
- 로그인 시도 (성공/실패)
- 권한 변경
- 민감한 작업 수행
- 비정상적인 API 호출 패턴

**로그 보안**
- 민감 정보 마스킹
- 로그 무결성 보장
- 로그 보관 정책 (90일)

### OAuth2 설정 (추후 확장)

**지원 예정 OAuth2 제공자**
- Google OAuth2
- GitHub OAuth2
- Microsoft OAuth2

**OAuth2 보안 고려사항**
- PKCE (Proof Key for Code Exchange) 적용
- State 파라미터 검증
- 스코프 최소화 원칙

## 보안 체크리스트

### 개발 단계
- [ ] JWT 토큰 만료 시간 설정
- [ ] CORS 설정 검증
- [ ] Rate Limiting 적용
- [ ] 입력 검증 로직 구현
- [ ] 보안 헤더 설정

### 테스트 단계
- [ ] 인증/인가 테스트
- [ ] CORS 테스트
- [ ] Rate Limiting 테스트
- [ ] XSS/SQL Injection 테스트
- [ ] 파일 업로드 보안 테스트

### 배포 단계
- [ ] HTTPS 적용
- [ ] 프로덕션 환경 CORS 설정
- [ ] 보안 헤더 적용 확인
- [ ] 로그 모니터링 설정
- [ ] 백업 및 복구 계획

---

**문서 버전**: 1.0  
**최종 수정일**: 2024-12-19  
**작성자**: Boardly 개발팀
