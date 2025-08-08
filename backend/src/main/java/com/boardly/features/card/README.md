# 카드 상세 정보 조회 기능

## 개요

카드를 선택했을 때 카드의 모든 상세 정보를 조회하는 기능을 구현했습니다. 헥사고날 아키텍처에 맞게 설계되었습니다.

## 구현된 구조

### 1. 도메인 모델

#### CardDetail
- 카드의 모든 상세 정보를 포함하는 집계 루트
- 카드 기본 정보, 라벨, 멤버, 첨부파일, 댓글, 활동 내역 등을 포함

#### CardAttachment
- 카드 첨부파일 정보

#### CardComment
- 카드 댓글 정보

#### BoardMember
- 보드 멤버 정보

#### CardActivity
- 카드 활동 내역 정보

### 2. Application Layer

#### GetCardDetailQuery
- 카드 상세 정보 조회를 위한 Query 객체

#### CardDetailQueryPort
- 카드 상세 정보 조회를 위한 포트 인터페이스

#### GetCardDetailUseCase
- 카드 상세 정보 조회 비즈니스 로직

### 3. Presentation Layer

#### CardDetailResponse
- 카드 상세 정보 응답 DTO
- 요청한 JSON 형식에 맞게 구성
- 중첩된 응답 객체들 포함 (AssigneeResponse, AttachmentResponse, CommentResponse 등)

#### CardController
- `GET /api/v1/cards/{cardId}/detail` 엔드포인트 추가
- 기존 카드 조회와 구분하여 상세 정보 조회 기능 제공

### 4. Infrastructure Layer

#### CardDetailQueryAdapter
- 카드 상세 정보 조회를 위한 Infrastructure 구현체
- 현재는 기본 구조만 구현 (실제 데이터 조회는 TODO)

## API 엔드포인트

### 카드 상세 정보 조회
```
GET /api/v1/cards/{cardId}/detail
```

**응답 예시:**
```json
{
  "cardId": "card-1",
  "title": "메인 페이지 디자인",
  "description": "사용자 인터페이스 디자인 작업",
  "position": 1,
  "priority": "high",
  "isCompleted": false,
  "isArchived": false,
  "dueDate": "2025-08-13T09:21:33.436705Z",
  "startDate": "2025-08-01T09:00:00.000Z",
  "completedAt": null,
  "completedBy": null,
  "listId": "list-1",
  "listName": "백로그",
  "boardId": "board-1",
  "boardName": "웹 개발 프로젝트",
  "labels": [...],
  "assignees": [...],
  "attachments": [...],
  "attachmentCount": 2,
  "commentCount": 3,
  "lastCommentAt": "2025-08-07T16:45:30.000Z",
  "comments": [...],
  "boardMembers": [...],
  "boardLabels": [...],
  "activities": [...],
  "createdBy": {...},
  "createdAt": "2025-08-06T09:21:33.436705Z",
  "updatedAt": "2025-08-07T16:45:30.000Z"
}
```

## 테스트

### UseCase 테스트
- `GetCardDetailUseCaseTest`: 카드 상세 정보 조회 UseCase 테스트
- 성공 케이스, 실패 케이스, 편의 메서드 테스트 포함

### Controller 테스트
- `CardDetailControllerTest`: 카드 상세 정보 조회 Controller 테스트
- 성공 케이스, 권한 없음, 인증 없음 등 다양한 시나리오 테스트

## TODO

### Infrastructure 구현
현재 `CardDetailQueryAdapter`는 기본 구조만 구현되어 있습니다. 실제 구현에서는 다음 작업들이 필요합니다:

1. **카드 기본 정보 조회**
2. **리스트 정보 조회** (리스트 이름)
3. **보드 정보 조회** (보드 이름)
4. **카드 라벨 정보 조회**
5. **카드 멤버 정보 조회** (사용자 정보 포함)
6. **첨부파일 정보 조회**
7. **댓글 정보 조회**
8. **보드 멤버 정보 조회**
9. **보드 라벨 정보 조회**
10. **활동 내역 조회**
11. **생성자 정보 조회**

### 성능 최적화
- 여러 테이블 조인을 통한 단일 쿼리로 최적화
- 필요한 경우 캐싱 적용
- 페이징 처리 (댓글, 활동 내역 등)

### 보안
- 사용자 권한 검증 강화
- 민감한 정보 필터링

## 사용법

1. **기본 카드 조회**: `GET /api/v1/cards/{cardId}`
   - 카드의 기본 정보만 조회

2. **상세 카드 조회**: `GET /api/v1/cards/{cardId}/detail`
   - 카드의 모든 상세 정보 조회 (라벨, 멤버, 첨부파일, 댓글, 활동 내역 등)

## 아키텍처 원칙 준수

- **의존성 역전**: Application Layer가 Infrastructure Layer에 의존하지 않음
- **단일 책임**: 각 클래스가 명확한 책임을 가짐
- **개방-폐쇄**: 새로운 기능 추가 시 기존 코드 수정 없이 확장 가능
- **테스트 용이성**: 각 레이어별로 독립적인 테스트 가능
