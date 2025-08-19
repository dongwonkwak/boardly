-- =====================================================
-- Boardly Multi-Tenant Schema Migration V2 (H2)
-- Based on docs/db/schema.md | Compatible with H2 (MODE=PostgreSQL)
-- =====================================================

-- NOTE: H2 doesn't support PostgreSQL extensions and enum types.
-- We emulate enums with VARCHAR + CHECK constraints and avoid DO-blocks.

-- =========================
-- Core tables (Multi-tenant structure)
-- =========================

-- WORKSPACES
CREATE TABLE IF NOT EXISTS workspaces (
  id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
  name VARCHAR(255) NOT NULL,
  description TEXT,
  type VARCHAR(50),
  owner_user_id VARCHAR(50),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_workspaces_owner_user
    FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_workspaces_owner_user_id ON workspaces(owner_user_id);

-- BOARDS: add workspace_id
ALTER TABLE boards ADD COLUMN IF NOT EXISTS workspace_id UUID;
ALTER TABLE boards ADD CONSTRAINT fk_boards_workspace
  FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_boards_workspace_id ON boards(workspace_id);

-- LISTS
CREATE TABLE IF NOT EXISTS lists (
  id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
  workspace_id UUID NOT NULL,
  board_id VARCHAR(50) NOT NULL,
  title VARCHAR(255) NOT NULL,
  position INTEGER NOT NULL,
  color VARCHAR(7),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_lists_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
  CONSTRAINT fk_lists_board FOREIGN KEY (board_id) REFERENCES boards(board_id) ON DELETE CASCADE,
  CONSTRAINT uq_lists_workspace_board_position UNIQUE (workspace_id, board_id, position)
);

-- CARDS: add workspace_id
ALTER TABLE cards ADD COLUMN IF NOT EXISTS workspace_id UUID;
ALTER TABLE cards ADD CONSTRAINT fk_cards_workspace
  FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_cards_workspace_id ON cards(workspace_id);

-- LABELS: add workspace_id
ALTER TABLE labels ADD COLUMN IF NOT EXISTS workspace_id UUID;
ALTER TABLE labels ADD CONSTRAINT fk_labels_workspace
  FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_labels_workspace_id ON labels(workspace_id);

-- CARD_LABELS: add workspace_id
ALTER TABLE card_labels ADD COLUMN IF NOT EXISTS workspace_id UUID;
ALTER TABLE card_labels ADD CONSTRAINT fk_card_labels_workspace
  FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_card_labels_workspace_id ON card_labels(workspace_id);

-- COMMENTS: add workspace_id
ALTER TABLE comments ADD COLUMN IF NOT EXISTS workspace_id UUID;
ALTER TABLE comments ADD CONSTRAINT fk_comments_workspace
  FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_comments_workspace_id ON comments(workspace_id);

-- ACTIVITIES
CREATE TABLE IF NOT EXISTS activities (
  id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
  workspace_id UUID NOT NULL,
  board_id VARCHAR(50),
  user_id VARCHAR(50),
  type VARCHAR(50) NOT NULL,
  payload_json CLOB, -- H2: use CLOB instead of JSONB
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_activities_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
  CONSTRAINT fk_activities_board FOREIGN KEY (board_id) REFERENCES boards(board_id) ON DELETE CASCADE,
  CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =========================
-- Membership & Invitations (New)
-- =========================

-- WORKSPACE_MEMBERS (role / invite_status as VARCHAR with CHECK)
CREATE TABLE IF NOT EXISTS workspace_members (
  workspace_id UUID NOT NULL,
  user_id VARCHAR(50) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
  invite_status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED',
  invited_by VARCHAR(50),
  invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
  joined_at TIMESTAMP,
  CONSTRAINT fk_workspace_members_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
  CONSTRAINT fk_workspace_members_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT pk_workspace_members PRIMARY KEY (workspace_id, user_id),
  CONSTRAINT ck_workspace_members_role CHECK (role IN ('OWNER','ADMIN','MEMBER')),
  CONSTRAINT ck_workspace_members_invite CHECK (invite_status IN ('PENDING','ACCEPTED','DECLINED','EXPIRED'))
);

-- BOARD_MEMBERS: add invitation columns
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS invite_status VARCHAR(20) DEFAULT 'ACCEPTED';
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS invited_by VARCHAR(50);
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS invited_at TIMESTAMP DEFAULT NOW();
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS joined_at TIMESTAMP;

-- Add optional CHECKs for H2 if column exists
ALTER TABLE board_members ADD CONSTRAINT IF NOT EXISTS ck_board_members_invite CHECK (invite_status IN ('PENDING','ACCEPTED','DECLINED','EXPIRED'));

-- WORKSPACE_INVITATIONS
CREATE TABLE IF NOT EXISTS workspace_invitations (
  id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
  workspace_id UUID NOT NULL,
  email VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  invited_by VARCHAR(50),
  invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMP NOT NULL DEFAULT DATEADD('DAY', 7, NOW()),
  accepted_at TIMESTAMP,
  CONSTRAINT fk_workspace_invitations_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
  CONSTRAINT fk_workspace_invitations_invited_by FOREIGN KEY (invited_by) REFERENCES users(user_id),
  CONSTRAINT uq_workspace_invitations UNIQUE (workspace_id, email, status),
  CONSTRAINT ck_workspace_invitations_role CHECK (role IN ('OWNER','ADMIN','MEMBER')),
  CONSTRAINT ck_workspace_invitations_status CHECK (status IN ('PENDING','ACCEPTED','DECLINED','EXPIRED'))
);

-- BOARD_INVITATIONS
CREATE TABLE IF NOT EXISTS board_invitations (
  id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
  board_id VARCHAR(50) NOT NULL,
  email VARCHAR(255) NOT NULL,
  role VARCHAR(30) NOT NULL DEFAULT 'BOARD_VIEWER',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  invited_by VARCHAR(50),
  invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMP NOT NULL DEFAULT DATEADD('DAY', 7, NOW()),
  accepted_at TIMESTAMP,
  CONSTRAINT fk_board_invitations_board FOREIGN KEY (board_id) REFERENCES boards(board_id) ON DELETE CASCADE,
  CONSTRAINT fk_board_invitations_invited_by FOREIGN KEY (invited_by) REFERENCES users(user_id),
  CONSTRAINT uq_board_invitations UNIQUE (board_id, email, status),
  CONSTRAINT ck_board_invitations_role CHECK (role IN ('BOARD_ADMIN','BOARD_EDITOR','BOARD_VIEWER')),
  CONSTRAINT ck_board_invitations_status CHECK (status IN ('PENDING','ACCEPTED','DECLINED','EXPIRED'))
);

-- =========================
-- Indexes
-- =========================

CREATE INDEX IF NOT EXISTS idx_workspace_members_user_workspace ON workspace_members(user_id, workspace_id);
CREATE INDEX IF NOT EXISTS idx_workspace_members_status ON workspace_members(invite_status);

CREATE INDEX IF NOT EXISTS idx_board_members_user_board ON board_members(user_id, board_id);
CREATE INDEX IF NOT EXISTS idx_board_members_status ON board_members(invite_status);

CREATE INDEX IF NOT EXISTS idx_workspace_invitations_email_status ON workspace_invitations(email, status);
CREATE INDEX IF NOT EXISTS idx_workspace_invitations_expires_at ON workspace_invitations(expires_at);
CREATE INDEX IF NOT EXISTS idx_board_invitations_email_status ON board_invitations(email, status);
CREATE INDEX IF NOT EXISTS idx_board_invitations_expires_at ON board_invitations(expires_at);

CREATE INDEX IF NOT EXISTS idx_lists_workspace_board ON lists(workspace_id, board_id);
CREATE INDEX IF NOT EXISTS idx_lists_position ON lists(workspace_id, board_id, position);

CREATE INDEX IF NOT EXISTS idx_cards_workspace_list ON cards(workspace_id, list_id);
CREATE INDEX IF NOT EXISTS idx_cards_position ON cards(workspace_id, list_id, position);
CREATE INDEX IF NOT EXISTS idx_cards_due_date ON cards(workspace_id, due_date);
CREATE INDEX IF NOT EXISTS idx_cards_completed ON cards(workspace_id, is_completed);

CREATE INDEX IF NOT EXISTS idx_labels_workspace ON labels(workspace_id);

CREATE INDEX IF NOT EXISTS idx_card_labels_workspace_card ON card_labels(workspace_id, card_id);
CREATE INDEX IF NOT EXISTS idx_card_labels_workspace_label ON card_labels(workspace_id, label_id);

CREATE INDEX IF NOT EXISTS idx_comments_workspace_card ON comments(workspace_id, card_id);
CREATE INDEX IF NOT EXISTS idx_comments_card_created ON comments(workspace_id, card_id, created_at);

CREATE INDEX IF NOT EXISTS idx_activities_workspace_board ON activities(workspace_id, board_id);
CREATE INDEX IF NOT EXISTS idx_activities_user_created ON activities(workspace_id, user_id, created_at);
