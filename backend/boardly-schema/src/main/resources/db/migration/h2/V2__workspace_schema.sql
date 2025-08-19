-- =====================================================
-- Boardly Database Schema V2 (H2, PostgreSQL compatibility mode)
-- Workspace-based multi-tenant schema with invitations
-- =====================================================

-- Notes for H2:
-- - Use VARCHAR(36) for UUID-like string IDs
-- - Replace JSONB with CLOB
-- - No native ENUM: use VARCHAR with CHECK constraints where possible

-- -----------------------------------------------------
-- Drop legacy tables if exist (order by FKs)
-- -----------------------------------------------------
DROP TABLE IF EXISTS user_activity;
DROP TABLE IF EXISTS attachments;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS card_labels;
DROP TABLE IF EXISTS card_members;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS labels;
DROP TABLE IF EXISTS board_lists;
DROP TABLE IF EXISTS board_members;
DROP TABLE IF EXISTS boards;
DROP TABLE IF EXISTS workspace_members;
DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS invitations;
DROP TABLE IF EXISTS lists;
DROP TABLE IF EXISTS workspaces;
DROP TABLE IF EXISTS users;

-- -----------------------------------------------------
-- Core tables
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS workspaces (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description CLOB,
    type VARCHAR(20) NOT NULL,
    owner_user_id VARCHAR(50),
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(owner_user_id, name)
);

CREATE TABLE IF NOT EXISTS workspace_members (
    workspace_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    invite_status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED',
    invited_by VARCHAR(50),
    invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
    joined_at TIMESTAMP,
    PRIMARY KEY (workspace_id, user_id),
    CONSTRAINT fk_wm_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_wm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS boards (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description CLOB,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_starred BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_board_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS board_members (
    board_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    invited_by VARCHAR(50),
    invited_at TIMESTAMP,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    invitation_type VARCHAR(20) NOT NULL,
    PRIMARY KEY (board_id, user_id),
    CONSTRAINT fk_bm_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_bm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS lists (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL,
    board_id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, board_id, position),
    CONSTRAINT fk_list_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_list_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS labels (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, name),
    CONSTRAINT fk_label_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cards (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL,
    list_id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description CLOB,
    position INTEGER NOT NULL,
    due_date DATE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, list_id, position),
    CONSTRAINT fk_card_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_card_list FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS card_members (
    card_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (card_id, user_id),
    CONSTRAINT fk_cm_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    CONSTRAINT fk_cm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS card_labels (
    workspace_id VARCHAR(50) NOT NULL,
    card_id VARCHAR(50) NOT NULL,
    label_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (workspace_id, card_id, label_id),
    CONSTRAINT fk_cl_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_cl_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    CONSTRAINT fk_cl_label FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL,
    card_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    content CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_comment_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS activities (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL,
    board_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    payload_json CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_activity_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS invitations (
    id VARCHAR(50) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    target_id VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    invite_code VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    invited_by VARCHAR(50),
    expires_at TIMESTAMP NOT NULL DEFAULT DATEADD('DAY', 7, NOW()),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    accepted_at TIMESTAMP,
    CONSTRAINT uq_invite_code UNIQUE(invite_code),
    CONSTRAINT ck_inv_email_or_code CHECK ((email IS NOT NULL AND invite_code IS NULL) OR (email IS NULL AND invite_code IS NOT NULL))
);

-- -----------------------------------------------------
-- Indexes
-- -----------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_workspace_members_user_workspace ON workspace_members(user_id, workspace_id);
CREATE INDEX IF NOT EXISTS idx_workspace_members_type ON workspace_members(workspace_id, type);
CREATE INDEX IF NOT EXISTS idx_workspace_members_role ON workspace_members(workspace_id, role);

CREATE INDEX IF NOT EXISTS idx_board_members_user_board ON board_members(user_id, board_id);
CREATE INDEX IF NOT EXISTS idx_board_members_invitation_type ON board_members(board_id, invitation_type);
CREATE INDEX IF NOT EXISTS idx_board_members_role ON board_members(board_id, role);

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

CREATE INDEX IF NOT EXISTS idx_invitations_email_status ON invitations(email, status);
CREATE INDEX IF NOT EXISTS idx_invitations_invite_code ON invitations(invite_code);
CREATE INDEX IF NOT EXISTS idx_invitations_expires_at ON invitations(expires_at);
CREATE INDEX IF NOT EXISTS idx_invitations_type_target ON invitations(type, target_id);
CREATE INDEX IF NOT EXISTS idx_invitations_status_created ON invitations(status, created_at);
