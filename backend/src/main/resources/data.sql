-- =====================================================
-- Boardly Dev Environment Test Data
-- =====================================================

-- 테스트 사용자 데이터
-- 이메일: test@example.com, 비밀번호: Password1! (BCrypt 해시)
INSERT INTO users (user_id, email, hashed_password, first_name, last_name, is_active, created_at, updated_at, version) 
VALUES (
    'user_test123',
    'test@example.com',
    '{noop}Password1!',
    '테스트',
    '사용자',
    true,
    TIMESTAMP '2024-01-01 00:00:00',
    TIMESTAMP '2024-01-01 00:00:00',
    0
);

-- 테스트 보드 데이터
INSERT INTO boards (board_id, title, description, is_archived, owner_id, is_starred, created_at, updated_at, version) 
VALUES 
    ('board_proj1', '프로젝트 관리', '팀 프로젝트 관리를 위한 보드입니다.', false, 'user_test123', true, TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('board_todo', '개인 할일', '개인적인 할일과 목표를 관리하는 보드입니다.', false, 'user_test123', false, TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('board_archive', '아카이브 보드', '완료된 프로젝트들을 보관하는 보드입니다.', true, 'user_test123', false, TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0);

-- 테스트 보드 리스트 데이터 (프로젝트 관리 보드)
INSERT INTO board_lists (list_id, title, description, position, color, board_id, created_at, updated_at, version) 
VALUES 
    ('list_todo', '할 일', '아직 시작하지 않은 작업들', 0, '#FF6B6B', 'board_proj1', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('list_progress', '진행 중', '현재 진행 중인 작업들', 1, '#4ECDC4', 'board_proj1', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('list_review', '검토 중', '검토가 필요한 작업들', 2, '#45B7D1', 'board_proj1', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('list_done', '완료', '완료된 작업들', 3, '#96CEB4', 'board_proj1', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0);

-- 테스트 보드 리스트 데이터 (개인 할일 보드)
INSERT INTO board_lists (list_id, title, description, position, color, board_id, created_at, updated_at, version) 
VALUES 
    ('list_personal_todo', '할 일', '개인적으로 해야 할 일들', 0, '#FF9FF3', 'board_todo', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('list_personal_progress', '진행 중', '진행 중인 개인 일정들', 1, '#54A0FF', 'board_todo', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('list_personal_done', '완료', '완료된 개인 일정들', 2, '#5F27CD', 'board_todo', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0);

-- 테스트 카드 데이터 (프로젝트 관리 보드 - 할 일 리스트)
INSERT INTO cards (card_id, title, description, position, list_id, created_at, updated_at, version) 
VALUES 
    ('card_ui_design', 'UI 디자인 완성', '메인 페이지와 대시보드 UI 디자인을 완성해야 합니다.', 0, 'list_todo', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('card_api_dev', 'API 개발', '사용자 인증 및 보드 관리 API를 개발합니다.', 1, 'list_todo', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('card_testing', '테스트 작성', '단위 테스트와 통합 테스트를 작성합니다.', 2, 'list_todo', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0);

-- 테스트 카드 데이터 (프로젝트 관리 보드 - 진행 중 리스트)
INSERT INTO cards (card_id, title, description, position, list_id, created_at, updated_at, version) 
VALUES 
    ('card_db_design', '데이터베이스 설계', 'ERD 설계 및 테이블 구조를 정의합니다.', 0, 'list_progress', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('card_frontend', '프론트엔드 개발', 'React 컴포넌트 개발 및 상태 관리 구현', 1, 'list_progress', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0);

-- 테스트 카드 데이터 (프로젝트 관리 보드 - 검토 중 리스트)
INSERT INTO cards (card_id, title, description, position, list_id, created_at, updated_at, version) 
VALUES 
    ('card_requirements', '요구사항 분석', '프로젝트 요구사항을 분석하고 문서화합니다.', 0, 'list_review', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0);

-- 테스트 카드 데이터 (프로젝트 관리 보드 - 완료 리스트)
INSERT INTO cards (card_id, title, description, position, list_id, created_at, updated_at, version) 
VALUES 
    ('card_project_setup', '프로젝트 설정', 'Git 저장소 설정 및 개발 환경 구성', 0, 'list_done', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('card_planning', '프로젝트 기획', '프로젝트 일정 및 역할 분담 계획 수립', 1, 'list_done', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0);

-- 테스트 카드 데이터 (개인 할일 보드)
INSERT INTO cards (card_id, title, description, position, list_id, created_at, updated_at, version) 
VALUES 
    ('card_exercise', '운동하기', '30분 조깅 및 스트레칭', 0, 'list_personal_todo', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('card_reading', '책 읽기', '월 1권 목표 - 현재 읽는 책: 클린 코드', 1, 'list_personal_todo', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('card_study', '온라인 강의', 'React 고급 과정 수강 중', 0, 'list_personal_progress', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('card_cleaning', '방 정리', '주말 방 청소 완료', 0, 'list_personal_done', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0);

-- 테스트 활동 데이터 (사용자 활동 기록)
INSERT INTO user_activity (activity_id, actor_id, board_id, list_id, card_id, activity_type, actor_first_name, actor_last_name, actor_profile_image_url, payload, created_at) 
VALUES 
    ('activity_1', 'user_test123', 'board_proj1', NULL, NULL, 'BOARD_CREATE', '테스트', '사용자', 'https://placehold.co/40x40/0284C7/FFFFFF?text=테', '{"boardName": "프로젝트 관리"}', TIMESTAMP '2024-01-01 00:00:00'),
    ('activity_2', 'user_test123', 'board_proj1', 'list_todo', NULL, 'LIST_CREATE', '테스트', '사용자', 'https://placehold.co/40x40/0284C7/FFFFFF?text=테', '{"listName": "할 일", "listColor": "#FF6B6B"}', TIMESTAMP '2024-01-01 00:00:00'),
    ('activity_3', 'user_test123', 'board_proj1', 'list_todo', 'card_ui_design', 'CARD_CREATE', '테스트', '사용자', 'https://placehold.co/40x40/0284C7/FFFFFF?text=테', '{"listName": "할 일", "cardTitle": "UI 디자인 완성"}', TIMESTAMP '2024-01-01 00:00:00'),
    ('activity_4', 'user_test123', 'board_todo', NULL, NULL, 'BOARD_CREATE', '테스트', '사용자', 'https://placehold.co/40x40/0284C7/FFFFFF?text=테', '{"boardName": "개인 할일"}', TIMESTAMP '2024-01-01 00:00:00');

-- 테스트 보드 멤버 데이터 (보드 소유자)
INSERT INTO board_members (member_id, board_id, user_id, role, is_active, created_at, updated_at, version) 
VALUES 
    ('member_1', 'board_proj1', 'user_test123', 'OWNER', true, TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('member_2', 'board_todo', 'user_test123', 'OWNER', true, TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    ('member_3', 'board_archive', 'user_test123', 'OWNER', true, TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0); 