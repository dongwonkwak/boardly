# BoardList API 문서

## 개요

보드 리스트의 생성, 조회, 수정, 삭제 및 위치 변경을 관리하는 API입니다. 보드 내에서 리스트를 관리하는 기능을 제공합니다.

## 엔드포인트

### GET /api/board-lists/{boardId}

특정 보드에 속한 모든 리스트를 position 순서대로 조회합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 조회할 보드 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
[
  {
    "listId": "list_123",
    "title": "할 일",
    "description": "해야 할 일들을 관리하는 리스트입니다",
    "position": 0,
    "color": "#0079BF",
    "boardId": "board_456",
    "createdAt": "2025-01-15T09:30:00Z",
    "updatedAt": "2025-01-20T15:30:00Z"
  }
]
```

### POST /api/board-lists/{boardId}

특정 보드에 새로운 리스트를 생성합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 리스트를 생성할 보드 ID |

#### 요청 데이터

```json
{
  "title": "할 일",
  "description": "해야 할 일들을 관리하는 리스트입니다",
  "color": "#0079BF"
}
```

#### 응답

**HTTP Status**: 201 Created  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "listId": "list_123",
  "title": "할 일",
  "description": "해야 할 일들을 관리하는 리스트입니다",
  "position": 0,
  "color": "#0079BF",
  "boardId": "board_456",
  "createdAt": "2025-01-15T09:30:00Z",
  "updatedAt": "2025-01-15T09:30:00Z"
}
```

### PUT /api/board-lists/{listId}

특정 보드 리스트의 정보를 수정합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `listId` | string | Y | 수정할 리스트 ID |

#### 요청 데이터

```json
{
  "title": "수정된 할 일",
  "description": "수정된 리스트 설명입니다",
  "color": "#FF6B6B"
}
```

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### DELETE /api/board-lists/{listId}

특정 보드 리스트를 삭제합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `listId` | string | Y | 삭제할 리스트 ID |

#### 응답

**HTTP Status**: 204 No Content

### PUT /api/board-lists/{listId}/position

특정 보드 리스트의 위치를 변경합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `listId` | string | Y | 위치를 변경할 리스트 ID |

#### 요청 데이터

```json
{
  "position": 2
}
```

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

## 데이터 모델

### BoardList 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `listId` | string | Y | 리스트 고유 식별자 |
| `title` | string | Y | 리스트 제목 |
| `description` | string | N | 리스트 설명 |
| `position` | integer | Y | 리스트 위치 (0부터 시작) |
| `color` | string | N | 리스트 색상 (HEX 코드) |
| `boardId` | string | Y | 소속 보드 ID |
| `createdAt` | string | Y | 생성 시간 (ISO 8601 형식) |
| `updatedAt` | string | Y | 수정 시간 (ISO 8601 형식) |

### CreateBoardListRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | string | Y | 리스트 제목 (1-100자) |
| `description` | string | N | 리스트 설명 (0-500자) |
| `color` | string | N | 리스트 색상 (HEX 코드) |

### UpdateBoardListRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | string | Y | 리스트 제목 (1-100자) |
| `description` | string | N | 리스트 설명 (0-500자) |
| `color` | string | N | 리스트 색상 (HEX 코드) |

### UpdateBoardListPositionRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `position` | integer | Y | 새로운 위치 (0 이상) |

## 에러 응답

### 400 Bad Request
```json
{
  "code": "BAD_REQUEST",
  "message": "잘못된 요청 데이터입니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/board-lists/list_123"
}
```

### 403 Forbidden
```json
{
  "code": "FORBIDDEN",
  "message": "보드 접근 권한이 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/board-lists/board_456"
}
```

### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "보드 또는 리스트를 찾을 수 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/board-lists/list_123"
}
```

### 422 Unprocessable Entity
```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력 값이 유효하지 않습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/board-lists/board_456",
  "details": [
    {
      "field": "title",
      "message": "리스트 제목은 1자 이상 100자 이하여야 합니다.",
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
  "path": "/api/board-lists/list_123"
}
```

## 구현 참고사항

### 프론트엔드 처리

1. **드래그 앤 드롭**: 리스트 위치 변경 시 드래그 앤 드롭 UI 구현
2. **실시간 업데이트**: WebSocket을 활용한 실시간 리스트 상태 동기화 (미구현)
3. **색상 선택**: 리스트 색상 선택을 위한 컬러 피커 구현 (미구현)
4. **카드 개수 표시**: 각 리스트에 포함된 카드 개수 표시 (미구현)

### 백엔드 구현

1. **인증**: JWT Bearer Token을 통한 사용자 인증
2. **권한 확인**: 사용자가 접근할 수 있는 보드의 리스트만 조회/수정 가능
3. **트랜잭션**: 리스트 위치 변경 시 원자적 처리
4. **성능 최적화**: 인덱스를 활용한 빠른 조회 (boardId, position)

## 예시 cURL 요청

### 리스트 목록 조회
```bash
curl -X GET "https://api.boardly.com/api/board-lists/board_456" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### 리스트 생성
```bash
curl -X POST "https://api.boardly.com/api/board-lists/board_456" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "할 일",
    "description": "해야 할 일들을 관리하는 리스트입니다",
    "color": "#0079BF"
  }'
```

### 리스트 수정
```bash
curl -X PUT "https://api.boardly.com/api/board-lists/list_123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "수정된 할 일",
    "description": "수정된 리스트 설명입니다",
    "color": "#FF6B6B"
  }'
```

### 리스트 위치 변경
```bash
curl -X PUT "https://api.boardly.com/api/board-lists/list_123/position" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "position": 2
  }'
```

### 리스트 삭제
```bash
curl -X DELETE "https://api.boardly.com/api/board-lists/list_123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
``` 