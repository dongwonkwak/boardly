# 도메인 모델(DDD)

핵심 도메인 개념과 불변조건을 정리합니다.

## 핵심 애그리게잇
- **Workspace**
  - Board와 멤버십을 소유. RBAC 경계 제공.
  - 불변조건: 소유자 기준 이름 유일; 접근은 멤버에게만 허용.
- **Board**
  - List와 Card를 포함. 하나의 Workspace에 속함.
  - 불변조건: 정렬 지수의 안정성; 아카이브 보드는 기본 조회에서 제외.
- **List**
  - 보드 내 카드의 순서 있는 컬렉션.
  - 불변조건: 재정렬 후 position은 밀집(dense)하고 단조 증가.
- **Card**
  - 작업 단위. 라벨, 담당자, 마감일, 댓글, 체크리스트(향후)를 가짐.
  - 불변조건: 정확히 하나의 List에 속함; 이동은 Activity로 이력 보존.
- **User**
  - 로케일 선호와 프로필을 가진 계정.
- **Label**
  - 워크스페이스 내 카드 분류를 위한 색상과 이름을 가진 태그.
  - 불변조건: 워크스페이스 내 이름 유일; 카드와 N:M 관계.
- **Comment**
  - 카드에 대한 사용자 의견. 카드와 1:N 관계.
  - 불변조건: 작성자만 수정/삭제 가능; 카드 삭제 시 함께 삭제.
- **Activity**
  - 보드/워크스페이스 내 모든 변경사항의 감사 로그.
  - 불변조건: 생성 후 변경 불가; 보드/워크스페이스 범위로 조회.
- **Invitation** (P1)
  - 워크스페이스/보드 초대 관리. 이메일과 역할 정보 포함.
  - 불변조건: 만료 시간 후 자동 무효화; 상태 변경은 한 번만 가능.
- **Notification** (P1)
  - 사용자별 개인 알림. 읽음 상태와 타겟 정보 포함.
  - 불변조건: 수신자만 읽음 상태 변경 가능; 타겟 삭제 시 함께 삭제.

## 값 객체
- **ID 값 객체**: `WorkspaceId`, `WorkspaceInvitationId` (`boardly-shared`)
- **도메인 ID**: BoardId, ListId, CardId, LabelId, CommentId, ActivityId (현재 DB ID 사용, 필요시 래핑 예정)
- **비즈니스 값**: `Position` (정렬 순서), `Color` (색상 코드), `DueDate` (마감일)

## 역할과 권한(RBAC)
- **워크스페이스 역할**: Owner, Admin, Member
- **보드 역할**: Board Admin, Board Editor, Board Viewer
- **권한 우선순위**: 워크스페이스 권한이 보드 권한보다 우선
- **상세 권한 정의**: [권한 시스템 문서](../architecture/permissions/) 참조

## 활동(Activity) 모델
- **이벤트 타입**: CardCreated, CardMoved, CommentAdded, LabelUpdated, AssigneeChanged, BoardCreated, ListCreated 등
- **범위**: 보드/워크스페이스 단위 감사/피드에 사용
- **불변성**: 생성 후 변경 불가, 시간순 정렬 보장
- **상세 정의**: [Activity API 문서](../api/activity.md) 참조

## 국제화(i18n)
- 사용자 로케일로 메시지 선택.
- 에러 키는 `messages.properties`, `messages_ko.properties`에 매핑.

## 일관성과 트랜잭션
- 애그리게잇 내부 쓰기는 트랜잭션으로 처리.
- 교차 애그리게잇 작업은 애플리케이션 서비스가 조정하고 필요시 보상 처리.

## 페이징/정렬 규칙
- 모든 컬렉션 API는 페이징을 사용(size, page, sort=createdAt|updatedAt|position).

## 관련 문서
- [권한 시스템](../architecture/permissions/) - 상세한 권한 정의 및 매트릭스
- [아키텍처 개요](../architecture/) - 전체 시스템 아키텍처
- [API 문서](../api/) - REST API 명세
- [데이터베이스 스키마](../db/) - 관계형 데이터 모델
- [사용자 스토리](../user_stories/) - 기능별 상세 요구사항
