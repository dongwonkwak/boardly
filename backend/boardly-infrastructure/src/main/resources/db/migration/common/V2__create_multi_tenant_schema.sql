-- =====================================================
-- Boardly Multi-Tenant Schema Migration V2
-- =====================================================

-- Enable required extension (uuid generation)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================
-- ENUM types
-- =========================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'workspace_role') THEN
    CREATE TYPE workspace_role AS ENUM ('OWNER', 'ADMIN', 'MEMBER');
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'board_role') THEN
    CREATE TYPE board_role AS ENUM ('BOARD_ADMIN', 'BOARD_EDITOR', 'BOARD_VIEWER');
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invite_status_type') THEN
    CREATE TYPE invite_status_type AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED');
  END IF;
END$$;

-- =========================
-- Core tables (Multi-tenant structure)
-- =========================

-- WORKSPACES (New multi-tenant root)
CREATE TABLE IF NOT EXISTS workspaces (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  description TEXT,
  type VARCHAR(50),
  owner_user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Update BOARDS to include workspace_id
ALTER TABLE boards ADD COLUMN IF NOT EXISTS workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_boards_workspace_id ON boards(workspace_id);

-- Update BOARD_LISTS to include workspace_id and rename to LISTS
CREATE TABLE IF NOT EXISTS lists (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
  board_id VARCHAR(50) REFERENCES boards(board_id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  position INTEGER NOT NULL,
  color VARCHAR(7),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (workspace_id, board_id, position)
);

-- Update CARDS to include workspace_id
ALTER TABLE cards ADD COLUMN IF NOT EXISTS workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_cards_workspace_id ON cards(workspace_id);

-- Update LABELS to include workspace_id
ALTER TABLE labels ADD COLUMN IF NOT EXISTS workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_labels_workspace_id ON labels(workspace_id);

-- Update CARD_LABELS to include workspace_id
ALTER TABLE card_labels ADD COLUMN IF NOT EXISTS workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_card_labels_workspace_id ON card_labels(workspace_id);

-- Update COMMENTS to include workspace_id
ALTER TABLE comments ADD COLUMN IF NOT EXISTS workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_comments_workspace_id ON comments(workspace_id);

-- Update USER_ACTIVITY to include workspace_id and rename to ACTIVITIES
CREATE TABLE IF NOT EXISTS activities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
  board_id VARCHAR(50) REFERENCES boards(board_id) ON DELETE CASCADE,
  user_id VARCHAR(50) REFERENCES users(user_id) ON DELETE CASCADE,
  type VARCHAR(50) NOT NULL,
  payload_json JSONB,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================
-- Membership & Invitations (New)
-- =========================

-- WORKSPACE_MEMBERS
CREATE TABLE IF NOT EXISTS workspace_members (
  workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
  user_id VARCHAR(50) REFERENCES users(user_id) ON DELETE CASCADE,
  role workspace_role NOT NULL DEFAULT 'MEMBER',
  invite_status invite_status_type NOT NULL DEFAULT 'ACCEPTED',
  invited_by VARCHAR(50) REFERENCES users(user_id),
  invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
  joined_at TIMESTAMP,
  PRIMARY KEY (workspace_id, user_id)
);

-- Update BOARD_MEMBERS to include new role structure
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS role_new board_role;
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS invite_status invite_status_type DEFAULT 'ACCEPTED';
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS invited_by VARCHAR(50) REFERENCES users(user_id);
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS invited_at TIMESTAMP DEFAULT NOW();
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS joined_at TIMESTAMP;

-- WORKSPACE_INVITATIONS
CREATE TABLE IF NOT EXISTS workspace_invitations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
  email VARCHAR(255) NOT NULL,
  role workspace_role NOT NULL DEFAULT 'MEMBER',
  status invite_status_type NOT NULL DEFAULT 'PENDING',
  invited_by VARCHAR(50) REFERENCES users(user_id),
  invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMP NOT NULL DEFAULT (NOW() + INTERVAL '7 days'),
  accepted_at TIMESTAMP,
  UNIQUE (workspace_id, email, status)
);

-- BOARD_INVITATIONS
CREATE TABLE IF NOT EXISTS board_invitations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  board_id VARCHAR(50) REFERENCES boards(board_id) ON DELETE CASCADE,
  email VARCHAR(255) NOT NULL,
  role board_role NOT NULL DEFAULT 'BOARD_VIEWER',
  status invite_status_type NOT NULL DEFAULT 'PENDING',
  invited_by VARCHAR(50) REFERENCES users(user_id),
  invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMP NOT NULL DEFAULT (NOW() + INTERVAL '7 days'),
  accepted_at TIMESTAMP,
  UNIQUE (board_id, email, status)
);

-- =========================
-- Additional Indexes for Multi-tenant Performance
-- =========================

-- Workspace membership
CREATE INDEX IF NOT EXISTS idx_workspace_members_user_workspace ON workspace_members(user_id, workspace_id);
CREATE INDEX IF NOT EXISTS idx_workspace_members_status ON workspace_members(invite_status);

-- Board membership
CREATE INDEX IF NOT EXISTS idx_board_members_user_board ON board_members(user_id, board_id);
CREATE INDEX IF NOT EXISTS idx_board_members_status ON board_members(invite_status);

-- Invitations
CREATE INDEX IF NOT EXISTS idx_workspace_invitations_email_status ON workspace_invitations(email, status);
CREATE INDEX IF NOT EXISTS idx_workspace_invitations_expires_at ON workspace_invitations(expires_at);
CREATE INDEX IF NOT EXISTS idx_board_invitations_email_status ON board_invitations(email, status);
CREATE INDEX IF NOT EXISTS idx_board_invitations_expires_at ON board_invitations(expires_at);

-- Lists (new structure)
CREATE INDEX IF NOT EXISTS idx_lists_workspace_board ON lists(workspace_id, board_id);
CREATE INDEX IF NOT EXISTS idx_lists_position ON lists(workspace_id, board_id, position);

-- Cards (updated)
CREATE INDEX IF NOT EXISTS idx_cards_workspace_list ON cards(workspace_id, list_id);
CREATE INDEX IF NOT EXISTS idx_cards_position ON cards(workspace_id, list_id, position);
CREATE INDEX IF NOT EXISTS idx_cards_due_date ON cards(workspace_id, due_date);
CREATE INDEX IF NOT EXISTS idx_cards_completed ON cards(workspace_id, is_completed);

-- Labels (updated)
CREATE INDEX IF NOT EXISTS idx_labels_workspace ON labels(workspace_id);

-- Card-Labels (updated)
CREATE INDEX IF NOT EXISTS idx_card_labels_workspace_card ON card_labels(workspace_id, card_id);
CREATE INDEX IF NOT EXISTS idx_card_labels_workspace_label ON card_labels(workspace_id, label_id);

-- Comments (updated)
CREATE INDEX IF NOT EXISTS idx_comments_workspace_card ON comments(workspace_id, card_id);
CREATE INDEX IF NOT EXISTS idx_comments_card_created ON comments(workspace_id, card_id, created_at);

-- Activities (new)
CREATE INDEX IF NOT EXISTS idx_activities_workspace_board ON activities(workspace_id, board_id);
CREATE INDEX IF NOT EXISTS idx_activities_user_created ON activities(workspace_id, user_id, created_at);

-- =========================
-- Data Migration Helpers
-- =========================

-- Create a default workspace for existing data
INSERT INTO workspaces (id, name, description, type, owner_user_id, created_at, updated_at)
SELECT 
  gen_random_uuid(),
  'Default Workspace',
  'Default workspace for existing data',
  'DEFAULT',
  owner_id,
  MIN(created_at),
  MAX(updated_at)
FROM boards 
WHERE workspace_id IS NULL
GROUP BY owner_id
ON CONFLICT DO NOTHING;

-- Update existing boards with workspace_id
UPDATE boards 
SET workspace_id = w.id
FROM workspaces w
WHERE boards.workspace_id IS NULL 
  AND boards.owner_id = w.owner_user_id;

-- Update existing cards with workspace_id
UPDATE cards 
SET workspace_id = b.workspace_id
FROM boards b
WHERE cards.workspace_id IS NULL 
  AND cards.list_id IN (SELECT list_id FROM board_lists WHERE board_id = b.board_id);

-- Update existing labels with workspace_id
UPDATE labels 
SET workspace_id = b.workspace_id
FROM boards b
WHERE labels.workspace_id IS NULL 
  AND labels.board_id = b.board_id;

-- Update existing comments with workspace_id
UPDATE comments 
SET workspace_id = c.workspace_id
FROM cards c
WHERE comments.workspace_id IS NULL 
  AND comments.card_id = c.card_id;

-- Update existing card_labels with workspace_id
UPDATE card_labels 
SET workspace_id = c.workspace_id
FROM cards c
WHERE card_labels.workspace_id IS NULL 
  AND card_labels.card_id = c.card_id;
