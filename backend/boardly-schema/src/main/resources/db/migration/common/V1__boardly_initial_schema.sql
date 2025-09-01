-- 1) users
CREATE TABLE users (
  id             VARCHAR PRIMARY KEY,
  email          VARCHAR NOT NULL UNIQUE,
  password_hash  VARCHAR NOT NULL,
  display_name   VARCHAR,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2) workspaces
CREATE TABLE workspaces (
  id             VARCHAR PRIMARY KEY,
  name           VARCHAR NOT NULL,
  description    VARCHAR,
  type           VARCHAR NOT NULL,  -- PERSONAL | TEAM
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3) boards (last_access_at 제거됨)
CREATE TABLE boards (
  id             VARCHAR PRIMARY KEY,
  workspace_id   VARCHAR NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
  name           VARCHAR NOT NULL,
  description    VARCHAR,
  visibility     VARCHAR NOT NULL,  -- PRIVATE | WORKSPACE | PUBLIC
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_boards_workspace ON boards (workspace_id);
CREATE INDEX idx_boards_visibility ON boards (visibility);

-- 4) lists  (※ 'columns'로 바꾸고 싶으면 테이블명만 교체하면 됨)
CREATE TABLE lists (
  id             VARCHAR PRIMARY KEY,
  board_id       VARCHAR NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
  name           VARCHAR NOT NULL,
  position       BIGINT NOT NULL,   -- 정렬용
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  archived_at    TIMESTAMP NULL
);
CREATE INDEX idx_lists_board_position ON lists (board_id, position);

-- 5) cards
CREATE TABLE cards (
  id             VARCHAR PRIMARY KEY,
  board_id       VARCHAR NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
  list_id        VARCHAR NOT NULL REFERENCES lists(id)  ON DELETE CASCADE,
  title          VARCHAR NOT NULL,
  description    TEXT,
  position       BIGINT NOT NULL,   -- 리스트 내 정렬
  start_date     TIMESTAMP NULL,
  due_date       TIMESTAMP NULL,
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  archived_at    TIMESTAMP NULL
);
CREATE INDEX idx_cards_list_position ON cards (list_id, position);
CREATE INDEX idx_cards_board        ON cards (board_id);
CREATE INDEX idx_cards_due_date     ON cards (due_date);

-- 6) workspace_members (합성 PK)
CREATE TABLE workspace_members (
  workspace_id   VARCHAR NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
  user_id        VARCHAR NOT NULL REFERENCES users(id)      ON DELETE CASCADE,
  role           VARCHAR NOT NULL,      --  ADMIN | MEMBER (필요시 축소/확장)
  invited_by     VARCHAR REFERENCES users(id),
  invited_at     TIMESTAMP,
  joined_at      TIMESTAMP,
  status         VARCHAR NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | INVITED | REMOVED
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (workspace_id, user_id)
);
CREATE INDEX idx_workspace_members_user ON workspace_members (user_id);
CREATE INDEX idx_workspace_members_role ON workspace_members (workspace_id, role);

-- 7) board_members (합성 PK, role = ADMIN | MEMBER | VIEWER)
CREATE TABLE board_members (
  board_id       VARCHAR NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
  user_id        VARCHAR NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
  role           VARCHAR NOT NULL CHECK (role IN ('ADMIN','MEMBER','VIEWER')),
  status         VARCHAR NOT NULL DEFAULT 'ACTIVE',
  added_by       VARCHAR REFERENCES users(id),
  added_at       TIMESTAMP,
  is_favorite    BOOLEAN NOT NULL DEFAULT FALSE,
  favorite_at    TIMESTAMP,
  last_access_at TIMESTAMP,   -- 해당 user가 해당 board에 마지막 접속한 시각
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (board_id, user_id)
);
CREATE INDEX idx_board_members_user_sort ON board_members (user_id, is_favorite DESC, last_access_at DESC);
CREATE INDEX idx_board_members_board     ON board_members (board_id);

-- 8) labels
CREATE TABLE labels (
  id             VARCHAR PRIMARY KEY,
  board_id       VARCHAR NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
  name           VARCHAR NOT NULL,
  color          VARCHAR,
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (board_id, name)
);
CREATE INDEX idx_labels_board ON labels (board_id);

-- 9) card_labels (합성 PK)
CREATE TABLE card_labels (
  card_id        VARCHAR NOT NULL REFERENCES cards(id)  ON DELETE CASCADE,
  label_id       VARCHAR NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (card_id, label_id)
);

-- 10) card_members (합성 PK)
CREATE TABLE card_members (
  card_id        VARCHAR NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
  user_id        VARCHAR NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  added_by       VARCHAR REFERENCES users(id),
  added_at       TIMESTAMP,
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (card_id, user_id)
);
CREATE INDEX idx_card_members_user ON card_members (user_id);

-- 11) comments
CREATE TABLE comments (
  id             VARCHAR PRIMARY KEY,
  card_id        VARCHAR NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
  author_id      VARCHAR NOT NULL REFERENCES users(id),
  body           TEXT NOT NULL,
  created_by     VARCHAR NOT NULL REFERENCES users(id),  -- 보통 author_id와 동일
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at     TIMESTAMP
);
CREATE INDEX idx_comments_card ON comments (card_id);
CREATE INDEX idx_comments_author ON comments (author_id);

-- 12) attachments
CREATE TABLE attachments (
  id             VARCHAR PRIMARY KEY,
  card_id        VARCHAR NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
  filename       VARCHAR NOT NULL,
  mime_type      VARCHAR,
  size_bytes     BIGINT,
  storage_url    VARCHAR NOT NULL,
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_attachments_card ON attachments (card_id);

-- 13) activity_logs
CREATE TABLE activity_logs (
  id             VARCHAR PRIMARY KEY,
  actor_id       VARCHAR NOT NULL REFERENCES users(id),
  workspace_id   VARCHAR REFERENCES workspaces(id) ON DELETE CASCADE,
  board_id       VARCHAR REFERENCES boards(id)     ON DELETE CASCADE,
  card_id        VARCHAR REFERENCES cards(id)      ON DELETE CASCADE,
  type           VARCHAR NOT NULL,   -- e.g., CARD_MOVED, LIST_CREATED ...
  payload        JSON,
  created_by     VARCHAR NOT NULL REFERENCES users(id),  -- 보통 actor_id와 동일
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_activity_logs_board_time ON activity_logs (board_id, created_at);
CREATE INDEX idx_activity_logs_card_time  ON activity_logs (card_id, created_at);
CREATE INDEX idx_activity_logs_actor_time ON activity_logs (actor_id, created_at);

-- 14) invites  (scope별 role 제한, 7일 만료, invite_url 포함)
CREATE TABLE invites (
  id             VARCHAR PRIMARY KEY,
  scope          VARCHAR NOT NULL,                             -- WORKSPACE | BOARD
  workspace_id   VARCHAR REFERENCES workspaces(id) ON DELETE CASCADE,
  board_id       VARCHAR REFERENCES boards(id)     ON DELETE CASCADE,

  email          VARCHAR NOT NULL,
  role           VARCHAR NOT NULL,                             -- scope별 허용값 제한(아래 CHECK)
  token          VARCHAR NOT NULL UNIQUE,                      -- 서명 토큰(JWT 등) *해시 저장 권장 시 token_hash로 교체
  invite_url     VARCHAR NOT NULL,

  expires_at     TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '7 days'),
  invited_by     VARCHAR NOT NULL REFERENCES users(id),
  accepted_by    VARCHAR REFERENCES users(id),
  accepted_at    TIMESTAMP,
  status         VARCHAR NOT NULL CHECK (status IN ('PENDING','ACCEPTED','DECLINED','EXPIRED')),
  created_by     VARCHAR NOT NULL REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  -- scope-target 일관성
  CHECK (
    (scope = 'WORKSPACE' AND workspace_id IS NOT NULL AND board_id IS NULL) OR
    (scope = 'BOARD'     AND board_id     IS NOT NULL AND workspace_id IS NULL)
  ),

  -- scope별 role 허용
  CHECK (
    (scope = 'WORKSPACE' AND role IN ('ADMIN','MEMBER')) OR
    (scope = 'BOARD'     AND role IN ('ADMIN','MEMBER','VIEWER'))
  )
);
CREATE INDEX idx_invites_workspace ON invites (scope, workspace_id);
CREATE INDEX idx_invites_board     ON invites (scope, board_id);
CREATE INDEX idx_invites_status    ON invites (status);
CREATE INDEX idx_invites_expires   ON invites (expires_at);