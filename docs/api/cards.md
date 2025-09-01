# Cards API 문서

## 개요

카드의 생성, 조회, 수정, 삭제 및 이동을 관리하는 API입니다. 카드에 멤버 할당, 라벨 추가, 복제, 우선순위 설정, 완료 상태 관리 등의 기능을 제공합니다.

## 엔드포인트

### GET /api/cards/{cardId}

특정 카드의 상세 정보를 조회합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 조회할 카드 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "cardId": "card_123",
  "title": "API 설계",
  "description": "RESTful API 설계 및 문서화",
  "position": 0,
  "listId": "list_456",
  "createdAt": "2025-01-15T09:30:00Z",
  "updatedAt": "2025-01-20T15:30:00Z"
}
```

### PUT /api/cards/{cardId}

카드의 제목과 설명을 수정합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 수정할 카드 ID |

#### 요청 데이터

```json
{
  "title": "수정된 카드 제목",
  "description": "수정된 카드 설명"
}
```

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### DELETE /api/cards/{cardId}

카드를 영구적으로 삭제합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 삭제할 카드 ID |

#### 응답

**HTTP Status**: 204 No Content

### PUT /api/cards/{cardId}/start-date

카드의 시작일을 수정합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 수정할 카드 ID |

#### 요청 데이터

```json
{
  "startDate": "2024-01-01T00:00:00Z"
}
```

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### PUT /api/cards/{cardId}/priority

카드의 우선순위를 수정합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 수정할 카드 ID |

#### 요청 데이터

```json
{
  "priority": "high"
}
```

#### 우선순위 값

| 값 | 설명 |
|----|------|
| `low` | 낮음 |
| `medium` | 보통 |
| `high` | 높음 |
| `urgent` | 긴급 |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### PUT /api/cards/{cardId}/completed

카드의 완료 상태를 수정합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 수정할 카드 ID |

#### 요청 데이터

```json
{
  "isCompleted": true
}
```

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### PUT /api/cards/{cardId}/move

카드를 같은 리스트 내에서 이동하거나 다른 리스트로 이동합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 이동할 카드 ID |

#### 요청 데이터

```json
{
  "targetListId": "list_789",
  "newPosition": 2
}
```

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### POST /api/cards

새로운 카드를 생성합니다.

#### 요청 데이터

```json
{
  "title": "새로운 카드",
  "description": "카드 설명",
  "listId": "list_456"
}
```

#### 응답

**HTTP Status**: 201 Created  
**Content-Type**: application/json

### GET /api/cards/lists/{listId}

특정 리스트에 속한 모든 카드를 위치 순서대로 조회합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `listId` | string | Y | 조회할 리스트 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
[
  {
    "cardId": "card_123",
    "title": "API 설계",
    "description": "RESTful API 설계 및 문서화",
    "position": 0,
    "listId": "list_456",
    "createdAt": "2025-01-15T09:30:00Z",
    "updatedAt": "2025-01-20T15:30:00Z"
  }
]
```

### GET /api/cards/lists/{listId}/search

특정 리스트에서 카드 제목으로 검색합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `listId` | string | Y | 검색할 리스트 ID |

#### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `searchTerm` | string | Y | 검색어 |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### GET /api/cards/{cardId}/members

카드에 할당된 멤버 목록을 조회합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 멤버를 조회할 카드 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
[
  {
    "memberId": "user_101",
    "firstName": "개발",
    "lastName": "김",
    "email": "dev@example.com"
  }
]
```

### POST /api/cards/{cardId}/members

카드에 멤버를 할당합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 멤버를 할당할 카드 ID |

#### 요청 데이터

```json
{
  "memberId": "user_101"
}
```

#### 응답

**HTTP Status**: 200 OK

### DELETE /api/cards/{cardId}/members

카드에서 멤버를 해제합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 멤버를 해제할 카드 ID |

#### 요청 데이터

```json
{
  "memberId": "user_101"
}
```

#### 응답

**HTTP Status**: 200 OK

### GET /api/cards/{cardId}/labels

카드에 추가된 라벨 목록을 조회합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 라벨을 조회할 카드 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
[
  {
    "labelId": "label_123",
    "name": "긴급",
    "color": "#FF0000"
  }
]
```

### POST /api/cards/{cardId}/labels

카드에 라벨을 추가합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 라벨을 추가할 카드 ID |

#### 요청 데이터

```json
{
  "labelId": "label_123"
}
```

#### 응답

**HTTP Status**: 200 OK

### DELETE /api/cards/{cardId}/labels

카드에서 라벨을 제거합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 라벨을 제거할 카드 ID |

#### 요청 데이터

```json
{
  "labelId": "label_123"
}
```

#### 응답

**HTTP Status**: 200 OK

### POST /api/cards/{cardId}/clone

기존 카드의 내용을 복사하여 새로운 카드를 생성합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cardId` | string | Y | 복제할 카드 ID |

#### 요청 데이터

```json
{
  "newTitle": "복제된 카드",
  "targetListId": "list_789"
}
```

#### 응답

**HTTP Status**: 201 Created  
**Content-Type**: application/json

## 데이터 모델

### Card 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `cardId` | string | Y | 카드 고유 식별자 |
| `title` | string | Y | 카드 제목 |
| `description` | string | N | 카드 설명 |
| `position` | integer | Y | 카드 위치 (0부터 시작) |
| `listId` | string | Y | 소속 리스트 ID |
| `createdAt` | string | Y | 생성 시간 (ISO 8601 형식) |
| `updatedAt` | string | Y | 수정 시간 (ISO 8601 형식) |

### UpdateCardRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | string | N | 카드 제목 |
| `description` | string | N | 카드 설명 |

### UpdateCardStartDateRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `startDate` | string | N | 시작일 (ISO 8601 형식) |

### UpdateCardPriorityRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `priority` | string | Y | 우선순위 (low, medium, high, urgent) |

### UpdateCardCompletedRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `isCompleted` | boolean | Y | 완료 상태 |

### MoveCardRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `targetListId` | string | Y | 이동할 리스트 ID |
| `newPosition` | integer | Y | 새로운 위치 |

### CardMember 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `memberId` | string | Y | 멤버 고유 식별자 |
| `firstName` | string | Y | 멤버 이름 |
| `lastName` | string | Y | 멤버 성 |
| `email` | string | Y | 멤버 이메일 |

### LabelId 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `labelId` | string | Y | 라벨 고유 식별자 |
| `name` | string | Y | 라벨 이름 |
| `color` | string | Y | 라벨 색상 (HEX 코드) |

## 에러 응답

### 400 Bad Request
```json
{
  "code": "BAD_REQUEST",
  "message": "잘못된 요청 데이터입니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/cards/card_123"
}
```

### 403 Forbidden
```json
{
  "code": "FORBIDDEN",
  "message": "카드 조회 권한이 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/cards/card_123"
}
```

### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "카드를 찾을 수 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/cards/card_123"
}
```

### 409 Conflict
```json
{
  "code": "CONFLICT",
  "message": "아카이브된 보드의 카드는 수정 불가합니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/cards/card_123"
}
```

### 422 Unprocessable Entity
```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력 값이 유효하지 않습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/cards",
  "details": [
    {
      "field": "title",
      "message": "카드 제목은 필수입니다.",
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
  "path": "/api/cards/card_123"
}
```

## 구현 참고사항

### 프론트엔드 처리

1. **드래그 앤 드롭**: 카드 이동 시 드래그 앤 드롭 UI 구현 (미구현)
2. **실시간 업데이트**: WebSocket을 활용한 실시간 카드 상태 동기화 (미구현)
3. **검색 기능**: 카드 제목 검색 시 실시간 필터링 (미구현)
4. **멤버 표시**: 카드에 할당된 멤버들의 아바타 표시
5. **우선순위 표시**: 우선순위에 따른 시각적 표시 (색상, 아이콘 등)
6. **완료 상태 표시**: 완료된 카드의 시각적 구분

### 백엔드 구현

1. **인증**: JWT Bearer Token을 통한 사용자 인증
2. **권한 확인**: 사용자가 접근할 수 있는 카드만 조회/수정 가능
3. **트랜잭션**: 카드 이동 시 원자적 처리
4. **성능 최적화**: 인덱스를 활용한 빠른 조회 (listId, position)
5. **우선순위 검증**: 유효한 우선순위 값 검증
6. **완료 상태 관리**: 완료 시간 및 완료자 정보 추적


## 예시 cURL 요청

### 카드 조회
```bash
curl -X GET "https://api.boardly.com/api/cards/card_123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### 카드 생성
```bash
curl -X POST "https://api.boardly.com/api/cards" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "새로운 카드",
    "description": "카드 설명",
    "listId": "list_456"
  }'
```

### 카드 우선순위 업데이트
```bash
curl -X PUT "https://api.boardly.com/api/cards/card_123/priority" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "priority": "high"
  }'
```

### 카드 시작일 업데이트
```bash
curl -X PUT "https://api.boardly.com/api/cards/card_123/start-date" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2024-01-01T00:00:00Z"
  }'
```

### 카드 완료 상태 업데이트
```bash
curl -X PUT "https://api.boardly.com/api/cards/card_123/completed" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "isCompleted": true
  }'
```

### 카드 이동
```bash
curl -X PUT "https://api.boardly.com/api/cards/card_123/move" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "targetListId": "list_789",
    "newPosition": 2
  }'
```

### 카드 검색
```bash
curl -X GET "https://api.boardly.com/api/cards/lists/list_456/search?searchTerm=API" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
``` 