# Activities API 문서

## 개요

사용자와 보드의 활동 내역을 관리하는 API입니다. 카드 생성, 이동, 수정 등의 활동을 추적하고 조회할 수 있습니다.

## 엔드포인트

### GET /api/activities/me

현재 사용자의 모든 활동 목록을 최신순으로 조회합니다.

#### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `page` | integer | N | 0 | 페이지 번호 (0부터 시작) |
| `size` | integer | N | 50 | 페이지 크기 (최대: 100) |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "activities": [
    {
      "id": "activity_9c8b7a6d",
      "type": "CARD_MOVE",
      "actor": {
        "id": "user_101",
        "firstName": "개발",
        "lastName": "김"
      },
      "timestamp": "2025-01-20T15:30:00.123Z",
      "payload": {
        "cardTitle": "API 설계",
        "sourceListName": "진행 중",
        "destListName": "완료",
        "cardId": "card_456",
        "sourceListId": "list_123",
        "destListId": "list_789"
      },
      "boardName": "프로젝트 A",
      "boardId": "board_123"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 50,
    "totalElements": 150,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### GET /api/activities/boards/{boardId}

특정 보드의 활동 목록을 페이징과 시간 필터링을 통해 조회합니다.

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `boardId` | string | Y | 조회할 보드 ID |

#### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `page` | integer | N | 0 | 페이지 번호 (0부터 시작) |
| `size` | integer | N | 50 | 페이지 크기 (최대: 100) |
| `since` | string | N | - | 특정 시점 이후 활동 조회 (ISO 8601 형식) |

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "activities": [
    {
      "id": "activity_9c8b7a6d",
      "type": "CARD_MOVE",
      "actor": {
        "id": "user_101",
        "firstName": "개발",
        "lastName": "김"
      },
      "timestamp": "2025-01-20T15:30:00.123Z",
      "payload": {
        "cardTitle": "API 설계",
        "sourceListName": "진행 중",
        "destListName": "완료",
        "cardId": "card_456",
        "sourceListId": "list_123",
        "destListId": "list_789"
      },
      "boardName": "프로젝트 A",
      "boardId": "board_123"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 50,
    "totalElements": 150,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

## 데이터 모델

### Activity 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | string | Y | 활동 고유 식별자 |
| `type` | string | Y | 활동 타입 |
| `actor` | Actor | Y | 활동 수행자 정보 |
| `timestamp` | string | Y | 활동 발생 시간 (ISO 8601, 밀리초 포함) |
| `payload` | object | Y | 활동 타입별 상세 데이터 |
| `boardName` | string | Y | 보드 이름 |
| `boardId` | string | Y | 보드 ID |

#### Actor 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | string | Y | 사용자 고유 식별자 |
| `firstName` | string | Y | 사용자 이름 |
| `lastName` | string | Y | 사용자 성 |

#### Activity Type

활동 타입과 해당 payload 구조:

##### CARD_MOVE (카드 이동)
```json
{
  "cardTitle": "API 설계",
  "sourceListName": "진행 중",
  "destListName": "완료",
  "cardId": "card_456",
  "sourceListId": "list_123",
  "destListId": "list_789"
}
```

##### CARD_CREATE (카드 생성)
```json
{
  "listName": "할 일",
  "cardTitle": "사용자 인증 구현",
  "listId": "list_123",
  "cardId": "card_789"
}
```

##### BOARD_CREATE (보드 생성)
```json
{
  "boardName": "독서 계획",
  "boardId": "board_4"
}
```

##### LIST_CREATE (리스트 생성)
```json
{
  "listName": "리뷰 완료",
  "listId": "list_567",
  "boardName": "팀 스터디 계획"
}
```

##### CARD_RENAME (카드 이름 변경)
```json
{
  "oldTitle": "데이터베이스 설계",
  "newTitle": "PostgreSQL 데이터베이스 설계 및 최적화",
  "cardId": "card_234"
}
```

##### CARD_ADD_COMMENT (댓글 추가)
```json
{
  "cardTitle": "알고리즘 문제 풀이",
  "cardId": "card_345"
}
```

##### CARD_SET_DUE_DATE (마감일 설정)
```json
{
  "cardTitle": "클린 코드 완독",
  "dueDate": "2025-01-31",
  "cardId": "card_456"
}
```

##### CARD_ADD_ATTACHMENT (파일 첨부)
```json
{
  "cardTitle": "프로젝트 명세서",
  "fileName": "project-spec-v2.pdf",
  "cardId": "card_567"
}
```

### Pagination 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `page` | integer | Y | 현재 페이지 번호 |
| `size` | integer | Y | 페이지 크기 |
| `totalElements` | integer | Y | 전체 요소 수 |
| `totalPages` | integer | Y | 전체 페이지 수 |
| `hasNext` | boolean | Y | 다음 페이지 존재 여부 |
| `hasPrevious` | boolean | Y | 이전 페이지 존재 여부 |

## 에러 응답

### 400 Bad Request
```json
{
  "code": "BAD_REQUEST",
  "message": "잘못된 요청 데이터입니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/activities/me"
}
```

### 403 Forbidden
```json
{
  "code": "FORBIDDEN",
  "message": "활동 조회 권한이 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/activities/boards/board_123"
}
```

### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "보드를 찾을 수 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/activities/boards/board_123"
}
```

### 422 Unprocessable Entity
```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력 값이 유효하지 않습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/activities/me",
  "details": [
    {
      "field": "page",
      "message": "페이지 번호는 0 이상이어야 합니다.",
      "rejectedValue": -1
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
  "path": "/api/activities/me"
}
```

## 구현 참고사항

### 프론트엔드 처리

1. **시간 표시**: `timestamp` 필드는 ISO 8601 형식이므로 프론트엔드에서 "2시간 전", "1일 전" 등으로 변환 처리
2. **페이지네이션**: `pagination` 객체를 활용하여 무한 스크롤 또는 페이지네이션 UI 구현
3. **활동 타입별 아이콘**: 각 `type`에 맞는 아이콘을 표시하여 시각적 구분
4. **다국어 처리**: `actor.firstName`과 `actor.lastName`을 언어별로 조합 (한국어: 성+이름, 영어: 이름+성)

### 백엔드 구현

1. **인증**: JWT Bearer Token을 통한 사용자 인증
2. **권한 확인**: 사용자가 접근할 수 있는 보드의 활동만 필터링하여 반환
3. **성능 최적화**: 인덱스를 활용한 빠른 조회 (timestamp, boardId, userId)
4. **캐싱**: 자주 조회되는 활동 데이터는 Redis 캐싱 활용


## 예시 cURL 요청

### 내 활동 목록 조회
```bash
curl -X GET "https://api.boardly.com/api/activities/me?page=0&size=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### 보드 활동 목록 조회
```bash
curl -X GET "https://api.boardly.com/api/activities/boards/board_123?page=0&size=20&since=2025-01-01T00:00:00Z" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
``` 