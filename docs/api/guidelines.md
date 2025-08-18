# API 가이드라인

OpenAPI는 코드에서 생성하되, 일관된 API 경험을 위한 규칙을 정의합니다.

## 기본 규칙
- Base URL: `/api`
- 버저닝: URL 버전 사용(예: `/api/v1`)
- 인증: `Authorization: Bearer <access_token>`
- 컨텐츠 타입: `application/json; charset=UTF-8`
- 타임스탬프: ISO-8601(예: `2025-08-18T12:34:56Z`), UTC 기준
- 로케일: `Accept-Language` 요청 헤더 사용(예: `ko-KR`, `en-US`)
- 워크스페이스 컨텍스트: `X-Workspace-Id` 요청 헤더 사용(필요한 엔드포인트에 한함)

## 페이징/정렬
- 파라미터: `page`(0-base), `size`(기본 20, 최대 100), `sort`(예: `createdAt,desc`)
- 응답 메타
```json
{
  "content": [/* items */],
  "page": 0,
  "size": 20,
  "totalElements": 123,
  "totalPages": 7,
  "sort": "createdAt,desc"
}
```

## 표준 응답
- 성공
```json
{
  "data": { /* 리소스 또는 결과 */ },
  "meta": { "requestId": "..." }
}
```
- 에러(표준화, Flat ErrorResponse)
```json
{
  "code": "AUTH-INVALID_TOKEN",
  "messageKey": "error.auth.invalidToken",
  "message": "토큰이 유효하지 않습니다.",
  "timestamp": "2025-08-18T12:34:56Z",
  "path": "/api/v1/...",
  "requestId": "...",
  "details": { /* 필드 오류 등 선택 */ }
}
```

## 필드 검증
- 요청 본문 검증 실패 시 `400 Bad Request`
- 필드별 오류 포맷(Flat ErrorResponse)
```json
{
  "code": "COMMON-VALIDATION",
  "messageKey": "error.common.validation",
  "message": "요청이 유효하지 않습니다.",
  "details": [
    { "field": "title", "message": "must not be blank" },
    { "field": "size", "message": "must be between 1 and 100" }
  ]
}
```

## 상태 코드 매핑(가이드)
- 200/201/204: 정상 처리(조회/생성/삭제)
- 400: 유효성 실패, 잘못된 파라미터
- 401: 인증 실패(토큰 누락/만료/무효)
- 403: 인가 실패(권한 부족, 워크스페이스 멤버 아님)
- 404: 리소스 없음
- 409: 충돌(중복, position 경쟁 등)
- 429: 속도 제한 초과
- 5xx: 서버 오류

## 에러 국제화(i18n)
- 서버는 `Accept-Language`를 고려해 `message`를 현지화하여 반환합니다.
- 클라이언트는 `messageKey`가 있으면 번역 키로 표출하고, 없으면 `message`를 사용합니다.

## 멀티테넌시: 워크스페이스 컨텍스트

- 목적: 멀티테넌트 자원 접근 시 활성 워크스페이스를 명시합니다.
- 전달 방식(권장 순서)
  1) 요청 헤더 `X-Workspace-Id: <workspace_id>`
  2) 경로 변수: `/api/v1/workspaces/{workspaceId}/...` (엔드포인트가 명시적 컨텍스트를 가지는 경우)
- 적용 대상: 워크스페이스/보드/리스트/카드 등 테넌트 종속 리소스. 전역 리소스(가입/로그인/헬스체크 등)는 제외.
- 검증 규칙
  - 헤더가 필요한 엔드포인트에서 누락 시: 400 `COMMON-VALIDATION`(필드: `X-Workspace-Id`)
  - 멤버가 아닌 워크스페이스: 403 `WS-NOT_MEMBER`
  - 권한 부족(역할/스코프 미흡): 403 `AUTH-INSUFFICIENT_SCOPE`
  - 존재하지 않는 워크스페이스: 404 `WS-NOT_FOUND`

### 요청 예시
```http
GET /api/boards HTTP/1.1
Host: api.example.com
Authorization: Bearer <access_token>
X-Workspace-Id: ws-abc
Accept-Language: ko-KR
```

### 오류 응답 예시(Flat ErrorResponse)
헤더 누락(필수 엔드포인트):
```json
{
  "code": "COMMON-VALIDATION",
  "messageKey": "error.common.validation",
  "message": "요청이 유효하지 않습니다.",
  "details": [ { "field": "X-Workspace-Id", "message": "required" } ]
}
```

멤버 아님:
```json
{
  "code": "WS-NOT_MEMBER",
  "messageKey": "error.workspace.notMember",
  "message": "워크스페이스 멤버가 아닙니다."
}
```

## 보안 모범사례
- JWT 수명 최소화, 리프레시 토큰 회전
- 민감 데이터는 로그에 남기지 않음(토큰/비밀번호/쿠키)
- CORS는 허용 오리진을 명시, 자격 증명 전송 제한
- 속도 제한 헤더 사용(예: `X-RateLimit-Remaining`)
