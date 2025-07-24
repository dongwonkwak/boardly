# Dashboard API 문서

## 개요

대시보드 화면에서 사용자의 보드 목록, 최근 활동, 통계 정보를 제공하는 API입니다.

## 엔드포인트

### GET /api/dashboard

사용자의 대시보드 데이터를 조회합니다.


#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "boards": [
    {
      "id": 1,
      "title": "웹 개발 프로젝트",
      "description": "React와 Spring Boot를 사용한 칸반 보드 애플리케이션",
      "createdAt": "2025-01-15T09:30:00Z",
      "listCount": 4,
      "cardCount": 12,
      "isStarred": true,
      "color": "blue-purple",
      "role": "owner"
    }
  ],
  "recentActivity": [
    {
      "id": "activity_9c8b7a6d",
      "type": "CARD_MOVE",
      "actor": {
        "id": "user_101",
        "firstName": "개발",
        "lastName": "김",
        "profileImageUrl": "https://placehold.co/40x40/0284C7/FFFFFF?text=김"
      },
      "timestamp": "2025-01-20T15:30:00.123Z",
      "payload": {
        "cardTitle": "API 설계",
        "sourceListName": "진행 중",
        "destListName": "완료",
        "cardId": "card_456",
        "sourceListId": "list_123",
        "destListId": "list_789"
      }
    }
  ],
  "statistics": {
    "totalBoards": 4,
    "totalCards": 41,
    "starredBoards": 2,
    "archivedBoards": 0
  }
}
```

## 데이터 모델

### Board 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | number | Y | 보드 고유 식별자 |
| `title` | string | Y | 보드 제목 |
| `description` | string | N | 보드 설명 |
| `createdAt` | string | Y | 보드 생성 일시 (ISO 8601 형식) |
| `listCount` | number | Y | 보드 내 리스트 개수 |
| `cardCount` | number | Y | 보드 내 카드 개수 |
| `isStarred` | boolean | Y | 즐겨찾기 여부 |
| `color` | string | Y | 보드 색상 테마 |
| `role` | string | Y | 현재 사용자의 보드 내 역할 |

#### Role 타입

- `owner`: 보드 소유자 (모든 권한)
- `admin`: 관리자 (멤버 관리, 보드 설정 변경 가능)
- `member`: 일반 멤버 (카드/리스트 편집 가능)

#### Color 타입

사용 가능한 보드 색상 테마:
- `blue-purple`: 파란색-보라색 그라데이션
- `green-teal`: 초록색-청록색 그라데이션
- `orange-red`: 주황색-빨간색 그라데이션
- `purple-pink`: 보라색-분홍색 그라데이션
- `gray-slate`: 회색-슬레이트 그라데이션
- `indigo-blue`: 인디고-파란색 그라데이션

### Activity 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | string | Y | 활동 고유 식별자 |
| `type` | string | Y | 활동 타입 |
| `actor` | Actor | Y | 활동 수행자 정보 |
| `timestamp` | string | Y | 활동 발생 시간 (ISO 8601, 밀리초 포함) |
| `payload` | object | Y | 활동 타입별 상세 데이터 |

#### Actor 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | string | Y | 사용자 고유 식별자 |
| `firstName` | string | Y | 사용자 이름 |
| `lastName` | string | Y | 사용자 성 |
| `profileImageUrl` | string | Y | 프로필 이미지 URL |

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

### Statistics 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `totalBoards` | number | Y | 사용자의 전체 보드 수 |
| `totalCards` | number | Y | 사용자의 전체 카드 수 |
| `starredBoards` | number | Y | 즐겨찾기한 보드 수 |
| `archivedBoards` | number | Y | 보관된 보드 수 |

## 에러 응답

### 401 Unauthorized
```json
{
  "error": "UNAUTHORIZED",
  "message": "유효하지 않은 인증 토큰입니다.",
  "timestamp": "2025-01-20T15:30:00Z"
}
```

### 403 Forbidden
```json
{
  "error": "FORBIDDEN",
  "message": "접근 권한이 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z"
}
```

### 500 Internal Server Error
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "서버 내부 오류가 발생했습니다.",
  "timestamp": "2025-01-20T15:30:00Z"
}
```

## 구현 참고사항

### 프론트엔드 처리

1. **시간 표시**: `timestamp` 필드는 ISO 8601 형식이므로 프론트엔드에서 "2시간 전", "1일 전" 등으로 변환 처리
2. **색상 매핑**: `color` 필드값을 CSS 클래스로 매핑하여 보드 카드 스타일링
3. **권한 제어**: `role` 필드를 기반으로 UI 요소 표시/숨김 처리
4. **다국어 처리**: `actor.firstName`과 `actor.lastName`을 언어별로 조합 (한국어: 성+이름, 영어: 이름+성)

### 백엔드 구현

1. **인증**: JWT Bearer Token을 통한 사용자 인증
2. **권한 확인**: 사용자가 접근할 수 있는 보드들만 필터링하여 반환
3. **활동 로그**: 최근 20개 활동만 반환 (페이지네이션 고려)
4. **성능 최적화**: 보드별 리스트/카드 개수는 캐싱 또는 집계 테이블 활용

### 캐싱 전략

- **응답 캐싱**: 5분간 Redis 캐싱 권장
- **무효화 조건**: 보드/리스트/카드 생성/수정/삭제 시 캐시 무효화
- **사용자별 캐싱**: 사용자 ID를 캐시 키에 포함

## 예시 cURL 요청

```bash
curl -X GET "https://api.boardly.com/api/dashboard" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```