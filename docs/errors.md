# Errors (Backend Internal Guide)
---

이 문서는 백엔드 내부의 에러 분류 원칙과 구현 가이드를 제공합니다. 외부 API 응답 형식과 코드 표준은 `api/error_catalog.md`를 단일 기준으로 사용합니다.

## 1. HTTP 상태 코드 사용 원칙 (요약)

- 400 Bad Request: 입력 형식/데이터 오류
- 401 Unauthorized: 인증 필요/무효 토큰
- 403 Forbidden: 권한/정책 위반
- 404 Not Found: 리소스 미발견
- 409 Conflict: 리소스 충돌(중복, 경쟁 상태)
- 412 Precondition Failed: 전제 조건 실패(선행 단계 미완료 등)
- 422 Unprocessable Entity: 비즈니스 규칙 위반
- 429 Too Many Requests: 레이트 리밋 초과
- 500 Internal Server Error: 내부 서버 오류

상세 매핑과 예시는 `api/error_catalog.md`를 참고하세요.

## 2. Failure/Exception 분류 가이드

도메인 서비스/애플리케이션 계층에서 다음 분류를 사용하여 컨트롤러 어드바이스로 매핑합니다.

```
Failure.InputError             -> HTTP 400 (code: COMMON-VALIDATION)
Failure.PermissionDenied       -> HTTP 403
Failure.NotFound               -> HTTP 404
Failure.ResourceConflict       -> HTTP 409
Failure.PreconditionFailed     -> HTTP 412
Failure.BusinessRuleViolation  -> HTTP 422
Failure.InternalError          -> HTTP 500
```

각 Failure에는 다음 정보를 포함하도록 합니다.
- `code`: 카탈로그에 등록된 표준 코드 (예: `BOARD-ARCHIVED`)
- `messageKey`: 번역 키(가능하면 제공)
- `message`: 기본 로케일 메시지(폴백)
- `context`/`details`: 디버깅 및 UX 보조 정보

## 3. 응답 포맷

외부로 반환되는 모든 에러는 평면 스키마(Flat ErrorResponse)를 사용합니다. 스키마는 `api/error_catalog.md`의 “표준 에러 응답 스키마”를 따릅니다.

## 4. 운영 가이드

- 모든 요청에 `requestId`를 생성/전파하고, 로그와 응답에 포함합니다.
- 스택트레이스는 서버 로그에만 남기고, 응답에는 요약 메시지만 표출합니다.
- `messageKey`는 서버의 i18n 리소스와 동기화하며, 프론트엔드는 키가 없을 때 `message`를 사용합니다.