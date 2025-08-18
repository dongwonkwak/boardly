-- =====================================================
-- Boardly Boards UUID Migration V5
-- =====================================================

-- Update boards table to use UUID for board_id
ALTER TABLE boards DROP CONSTRAINT IF EXISTS boards_pkey;
ALTER TABLE boards ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();

-- Update the new column with the UUID from the old board_id
UPDATE boards 
SET id = board_id::UUID
WHERE board_id ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- Drop the old VARCHAR column and rename the new one
ALTER TABLE boards DROP COLUMN board_id;
ALTER TABLE boards RENAME COLUMN id TO id;

-- Add the primary key constraint back
ALTER TABLE boards ADD CONSTRAINT boards_pkey PRIMARY KEY (id);

-- Update foreign key references in lists
ALTER TABLE lists DROP CONSTRAINT IF EXISTS lists_board_id_fkey;
ALTER TABLE lists ADD COLUMN IF NOT EXISTS board_id_new UUID;

UPDATE lists 
SET board_id_new = b.id
FROM boards b
WHERE lists.board_id = b.id::text;

ALTER TABLE lists DROP COLUMN board_id;
ALTER TABLE lists RENAME COLUMN board_id_new TO board_id;

ALTER TABLE lists ADD CONSTRAINT lists_board_id_fkey 
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

-- Update the unique constraint
ALTER TABLE lists DROP CONSTRAINT IF EXISTS lists_workspace_id_board_id_position_key;
ALTER TABLE lists ADD CONSTRAINT lists_workspace_id_board_id_position_key 
  UNIQUE (workspace_id, board_id, position);

-- Update indexes for the new board_id column
DROP INDEX IF EXISTS idx_lists_workspace_board;
DROP INDEX IF EXISTS idx_lists_position;
CREATE INDEX IF NOT EXISTS idx_lists_workspace_board ON lists(workspace_id, board_id);
CREATE INDEX IF NOT EXISTS idx_lists_position ON lists(workspace_id, board_id, position);

-- Update foreign key references in activities
ALTER TABLE activities DROP CONSTRAINT IF EXISTS activities_board_id_fkey;
ALTER TABLE activities ADD COLUMN IF NOT EXISTS board_id_new UUID;

UPDATE activities 
SET board_id_new = b.id
FROM boards b
WHERE activities.board_id = b.id::text;

ALTER TABLE activities DROP COLUMN board_id;
ALTER TABLE activities RENAME COLUMN board_id_new TO board_id;

ALTER TABLE activities ADD CONSTRAINT activities_board_id_fkey 
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

-- Update indexes for the new board_id column
DROP INDEX IF EXISTS idx_activities_workspace_board;
CREATE INDEX IF NOT EXISTS idx_activities_workspace_board ON activities(workspace_id, board_id);

-- Update foreign key references in board_invitations
ALTER TABLE board_invitations DROP CONSTRAINT IF EXISTS board_invitations_board_id_fkey;
ALTER TABLE board_invitations ADD COLUMN IF NOT EXISTS board_id_new UUID;

UPDATE board_invitations 
SET board_id_new = b.id
FROM boards b
WHERE board_invitations.board_id = b.id::text;

ALTER TABLE board_invitations DROP COLUMN board_id;
ALTER TABLE board_invitations RENAME COLUMN board_id_new TO board_id;

ALTER TABLE board_invitations ADD CONSTRAINT board_invitations_board_id_fkey 
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

-- Update the unique constraint
ALTER TABLE board_invitations DROP CONSTRAINT IF EXISTS board_invitations_board_id_email_status_key;
ALTER TABLE board_invitations ADD CONSTRAINT board_invitations_board_id_email_status_key 
  UNIQUE (board_id, email, status);

-- Update indexes for the new board_id column
DROP INDEX IF EXISTS idx_board_invitations_email_status;
CREATE INDEX IF NOT EXISTS idx_board_invitations_email_status ON board_invitations(email, status);

-- Update foreign key references in board_members
ALTER TABLE board_members DROP CONSTRAINT IF EXISTS board_members_board_id_fkey;
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS board_id_new UUID;

UPDATE board_members 
SET board_id_new = b.id
FROM boards b
WHERE board_members.board_id = b.id::text;

ALTER TABLE board_members DROP COLUMN board_id;
ALTER TABLE board_members RENAME COLUMN board_id_new TO board_id;

ALTER TABLE board_members ADD CONSTRAINT board_members_board_id_fkey 
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

-- Update the primary key constraint
ALTER TABLE board_members DROP CONSTRAINT IF EXISTS board_members_pkey;
ALTER TABLE board_members ADD CONSTRAINT board_members_pkey PRIMARY KEY (board_id, user_id);

-- Update indexes for the new board_id column
DROP INDEX IF EXISTS idx_board_members_user_board;
CREATE INDEX IF NOT EXISTS idx_board_members_user_board ON board_members(user_id, board_id);

-- Update foreign key references in labels
ALTER TABLE labels DROP CONSTRAINT IF EXISTS labels_board_id_fkey;
ALTER TABLE labels ADD COLUMN IF NOT EXISTS board_id_new UUID;

UPDATE labels 
SET board_id_new = b.id
FROM boards b
WHERE labels.board_id = b.id::text;

ALTER TABLE labels DROP COLUMN board_id;
ALTER TABLE labels RENAME COLUMN board_id_new TO board_id;

ALTER TABLE labels ADD CONSTRAINT labels_board_id_fkey 
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

-- Update indexes for the new board_id column
DROP INDEX IF EXISTS idx_labels_board_id;
CREATE INDEX IF NOT EXISTS idx_labels_board_id ON labels(board_id);

-- Update foreign key references in cards (through lists)
-- This is already handled by the list_id update in V4

-- Update indexes for boards table
DROP INDEX IF EXISTS idx_boards_owner_id;
DROP INDEX IF EXISTS idx_boards_archived;
DROP INDEX IF EXISTS idx_boards_starred;

CREATE INDEX IF NOT EXISTS idx_boards_owner_id ON boards(owner_id);
CREATE INDEX IF NOT EXISTS idx_boards_archived ON boards(is_archived);
CREATE INDEX IF NOT EXISTS idx_boards_starred ON boards(is_starred);
CREATE INDEX IF NOT EXISTS idx_boards_workspace_id ON boards(workspace_id);

-- Update users table to use UUID for user_id
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE users ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();

-- Update the new column with the UUID from the old user_id
UPDATE users 
SET id = user_id::UUID
WHERE user_id ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- Drop the old VARCHAR column and rename the new one
ALTER TABLE users DROP COLUMN user_id;
ALTER TABLE users RENAME COLUMN id TO id;

-- Add the primary key constraint back
ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id);

-- Update foreign key references in workspaces
ALTER TABLE workspaces DROP CONSTRAINT IF EXISTS workspaces_owner_user_id_fkey;
ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS owner_user_id_new UUID;

UPDATE workspaces 
SET owner_user_id_new = u.id
FROM users u
WHERE workspaces.owner_user_id = u.id::text;

ALTER TABLE workspaces DROP COLUMN owner_user_id;
ALTER TABLE workspaces RENAME COLUMN owner_user_id_new TO owner_user_id;

ALTER TABLE workspaces ADD CONSTRAINT workspaces_owner_user_id_fkey 
  FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Update foreign key references in boards
ALTER TABLE boards DROP CONSTRAINT IF EXISTS boards_owner_id_fkey;
ALTER TABLE boards ADD COLUMN IF NOT EXISTS owner_id_new UUID;

UPDATE boards 
SET owner_id_new = u.id
FROM users u
WHERE boards.owner_id = u.id::text;

ALTER TABLE boards DROP COLUMN owner_id;
ALTER TABLE boards RENAME COLUMN owner_id_new TO owner_id;

ALTER TABLE boards ADD CONSTRAINT boards_owner_id_fkey 
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;

-- Update foreign key references in workspace_members
ALTER TABLE workspace_members DROP CONSTRAINT IF EXISTS workspace_members_user_id_fkey;
ALTER TABLE workspace_members ADD COLUMN IF NOT EXISTS user_id_new UUID;

UPDATE workspace_members 
SET user_id_new = u.id
FROM users u
WHERE workspace_members.user_id = u.id::text;

ALTER TABLE workspace_members DROP COLUMN user_id;
ALTER TABLE workspace_members RENAME COLUMN user_id_new TO user_id;

ALTER TABLE workspace_members ADD CONSTRAINT workspace_members_user_id_fkey 
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Update the primary key constraint
ALTER TABLE workspace_members DROP CONSTRAINT IF EXISTS workspace_members_pkey;
ALTER TABLE workspace_members ADD CONSTRAINT workspace_members_pkey PRIMARY KEY (workspace_id, user_id);

-- Update foreign key references in board_members
ALTER TABLE board_members DROP CONSTRAINT IF EXISTS board_members_user_id_fkey;
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS user_id_new UUID;

UPDATE board_members 
SET user_id_new = u.id
FROM users u
WHERE board_members.user_id = u.id::text;

ALTER TABLE board_members DROP COLUMN user_id;
ALTER TABLE board_members RENAME COLUMN user_id_new TO user_id;

ALTER TABLE board_members ADD CONSTRAINT board_members_user_id_fkey 
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Update the primary key constraint
ALTER TABLE board_members DROP CONSTRAINT IF EXISTS board_members_pkey;
ALTER TABLE board_members ADD CONSTRAINT board_members_pkey PRIMARY KEY (board_id, user_id);

-- Update foreign key references in workspace_invitations
ALTER TABLE workspace_invitations DROP CONSTRAINT IF EXISTS workspace_invitations_invited_by_fkey;
ALTER TABLE workspace_invitations ADD COLUMN IF NOT EXISTS invited_by_new UUID;

UPDATE workspace_invitations 
SET invited_by_new = u.id
FROM users u
WHERE workspace_invitations.invited_by = u.id::text;

ALTER TABLE workspace_invitations DROP COLUMN invited_by;
ALTER TABLE workspace_invitations RENAME COLUMN invited_by_new TO invited_by;

ALTER TABLE workspace_invitations ADD CONSTRAINT workspace_invitations_invited_by_fkey 
  FOREIGN KEY (invited_by) REFERENCES users(id);

-- Update foreign key references in board_invitations
ALTER TABLE board_invitations DROP CONSTRAINT IF EXISTS board_invitations_invited_by_fkey;
ALTER TABLE board_invitations ADD COLUMN IF NOT EXISTS invited_by_new UUID;

UPDATE board_invitations 
SET invited_by_new = u.id
FROM users u
WHERE board_invitations.invited_by = u.id::text;

ALTER TABLE board_invitations DROP COLUMN invited_by;
ALTER TABLE board_invitations RENAME COLUMN invited_by_new TO invited_by;

ALTER TABLE board_invitations ADD CONSTRAINT board_invitations_invited_by_fkey 
  FOREIGN KEY (invited_by) REFERENCES users(id);

-- Update foreign key references in cards
ALTER TABLE cards DROP CONSTRAINT IF EXISTS cards_created_by_fkey;
ALTER TABLE cards ADD COLUMN IF NOT EXISTS created_by_new UUID;

UPDATE cards 
SET created_by_new = u.id
FROM users u
WHERE cards.created_by = u.id::text;

ALTER TABLE cards DROP COLUMN created_by;
ALTER TABLE cards RENAME COLUMN created_by_new TO created_by;

ALTER TABLE cards ADD CONSTRAINT cards_created_by_fkey 
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE;

-- Update foreign key references in card_members
ALTER TABLE card_members DROP CONSTRAINT IF EXISTS card_members_user_id_fkey;
ALTER TABLE card_members ADD COLUMN IF NOT EXISTS user_id_new UUID;

UPDATE card_members 
SET user_id_new = u.id
FROM users u
WHERE card_members.user_id = u.id::text;

ALTER TABLE card_members DROP COLUMN user_id;
ALTER TABLE card_members RENAME COLUMN user_id_new TO user_id;

ALTER TABLE card_members ADD CONSTRAINT card_members_user_id_fkey 
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Update foreign key references in comments
ALTER TABLE comments DROP CONSTRAINT IF EXISTS comments_author_id_fkey;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS author_id_new UUID;

UPDATE comments 
SET author_id_new = u.id
FROM users u
WHERE comments.author_id = u.id::text;

ALTER TABLE comments DROP COLUMN author_id;
ALTER TABLE comments RENAME COLUMN author_id_new TO author_id;

ALTER TABLE comments ADD CONSTRAINT comments_author_id_fkey 
  FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE;

-- Update foreign key references in attachments
ALTER TABLE attachments DROP CONSTRAINT IF EXISTS attachments_uploader_id_fkey;
ALTER TABLE attachments ADD COLUMN IF NOT EXISTS uploader_id_new UUID;

UPDATE attachments 
SET uploader_id_new = u.id
FROM users u
WHERE attachments.uploader_id = u.id::text;

ALTER TABLE attachments DROP COLUMN uploader_id;
ALTER TABLE attachments RENAME COLUMN uploader_id_new TO uploader_id;

ALTER TABLE attachments ADD CONSTRAINT attachments_uploader_id_fkey 
  FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE;

-- Update foreign key references in activities
ALTER TABLE activities DROP CONSTRAINT IF EXISTS activities_user_id_fkey;
ALTER TABLE activities ADD COLUMN IF NOT EXISTS user_id_new UUID;

UPDATE activities 
SET user_id_new = u.id
FROM users u
WHERE activities.user_id = u.id::text;

ALTER TABLE activities DROP COLUMN user_id;
ALTER TABLE activities RENAME COLUMN user_id_new TO user_id;

ALTER TABLE activities ADD CONSTRAINT activities_user_id_fkey 
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Update indexes for the new user_id columns
DROP INDEX IF EXISTS idx_board_members_user_id;
DROP INDEX IF EXISTS idx_comments_author_id;
DROP INDEX IF EXISTS idx_attachments_uploader_id;

CREATE INDEX IF NOT EXISTS idx_board_members_user_id ON board_members(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments(author_id);
CREATE INDEX IF NOT EXISTS idx_attachments_uploader_id ON attachments(uploader_id);

-- Update indexes for workspace_members
DROP INDEX IF EXISTS idx_workspace_members_user_workspace;
CREATE INDEX IF NOT EXISTS idx_workspace_members_user_workspace ON workspace_members(user_id, workspace_id);

-- Update indexes for activities
DROP INDEX IF EXISTS idx_activities_user_created;
CREATE INDEX IF NOT EXISTS idx_activities_user_created ON activities(workspace_id, user_id, created_at);

-- Update indexes for card_members
DROP INDEX IF EXISTS idx_card_members_user_id;
CREATE INDEX IF NOT EXISTS idx_card_members_user_id ON card_members(user_id);

-- Update indexes for boards
DROP INDEX IF EXISTS idx_boards_owner_id;
CREATE INDEX IF NOT EXISTS idx_boards_owner_id ON boards(owner_id);

-- Update indexes for workspace_members
DROP INDEX IF EXISTS idx_workspace_members_user_workspace;
CREATE INDEX IF NOT EXISTS idx_workspace_members_user_workspace ON workspace_members(user_id, workspace_id);

-- Update indexes for board_members
DROP INDEX IF EXISTS idx_board_members_user_board;
CREATE INDEX IF NOT EXISTS idx_board_members_user_board ON board_members(user_id, board_id);
