-- =====================================================
-- Boardly Database Schema V2 (PostgreSQL)
-- Workspace-based multi-tenant schema with invitations
-- =====================================================

-- (No extensions required when using VARCHAR ids)

-- -----------------------------------------------------
-- Drop legacy tables (from V1) if they exist
-- -----------------------------------------------------
DROP TABLE IF EXISTS user_activity CASCADE;
DROP TABLE IF EXISTS attachments CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS card_labels CASCADE;
DROP TABLE IF EXISTS card_members CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS labels CASCADE;
DROP TABLE IF EXISTS board_lists CASCADE;
DROP TABLE IF EXISTS board_members CASCADE;
DROP TABLE IF EXISTS boards CASCADE;
DROP TABLE IF EXISTS workspace_members CASCADE;
DROP TABLE IF EXISTS activities CASCADE;
DROP TABLE IF EXISTS invitations CASCADE;
DROP TABLE IF EXISTS lists CASCADE;
DROP TABLE IF EXISTS workspaces CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- -----------------------------------------------------
-- Enum types
-- -----------------------------------------------------
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'workspace_role') THEN
        CREATE TYPE workspace_role AS ENUM ('OWNER', 'MEMBER');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'member_type') THEN
        CREATE TYPE member_type AS ENUM ('MEMBER', 'BOARD_ONLY');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'board_role') THEN
        CREATE TYPE board_role AS ENUM ('OWNER', 'EDITOR', 'VIEWER');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invitation_type') THEN
        CREATE TYPE invitation_type AS ENUM ('WORKSPACE_AUTO', 'INDIVIDUAL');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invite_status_type') THEN
        CREATE TYPE invite_status_type AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invitation_target_type') THEN
        CREATE TYPE invitation_target_type AS ENUM ('WORKSPACE', 'BOARD');
    END IF;
END $$;

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
    description TEXT,
    type VARCHAR(20) NOT NULL CHECK (type IN ('PERSONAL', 'TEAM')),
    owner_user_id VARCHAR(50) REFERENCES users(id) ON DELETE CASCADE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(owner_user_id, name)
);

CREATE TABLE IF NOT EXISTS workspace_members (
    workspace_id VARCHAR(50) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role workspace_role NOT NULL DEFAULT 'MEMBER',
    type member_type NOT NULL DEFAULT 'MEMBER',
    invite_status invite_status_type NOT NULL DEFAULT 'ACCEPTED',
    invited_by VARCHAR(50) REFERENCES users(id),
    invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
    joined_at TIMESTAMP,
    PRIMARY KEY (workspace_id, user_id)
);

CREATE TABLE IF NOT EXISTS boards (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_starred BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS board_members (
    board_id VARCHAR(50) NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role board_role NOT NULL DEFAULT 'VIEWER',
    invited_by VARCHAR(50) REFERENCES users(id),
    invited_at TIMESTAMP,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    invitation_type invitation_type NOT NULL DEFAULT 'INDIVIDUAL',
    PRIMARY KEY (board_id, user_id)
);

CREATE TABLE IF NOT EXISTS lists (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    board_id VARCHAR(50) NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, board_id, position)
);

CREATE TABLE IF NOT EXISTS labels (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, name)
);

CREATE TABLE IF NOT EXISTS cards (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    list_id VARCHAR(50) NOT NULL REFERENCES lists(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    position INTEGER NOT NULL,
    due_date DATE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, list_id, position)
);

CREATE TABLE IF NOT EXISTS card_members (
    card_id VARCHAR(50) NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (card_id, user_id)
);

CREATE TABLE IF NOT EXISTS card_labels (
    workspace_id VARCHAR(50) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    card_id VARCHAR(50) NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    label_id VARCHAR(50) NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
    PRIMARY KEY (workspace_id, card_id, label_id)
);

CREATE TABLE IF NOT EXISTS comments (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    card_id VARCHAR(50) NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS activities (
    id VARCHAR(50) PRIMARY KEY,
    workspace_id VARCHAR(50) NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    board_id VARCHAR(50) NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    payload_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS invitations (
    id VARCHAR(50) PRIMARY KEY,
    type invitation_target_type NOT NULL,
    target_id VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    invite_code VARCHAR(255) UNIQUE,
    role VARCHAR(50) NOT NULL,
    status invite_status_type NOT NULL DEFAULT 'PENDING',
    invited_by VARCHAR(50) REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL DEFAULT (NOW() + INTERVAL '7 days'),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    accepted_at TIMESTAMP,
    CHECK ((email IS NOT NULL AND invite_code IS NULL) OR (email IS NULL AND invite_code IS NOT NULL))
);

-- -----------------------------------------------------
-- Indexes
-- -----------------------------------------------------
-- Workspace membership
CREATE INDEX IF NOT EXISTS idx_workspace_members_user_workspace ON workspace_members(user_id, workspace_id);
CREATE INDEX IF NOT EXISTS idx_workspace_members_type ON workspace_members(workspace_id, type);
CREATE INDEX IF NOT EXISTS idx_workspace_members_role ON workspace_members(workspace_id, role);

-- Board memberships
CREATE INDEX IF NOT EXISTS idx_board_members_user_board ON board_members(user_id, board_id);
CREATE INDEX IF NOT EXISTS idx_board_members_invitation_type ON board_members(board_id, invitation_type);
CREATE INDEX IF NOT EXISTS idx_board_members_role ON board_members(board_id, role);

-- Lists
CREATE INDEX IF NOT EXISTS idx_lists_workspace_board ON lists(workspace_id, board_id);
CREATE INDEX IF NOT EXISTS idx_lists_position ON lists(workspace_id, board_id, position);

-- Cards
CREATE INDEX IF NOT EXISTS idx_cards_workspace_list ON cards(workspace_id, list_id);
CREATE INDEX IF NOT EXISTS idx_cards_position ON cards(workspace_id, list_id, position);
CREATE INDEX IF NOT EXISTS idx_cards_due_date ON cards(workspace_id, due_date);
CREATE INDEX IF NOT EXISTS idx_cards_completed ON cards(workspace_id, is_completed);

-- Labels
CREATE INDEX IF NOT EXISTS idx_labels_workspace ON labels(workspace_id);

-- Card-Labels
CREATE INDEX IF NOT EXISTS idx_card_labels_workspace_card ON card_labels(workspace_id, card_id);
CREATE INDEX IF NOT EXISTS idx_card_labels_workspace_label ON card_labels(workspace_id, label_id);

-- Comments
CREATE INDEX IF NOT EXISTS idx_comments_workspace_card ON comments(workspace_id, card_id);
CREATE INDEX IF NOT EXISTS idx_comments_card_created ON comments(workspace_id, card_id, created_at);

-- Activities
CREATE INDEX IF NOT EXISTS idx_activities_workspace_board ON activities(workspace_id, board_id);
CREATE INDEX IF NOT EXISTS idx_activities_user_created ON activities(workspace_id, user_id, created_at);

-- Invitations
CREATE INDEX IF NOT EXISTS idx_invitations_email_status ON invitations(email, status);
CREATE INDEX IF NOT EXISTS idx_invitations_invite_code ON invitations(invite_code);
CREATE INDEX IF NOT EXISTS idx_invitations_expires_at ON invitations(expires_at);
CREATE INDEX IF NOT EXISTS idx_invitations_type_target ON invitations(type, target_id);
CREATE INDEX IF NOT EXISTS idx_invitations_status_created ON invitations(status, created_at);

-- -----------------------------------------------------
-- Data integrity triggers (PostgreSQL only)
-- -----------------------------------------------------
-- Personal workspace: only one OWNER allowed
CREATE OR REPLACE FUNCTION check_personal_workspace_owner()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM workspaces w
        JOIN workspace_members wm ON w.id = wm.workspace_id
        WHERE w.type = 'PERSONAL' AND wm.role = 'OWNER'
        AND wm.workspace_id = NEW.workspace_id
        GROUP BY wm.workspace_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Personal workspace allows only one OWNER';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_check_personal_workspace_owner ON workspace_members;
CREATE TRIGGER trigger_check_personal_workspace_owner
    AFTER INSERT OR UPDATE ON workspace_members
    FOR EACH ROW EXECUTE FUNCTION check_personal_workspace_owner();

-- WORKSPACE_AUTO on board_members requires workspace MEMBER
CREATE OR REPLACE FUNCTION check_workspace_auto_member()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.invitation_type = 'WORKSPACE_AUTO' THEN
        IF NOT EXISTS (
            SELECT 1 FROM workspace_members wm
            WHERE wm.user_id = NEW.user_id 
            AND wm.workspace_id = (
                SELECT workspace_id FROM boards WHERE id = NEW.board_id
            )
            AND wm.type = 'MEMBER'
        ) THEN
            RAISE EXCEPTION 'WORKSPACE_AUTO permission allowed only for workspace MEMBER';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_check_workspace_auto_member ON board_members;
CREATE TRIGGER trigger_check_workspace_auto_member
    AFTER INSERT OR UPDATE ON board_members
    FOR EACH ROW EXECUTE FUNCTION check_workspace_auto_member();
