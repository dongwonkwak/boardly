# Label API 문서

## 개요

라벨의 생성, 조회, 수정, 삭제를 관리하는 API입니다. 보드별 라벨 관리 및 카드에 라벨을 추가/제거하는 기능을 제공합니다.

## 엔드포인트

### GET /api/labels/board/{boardId}

특정 보드의 모든 라벨 목록을 조회합니다.

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
    "labelId": "label_123",
    "boardId": "board_456",
    "name": "긴급",
    "color": "#FF0000",
    "createdAt": "2025-01-15T09:30:00Z",
    "updatedAt": "2025-01-20T15:30:00Z"
  }
]
```

### POST /api/labels

새로운 라벨을 생성합니다.

#### 요청 데이터

```json
{
  "boardId": "board_456",
  "name": "긴급",
  "color": "#FF0000"
}
```

#### 응답

**HTTP Status**: 201 Created  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "labelId": "label_123",
  "boardId": "board_456",
  "name": "긴급",
  "color": "#FF0000",
  "createdAt": "2025-01-15T09:30:00Z",
  "updatedAt": "2025-01-15T09:30:00Z"
}
```

### GET /api/labels/{labelId}

특정 라벨의 정보를 조회합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `labelId` | string | Y | 조회할 라벨 ID |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "labelId": "label_123",
  "boardId": "board_456",
  "name": "긴급",
  "color": "#FF0000",
  "createdAt": "2025-01-15T09:30:00Z",
  "updatedAt": "2025-01-20T15:30:00Z"
}
```

### PUT /api/labels/{labelId}

기존 라벨의 이름과 색상을 수정합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `labelId` | string | Y | 수정할 라벨 ID |

#### 요청 데이터

```json
{
  "name": "수정된 라벨",
  "color": "#00FF00"
}
```

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

### DELETE /api/labels/{labelId}

라벨을 삭제합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `labelId` | string | Y | 삭제할 라벨 ID |

#### 응답

**HTTP Status**: 204 No Content

## 데이터 모델

### Label 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `labelId` | string | Y | 라벨 고유 식별자 |
| `boardId` | string | Y | 소속 보드 ID |
| `name` | string | Y | 라벨 이름 |
| `color` | string | Y | 라벨 색상 (HEX 코드) |
| `createdAt` | string | Y | 생성 시간 (ISO 8601 형식) |
| `updatedAt` | string | Y | 수정 시간 (ISO 8601 형식) |

### CreateLabelRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `boardId` | string | Y | 라벨을 생성할 보드 ID (1자 이상) |
| `name` | string | Y | 라벨 이름 (1-50자) |
| `color` | string | Y | 라벨 색상 (HEX 코드, 4-7자) |

### UpdateLabelRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | Y | 라벨 이름 (1-50자) |
| `color` | string | Y | 라벨 색상 (HEX 코드, 4-7자) |

## 에러 응답

### 400 Bad Request
```json
{
  "code": "BAD_REQUEST",
  "message": "잘못된 요청 데이터입니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/labels"
}
```

### 403 Forbidden
```json
{
  "code": "FORBIDDEN",
  "message": "라벨 생성 권한이 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/labels"
}
```

### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "보드 또는 라벨을 찾을 수 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/labels/label_123"
}
```

### 422 Unprocessable Entity
```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력 값이 유효하지 않습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/labels",
  "details": [
    {
      "field": "name",
      "message": "라벨 이름은 1자 이상 50자 이하여야 합니다.",
      "rejectedValue": ""
    },
    {
      "field": "color",
      "message": "라벨 색상은 유효한 HEX 코드여야 합니다.",
      "rejectedValue": "invalid-color"
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
  "path": "/api/labels/label_123"
}
```

## 구현 참고사항

### 프론트엔드 처리

1. **컬러 피커**: 라벨 색상 선택을 위한 컬러 피커 구현
2. **라벨 표시**: 카드에 라벨을 시각적으로 표시
3. **라벨 필터링**: 라벨별로 카드 필터링 기능 (미구현)
4. **실시간 업데이트**: WebSocket을 활용한 실시간 라벨 상태 동기화 (미구현)

### 백엔드 구현

1. **인증**: JWT Bearer Token을 통한 사용자 인증
2. **권한 확인**: 사용자가 접근할 수 있는 보드의 라벨만 조회/수정 가능
3. **색상 검증**: HEX 코드 형식 검증
4. **성능 최적화**: 인덱스를 활용한 빠른 조회 (boardId)


## 예시 cURL 요청

### 보드 라벨 목록 조회
```bash
curl -X GET "https://api.boardly.com/api/labels/board/board_456" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### 라벨 생성
```bash
curl -X POST "https://api.boardly.com/api/labels" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "boardId": "board_456",
    "name": "긴급",
    "color": "#FF0000"
  }'
```

### 라벨 조회
```bash
curl -X GET "https://api.boardly.com/api/labels/label_123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### 라벨 수정
```bash
curl -X PUT "https://api.boardly.com/api/labels/label_123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "수정된 라벨",
    "color": "#00FF00"
  }'
```

### 라벨 삭제
```bash
curl -X DELETE "https://api.boardly.com/api/labels/label_123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
``` 