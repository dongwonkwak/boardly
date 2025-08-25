# Boardly 도메인 모델

## 개요
Boardly는 칸반 보드 기반의 프로젝트 관리 서비스로, 워크스페이스, 보드, 카드를 중심으로 한 도메인 모델을 가지고 있습니다.

## 핵심 도메인

### 1. 사용자 (User)
시스템을 사용하는 사용자를 나타냅니다.

#### 속성
- `id`: 사용자 고유 식별자 (usr_ + ULID)
- `email`: 이메일 주소
- `username`: 사용자명
- `displayName`: 표시명
- `passwordHash`: 비밀번호 해시
- `createdAt`: 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 사용자 ID는 고유해야 한다
- 이메일은 고유해야 한다
- 사용자명은 고유해야 한다
- 비밀번호는 해시화되어 저장되어야 한다
- 사용자 생성 시 생성일시가 자동으로 설정된다
- 사용자 정보 수정 시 수정일시가 자동으로 업데이트된다
- 이메일과 사용자명은 중복될 수 없다

### 2. 워크스페이스 (Workspace)
개인 또는 팀이 작업하는 공간을 나타냅니다.

#### 속성
- `id`: 워크스페이스 고유 식별자 (wks_ + ULID)
- `name`: 워크스페이스 이름
- `description`: 워크스페이스 설명
- `type`: 워크스페이스 타입 (PERSONAL/TEAM)
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 워크스페이스 이름은 1-50자 사이여야 한다
- 워크스페이스 설명은 최대 200자까지 입력 가능하다
- 개인 워크스페이스는 사용자당 1개씩 자동 생성된다
- 팀 워크스페이스는 무제한으로 생성 가능하다
- 동일한 사용자는 같은 워크스페이스 이름을 가질 수 없다
- 워크스페이스를 생성한 사용자는 자동으로 생성자 역할을 받는다
- 워크스페이스에는 최소 1명의 ADMIN이 있어야 한다

### 3. 워크스페이스 멤버 (WorkspaceMember)
사용자와 워크스페이스 간의 관계를 나타냅니다.

#### 속성
- `workspaceId`: 워크스페이스 ID (합성키 일부)
- `userId`: 사용자 ID (합성키 일부)
- `role`: 멤버 역할 (ADMIN/MEMBER)
- `invitedBy`: 초대한 사용자 ID
- `invitedAt`: 초대일시
- `joinedAt`: 가입일시
- `status`: 멤버 상태 (ACTIVE/INVITED/REMOVED)
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 워크스페이스 생성자는 자동으로 생성자가 되고 ADMIN 권한을 받는다
- 생성자는 다른 ADMIN 멤버에게 생성자 권한을 이전할 수 있다
- 워크스페이스에는 최소 1명의 ADMIN이 있어야 한다
- ADMIN과 생성자만 멤버를 초대/제거할 수 있다
- ADMIN과 생성자만 멤버의 역할을 변경할 수 있다
- 생성자만 워크스페이스를 삭제할 수 있다
- 동일한 사용자는 같은 워크스페이스에 중복 가입할 수 없다

#### 역할 정의
- **ADMIN**: 워크스페이스 설정 변경, 멤버 초대/제거, 역할 변경, 모든 보드 생성/수정/삭제
- **MEMBER**: 보드 생성/편집, 카드 관리, 댓글 작성

#### 생성자 관리
- 생성자 권한은 Workspace 테이블의 `createdBy` 필드로 관리
- 생성자는 모든 ADMIN 권한을 가지며 추가로 생성자 이전 및 워크스페이스 삭제 권한 보유

### 4. 보드 (Board)
프로젝트나 작업을 관리하는 단위를 나타냅니다.

#### 속성
- `id`: 보드 고유 식별자 (brd_ + ULID)
- `name`: 보드 이름
- `description`: 보드 설명
- `workspaceId`: 소속 워크스페이스 ID
- `createdBy`: 생성자 사용자 ID
- `visibility`: 공개 범위 (PRIVATE/WORKSPACE/PUBLIC)
- `createdAt`: 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 보드 이름은 1-100자 사이여야 한다
- 보드 설명은 최대 500자까지 입력 가능하다
- 보드는 반드시 하나의 워크스페이스에 속해야 한다
- 워크스페이스 ADMIN과 MEMBER 모두 보드를 생성할 수 있다
- 워크스페이스 ADMIN이 보드를 생성하면 해당 보드의 ADMIN 권한을 가진다
- 워크스페이스 MEMBER가 보드를 생성하면 해당 보드의 ADMIN 권한을 가진다
- **보드 생성 시 공개 범위를 설정할 수 있다 (기본값: PRIVATE)**
- **보드 생성 후에도 공개 범위를 변경할 수 있다 (ADMIN만 가능)**
- **PRIVATE 보드**: 보드에 직접 초대된 멤버만 접근 가능
- **WORKSPACE 공개 보드**: 해당 워크스페이스의 모든 멤버가 자동으로 MEMBER 권한으로 접근 가능
- **PUBLIC 보드**: 링크를 아는 사용자는 누구나 VIEWER 권한으로 접근 가능 (로그인 불필요)
- **워크스페이스 멤버는 해당 워크스페이스의 WORKSPACE 공개 보드에 자동으로 MEMBER 권한으로 접근 가능하다**
- **워크스페이스 멤버는 해당 워크스페이스의 PUBLIC 보드에 자동으로 VIEWER 권한으로 접근 가능하다**
- **워크스페이스 멤버라도 PRIVATE 보드에 접근하려면 별도의 보드별 초대가 필요하다**
- **공개 범위 변경 시 관련 멤버들에게 알림이 발송된다**

**워크스페이스 타입별 보드 공개 범위:**
- **개인 워크스페이스**: PRIVATE, PUBLIC (WORKSPACE 옵션 없음)
- **팀 워크스페이스**: PRIVATE, WORKSPACE, PUBLIC (모든 옵션 사용 가능)

### 5. 보드 멤버 (BoardMember)
사용자와 보드 간의 관계를 나타냅니다.

#### 속성
- `boardId`: 보드 ID (합성키 일부)
- `userId`: 사용자 ID (합성키 일부)
- `role`: 멤버 역할 (ADMIN/MEMBER/VIEWER)
- `status`: 멤버 상태 (ACTIVE)
- `addedBy`: 추가한 사용자 ID
- `addedAt`: 추가일시
- `isFavorite`: 즐겨찾기 여부
- `favoriteAt`: 즐겨찾기 등록일시
- `lastAccessAt`: 최근 접근일시
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 보드 생성자는 자동으로 ADMIN 권한을 받는다
- 워크스페이스 ADMIN은 모든 보드에 대해 ADMIN 권한을 가진다
- 워크스페이스 MEMBER가 보드를 생성하면 해당 보드의 ADMIN 권한을 가진다
- 보드에는 최소 1명의 ADMIN이 있어야 한다
- 보드 ADMIN만 멤버를 초대/제거할 수 있다
- 보드 ADMIN만 멤버의 역할을 변경할 수 있다
- 워크스페이스 멤버는 해당 워크스페이스의 WORKSPACE 공개 보드에 자동으로 MEMBER 권한으로 접근 가능하다
- 워크스페이스 멤버는 해당 워크스페이스의 PUBLIC 보드에 자동으로 VIEWER 권한으로 접근 가능하다
- 워크스페이스 멤버라도 PRIVATE 보드에 접근하려면 별도 초대가 필요하다
- 동일한 사용자는 같은 보드에 중복 가입할 수 없다
- 사용자는 자신이 접근 권한을 가진 보드만 즐겨찾기로 등록할 수 있다
- 사용자당 최대 50개의 보드를 즐겨찾기로 등록할 수 있다
- 보드가 삭제되면 해당 보드는 자동으로 모든 사용자의 즐겨찾기에서 제거된다
- 사용자가 보드에 대한 접근 권한을 상실하면 해당 보드는 자동으로 즐겨찾기에서 제거된다
- 즐겨찾기 목록은 최근 접근 시간 순으로 정렬된다

**워크스페이스 타입별 보드 공개 범위:**
- **개인 워크스페이스**: PRIVATE, PUBLIC (WORKSPACE 옵션 없음)
- **팀 워크스페이스**: PRIVATE, WORKSPACE, PUBLIC (모든 옵션 사용 가능)

### 6. 리스트 (List)
보드 내에서 카드를 분류하는 리스트를 나타냅니다.

#### 속성
- `id`: 리스트 고유 식별자 (lst_ + ULID)
- `name`: 리스트 이름
- `boardId`: 소속 보드 ID
- `position`: 리스트 순서
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시
- `updatedAt`: 수정일시
- `archivedAt`: 아카이브일시

#### 비즈니스 규칙
- 리스트 이름은 1-50자 사이여야 한다
- 보드 생성 시 기본 리스트가 자동 생성된다 (할 일, 진행중, 완료)
- 리스트 순서는 변경 가능하다
- 리스트는 소프트 삭제(아카이브)가 가능하다

### 7. 카드 (Card)
실제 작업 항목을 나타냅니다.

#### 속성
- `id`: 카드 고유 식별자 (crd_ + ULID)
- `title`: 카드 제목
- `description`: 카드 설명
- `boardId`: 소속 보드 ID
- `listId`: 소속 리스트 ID
- `position`: 카드 순서
- `startDate`: 시작일
- `dueDate`: 마감일
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시
- `updatedAt`: 수정일시
- `archivedAt`: 아카이브일시

#### 비즈니스 규칙
- 카드 제목은 1-200자 사이여야 한다
- 카드는 반드시 하나의 리스트에 속해야 한다
- 카드는 같은 보드 내의 다른 리스트로 이동 가능하다
- 카드 순서는 변경 가능하다
- 카드는 소프트 삭제(아카이브)가 가능하다

### 8. 카드 담당자 (CardMember)
카드와 담당자 간의 관계를 나타냅니다.

#### 속성
- `cardId`: 카드 ID (합성키 일부)
- `userId`: 담당자 사용자 ID (합성키 일부)
- `addedBy`: 할당한 사용자 ID
- `addedAt`: 할당일시
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 카드에는 여러 명의 담당자를 할당할 수 있다
- 담당자는 워크스페이스 멤버 중에서만 선택 가능하다
- 담당자 할당 시 해당 사용자에게 알림이 발송된다
- 동일한 사용자는 같은 카드에 중복 할당될 수 없다

### 9. 댓글 (Comment)
카드에 대한 의견이나 소통을 나타냅니다.

#### 속성
- `id`: 댓글 고유 식별자 (cmt_ + ULID)
- `body`: 댓글 내용
- `cardId`: 소속 카드 ID
- `authorId`: 작성자 ID
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 작성일시
- `updatedAt`: 수정일시
- `deletedAt`: 삭제일시

#### 비즈니스 규칙
- 댓글 내용은 1-1000자 사이여야 한다
- 댓글 작성자는 자신의 댓글을 수정/삭제할 수 있다
- 댓글 작성 시 카드 담당자와 보드 멤버에게 알림이 발송된다
- 댓글은 소프트 삭제가 가능하다

### 10. 라벨 (Label)
카드를 분류하기 위한 라벨을 나타냅니다.

#### 속성
- `id`: 라벨 고유 식별자 (lbl_ + ULID)
- `name`: 라벨 이름
- `color`: 라벨 색상
- `boardId`: 소속 보드 ID
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 라벨 이름은 1-20자 사이여야 한다
- 라벨은 보드별로 관리된다
- 카드에는 여러 개의 라벨을 적용할 수 있다
- 동일한 보드 내에서 라벨 이름은 고유해야 한다

### 11. 카드 라벨 (CardLabel)
카드와 라벨 간의 관계를 나타냅니다.

#### 속성
- `cardId`: 카드 ID (합성키 일부)
- `labelId`: 라벨 ID (합성키 일부)
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 동일한 카드에 같은 라벨을 중복 적용할 수 없다

### 12. 첨부파일 (Attachment)
카드에 첨부된 파일을 나타냅니다.

#### 속성
- `id`: 첨부파일 고유 식별자 (att_ + ULID)
- `cardId`: 소속 카드 ID
- `filename`: 파일명
- `mimeType`: MIME 타입
- `sizeBytes`: 파일 크기 (바이트)
- `storageUrl`: 저장소 URL
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 카드에는 여러 개의 첨부파일을 추가할 수 있다
- 첨부파일 크기는 설정에 따라 제한된다 (기본값: 10MB-50MB)
- 지원되는 파일 형식은 설정에 따라 제한된다

### 13. 활동 로그 (ActivityLog)
시스템 내 사용자 활동을 추적하는 로그를 나타냅니다.

#### 속성
- `id`: 활동 로그 고유 식별자 (act_ + ULID)
- `actorId`: 활동 수행자 ID
- `workspaceId`: 관련 워크스페이스 ID
- `boardId`: 관련 보드 ID
- `cardId`: 관련 카드 ID
- `type`: 활동 타입
- `payload`: 활동 상세 정보 (JSON)
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 생성일시

#### 비즈니스 규칙
- 모든 중요 활동은 자동으로 로그가 기록된다
- 활동 로그는 설정된 기간(기본값: 90일-180일) 동안 보관된다
- 활동 로그는 감사 및 분석 목적으로 사용된다

### 14. 초대 (Invite)
워크스페이스 또는 보드에 사용자를 초대하는 정보를 나타냅니다.

#### 속성
- `id`: 초대 고유 식별자 (inv_ + ULID)
- `scope`: 초대 범위 (WORKSPACE/BOARD)
- `workspaceId`: 초대할 워크스페이스 ID
- `boardId`: 초대할 보드 ID
- `email`: 초대받을 사용자 이메일
- `role`: 초대할 역할
- `token`: 초대 토큰 (고유값)
- `inviteUrl`: 초대 URL
- `expiresAt`: 초대 만료일시
- `invitedBy`: 초대한 사용자 ID
- `acceptedBy`: 수락한 사용자 ID
- `acceptedAt`: 수락일시
- `status`: 초대 상태 (PENDING/ACCEPTED/DECLINED/EXPIRED)
- `createdBy`: 생성자 사용자 ID
- `createdAt`: 초대 생성일시
- `updatedAt`: 수정일시

#### 비즈니스 규칙
- 워크스페이스 초대: role은 ADMIN 또는 MEMBER만 가능
- 보드 초대: role은 ADMIN, MEMBER, VIEWER만 가능
- 초대 만료 기간은 기본값 7일이며 설정 가능하다
- 초대 토큰은 고유해야 한다
- 이미 멤버인 사용자에게는 초대를 보낼 수 없다
- 초대 수락 시 해당 사용자가 자동으로 멤버가 된다

## 도메인 이벤트

### 1. 워크스페이스 관련 이벤트
- `WorkspaceCreated`: 워크스페이스 생성
- `WorkspaceUpdated`: 워크스페이스 정보 수정
- `WorkspaceDeleted`: 워크스페이스 삭제
- `WorkspaceCreatorTransferred`: 워크스페이스 생성자 이전
- `MemberInvited`: 멤버 초대
- `MemberJoined`: 멤버 가입
- `MemberRoleChanged`: 멤버 역할 변경
- `MemberRemoved`: 멤버 제거

### 2. 보드 관련 이벤트
- `BoardCreated`: 보드 생성
- `BoardUpdated`: 보드 정보 수정
- `BoardDeleted`: 보드 삭제
- `BoardFavorited`: 보드 즐겨찾기 등록
- `BoardUnfavorited`: 보드 즐겨찾기 해제
- `BoardVisibilityChanged`: 보드 공개 범위 변경
- `ListCreated`: 리스트 생성
- `ListUpdated`: 리스트 정보 수정
- `ListDeleted`: 리스트 삭제
- `ListArchived`: 리스트 아카이브

### 3. 초대 관련 이벤트
- `WorkspaceInvitationSent`: 워크스페이스 초대 발송
- `WorkspaceInvitationAccepted`: 워크스페이스 초대 수락
- `WorkspaceInvitationExpired`: 워크스페이스 초대 만료
- `BoardInvitationSent`: 보드 초대 발송
- `BoardInvitationAccepted`: 보드 초대 수락
- `BoardInvitationExpired`: 보드 초대 만료
- `BoardMemberJoined`: 보드 멤버 가입
- `BoardMemberRemoved`: 보드 멤버 제거
- `BoardMemberRoleChanged`: 보드 멤버 역할 변경

### 4. 카드 관련 이벤트
- `CardCreated`: 카드 생성
- `CardUpdated`: 카드 정보 수정
- `CardMoved`: 카드 이동
- `CardDeleted`: 카드 삭제
- `CardArchived`: 카드 아카이브
- `CardAssigned`: 카드 담당자 할당
- `CardUnassigned`: 카드 담당자 해제
- `CommentAdded`: 댓글 추가
- `CommentUpdated`: 댓글 수정
- `CommentDeleted`: 댓글 삭제
- `AttachmentAdded`: 첨부파일 추가
- `AttachmentRemoved`: 첨부파일 제거
- `LabelApplied`: 라벨 적용
- `LabelRemoved`: 라벨 제거

## 도메인 서비스

### 1. 권한 검증 서비스 (PermissionService)
- 사용자의 워크스페이스 접근 권한 검증
- 사용자의 보드 수정 권한 검증
- 사용자의 카드 관리 권한 검증
- 공개 보드 접근 권한 검증
- 보드 공개 범위 변경 권한 검증

### 2. 초대 관리 서비스 (InvitationService)
- 워크스페이스 초대 생성 및 관리
- 보드 초대 생성 및 관리
- 초대 토큰 검증
- 초대 만료 처리
- 초대 상태 관리

### 3. 공개 보드 서비스 (PublicBoardService)
- 공개 보드 URL 생성 및 관리
- 공개 보드 접근 권한 검증
- 공개 보드 접근 로그 관리
- 공개 URL 재생성 및 무효화 처리

### 4. 알림 서비스 (NotificationService)
- 멤버 초대 알림
- 카드 담당자 할당 알림
- 댓글 작성 알림
- 마감일 알림
- 초대 수락/거절 알림
- 권한 변경 알림
- 공개 범위 변경 알림
- 워크스페이스 설정 변경 알림

### 5. 실시간 협업 서비스 (CollaborationService)
- 실시간 업데이트 브로드캐스팅
- 동시 편집 충돌 방지
- 사용자 활동 추적

### 6. 활동 로그 서비스 (ActivityLogService)
- 사용자 활동 자동 기록
- 활동 로그 조회 및 분석
- 활동 로그 아카이빙 및 정리

### 7. 파일 관리 서비스 (FileManagementService)
- 첨부파일 업로드 및 저장
- 파일 형식 검증
- 파일 크기 제한 관리
- 파일 보안 검증

## 바운디드 컨텍스트

### 1. 워크스페이스 관리 컨텍스트
- 워크스페이스 생성/수정/삭제
- 멤버 관리
- 권한 관리

### 2. 보드 관리 컨텍스트
- 보드 생성/수정/삭제
- 리스트 관리
- 보드 설정
- 보드 즐겨찾기 관리
- 보드 공개 범위 설정

### 3. 초대 관리 컨텍스트
- 워크스페이스 초대 생성/관리
- 보드 초대 생성/관리
- 초대 토큰 검증
- 초대 상태 관리
- 초대 만료 처리

### 4. 카드 관리 컨텍스트
- 카드 생성/수정/삭제
- 카드 이동
- 담당자 할당
- 라벨 관리
- 첨부파일 관리

### 5. 협업 컨텍스트
- 댓글 관리
- 실시간 협업
- 알림 관리

### 6. 사용자 관리 컨텍스트
- 사용자 등록/수정
- 인증/인가
- 프로필 관리

### 7. 활동 추적 컨텍스트
- 활동 로그 기록
- 활동 분석
- 감사 추적