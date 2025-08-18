# 데이터 모델 및 마이그레이션

관계형 스키마와 마이그레이션 정책을 요약합니다.

## 멀티테넌시 전략
- 테넌트 경계는 `workspace_id` 외래키로 구현(Board, List, Card, Label, Activity, Invitation 등).
- 테넌트 로컬 고유성은 `workspace_id`를 포함한 복합 Unique 제약으로 보장.
- 모든 조회는 `workspace_id` 기준으로 필터링.

## 권한 관리 전략
- **워크스페이스 권한 우선**: 워크스페이스 OWNER > MEMBER > BOARD_ONLY 순서로 권한 우선순위 적용
- **역할 기반 접근 제어(RBAC)**: 워크스페이스 역할(OWNER, MEMBER, BOARD_ONLY)과 보드 역할(OWNER, EDITOR, VIEWER) 분리
- **보드 공개/비공개 설정**: 공개 보드는 워크스페이스 멤버에게 자동 Editor 권한, 비공개 보드는 개별 초대 필요
- **초대 상태 관리**: PENDING, ACCEPTED, DECLINED, EXPIRED 상태 추적

## 주요 테이블

### 핵심 엔티티
- **workspaces**(id, name, description, type, owner_user_id, created_at, updated_at)
- **boards**(id, workspace_id, title, description, is_public, is_archived, created_at, updated_at)
- **lists**(id, workspace_id, board_id, title, position, color, created_at, updated_at)
- **cards**(id, workspace_id, list_id, title, description, position, due_date, is_completed, created_at, updated_at)

### 권한 관리
- **workspace_members**(workspace_id, user_id, role, invite_status, invited_by, invited_at, joined_at)
- **board_members**(board_id, user_id, role, invite_status, invited_by, invited_at, joined_at)

### 콘텐츠 관리
- **labels**(id, workspace_id, name, color, created_at)
- **card_labels**(workspace_id, card_id, label_id)
- **comments**(id, workspace_id, card_id, user_id, content, created_at, updated_at)
- **activities**(id, workspace_id, board_id, user_id, type, payload_json, created_at)

### 초대 관리
- **workspace_invitations**(id, workspace_id, email, role, status, invited_by, invited_at, expires_at, accepted_at)
- **board_invitations**(id, board_id, email, role, status, invited_by, invited_at, expires_at, accepted_at)

## 권한 매트릭스 구현

### 워크스페이스 권한
```sql
-- 워크스페이스 역할 enum
CREATE TYPE workspace_role AS ENUM ('OWNER', 'MEMBER');

-- 워크스페이스 멤버 타입 enum
CREATE TYPE member_type AS ENUM ('MEMBER', 'BOARD_ONLY');

-- 워크스페이스 멤버 테이블
CREATE TABLE workspace_members (
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role workspace_role NOT NULL DEFAULT 'MEMBER',
    type member_type NOT NULL DEFAULT 'MEMBER',
    invited_by UUID REFERENCES users(id),
    invited_at TIMESTAMP,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (workspace_id, user_id)
);
```

### 보드 권한
```sql
-- 보드 역할 enum
CREATE TYPE board_role AS ENUM ('OWNER', 'EDITOR', 'VIEWER');

-- 초대 타입 enum
CREATE TYPE invitation_type AS ENUM ('WORKSPACE_AUTO', 'INDIVIDUAL');

-- 보드 멤버 테이블
CREATE TABLE board_members (
    board_id UUID REFERENCES boards(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role board_role NOT NULL DEFAULT 'VIEWER',
    invited_by UUID REFERENCES users(id),
    invited_at TIMESTAMP,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    invitation_type invitation_type NOT NULL DEFAULT 'INDIVIDUAL',
    PRIMARY KEY (board_id, user_id)
);
```

### 초대 상태 관리
```sql
-- 초대 상태 enum
CREATE TYPE invite_status_type AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED');

-- 초대 테이블 (워크스페이스 및 보드 통합)
CREATE TABLE invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL CHECK (type IN ('WORKSPACE', 'BOARD')),
    target_id UUID NOT NULL,
    email VARCHAR(255),
    invite_code VARCHAR(255) UNIQUE,
    role VARCHAR(50) NOT NULL,
    status invite_status_type NOT NULL DEFAULT 'PENDING',
    invited_by UUID REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL DEFAULT (NOW() + INTERVAL '7 days'),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    accepted_at TIMESTAMP,
    CHECK ((email IS NOT NULL AND invite_code IS NULL) OR (email IS NULL AND invite_code IS NOT NULL))
);
```

### 핵심 엔티티 테이블
```sql
-- 워크스페이스 테이블
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL CHECK (type IN ('PERSONAL', 'TEAM')),
    owner_user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(owner_user_id, name)
);

-- 보드 테이블
CREATE TABLE boards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_starred BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 리스트 테이블
CREATE TABLE lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    board_id UUID REFERENCES boards(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, board_id, position)
);

-- 카드 테이블
CREATE TABLE cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    list_id UUID REFERENCES lists(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    position INTEGER NOT NULL,
    due_date DATE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, list_id, position)
);

-- 라벨 테이블
CREATE TABLE labels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, name)
);

-- 카드-라벨 관계 테이블
CREATE TABLE card_labels (
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    card_id UUID REFERENCES cards(id) ON DELETE CASCADE,
    label_id UUID REFERENCES labels(id) ON DELETE CASCADE,
    PRIMARY KEY (workspace_id, card_id, label_id)
);

-- 댓글 테이블
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    card_id UUID REFERENCES cards(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 활동 로그 테이블
CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    board_id UUID REFERENCES boards(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    payload_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

## Flyway 마이그레이션
- 공통: `/backend/boardly-infrastructure/src/main/resources/db/migration/common` (예: `V2__workspace_multi_tenant.sql`, `V3__migrate_workspace_initial_data.sql`)
- 환경별 시드: `/db/migration/{local,dev}/R__insert_*_dummy_data.sql`
- 정책
  - 모든 스키마 변경은 버전 스크립트 `V__`로.
  - 데이터 보정은 반복 스크립트 `R__`로, 멱등 가드 포함.
  - 적용된 마이그레이션은 수정하지 않고 새 `V__`를 추가.

## ERD(Mermaid)
```mermaid
erDiagram
  WORKSPACES ||--o{ WORKSPACE_MEMBERS : has
  WORKSPACES ||--o{ INVITATIONS : sends
  WORKSPACES ||--o{ BOARDS : contains
  WORKSPACES ||--o{ LABELS : has
  WORKSPACES ||--o{ ACTIVITIES : logs
  
  BOARDS ||--o{ BOARD_MEMBERS : has
  BOARDS ||--o{ INVITATIONS : sends
  BOARDS ||--o{ LISTS : contains
  BOARDS ||--o{ ACTIVITIES : logs
  
  LISTS ||--o{ CARDS : contains
  CARDS ||--o{ CARD_LABELS : tagged
  CARDS ||--o{ COMMENTS : has
  
  USERS ||--o{ WORKSPACE_MEMBERS : belongs_to
  USERS ||--o{ BOARD_MEMBERS : belongs_to
  USERS ||--o{ COMMENTS : writes
  USERS ||--o{ ACTIVITIES : performs

  WORKSPACES {
    uuid id PK
    text name
    text description
    text type
    uuid owner_user_id FK
    timestamp created_at
    timestamp updated_at
  }
  
  WORKSPACE_MEMBERS {
    uuid workspace_id FK
    uuid user_id FK
    workspace_role role
    member_type type
    uuid invited_by FK
    timestamp invited_at
    timestamp joined_at
  }
  
  INVITATIONS {
    uuid id PK
    text type
    uuid target_id FK
    text email
    text invite_code
    text role
    invite_status_type status
    uuid invited_by FK
    timestamp expires_at
    timestamp created_at
    timestamp accepted_at
  }
  
  BOARDS {
    uuid id PK
    uuid workspace_id FK
    text title
    text description
    bool is_public
    bool is_starred
    timestamp created_at
    timestamp updated_at
  }
  
  BOARD_MEMBERS {
    uuid board_id FK
    uuid user_id FK
    board_role role
    uuid invited_by FK
    timestamp invited_at
    timestamp joined_at
    invitation_type invitation_type
  }
  
  LISTS {
    uuid id PK
    uuid workspace_id FK
    uuid board_id FK
    text title
    int position
    text color
    timestamp created_at
    timestamp updated_at
  }
  
  CARDS {
    uuid id PK
    uuid workspace_id FK
    uuid list_id FK
    text title
    text description
    int position
    date due_date
    bool is_completed
    timestamp created_at
    timestamp updated_at
  }
  
  LABELS {
    uuid id PK
    uuid workspace_id FK
    text name
    text color
    timestamp created_at
  }
  
  CARD_LABELS {
    uuid workspace_id FK
    uuid card_id FK
    uuid label_id FK
  }
  
  COMMENTS {
    uuid id PK
    uuid workspace_id FK
    uuid card_id FK
    uuid user_id FK
    text content
    timestamp created_at
    timestamp updated_at
  }
  
  ACTIVITIES {
    uuid id PK
    uuid workspace_id FK
    uuid board_id FK
    uuid user_id FK
    text type
    text payload_json
    timestamp created_at
  }
  
  USERS {
    uuid id PK
    text email
    text first_name
    text last_name
    text password_hash
    bool is_active
    timestamp created_at
    timestamp updated_at
  }
```

## 인덱스 최적화

### 권한 검증 최적화
```sql
-- 워크스페이스 멤버십 조회 최적화
CREATE INDEX idx_workspace_members_user_workspace ON workspace_members(user_id, workspace_id);
CREATE INDEX idx_workspace_members_status ON workspace_members(invite_status);

-- 보드 멤버십 조회 최적화
CREATE INDEX idx_board_members_user_board ON board_members(user_id, board_id);
CREATE INDEX idx_board_members_status ON board_members(invite_status);

-- 초대 조회 최적화
CREATE INDEX idx_invitations_email_status ON invitations(email, status);
CREATE INDEX idx_invitations_invite_code ON invitations(invite_code);
CREATE INDEX idx_invitations_expires_at ON invitations(expires_at);
CREATE INDEX idx_invitations_type_target ON invitations(type, target_id);
```

### 콘텐츠 조회 최적화
```sql
-- 리스트 조회 최적화
CREATE INDEX idx_lists_workspace_board ON lists(workspace_id, board_id);
CREATE INDEX idx_lists_position ON lists(workspace_id, board_id, position);

-- 카드 조회 최적화
CREATE INDEX idx_cards_workspace_list ON cards(workspace_id, list_id);
CREATE INDEX idx_cards_position ON cards(workspace_id, list_id, position);
CREATE INDEX idx_cards_due_date ON cards(workspace_id, due_date);
CREATE INDEX idx_cards_completed ON cards(workspace_id, is_completed);

-- 라벨 조회 최적화
CREATE INDEX idx_labels_workspace ON labels(workspace_id);

-- 카드-라벨 관계 조회 최적화
CREATE INDEX idx_card_labels_workspace_card ON card_labels(workspace_id, card_id);
CREATE INDEX idx_card_labels_workspace_label ON card_labels(workspace_id, label_id);

-- 댓글 조회 최적화
CREATE INDEX idx_comments_workspace_card ON comments(workspace_id, card_id);
CREATE INDEX idx_comments_card_created ON comments(workspace_id, card_id, created_at);

-- 활동 조회 최적화
CREATE INDEX idx_activities_workspace_board ON activities(workspace_id, board_id);
CREATE INDEX idx_activities_user_created ON activities(workspace_id, user_id, created_at);
```
