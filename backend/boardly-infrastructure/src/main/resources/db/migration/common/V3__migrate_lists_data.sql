-- =====================================================
-- Boardly Lists Migration V3
-- =====================================================

-- Migrate data from board_lists to lists table
INSERT INTO lists (id, workspace_id, board_id, title, position, color, created_at, updated_at)
SELECT 
  gen_random_uuid(),
  b.workspace_id,
  bl.board_id,
  bl.title,
  bl.position,
  bl.color,
  bl.created_at,
  bl.updated_at
FROM board_lists bl
JOIN boards b ON bl.board_id = b.board_id
WHERE b.workspace_id IS NOT NULL
ON CONFLICT (workspace_id, board_id, position) DO NOTHING;

-- Update cards to reference the new lists table
-- First, create a temporary mapping table
CREATE TEMP TABLE list_mapping AS
SELECT 
  bl.list_id as old_list_id,
  l.id as new_list_id,
  l.workspace_id
FROM board_lists bl
JOIN lists l ON bl.board_id = l.board_id AND bl.position = l.position
JOIN boards b ON bl.board_id = b.board_id
WHERE b.workspace_id IS NOT NULL;

-- Update cards to use new list_id
UPDATE cards 
SET list_id = lm.new_list_id::VARCHAR(50)
FROM list_mapping lm
WHERE cards.list_id = lm.old_list_id;

-- Update card_labels to reference new list_id through cards
-- (This is handled by the workspace_id update in V2)

-- Update comments to reference new list_id through cards  
-- (This is handled by the workspace_id update in V2)

-- Update user_activity to reference new list_id through cards
-- (This is handled by the workspace_id update in V2)

-- Drop the temporary mapping table
DROP TABLE list_mapping;

-- Add NOT NULL constraint to workspace_id columns after data migration
ALTER TABLE boards ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE cards ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE labels ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE comments ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE card_labels ALTER COLUMN workspace_id SET NOT NULL;

-- Create composite primary key for card_labels with workspace_id
ALTER TABLE card_labels DROP CONSTRAINT IF EXISTS card_labels_pkey;
ALTER TABLE card_labels ADD PRIMARY KEY (workspace_id, card_id, label_id);

-- Add workspace_id to activities table for existing data
UPDATE activities 
SET workspace_id = b.workspace_id
FROM boards b
WHERE activities.workspace_id IS NULL 
  AND activities.board_id = b.board_id;

ALTER TABLE activities ALTER COLUMN workspace_id SET NOT NULL;

-- Add workspace_id to workspace_members for existing board owners
INSERT INTO workspace_members (workspace_id, user_id, role, invite_status, invited_at, joined_at)
SELECT DISTINCT
  w.id,
  w.owner_user_id,
  'OWNER'::workspace_role,
  'ACCEPTED'::invite_status_type,
  w.created_at,
  w.created_at
FROM workspaces w
WHERE w.owner_user_id IS NOT NULL
ON CONFLICT (workspace_id, user_id) DO NOTHING;

-- Add workspace_id to board_members for existing members
INSERT INTO board_members (board_id, user_id, role, is_active, created_at, updated_at, version, role_new, invite_status, invited_at, joined_at)
SELECT DISTINCT
  bm.board_id,
  bm.user_id,
  bm.role,
  bm.is_active,
  bm.created_at,
  bm.updated_at,
  bm.version,
  CASE 
    WHEN bm.role = 'ADMIN' THEN 'BOARD_ADMIN'::board_role
    WHEN bm.role = 'EDITOR' THEN 'BOARD_EDITOR'::board_role
    ELSE 'BOARD_VIEWER'::board_role
  END,
  'ACCEPTED'::invite_status_type,
  bm.created_at,
  bm.created_at
FROM board_members bm
JOIN boards b ON bm.board_id = b.board_id
WHERE b.workspace_id IS NOT NULL
ON CONFLICT (board_id, user_id) DO NOTHING;
