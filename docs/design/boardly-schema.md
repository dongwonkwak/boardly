```sql
-- =============================================================
-- Boardly DB Schema — V1 Baseline (Revised)
-- Key changes: TIMESTAMPTZ, ULID-prefix checks, uniqueness, role/status checks,
--              position uniqueness, visibility checks, safer FKs, misc constraints
-- Target: PostgreSQL (primary) + H2 (test, PostgreSQL mode 권장)
-- =============================================================

-- 참고: H2 테스트에서는 jdbc URL에 MODE=PostgreSQL 권장
-- ex) jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE

-- ========== USERS ==========
CREATE TABLE users (
  id             VARCHAR(32) PRIMARY KEY,
  email          VARCHAR(255) NOT NULL UNIQUE,
  username       VARCHAR(100) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  display_name   VARCHAR(120),
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT users_id_chk CHECK (id ~ '^usr_[0-9A-HJKMNP-TV-Z]{26}$')
);

-- (Optional, PostgreSQL) 대소문자 무시 유니크를 원하면 citext/함수 인덱스 사용
-- CREATE EXTENSION IF NOT EXISTS citext;
-- ALTER TABLE users ALTER COLUMN email TYPE citext;
-- ALTER TABLE users ALTER COLUMN username TYPE citext;
-- 또는
-- CREATE UNIQUE INDEX users_email_lower_uk ON users (LOWER(email));
-- CREATE UNIQUE INDEX users_username_lower_uk ON users (LOWER(username));


-- ========== WORKSPACES ==========
CREATE TABLE workspaces (
  id             VARCHAR(32) PRIMARY KEY,
  name           VARCHAR(120) NOT NULL,
  description    VARCHAR(1000),
  type           VARCHAR(16) NOT NULL,  -- PERSONAL | TEAM
  created_by     VARCHAR(32)  NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT workspaces_id_chk   CHECK (id   ~ '^wsp_[0-9A-HJKMNP-TV-Z]{26}$'),
  CONSTRAINT workspaces_type_chk CHECK (type IN ('PERSONAL','TEAM')),
  CONSTRAINT workspaces_name_owner_uk UNIQUE (created_by, name)
);


-- ========== BOARDS ==========
CREATE TABLE boards (
  id             VARCHAR(32) PRIMARY KEY,
  workspace_id   VARCHAR(32) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
  name           VARCHAR(140) NOT NULL,
  description    VARCHAR(1000),
  visibility     VARCHAR(16) NOT NULL,  -- PRIVATE | WORKSPACE | PUBLIC
  created_by     VARCHAR(32)  NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT boards_id_chk        CHECK (id ~ '^brd_[0-9A-HJKMNP-TV-Z]{26}$'),
  CONSTRAINT boards_visibility_chk CHECK (visibility IN ('PRIVATE','WORKSPACE','PUBLIC')),
  CONSTRAINT boards_workspace_name_uk UNIQUE (workspace_id, name)
);
CREATE INDEX idx_boards_workspace  ON boards (workspace_id);
CREATE INDEX idx_boards_visibility ON boards (visibility);


-- ========== LISTS ==========
CREATE TABLE lists (
  id             VARCHAR(32) PRIMARY KEY,
  board_id       VARCHAR(32) NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
  name           VARCHAR(140) NOT NULL,
  position       BIGINT NOT NULL,   -- 보드 내 정렬
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  archived_at    TIMESTAMPTZ NULL,
  CONSTRAINT lists_id_chk CHECK (id ~ '^lst_[0-9A-HJKMNP-TV-Z]{26}$'),
  CONSTRAINT lists_pos_uk UNIQUE (board_id, position)
);
CREATE INDEX idx_lists_board_position ON lists (board_id, position);


-- ========== CARDS ==========
CREATE TABLE cards (
  id             VARCHAR(32) PRIMARY KEY,
  board_id       VARCHAR(32) NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
  list_id        VARCHAR(32) NOT NULL REFERENCES lists(id)  ON DELETE CASCADE,
  title          VARCHAR(200) NOT NULL,
  description    TEXT,
  position       BIGINT NOT NULL,   -- 리스트 내 정렬
  start_date     TIMESTAMPTZ NULL,
  due_date       TIMESTAMPTZ NULL,
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  archived_at    TIMESTAMPTZ NULL,
  CONSTRAINT cards_id_chk CHECK (id ~ '^crd_[0-9A-HJKMNP-TV-Z]{26}$'),
  CONSTRAINT cards_pos_uk UNIQUE (list_id, position)
);
CREATE INDEX idx_cards_list_position ON cards (list_id, position);
CREATE INDEX idx_cards_board        ON cards (board_id);
CREATE INDEX idx_cards_due_date     ON cards (due_date);


-- ========== WORKSPACE MEMBERS ==========
CREATE TABLE workspace_members (
  workspace_id   VARCHAR(32) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
  user_id        VARCHAR(32) NOT NULL REFERENCES users(id)      ON DELETE CASCADE,
  role           VARCHAR(16) NOT NULL,      -- ADMIN | MEMBER
  invited_by     VARCHAR(32) REFERENCES users(id) ON DELETE SET NULL,
  invited_at     TIMESTAMPTZ,
  joined_at      TIMESTAMPTZ,
  status         VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | INVITED | REMOVED
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (workspace_id, user_id),
  CONSTRAINT workspace_members_role_chk   CHECK (role   IN ('ADMIN','MEMBER')),
  CONSTRAINT workspace_members_status_chk CHECK (status IN ('ACTIVE','INVITED','REMOVED'))
);
CREATE INDEX idx_workspace_members_user ON workspace_members (user_id);
CREATE INDEX idx_workspace_members_role ON workspace_members (workspace_id, role);


-- ========== BOARD MEMBERS ==========
CREATE TABLE board_members (
  board_id       VARCHAR(32) NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
  user_id        VARCHAR(32) NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
  role           VARCHAR(16) NOT NULL CHECK (role IN ('ADMIN','MEMBER','VIEWER')),
  status         VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  added_by       VARCHAR(32) REFERENCES users(id) ON DELETE SET NULL,
  added_at       TIMESTAMPTZ,
  is_favorite    BOOLEAN NOT NULL DEFAULT FALSE,
  favorite_at    TIMESTAMPTZ,
  last_access_at TIMESTAMPTZ,
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (board_id, user_id),
  CONSTRAINT board_members_status_chk CHECK (status IN ('ACTIVE','REMOVED'))
);
CREATE INDEX idx_board_members_user_sort ON board_members (user_id, is_favorite DESC, last_access_at DESC);
CREATE INDEX idx_board_members_board     ON board_members (board_id);


-- ========== LABELS ==========
CREATE TABLE labels (
  id             VARCHAR(32) PRIMARY KEY,
  board_id       VARCHAR(32) NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
  name           VARCHAR(120) NOT NULL,
  color          VARCHAR(16),
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT labels_board_name_uk UNIQUE (board_id, name),
  CONSTRAINT labels_color_chk CHECK (color IS NULL OR color ~ '^#?[0-9a-fA-F]{3}([0-9a-fA-F]{3})?$')
);
CREATE INDEX idx_labels_board ON labels (board_id);


-- ========== CARD LABELS ==========
CREATE TABLE card_labels (
  card_id        VARCHAR(32) NOT NULL REFERENCES cards(id)  ON DELETE CASCADE,
  label_id       VARCHAR(32) NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (card_id, label_id)
);


-- ========== CARD MEMBERS ==========
CREATE TABLE card_members (
  card_id        VARCHAR(32) NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
  user_id        VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  added_by       VARCHAR(32) REFERENCES users(id) ON DELETE SET NULL,
  added_at       TIMESTAMPTZ,
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (card_id, user_id)
);
CREATE INDEX idx_card_members_user ON card_members (user_id);


-- ========== COMMENTS ==========
CREATE TABLE comments (
  id             VARCHAR(32) PRIMARY KEY,
  card_id        VARCHAR(32) NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
  author_id      VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  body           TEXT NOT NULL,
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,  -- 보통 author_id와 동일
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at     TIMESTAMPTZ
);
CREATE INDEX idx_comments_card   ON comments (card_id);
CREATE INDEX idx_comments_author ON comments (author_id);
-- (Optional) 미삭제만 빠르게: CREATE INDEX idx_comments_card_alive ON comments (card_id) WHERE deleted_at IS NULL;


-- ========== ATTACHMENTS ==========
CREATE TABLE attachments (
  id             VARCHAR(32) PRIMARY KEY,
  card_id        VARCHAR(32) NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
  filename       VARCHAR(255) NOT NULL,
  mime_type      VARCHAR(255),
  size_bytes     BIGINT,
  storage_url    VARCHAR(1024) NOT NULL,
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT attachments_size_chk CHECK (size_bytes IS NULL OR size_bytes >= 0)
);
CREATE INDEX idx_attachments_card ON attachments (card_id);


-- ========== ACTIVITY LOGS ==========
CREATE TABLE activity_logs (
  id             VARCHAR(32) PRIMARY KEY,
  actor_id       VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  workspace_id   VARCHAR(32) REFERENCES workspaces(id) ON DELETE CASCADE,
  board_id       VARCHAR(32) REFERENCES boards(id)     ON DELETE CASCADE,
  card_id        VARCHAR(32) REFERENCES cards(id)      ON DELETE CASCADE,
  type           VARCHAR(64) NOT NULL,   -- e.g., CARD_MOVED, LIST_CREATED ...
  payload        JSON,                    -- PG 프로덕션에서는 JSONB + GIN 인덱스 권장
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,  -- 보통 actor_id와 동일
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_activity_logs_board_time ON activity_logs (board_id, created_at);
CREATE INDEX idx_activity_logs_card_time  ON activity_logs (card_id, created_at);
CREATE INDEX idx_activity_logs_actor_time ON activity_logs (actor_id, created_at);
-- (Optional, PostgreSQL 전용)
-- ALTER TABLE activity_logs ALTER COLUMN payload TYPE JSONB USING payload::jsonb;
-- CREATE INDEX idx_activity_logs_payload_gin ON activity_logs USING GIN (payload);


-- ========== INVITES ==========
CREATE TABLE invites (
  id             VARCHAR(32) PRIMARY KEY,
  scope          VARCHAR(16) NOT NULL,                             -- WORKSPACE | BOARD
  workspace_id   VARCHAR(32) REFERENCES workspaces(id) ON DELETE CASCADE,
  board_id       VARCHAR(32) REFERENCES boards(id)     ON DELETE CASCADE,

  email          VARCHAR(255) NOT NULL,
  role           VARCHAR(16)  NOT NULL,                            -- scope별 허용값 제한(아래 CHECK)
  token_hash     VARCHAR(128),                                     -- 보안: 토큰 해시 보관 권장
  invite_url     VARCHAR(1024) NOT NULL,

  expires_at     TIMESTAMPTZ NOT NULL DEFAULT (now() + INTERVAL '7 days'),
  invited_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  accepted_by    VARCHAR(32) REFERENCES users(id) ON DELETE SET NULL,
  accepted_at    TIMESTAMPTZ,
  status         VARCHAR(16) NOT NULL CHECK (status IN ('PENDING','ACCEPTED','DECLINED','EXPIRED')),
  created_by     VARCHAR(32) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

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

-- (Optional) 동일 대상 중복 초대 방지 (PENDING 1건 제한)
-- PostgreSQL Partial Unique Index 예시:
-- CREATE UNIQUE INDEX uq_invites_pending_workspace ON invites (email, workspace_id)
--   WHERE scope = 'WORKSPACE' AND status = 'PENDING';
-- CREATE UNIQUE INDEX uq_invites_pending_board ON invites (email, board_id)
--   WHERE scope = 'BOARD' AND status = 'PENDING';
```

---

## V1\_1\_\_compat\_pg\_extras.sql (Patch)

> 목표: **H2/PG 차이 최소화** + **PostgreSQL 전용 인덱스/확장** 적용.
> 운영 DB는 PostgreSQL 기준, 로컬/테스트는 H2(PostgreSQL 모드)를 권장합니다.

```sql
-- =============================================================
-- Patch: Cross-DB 호환 + PostgreSQL 전용 최적화
-- Target: PostgreSQL (prod), H2 (test; MODE=PostgreSQL)
-- =============================================================

-- 0) H2 권장 JDBC (application-test.yml/properties)
-- spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH

-- 1) (PostgreSQL) 확장 설치 ----------------------------------------------------
DO $$ BEGIN
  PERFORM 1 FROM pg_extension WHERE extname = 'citext';
  IF NOT FOUND THEN CREATE EXTENSION citext; END IF;
END $$;

DO $$ BEGIN
  PERFORM 1 FROM pg_extension WHERE extname = 'pg_trgm';
  IF NOT FOUND THEN CREATE EXTENSION pg_trgm; END IF;
END $$;

-- 2) (선택) 이메일/유저네임 대소문자 무시 유니크 ------------------------------
-- 2-A) citext 사용(권장)
ALTER TABLE users ALTER COLUMN email   TYPE CITEXT;
ALTER TABLE users ALTER COLUMN username TYPE CITEXT;

-- 2-B) (대안) citext 불가 환경이면 함수 인덱스 유니크 사용
-- CREATE UNIQUE INDEX IF NOT EXISTS users_email_lower_uk   ON users (LOWER(email));
-- CREATE UNIQUE INDEX IF NOT EXISTS users_username_lower_uk ON users (LOWER(username));

-- 3) (PostgreSQL) 검색/정렬 최적화 -------------------------------------------
-- 제목/이름 부분 검색을 위한 trigram 인덱스 (필요 시 활성화)
CREATE INDEX IF NOT EXISTS idx_boards_name_trgm ON boards USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_cards_title_trgm ON cards  USING GIN (title gin_trgm_ops);

-- 댓글 최신순 조회 최적화 (소프트 삭제 제외)
CREATE INDEX IF NOT EXISTS idx_comments_card_alive ON comments (card_id, created_at DESC)
  WHERE deleted_at IS NULL;

-- 4) (PostgreSQL) JSONB + GIN 인덱스 ------------------------------------------
-- 활동 로그 payload를 jsonb로 전환하고 질의 가속
ALTER TABLE activity_logs
  ALTER COLUMN payload TYPE JSONB USING payload::jsonb;
CREATE INDEX IF NOT EXISTS idx_activity_logs_payload_gin ON activity_logs USING GIN (payload);

-- 5) (PostgreSQL) 초대 중복 방지 (Partial Unique) ------------------------------
-- 동일 대상에게 동시에 1건만 PENDING 허용
CREATE UNIQUE INDEX IF NOT EXISTS uq_invites_pending_workspace ON invites (email, workspace_id)
  WHERE scope = 'WORKSPACE' AND status = 'PENDING';
CREATE UNIQUE INDEX IF NOT EXISTS uq_invites_pending_board ON invites (email, board_id)
  WHERE scope = 'BOARD' AND status = 'PENDING';

-- 6) (권장) 실행 계획 힌트: 정렬/필터 패턴에 맞춘 보조 인덱스 ------------------
-- 이미 존재: idx_board_members_user_sort(user_id, is_favorite DESC, last_access_at DESC)
-- 필요 시 보강 예시 (사용 패턴 확인 후 적용)
-- CREATE INDEX IF NOT EXISTS idx_cards_assignee ON cards (assignee_id);
-- CREATE INDEX IF NOT EXISTS idx_lists_board_name ON lists (board_id, name);
```

### 적용 메모

* **H2**는 위 Postgres 전용 구문(EXTENSION, GIN/TRGM)을 무시/실패할 수 있으므로,
  테스트 프로파일에서는 해당 패치를 **비활성**하거나 Flyway **placeholders**로 분기 적용하세요.
  예: `flyway.placeholders.isPg=true` → `(${isPg})` 조건부 실행 스니펫 사용.
* Citext를 사용하면 이메일/유저네임 비교/검색이 단순·안전해집니다. 불가 시 함수 인덱스로 대체하세요.
* Trigram 인덱스는 **부분 문자열 검색**이 있을 때만 유의미합니다. 필요 시점에 활성화해도 됩니다.


