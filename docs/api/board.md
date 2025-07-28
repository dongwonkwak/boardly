# Board API 문서

## 개요

보드의 생성, 조회, 수정, 삭제 및 상태 관리를 담당하는 API입니다. 보드의 즐겨찾기, 아카이브, 멤버 관리 등의 기능을 제공합니다.

## 엔드포인트

### GET /api/boards

현재 사용자가 소유한 보드 목록을 조회합니다.

#### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `includeArchived` | boolean | N | false | 아카이브된 보드 포함 여부 |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
[
  {
    "boardId": "board_123",
    "title": "웹 개발 프로젝트",
    "description": "React와 Spring Boot를 사용한 칸반 보드 애플리케이션",
    "isArchived": false,
    "ownerId": "user_101",
    "isStarred": true,
    "createdAt": "2025-01-15T09:30:00Z",
    "updatedAt": "2025-01-20T15:30:00Z"
  }
]
```

### POST /api/boards

새로운 보드를 생성합니다.

#### 요청 데이터

```json
{
  "title": "새로운 보드",
  "description": "보드 설명"
}
```

#### 응답

**HTTP Status**: 201 Created  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "boardId": "board_123",
  "title": "새로운 보드",
  "description": "보드 설명",
  "isArchived": false,
  "ownerId": "user_101",
  "isStarred": false,
  "createdAt": "2025-01-15T09:30:00Z",
  "updatedAt": "2025-01-15T09:30:00Z"
}
```

### PUT /api/boards/{boardId}

기존 보드의 제목과 설명을 업데이트합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 업데이트할 보드 ID |

#### 요청 데이터

```json
{
  "title": "수정된 보드 제목",
  "description": "수정된 보드 설명"
}
```

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### DELETE /api/boards/{boardId}

보드를 영구적으로 삭제합니다. 보드와 관련된 모든 데이터(리스트, 카드, 멤버)가 함께 삭제됩니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 삭제할 보드 ID |

#### 응답

**HTTP Status**: 204 No Content

### POST /api/boards/{boardId}/star

보드를 즐겨찾기에 추가합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 즐겨찾기에 추가할 보드 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### POST /api/boards/{boardId}/unstar

보드를 즐겨찾기에서 제거합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 즐겨찾기에서 제거할 보드 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### POST /api/boards/{boardId}/archive

보드를 아카이브합니다. 아카이브된 보드는 읽기 전용이 되며, 내용 수정이 불가능합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 아카이브할 보드 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### POST /api/boards/{boardId}/unarchive

보드를 언아카이브합니다. 언아카이브된 보드는 다시 활성 상태가 되며, 내용 수정이 가능합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 언아카이브할 보드 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### DELETE /api/boards/{boardId}/members/{targetUserId}

보드에서 특정 멤버를 삭제합니다. 보드 소유자만 멤버를 삭제할 수 있습니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 보드 ID |
| `targetUserId` | string | Y | 삭제할 멤버의 사용자 ID |

#### 응답

**HTTP Status**: 200 OK

## 데이터 모델

### Board 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `boardId` | string | Y | 보드 고유 식별자 |
| `title` | string | Y | 보드 제목 |
| `description` | string | N | 보드 설명 |
| `isArchived` | boolean | Y | 아카이브 상태 |
| `ownerId` | string | Y | 보드 소유자 ID |
| `isStarred` | boolean | Y | 즐겨찾기 상태 |
| `createdAt` | string | Y | 생성 시간 (ISO 8601 형식) |
| `updatedAt` | string | Y | 수정 시간 (ISO 8601 형식) |

### CreateBoardRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | string | Y | 보드 제목 |
| `description` | string | N | 보드 설명 |

### UpdateBoardRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | string | N | 보드 제목 |
| `description` | string | N | 보드 설명 |

## 에러 응답

### 400 Bad Request
```json
{
  "code": "BAD_REQUEST",
  "message": "잘못된 요청 데이터입니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/boards/board_123"
}
```

### 403 Forbidden
```json
{
  "code": "FORBIDDEN",
  "message": "보드 수정 권한이 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/boards/board_123"
}
```

### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "보드를 찾을 수 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/boards/board_123"
}
```

### 409 Conflict
```json
{
  "code": "CONFLICT",
  "message": "아카이브된 보드는 수정 불가합니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/boards/board_123"
}
```

### 422 Unprocessable Entity
```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력 값이 유효하지 않습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/boards",
  "details": [
    {
      "field": "title",
      "message": "보드 제목은 필수입니다.",
      "rejectedValue": ""
    }
  ]
}
```

### 500 Internal Server Error
```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "서버 오류가 발생했습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/boards/board_123"
}
```

## 구현 참고사항

### 프론트엔드 처리

1. **보드 카드 UI**: 보드 목록을 카드 형태로 표시
2. **즐겨찾기 기능**: 즐겨찾기한 보드를 상단에 표시
3. **아카이브 상태 표시**: 아카이브된 보드는 시각적으로 구분 (미구현)
4. **실시간 업데이트**: WebSocket을 활용한 실시간 보드 상태 동기화 (미구현)

### 백엔드 구현

1. **인증**: JWT Bearer Token을 통한 사용자 인증
2. **권한 확인**: 사용자가 접근할 수 있는 보드만 조회/수정 가능
3. **트랜잭션**: 보드 삭제 시 관련 데이터 모두 삭제
4. **성능 최적화**: 인덱스를 활용한 빠른 조회 (ownerId, isStarred)


## 예시 cURL 요청

### 보드 목록 조회
```bash
curl -X GET "https://api.boardly.com/api/boards?includeArchived=false" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### 보드 생성
```bash
curl -X POST "https://api.boardly.com/api/boards" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "새로운 보드",
    "description": "보드 설명"
  }'
```

### 보드 수정
```bash
curl -X PUT "https://api.boardly.com/api/boards/board_123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "수정된 보드 제목",
    "description": "수정된 보드 설명"
  }'
```

### 보드 즐겨찾기 추가
```bash
curl -X POST "https://api.boardly.com/api/boards/board_123/star" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### 보드 아카이브
```bash
curl -X POST "https://api.boardly.com/api/boards/board_123/archive" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### 보드 삭제
```bash
curl -X DELETE "https://api.boardly.com/api/boards/board_123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
``` 