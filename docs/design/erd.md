# ERD — Boardly

Boardly 데이터베이스 스키마를 기반으로 생성한 ERD입니다. 원본: `boardly-schema.md`.

```mermaid
erDiagram
  USERS {
    VARCHAR(32) id
    VARCHAR(255) email
    VARCHAR(255) password_hash
    VARCHAR(120) display_name
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  WORKSPACES {
    VARCHAR(32) id
    VARCHAR(120) name
    VARCHAR(1000) description
    VARCHAR(16) type
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  BOARDS {
    VARCHAR(32) id
    VARCHAR(32) workspace_id
    VARCHAR(140) name
    VARCHAR(1000) description
    VARCHAR(16) visibility
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  LISTS {
    VARCHAR(32) id
    VARCHAR(32) board_id
    VARCHAR(140) name
    BIGINT position
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  CARDS {
    VARCHAR(32) id
    VARCHAR(32) board_id
    VARCHAR(32) list_id
    VARCHAR(200) title
    TEXT description
    BIGINT position
    TIMESTAMPTZ start_date
    TIMESTAMPTZ due_date
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  WORKSPACE_MEMBERS {
    VARCHAR(32) workspace_id
    VARCHAR(32) user_id
    VARCHAR(16) role
    VARCHAR(32) invited_by
    TIMESTAMPTZ invited_at
    TIMESTAMPTZ joined_at
    VARCHAR(16) status
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  BOARD_MEMBERS {
    VARCHAR(32) board_id
    VARCHAR(32) user_id
    VARCHAR(16) role
    VARCHAR(32) added_by
    TIMESTAMPTZ added_at
    VARCHAR(16) status
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  LABELS {
    VARCHAR(32) id
    VARCHAR(32) board_id
    VARCHAR(120) name
    VARCHAR(16) color
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  CARD_LABELS {
    VARCHAR(32) card_id
    VARCHAR(32) label_id
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  CARD_MEMBERS {
    VARCHAR(32) card_id
    VARCHAR(32) user_id
    VARCHAR(32) added_by
    TIMESTAMPTZ added_at
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  COMMENTS {
    VARCHAR(32) id
    VARCHAR(32) card_id
    VARCHAR(32) author_id
    TEXT content
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
    TIMESTAMPTZ deleted_at
    VARCHAR(32) created_by
  }
  ATTACHMENTS {
    VARCHAR(32) id
    VARCHAR(32) card_id
    VARCHAR(255) filename
    VARCHAR(255) mime_type
    BIGINT size_bytes
    VARCHAR(1024) storage_url
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }
  ACTIVITY_LOGS {
    VARCHAR(32) id
    VARCHAR(32) actor_id
    VARCHAR(32) workspace_id
    VARCHAR(32) board_id
    VARCHAR(32) card_id
    VARCHAR(64) type
    JSONB payload
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
  }
  INVITES {
    VARCHAR(32) id
    VARCHAR(8)  scope
    VARCHAR(32) workspace_id
    VARCHAR(32) board_id
    VARCHAR(255) email
    VARCHAR(16) role
    VARCHAR(128) token_hash
    VARCHAR(1024) invite_url
    TIMESTAMPTZ expires_at
    VARCHAR(32) invited_by
    VARCHAR(32) accepted_by
    TIMESTAMPTZ accepted_at
    VARCHAR(16) status
    VARCHAR(32) created_by
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }

  USERS ||--o{ WORKSPACES : "created_by"
  WORKSPACES ||--o{ BOARDS : "workspace_id"
  USERS ||--o{ BOARDS : "created_by"
  BOARDS ||--o{ LISTS : "board_id"
  USERS ||--o{ LISTS : "created_by"
  BOARDS ||--o{ CARDS : "board_id"
  LISTS ||--o{ CARDS : "list_id"
  USERS ||--o{ CARDS : "created_by"
  WORKSPACES ||--o{ WORKSPACE_MEMBERS : "workspace_id"
  USERS ||--o{ WORKSPACE_MEMBERS : "user_id"
  USERS ||--o{ WORKSPACE_MEMBERS : "invited_by"
  USERS ||--o{ WORKSPACE_MEMBERS : "created_by"
  BOARDS ||--o{ BOARD_MEMBERS : "board_id"
  USERS ||--o{ BOARD_MEMBERS : "user_id"
  USERS ||--o{ BOARD_MEMBERS : "added_by"
  USERS ||--o{ BOARD_MEMBERS : "created_by"
  BOARDS ||--o{ LABELS : "board_id"
  USERS ||--o{ LABELS : "created_by"
  CARDS ||--o{ CARD_LABELS : "card_id"
  LABELS ||--o{ CARD_LABELS : "label_id"
  USERS ||--o{ CARD_LABELS : "created_by"
  CARDS ||--o{ CARD_MEMBERS : "card_id"
  USERS ||--o{ CARD_MEMBERS : "user_id"
  USERS ||--o{ CARD_MEMBERS : "added_by"
  USERS ||--o{ CARD_MEMBERS : "created_by"
  CARDS ||--o{ COMMENTS : "card_id"
  USERS ||--o{ COMMENTS : "author_id"
  USERS ||--o{ COMMENTS : "created_by"
  CARDS ||--o{ ATTACHMENTS : "card_id"
  USERS ||--o{ ATTACHMENTS : "created_by"
  USERS ||--o{ ACTIVITY_LOGS : "actor_id"
  WORKSPACES ||--o{ ACTIVITY_LOGS : "workspace_id"
  BOARDS ||--o{ ACTIVITY_LOGS : "board_id"
  CARDS ||--o{ ACTIVITY_LOGS : "card_id"
  USERS ||--o{ ACTIVITY_LOGS : "created_by"
  WORKSPACES ||--o{ INVITES : "workspace_id"
  BOARDS ||--o{ INVITES : "board_id"
  USERS ||--o{ INVITES : "invited_by"
  USERS ||--o{ INVITES : "accepted_by"
  USERS ||--o{ INVITES : "created_by"
```

## 테이블 설명

### users

* ULID prefix usr\_
* email unique (citext 또는 LOWER 함수 인덱스)
* created\_at/updated\_at TIMESTAMPTZ

### workspaces

* type: PERSONAL|TEAM
* unique (created\_by, name)

### boards

* visibility: PRIVATE|WORKSPACE|PUBLIC
* unique (workspace\_id, name)

### lists

* board 내 정렬용 position 컬럼 보유

### cards

* position unique per list (list\_id, position)
* due\_date 인덱스 권장

### workspace\_members

* role: ADMIN|MEMBER
* status: ACTIVE|INVITED|REMOVED
* PK(workspace\_id, user\_id)

### board\_members

* role: ADMIN|MEMBER|VIEWER
* PK(board\_id, user\_id)

### labels

* unique (board\_id, name)

### card\_labels

* PK(card\_id, label\_id)

### card\_members

* PK(card\_id, user\_id)

### comments

* 소프트 삭제(deleted\_at) 지원, 최신순 인덱스 예시 제공

### attachments

* size\_bytes >= 0 제약

### activity\_logs

* payload JSONB + GIN 인덱스 권장

### invites

* scope: WORKSPACE|BOARD (scope-target 일관성 체크)
* role 허용값은 scope별로 상이
* status: PENDING|ACCEPTED|DECLINED|EXPIRED
* PENDING 초대 partial unique 인덱스 예시

## 참고/제약 요약

* 모든 PK는 ULID prefix가 포함된 VARCHAR(32) (예: usr\_, wsp\_, brd\_, crd\_ 등).
* 시간 컬럼은 TIMESTAMPTZ 표준 사용 (PostgreSQL 기준).
* 선택적 PostgreSQL 최적화: citext, pg\_trgm, JSONB + GIN, partial unique indexes.
