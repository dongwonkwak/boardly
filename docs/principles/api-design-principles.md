# Boardly - API 설계 원칙

## RESTful API 설계 원칙

### 1. URL 설계 규칙

**리소스 중심의 URL 구조**
```
# 올바른 예시 (v1 API)
GET    /api/v1/workspaces                    # 워크스페이스 목록
POST   /api/v1/workspaces                    # 워크스페이스 생성
GET    /api/v1/workspaces/{id}               # 특정 워크스페이스 조회
PUT    /api/v1/workspaces/{id}               # 워크스페이스 수정
DELETE /api/v1/workspaces/{id}               # 워크스페이스 삭제

# 중첩 리소스
GET    /api/v1/workspaces/{id}/boards        # 워크스페이스의 보드 목록
POST   /api/v1/workspaces/{id}/boards        # 워크스페이스에 보드 생성
GET    /api/v1/boards/{id}/lists             # 보드의 리스트 목록
POST   /api/v1/lists/{id}/cards              # 리스트에 카드 생성

# 잘못된 예시 (동사 사용)
POST   /api/v1/workspaces/create             # ❌
POST   /api/v1/cards/move                    # ❌
GET    /api/v1/users/getProfile              # ❌
```

**URL 명명 규칙**
- 명사 사용 (동사 금지)
- 복수형 사용 (`/users`, `/workspaces`)
- 소문자 + 하이픈 (`/workspace-members`)
- 계층 구조 반영
- 버전 정보 포함 (`/api/v1/`, `/api/v2/`)

### 1.1. 도메인 ID 체계

> 📘 **참고**: 상세한 도메인 ID 체계는 [`design/domain-id-specification.md`](../design/domain-id-specification.md) 문서를 참조하세요.

**ULID 기반 도메인 ID 형식**
- **구조**: `{prefix}_{ulid}` (예: `usr_01ARZ3NDEKTSV4RRFFQ69G5FAV`)
- **길이**: 30자 (prefix 3자 + underscore 1자 + ULID 26자)
- **패턴**: 각 도메인별 고유 prefix 사용 (`usr_`, `wsp_`, `brd_` 등)

### 2. HTTP 메서드 활용

| 메서드 | 목적 | 멱등성 | 캐시 가능 |
|--------|------|--------|-----------|
| GET | 조회 | ✅ | ✅ |
| POST | 생성 | ❌ | ❌ |
| PUT | 전체 수정 | ✅ | ❌ |
| PATCH | 부분 수정 | ❌ | ❌ |
| DELETE | 삭제 | ✅ | ❌ |

**특수 동작 처리**
```
# 카드 이동 (PATCH 사용)
PATCH  /api/v1/cards/{id}/position
Body: { listId: "lst_01ARZ3NDEKTSV4RRFFQ69G5FAV", position: 3 }

# 멤버 초대 (POST 사용)  
POST   /api/v1/workspaces/{id}/invitations
Body: { email: "user@example.com", role: "member" }

# 복합 검색 (GET with Query Parameters)
GET    /api/v1/search?q=keyword&type=card&workspace=wsp_01ARZ3NDEKTSV4RRFFQ69G5FAV
```

### 3. 응답 구조 표준화

> 📋 **참고**: UseCase 반환 타입 정책에 따라 UseCase는 도메인 객체를 반환하고, Controller에서 응답 DTO로 변환합니다.

**단일 리소스 응답**
```json
{
  "data": {
    "id": "wsp_01HQ8K2N3M4P5Q6R7S8T9U0V1",
    "name": "개발팀",
    "description": "개발팀 워크스페이스",
    "type": "TEAM",
    "createdAt": "2025-01-01T00:00:00Z",
    "owner": {
      "id": "usr_01HQ8K2N3M4P5Q6R7S8T9U0V2",
      "displayName": "홍길동",
      "email": "hong@example.com"
    }
  },
  "meta": {
    "timestamp": "2025-01-01T12:34:56Z",
    "apiVersion": "v1",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

**목록 응답 (페이징)**
```json
{
  "data": {
    "items": [
      { 
        "id": "wsp_01HQ8K2N3M4P5Q6R7S8T9U0V1", 
        "name": "워크스페이스1",
        "type": "TEAM",
        "memberCount": 5,
        "boardCount": 12,
        "createdAt": "2025-01-01T00:00:00Z"
      },
      { 
        "id": "wsp_01HQ8K2N3M4P5Q6R7S8T9U0V3", 
        "name": "워크스페이스2",
        "type": "PERSONAL",
        "memberCount": 1,
        "boardCount": 3,
        "createdAt": "2025-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 45,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false,
      "first": true,
      "last": false
    }
  },
  "meta": {
    "timestamp": "2025-01-01T12:34:56Z",
    "apiVersion": "v1",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

**Meta 필드 상세 정의**

| 필드 | 설명 | 예시 | 필수 여부 |
|------|------|------|-----------|
| `timestamp` | 응답 생성 시점 (서버 시간, UTC) | `"2025-01-01T12:34:56Z"` | ✅ |
| `apiVersion` | API 버전 (URL path 버전과 동일) | `"v1"`, `"v2"` | ✅ |
| `requestId` | 요청 추적 ID (로깅/디버깅용) | `"req-xxx-xxx-xxx"` | ✅ |
| `etag` | 리소스 버전 (캐싱용, 조건부 요청) | `"33a64df551425fcc"` | 조건부 |
| `deprecation` | API 지원 중단 경고 | `{"version": "v2", "sunset": "2025-12-31"}` | 선택적 |

> 📋 **Meta 버전 정책**: 
> - `apiVersion`: URL path의 버전과 동일 (`/api/v1/users` → `"v1"`)
> - 리소스 자체의 버전이 필요한 경우 `etag` 사용
> - 애플리케이션 버전은 별도 엔드포인트 제공 (`/api/version`)

**에러 응답**
```json
{
  "error": {
    "code": "WORKSPACE_NOT_FOUND",
    "message": "워크스페이스를 찾을 수 없습니다.",
    "context": {
      "workspaceId": "wsp_01ARZ3NDEKTSV4RRFFQ69G5FAV"
    }
  },
  "meta": {
    "timestamp": "2025-01-01T12:34:56Z",
    "path": "/api/v1/workspaces/wsp_01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "apiVersion": "v1",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

**입력 검증 실패 응답**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력 데이터가 유효하지 않습니다.",
    "details": [
      {
        "field": "name",
        "message": "워크스페이스 이름은 1자 이상 100자 이하여야 합니다.",
        "rejectedValue": ""
      },
      {
        "field": "description", 
        "message": "워크스페이스 설명은 500자 이하여야 합니다.",
        "rejectedValue": "매우 긴 설명..."
      }
    ]
  },
  "meta": {
    "timestamp": "2025-01-01T12:34:56Z",
    "path": "/api/v1/workspaces",
    "apiVersion": "v1",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

### 4. HTTP 상태 코드 가이드

**성공 (2xx)**
- `200 OK`: 성공적인 GET, PUT, PATCH
- `201 Created`: 성공적인 POST (리소스 생성)
- `204 No Content`: 성공적인 DELETE

**클라이언트 에러 (4xx)**
- `400 Bad Request`: 입력 검증 실패
- `401 Unauthorized`: 인증 실패  
- `403 Forbidden`: 권한 부족
- `404 Not Found`: 리소스 없음
- `409 Conflict`: 리소스 충돌 (예: 중복 이메일)
- `412 Precondition Failed`: 전제 조건 실패 (예: ETag 불일치)
- `422 Unprocessable Entity`: 비즈니스 룰 위반

**서버 에러 (5xx)**
- `500 Internal Server Error`: 서버 내부 오류

### 5. API 버전 관리

**URL Path 버전 관리 (채택)**
```
/api/v1/workspaces
/api/v2/workspaces
```

**Header 버전 관리 (고려사항)**
```
Accept: application/vnd.boardly.v1+json
```

## 인증 및 보안

> 📘 **참고**: 상세한 보안 정책 및 설정은 [`principles/security-principles.md`](./security-principles.md) 문서를 참조하세요.

**주요 보안 정책**:
- **인증**: JWT 토큰 기반 인증 (`Authorization: Bearer {token}`)
- **CORS**: 환경별 허용 도메인 설정
- **Rate Limiting**: 사용자별 API 요청 제한
- **보안 헤더**: XSS, CSRF, Content-Type 보호

## 데이터 검증 및 응답 원칙

> 📘 **참고**: 상세한 구현 방법은 [`architecture/backend-api-implementation.md`](../architecture/backend-api-implementation.md) 문서를 참조하세요.

**핵심 원칙**:
- **검증 정책**: Service(UseCase) 레벨에서만 검증 수행 (Controller 검증 금지)
- **응답 데이터**: 민감 정보 제외, 도메인 객체를 응답 DTO로 변환
- **에러 처리**: 각 Controller에서 직접 처리 (글로벌 에러 핸들러 사용 안함)

## 에러 처리 전략

> 📘 **참고**: 상세한 에러 코드 목록은 [`design/error-code-specification.md`](../design/error-code-specification.md) 문서를 참조하세요.

### 에러 응답 구조

**표준 에러 응답**
```json
{
  "error": {
    "code": "WORKSPACE_NOT_FOUND",
    "message": "워크스페이스를 찾을 수 없습니다.",
    "context": {
      "workspaceId": "wsp_01ARZ3NDEKTSV4RRFFQ69G5FAV"
    }
  },
  "meta": {
    "timestamp": "2025-01-01T12:34:56Z",
    "path": "/api/v1/workspaces/wsp_01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "apiVersion": "v1",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

### 에러 처리 정책

> 📘 **프로젝트 정책**: 글로벌 에러 핸들러 사용하지 않음. 각 Controller에서 직접 에러 처리 수행.

**핵심 원칙**:
- Controller에서 `Either<Failure, Domain>` 결과를 직접 처리
- Failure 타입별 적절한 HTTP 상태 코드 매핑 (400, 401, 403, 404, 409, 422, 500)
- 일관된 에러 응답 구조 유지 (`error` + `meta` 필드)

## API 설계 체크리스트

### 설계 단계
- [ ] RESTful URL 구조 준수
- [ ] 적절한 HTTP 메서드 선택
- [ ] 헥사고날 아키텍처 기반 UseCase → Controller 흐름 설계
- [ ] 일관된 응답 구조 (data + meta)
- [ ] 적절한 HTTP 상태 코드
- [ ] 보안 고려사항 반영 (JWT, CORS, Rate Limiting)

### 구현 단계  
- [ ] UseCase는 `Either<Failure, Domain>` 반환 구조 적용
- [ ] **Service 레벨에서만 검증 수행** (Controller 검증 금지)
- [ ] Controller에서 도메인 객체를 응답 DTO로 변환
- [ ] Controller에서 직접 에러 처리 (글로벌 에러 처리 사용 안함)
- [ ] OpenAPI 문서 자동 생성
- [ ] 테스트 케이스 작성

### 응답 구조 검증
- [ ] 모든 성공 응답에 `data` + `meta` 구조 적용
- [ ] Meta 필드: timestamp, apiVersion, requestId 필수 포함
- [ ] 에러 응답에 `error` + `meta` 구조 적용
- [ ] 페이징 응답에 `data.items` + `data.pagination` 구조 적용
- [ ] ULID 기반 도메인 ID 사용 (prefix + ULID 형식)
- [ ] 도메인 ID 형식 검증 (정규식 패턴 적용)
- [ ] API URL에 버전 정보 포함 (`/api/v1/`)

### 문서화 및 테스트
- [ ] API 문서 작성 (OpenAPI 3.0)
- [ ] API 명세서 검토
- [ ] 성능 요구사항 정의