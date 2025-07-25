-- =====================================================
-- Boardly Database Schema
-- =====================================================

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) NOT NULL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- 보드 테이블
CREATE TABLE IF NOT EXISTS boards (
    board_id VARCHAR(50) NOT NULL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    owner_id VARCHAR(50) NOT NULL,
    is_starred BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

-- 보드 멤버 테이블
CREATE TABLE IF NOT EXISTS board_members (
    member_id VARCHAR(50) NOT NULL PRIMARY KEY,
    board_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (board_id) REFERENCES boards(board_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 보드 리스트 테이블
CREATE TABLE IF NOT EXISTS board_lists (
    list_id VARCHAR(50) NOT NULL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    position INTEGER NOT NULL,
    color VARCHAR(7) NOT NULL,
    board_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (board_id) REFERENCES boards(board_id)
);

-- 카드 테이블
CREATE TABLE IF NOT EXISTS cards (
    card_id VARCHAR(50) NOT NULL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    position INTEGER NOT NULL,
    list_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (list_id) REFERENCES board_lists(list_id)
);

-- 사용자 활동 테이블
CREATE TABLE IF NOT EXISTS user_activity (
    activity_id VARCHAR(50) NOT NULL PRIMARY KEY,
    actor_id VARCHAR(50) NOT NULL,
    board_id VARCHAR(50),
    list_id VARCHAR(50),
    card_id VARCHAR(50),
    activity_type VARCHAR(50) NOT NULL,
    actor_first_name VARCHAR(50) NOT NULL,
    actor_last_name VARCHAR(50) NOT NULL,
    actor_profile_image_url VARCHAR(500),
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (actor_id) REFERENCES users(user_id),
    FOREIGN KEY (board_id) REFERENCES boards(board_id),
    FOREIGN KEY (list_id) REFERENCES board_lists(list_id),
    FOREIGN KEY (card_id) REFERENCES cards(card_id)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_boards_owner_id ON boards(owner_id);
CREATE INDEX IF NOT EXISTS idx_board_members_board_id ON board_members(board_id);
CREATE INDEX IF NOT EXISTS idx_board_members_user_id ON board_members(user_id);
CREATE INDEX IF NOT EXISTS idx_board_lists_board_id ON board_lists(board_id);
CREATE INDEX IF NOT EXISTS idx_board_lists_position ON board_lists(position);
CREATE INDEX IF NOT EXISTS idx_cards_list_id ON cards(list_id);
CREATE INDEX IF NOT EXISTS idx_cards_position ON cards(position);
CREATE INDEX IF NOT EXISTS idx_activity_board_id ON user_activity(board_id);
CREATE INDEX IF NOT EXISTS idx_activity_actor_id ON user_activity(actor_id);
CREATE INDEX IF NOT EXISTS idx_activity_timestamp ON user_activity(created_at);
CREATE INDEX IF NOT EXISTS idx_activity_board_timestamp ON user_activity(board_id, created_at); 