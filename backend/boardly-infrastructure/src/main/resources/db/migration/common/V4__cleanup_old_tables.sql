-- =====================================================
-- Boardly Schema Cleanup V4
-- =====================================================

-- Drop old indexes that are no longer needed
DROP INDEX IF EXISTS idx_board_lists_board_id;
DROP INDEX IF EXISTS idx_board_lists_position;

-- Drop the old board_lists table (data has been migrated to lists)
DROP TABLE IF EXISTS board_lists;

-- Drop the old user_activity table (data has been migrated to activities)
DROP TABLE IF EXISTS user_activity;

-- Update foreign key references in cards table
-- Change list_id from VARCHAR(50) to UUID to match new lists table
ALTER TABLE cards DROP CONSTRAINT IF EXISTS cards_list_id_fkey;

-- Create a temporary column for the new UUID list_id
ALTER TABLE cards ADD COLUMN IF NOT EXISTS list_id_new UUID;

-- Update the new column with the UUID from lists table
UPDATE cards 
SET list_id_new = l.id
FROM lists l
WHERE cards.list_id = l.board_id || '_' || l.position::text;

-- Drop the old VARCHAR column and rename the new one
ALTER TABLE cards DROP COLUMN list_id;
ALTER TABLE cards RENAME COLUMN list_id_new TO list_id;

-- Add the foreign key constraint back
ALTER TABLE cards ADD CONSTRAINT cards_list_id_fkey 
  FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE;

-- Update indexes for the new list_id column
DROP INDEX IF EXISTS idx_cards_list_id;
DROP INDEX IF EXISTS idx_cards_position;
CREATE INDEX IF NOT EXISTS idx_cards_list_id ON cards(list_id);
CREATE INDEX IF NOT EXISTS idx_cards_position ON cards(list_id, position);

-- Update the lists table to use UUID for board_id as well
ALTER TABLE lists DROP CONSTRAINT IF EXISTS lists_board_id_fkey;

-- Create a temporary column for the new UUID board_id
ALTER TABLE lists ADD COLUMN IF NOT EXISTS board_id_new UUID;

-- Update the new column with the UUID from boards table
UPDATE lists 
SET board_id_new = b.id
FROM boards b
WHERE lists.board_id = b.board_id;

-- Drop the old VARCHAR column and rename the new one
ALTER TABLE lists DROP COLUMN board_id;
ALTER TABLE lists RENAME COLUMN board_id_new TO board_id;

-- Add the foreign key constraint back
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

-- Update activities table to use UUID for board_id
ALTER TABLE activities DROP CONSTRAINT IF EXISTS activities_board_id_fkey;

-- Create a temporary column for the new UUID board_id
ALTER TABLE activities ADD COLUMN IF NOT EXISTS board_id_new UUID;

-- Update the new column with the UUID from boards table
UPDATE activities 
SET board_id_new = b.id
FROM boards b
WHERE activities.board_id = b.board_id;

-- Drop the old VARCHAR column and rename the new one
ALTER TABLE activities DROP COLUMN board_id;
ALTER TABLE activities RENAME COLUMN board_id_new TO board_id;

-- Add the foreign key constraint back
ALTER TABLE activities ADD CONSTRAINT activities_board_id_fkey 
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

-- Update indexes for the new board_id column
DROP INDEX IF EXISTS idx_activities_workspace_board;
CREATE INDEX IF NOT EXISTS idx_activities_workspace_board ON activities(workspace_id, board_id);

-- Update board_invitations table to use UUID for board_id
ALTER TABLE board_invitations DROP CONSTRAINT IF EXISTS board_invitations_board_id_fkey;

-- Create a temporary column for the new UUID board_id
ALTER TABLE board_invitations ADD COLUMN IF NOT EXISTS board_id_new UUID;

-- Update the new column with the UUID from boards table
UPDATE board_invitations 
SET board_id_new = b.id
FROM boards b
WHERE board_invitations.board_id = b.board_id;

-- Drop the old VARCHAR column and rename the new one
ALTER TABLE board_invitations DROP COLUMN board_id;
ALTER TABLE board_invitations RENAME COLUMN board_id_new TO board_id;

-- Add the foreign key constraint back
ALTER TABLE board_invitations ADD CONSTRAINT board_invitations_board_id_fkey 
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

-- Update the unique constraint
ALTER TABLE board_invitations DROP CONSTRAINT IF EXISTS board_invitations_board_id_email_status_key;
ALTER TABLE board_invitations ADD CONSTRAINT board_invitations_board_id_email_status_key 
  UNIQUE (board_id, email, status);

-- Update indexes for the new board_id column
DROP INDEX IF EXISTS idx_board_invitations_email_status;
CREATE INDEX IF NOT EXISTS idx_board_invitations_email_status ON board_invitations(email, status);

-- Update board_members table to use UUID for board_id
ALTER TABLE board_members DROP CONSTRAINT IF EXISTS board_members_board_id_fkey;

-- Create a temporary column for the new UUID board_id
ALTER TABLE board_members ADD COLUMN IF NOT EXISTS board_id_new UUID;

-- Update the new column with the UUID from boards table
UPDATE board_members 
SET board_id_new = b.id
FROM boards b
WHERE board_members.board_id = b.board_id;

-- Drop the old VARCHAR column and rename the new one
ALTER TABLE board_members DROP COLUMN board_id;
ALTER TABLE board_members RENAME COLUMN board_id_new TO board_id;

-- Add the foreign key constraint back
ALTER TABLE board_members ADD CONSTRAINT board_members_board_id_fkey 
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

-- Update the primary key constraint
ALTER TABLE board_members DROP CONSTRAINT IF EXISTS board_members_pkey;
ALTER TABLE board_members ADD CONSTRAINT board_members_pkey PRIMARY KEY (board_id, user_id);

-- Update indexes for the new board_id column
DROP INDEX IF EXISTS idx_board_members_user_board;
CREATE INDEX IF NOT EXISTS idx_board_members_user_board ON board_members(user_id, board_id);

-- Update labels table to use UUID for board_id
ALTER TABLE labels DROP CONSTRAINT IF EXISTS labels_board_id_fkey;

-- Create a temporary column for the new UUID board_id
ALTER TABLE labels ADD COLUMN IF NOT EXISTS board_id_new UUID;

-- Update the new column with the UUID from boards table
UPDATE labels 
SET board_id_new = b.id
FROM boards b
WHERE labels.board_id = b.board_id;

-- Drop the old VARCHAR column and rename the new one
ALTER TABLE labels DROP COLUMN board_id;
ALTER TABLE labels RENAME COLUMN board_id_new TO board_id;

-- Add the foreign key constraint back
ALTER TABLE labels ADD CONSTRAINT labels_board_id_fkey 
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

-- Update the unique constraint
ALTER TABLE labels DROP CONSTRAINT IF EXISTS labels_board_id_name_key;
ALTER TABLE labels ADD CONSTRAINT labels_workspace_id_name_key 
  UNIQUE (workspace_id, name);

-- Update indexes for the new board_id column
DROP INDEX IF EXISTS idx_labels_board_id;
DROP INDEX IF EXISTS idx_labels_name;
CREATE INDEX IF NOT EXISTS idx_labels_board_id ON labels(board_id);
CREATE INDEX IF NOT EXISTS idx_labels_workspace ON labels(workspace_id);

-- Update cards table to use UUID for card_id
ALTER TABLE cards DROP CONSTRAINT IF EXISTS cards_pkey;
ALTER TABLE cards ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();

-- Update the new column with the UUID from the old card_id
UPDATE cards 
SET id = card_id::UUID
WHERE card_id ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- Drop the old VARCHAR column and rename the new one
ALTER TABLE cards DROP COLUMN card_id;
ALTER TABLE cards RENAME COLUMN id TO card_id;

-- Add the primary key constraint back
ALTER TABLE cards ADD CONSTRAINT cards_pkey PRIMARY KEY (card_id);

-- Update foreign key references in card_labels
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_card_id_fkey;
ALTER TABLE card_labels ADD COLUMN IF NOT EXISTS card_id_new UUID;

UPDATE card_labels 
SET card_id_new = c.card_id
FROM cards c
WHERE card_labels.card_id = c.card_id::text;

ALTER TABLE card_labels DROP COLUMN card_id;
ALTER TABLE card_labels RENAME COLUMN card_id_new TO card_id;

ALTER TABLE card_labels ADD CONSTRAINT card_labels_card_id_fkey 
  FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE CASCADE;

-- Update foreign key references in comments
ALTER TABLE comments DROP CONSTRAINT IF EXISTS comments_card_id_fkey;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS card_id_new UUID;

UPDATE comments 
SET card_id_new = c.card_id
FROM cards c
WHERE comments.card_id = c.card_id::text;

ALTER TABLE comments DROP COLUMN card_id;
ALTER TABLE comments RENAME COLUMN card_id_new TO card_id;

ALTER TABLE comments ADD CONSTRAINT comments_card_id_fkey 
  FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE CASCADE;

-- Update foreign key references in card_members
ALTER TABLE card_members DROP CONSTRAINT IF EXISTS card_members_card_id_fkey;
ALTER TABLE card_members ADD COLUMN IF NOT EXISTS card_id_new UUID;

UPDATE card_members 
SET card_id_new = c.card_id
FROM cards c
WHERE card_members.card_id = c.card_id::text;

ALTER TABLE card_members DROP COLUMN card_id;
ALTER TABLE card_members RENAME COLUMN card_id_new TO card_id;

ALTER TABLE card_members ADD CONSTRAINT card_members_card_id_fkey 
  FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE CASCADE;

-- Update foreign key references in attachments
ALTER TABLE attachments DROP CONSTRAINT IF EXISTS attachments_card_id_fkey;
ALTER TABLE attachments ADD COLUMN IF NOT EXISTS card_id_new UUID;

UPDATE attachments 
SET card_id_new = c.card_id
FROM cards c
WHERE attachments.card_id = c.card_id::text;

ALTER TABLE attachments DROP COLUMN card_id;
ALTER TABLE attachments RENAME COLUMN card_id_new TO card_id;

ALTER TABLE attachments ADD CONSTRAINT attachments_card_id_fkey 
  FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE CASCADE;

-- Update indexes for the new card_id columns
DROP INDEX IF EXISTS idx_card_labels_card_id;
DROP INDEX IF EXISTS idx_comments_card_id;
DROP INDEX IF EXISTS idx_card_members_card_id;
DROP INDEX IF EXISTS idx_attachments_card_id;

CREATE INDEX IF NOT EXISTS idx_card_labels_card_id ON card_labels(card_id);
CREATE INDEX IF NOT EXISTS idx_comments_card_id ON comments(card_id);
CREATE INDEX IF NOT EXISTS idx_card_members_card_id ON card_members(card_id);
CREATE INDEX IF NOT EXISTS idx_attachments_card_id ON attachments(card_id);

-- Update the composite primary key for card_labels
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_pkey;
ALTER TABLE card_labels ADD PRIMARY KEY (workspace_id, card_id, label_id);

-- Update the composite primary key for card_labels with new label_id
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_label_id_fkey;
ALTER TABLE card_labels ADD COLUMN IF NOT EXISTS label_id_new UUID;

UPDATE card_labels 
SET label_id_new = l.id
FROM labels l
WHERE card_labels.label_id = l.label_id;

ALTER TABLE card_labels DROP COLUMN label_id;
ALTER TABLE card_labels RENAME COLUMN label_id_new TO label_id;

ALTER TABLE card_labels ADD CONSTRAINT card_labels_label_id_fkey 
  FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE;

-- Update the composite primary key for card_labels
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_pkey;
ALTER TABLE card_labels ADD PRIMARY KEY (workspace_id, card_id, label_id);

-- Update indexes for the new label_id column
DROP INDEX IF EXISTS idx_card_labels_label_id;
CREATE INDEX IF NOT EXISTS idx_card_labels_label_id ON card_labels(label_id);

-- Update labels table to use UUID for label_id
ALTER TABLE labels DROP CONSTRAINT IF EXISTS labels_pkey;
ALTER TABLE labels ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();

UPDATE labels 
SET id = label_id::UUID
WHERE label_id ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

ALTER TABLE labels DROP COLUMN label_id;
ALTER TABLE labels RENAME COLUMN id TO id;

ALTER TABLE labels ADD CONSTRAINT labels_pkey PRIMARY KEY (id);

-- Update foreign key references in card_labels for label_id
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_label_id_fkey;
ALTER TABLE card_labels ADD COLUMN IF NOT EXISTS label_id_new UUID;

UPDATE card_labels 
SET label_id_new = l.id
FROM labels l
WHERE card_labels.label_id = l.id::text;

ALTER TABLE card_labels DROP COLUMN label_id;
ALTER TABLE card_labels RENAME COLUMN label_id_new TO label_id;

ALTER TABLE card_labels ADD CONSTRAINT card_labels_label_id_fkey 
  FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE;

-- Update the composite primary key for card_labels
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_pkey;
ALTER TABLE card_labels ADD PRIMARY KEY (workspace_id, card_id, label_id);

-- Update indexes for the new label_id column
DROP INDEX IF EXISTS idx_card_labels_label_id;
CREATE INDEX IF NOT EXISTS idx_card_labels_label_id ON card_labels(label_id);

-- Update comments table to use UUID for comment_id
ALTER TABLE comments DROP CONSTRAINT IF EXISTS comments_pkey;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();

UPDATE comments 
SET id = comment_id::UUID
WHERE comment_id ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

ALTER TABLE comments DROP COLUMN comment_id;
ALTER TABLE comments RENAME COLUMN id TO id;

ALTER TABLE comments ADD CONSTRAINT comments_pkey PRIMARY KEY (id);

-- Update attachments table to use UUID for attachment_id
ALTER TABLE attachments DROP CONSTRAINT IF EXISTS attachments_pkey;
ALTER TABLE attachments ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();

UPDATE attachments 
SET id = attachment_id::UUID
WHERE attachment_id ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

ALTER TABLE attachments DROP COLUMN attachment_id;
ALTER TABLE attachments RENAME COLUMN id TO id;

ALTER TABLE attachments ADD CONSTRAINT attachments_pkey PRIMARY KEY (id);

-- Update card_members table to use UUID for id
ALTER TABLE card_members DROP CONSTRAINT IF EXISTS card_members_pkey;
ALTER TABLE card_members ADD COLUMN IF NOT EXISTS id_new UUID DEFAULT gen_random_uuid();

UPDATE card_members 
SET id_new = id::UUID
WHERE id::text ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

ALTER TABLE card_members DROP COLUMN id;
ALTER TABLE card_members RENAME COLUMN id_new TO id;

ALTER TABLE card_members ADD CONSTRAINT card_members_pkey PRIMARY KEY (id);

-- Update card_labels table to use UUID for id
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_pkey;
ALTER TABLE card_labels ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();

ALTER TABLE card_labels ADD CONSTRAINT card_labels_pkey PRIMARY KEY (id);

-- Add unique constraint for card_labels
ALTER TABLE card_labels ADD CONSTRAINT card_labels_card_id_label_id_key 
  UNIQUE (card_id, label_id);

-- Update indexes for the new id columns
DROP INDEX IF EXISTS idx_comments_created_at;
DROP INDEX IF EXISTS idx_attachments_created_at;

CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at);
CREATE INDEX IF NOT EXISTS idx_attachments_created_at ON attachments(created_at);

-- Update the composite primary key for card_labels
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_pkey;
ALTER TABLE card_labels ADD PRIMARY KEY (workspace_id, card_id, label_id);

-- Update the unique constraint for card_labels
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_card_id_label_id_key;
ALTER TABLE card_labels ADD CONSTRAINT card_labels_workspace_id_card_id_label_id_key 
  UNIQUE (workspace_id, card_id, label_id);
