# 에러 코드 카탈로그

클라이언트/서버 간 일관된 에러 응답 규약(평면 스키마)과 코드/메시지 키/HTTP 상태 매핑을 정의합니다.

## 표준 에러 응답 스키마 (Flat ErrorResponse)

```json
{
  "code": "COMMON-VALIDATION",
  "messageKey": "error.common.validation",
  "message": "요청이 유효하지 않습니다.",
  "timestamp": "2025-07-20T10:30:00Z",
  "path": "/api/boards",
  "requestId": "abc-123",
  "details": [
    { "field": "title", "message": "must not be blank", "rejectedValue": "" }
  ],
  "context": { "boardId": "..." }
}
```

- `code`: 시스템 표준 에러 코드
- `messageKey`: 프론트엔드 i18n 번역 키(있으면 우선 사용; 없으면 `message` 사용)
- `message`: 사용자 표시용 기본 메시지(서버 기본 로케일)
- `timestamp`, `path`, `requestId`: 진단/추적을 위한 메타정보
- `details`: 필드 단위 검증 오류 등 상세 정보(주로 400에서 사용)
- `context`: 디버깅/UX 보조용 추가 컨텍스트(식별자, 제한치 등)

## HTTP 상태 코드 사용 원칙

- 400 Bad Request: 입력 형식/데이터 오류(이메일 형식, 필수값 누락)
- 401 Unauthorized: 인증 필요/토큰 누락 또는 무효
- 403 Forbidden: 권한 부족 또는 정책 위반(읽기 전용 리소스 수정 등)
- 404 Not Found: 리소스 미존재
- 409 Conflict: 리소스 충돌(중복, 동시 수정, 아카이브 상태 충돌 등)
- 412 Precondition Failed: 전제 조건 실패(필수 설정/선행 단계 미완료)
- 422 Unprocessable Entity: 비즈니스 규칙 위반(개수 제한, 상태 전이 불가 등)
- 429 Too Many Requests: 레이트 리밋 초과
- 500 Internal Server Error: 예기치 못한 서버 오류

## 에러 코드 카탈로그

| 코드 | messageKey | HTTP | 설명 |
|---|---|---:|---|
| COMMON-VALIDATION | error.common.validation | 400 | 요청 본문/파라미터 검증 실패 |
| COMMON-NOT_FOUND | error.common.notFound | 404 | 리소스가 존재하지 않음 |
| COMMON-CONFLICT | error.common.conflict | 409 | 중복/경쟁 상태 발생 |
| COMMON-RATE_LIMIT | error.common.rateLimit | 429 | 요청 속도 제한 초과 |
| AUTH-UNAUTHORIZED | error.auth.unauthorized | 401 | 인증 필요(토큰 누락) |
| AUTH-INVALID_TOKEN | error.auth.invalidToken | 401 | 토큰이 유효하지 않음 |
| AUTH-EXPIRED_TOKEN | error.auth.expiredToken | 401 | 토큰이 만료됨 |
| AUTH-INSUFFICIENT_SCOPE | error.auth.insufficientScope | 403 | 권한 부족 |
| WS-NOT_MEMBER | error.workspace.notMember | 403 | 워크스페이스 멤버가 아님 |
| WS-NOT_FOUND | error.workspace.notFound | 404 | 워크스페이스 없음 |
| WS-NOT_OWNER | error.workspace.notOwner | 403 | 워크스페이스 소유자가 아님 |
| WS-HAS_BOARDS | error.workspace.hasBoards | 409 | 워크스페이스에 보드가 있어 삭제 불가 |
| WS-LIMIT_EXCEEDED | error.workspace.limitExceeded | 422 | 워크스페이스 개수 제한 초과 |
| WS-PERSONAL_NO_INVITE | error.workspace.personalNoInvite | 422 | Personal 워크스페이스는 멤버 초대 불가 |
| BOARD-NOT_FOUND | error.board.notFound | 404 | 보드 없음 |
| BOARD-ARCHIVED | error.board.archived | 409 | 아카이브된 보드 |
| LIST-NOT_FOUND | error.list.notFound | 404 | 리스트 없음 |
| CARD-NOT_FOUND | error.card.notFound | 404 | 카드 없음 |
| CARD-POSITION_CONFLICT | error.card.positionConflict | 409 | 카드 정렬 충돌 |
| CARD-LIMIT_EXCEEDED | error.card.limitExceeded | 422 | 카드 개수 제한 초과 |
| LIST-NOT_FOUND | error.list.notFound | 404 | 리스트 없음 |
| LIST-POSITION_CONFLICT | error.list.positionConflict | 409 | 리스트 정렬 충돌 |
| LABEL-NOT_FOUND | error.label.notFound | 404 | 라벨 없음 |
| LABEL-NAME_DUPLICATE | error.label.nameDuplicate | 409 | 라벨 이름 중복 |
| COMMENT-NOT_FOUND | error.comment.notFound | 404 | 댓글 없음 |
| COMMENT-UNAUTHORIZED | error.comment.unauthorized | 403 | 댓글 수정/삭제 권한 없음 |
| USER-EMAIL_DUPLICATE | error.user.emailDuplicate | 409 | 이메일 중복 |
| USER-PASSWORD_MISMATCH | error.user.passwordMismatch | 401 | 비밀번호 불일치 |
| USER-INVALID_PASSWORD | error.user.invalidPassword | 422 | 비밀번호 규칙 위반 |

필요 시 도메인별 코드를 위 표에 추가하고, 각 코드에 대한 `HTTP`와 `messageKey`를 함께 등록합니다.

## 응답 예시

400 Bad Request - 검증 실패
```json
{
  "code": "COMMON-VALIDATION",
  "messageKey": "error.common.validation",
  "message": "요청이 유효하지 않습니다.",
  "timestamp": "2025-07-20T10:30:00Z",
  "path": "/api/v1/cards",
  "requestId": "abc-123",
  "details": [
    { "field": "title", "message": "must not be blank", "rejectedValue": "" }
  ]
}
```

422 Unprocessable Entity - 비즈니스 규칙 위반
```json
{
  "code": "CARD-LIMIT_EXCEEDED",
  "messageKey": "error.card.limitExceeded",
  "message": "리스트당 최대 100개의 카드만 생성할 수 있습니다.",
  "timestamp": "2025-07-20T10:30:00Z",
  "path": "/api/v1/cards",
  "requestId": "abc-123",
  "context": { "listId": "list-123", "currentCount": 100, "maxCount": 100 }
}
```

## 운영 가이드

- 로그와 응답 모두에 `requestId`를 포함해 추적성을 확보합니다.
- 클라이언트는 `messageKey`가 있으면 번역(`t(messageKey)`), 없으면 `message`를 표출합니다.
- 불필요한 스택트레이스/내부 정보는 응답에 노출하지 않습니다.
- i18n 메시지는 서버 `messages.properties`(또는 동등한 리소스)와 동기화합니다.
- 자세한 내부 분류/매핑은 `../errors.md`(백엔드 내부 가이드)를 참조하세요.
