## 1. HTTP 상태 코드 재분류

* 400 Bad Request: 입력 형식/데이터 오류 (이메일 형식, 필수값 누락)
* 403 Forbidden: 권한 기반 비즈니스 룰 위반 (다른 사용자 카드 수정, 읽기 전용)
* 404 Not Found: 리소스 미발견
* 409 Conflict: 리소스 충돌 (이메일 중복, 동시 수정 충돌)
* 412 Precondition Failed: 전제 조건 실패 (필수 설정 누락, 종속성 미충족)
* 422 Unprocessable Entity: 비즈니스 룰 위반 (카드 개수 제한, 아카이브된 카드 수정)
* 500 Internal Server Error: 내부 서버 오류

## 2. Failure 클래스 구조

```
// 기존 (422로 모든 비즈니스 룰 처리)
Failure.ValidationFailure  // 422
Failure.ConflictFailure    // 409
Failure.NotFoundFailure    // 404
Failure.ForbiddenFailure   // 403

// 개선 (세분화된 분류)
Failure.InputError           // 400 - 입력 형식 오류
Failure.PermissionDenied     // 403 - 권한 거부
Failure.NotFound            // 404 - 리소스 미발견
Failure.ResourceConflict    // 409 - 리소스 충돌
Failure.PreconditionFailed  // 412 - 전제 조건 실패
Failure.BusinessRuleViolation // 422 - 비즈니스 룰 위반
Failure.InternalError       // 500 - 내부 서버 오류
```

## 3. 에러 응답 구조

```json
{
  "code": "CARD_LIMIT_EXCEEDED",
  "message": "리스트당 최대 100개의 카드만 생성할 수 있습니다",
  "timestamp": "2025-07-20T10:30:00Z",
  "path": "/api/v1/cards",
  "details": [...],  // 400일 때만 검증 상세 정보
  "context": {...}   // 추가 컨텍스트 정보
}
```

## 4. 에러 응답 예시

400 Bad Request - 입력 형식 오류:
```json
{
  "code": "INVALID_EMAIL_FORMAT",
  "message": "입력 데이터가 올바르지 않습니다",
  "timestamp": "2025-07-20T10:30:00Z",
  "details": [
    {
      "field": "email",
      "message": "유효한 이메일 형식이 아닙니다",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

403 Forbidden - 권한 거부:
```json

{
  "code": "UNAUTHORIZED_CARD_MODIFICATION",
  "message": "다른 사용자의 카드를 수정할 권한이 없습니다",
  "timestamp": "2025-07-20T10:30:00Z",
  "context": {
    "cardId": "card-123",
    "cardOwnerId": "user-456",
    "requesterId": "user-789"
  }
}
```

404 Not Found - 리소스 미발견:
```json
{
  "code": "CARD_NOT_FOUND",
  "message": "요청한 카드를 찾을 수 없습니다",
  "timestamp": "2025-07-20T10:30:00Z",
  "context": {
    "cardId": "card-123"
  }
}
```

409 Conflict - 리소스 충돌:
```json
{
  "code": "EMAIL_ALREADY_EXISTS",
  "message": "이미 사용 중인 이메일입니다",
  "timestamp": "2025-07-20T10:30:00Z",
  "context": {
    "email": "user@example.com",
    "conflictType": "EMAIL_DUPLICATE"
  }
}
```

412 Precondition Failed - 전제 조건 실패:
```json
{
  "code": "ONBOARDING_INCOMPLETE",
  "message": "온보딩을 완료한 후 보드를 생성할 수 있습니다",
  "timestamp": "2025-07-20T10:30:00Z",
  "context": {
    "userId": "user-123",
    "onboardingStatus": "PROFILE_INCOMPLETE",
    "requiredSteps": ["profile", "preferences"]
  }
}
```

422 Unprocessable Entity - 비즈니스 룰 위반:
```json
{
  "code": "CARD_LIMIT_EXCEEDED",
  "message": "리스트당 최대 100개의 카드만 생성할 수 있습니다. (현재: 100개)",
  "timestamp": "2025-07-20T10:30:00Z",
  "context": {
    "listId": "list-123",
    "currentCount": 100,
    "maxCount": 100,
    "availableSlots": 0
  }
}
```

500 Internal Server Error - 내부 서버 오류:
```json
{
  "code": "INTERNAL_ERROR",
  "message": "내부 서버 오류가 발생했습니다",
  "timestamp": "2025-07-20T10:30:00Z"
}
```