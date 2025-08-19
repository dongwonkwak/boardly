-- =====================================================
-- Boardly Database - H2 테스트 데이터 SQL (MODE=PostgreSQL)
-- 멀티테넌시 및 권한/초대 스키마 반영
-- =====================================================

-- 기존 데이터 정리 (FK 순서 고려)
DELETE FROM activities;
DELETE FROM comments;
DELETE FROM card_labels;
DELETE FROM card_members;
DELETE FROM cards;
DELETE FROM labels;
DELETE FROM board_lists;
DELETE FROM lists;
DELETE FROM board_members;
DELETE FROM board_invitations;
DELETE FROM boards;
DELETE FROM workspace_members;
DELETE FROM workspace_invitations;
DELETE FROM workspaces;
DELETE FROM users;

-- =====================================================
-- 1. 사용자
-- =====================================================
INSERT INTO users (user_id, email, hashed_password, first_name, last_name, is_active, created_at, updated_at, version) VALUES
('user-1', 'test@example.com', '{noop}Password1!', 'John', 'Doe', TRUE, NOW(), NOW(), 0),
('user-2', 'jane.smith@example.com', '{noop}Password1!', '제인', '스미스', TRUE, NOW(), NOW(), 0),
('user-3', 'mike.wilson@example.com', '{noop}Password1!', 'Mike', 'Wilson', TRUE, NOW(), NOW(), 0),
('user-4', 'sarah.jones@example.com', '{noop}Password1!', 'Sarah', 'Jones', TRUE, NOW(), NOW(), 0),
('user-5', 'david.brown@example.com', '{noop}Password1!', 'David', 'Brown', TRUE, NOW(), NOW(), 0);

-- =====================================================
-- 2. 워크스페이스
-- =====================================================
-- 고정 UUID로 재현성 확보
INSERT INTO workspaces (id, name, description, type, owner_user_id, created_at, updated_at) VALUES
('11111111-1111-1111-1111-111111111111', '회사 워크스페이스', '웹/마케팅 팀 공동 워크스페이스', 'DEFAULT', 'user-1', NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', '모바일 워크스페이스', '모바일 앱 팀 전용 워크스페이스', 'DEFAULT', 'user-2', NOW(), NOW());

-- =====================================================
-- 3. 보드 (workspace_id 포함)
-- =====================================================
INSERT INTO boards (board_id, title, description, is_archived, owner_id, is_starred, created_at, updated_at, version, workspace_id) VALUES
('board-1', '웹 개발 프로젝트', '회사 웹사이트 리뉴얼 프로젝트', FALSE, 'user-1', TRUE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('board-2', '모바일 앱 개발', '신규 모바일 애플리케이션 개발', FALSE, 'user-2', FALSE, NOW(), NOW(), 0, '22222222-2222-2222-2222-222222222222'),
('board-3', '마케팅 캠페인', '2024년 Q4 마케팅 캠페인 기획', FALSE, 'user-1', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('board-4', '인사 관리', 'HR 관련 업무 관리', FALSE, 'user-3', TRUE, NOW(), NOW(), 0, '22222222-2222-2222-2222-222222222222');

-- =====================================================
-- 4. 보드 멤버 (초대 컬럼은 기본값 사용)
-- =====================================================
INSERT INTO board_members (member_id, board_id, user_id, role, is_active, created_at, updated_at, version) VALUES
('member-1', 'board-1', 'user-1', 'OWNER', TRUE, NOW(), NOW(), 0),
('member-2', 'board-1', 'user-2', 'MEMBER', TRUE, NOW(), NOW(), 0),
('member-3', 'board-1', 'user-3', 'MEMBER', TRUE, NOW(), NOW(), 0),
('member-4', 'board-2', 'user-2', 'OWNER', TRUE, NOW(), NOW(), 0),
('member-5', 'board-2', 'user-4', 'MEMBER', TRUE, NOW(), NOW(), 0),
('member-6', 'board-3', 'user-1', 'OWNER', TRUE, NOW(), NOW(), 0),
('member-7', 'board-3', 'user-5', 'MEMBER', TRUE, NOW(), NOW(), 0),
('member-8', 'board-4', 'user-3', 'OWNER', TRUE, NOW(), NOW(), 0);

-- =====================================================
-- 5. 보드 리스트 (기존 board_lists 유지) + 신규 lists 채우기
-- =====================================================
INSERT INTO board_lists (list_id, title, description, position, color, board_id, created_at, updated_at, version) VALUES
('list-1', '백로그', '해야 할 일 목록', 1, '#FF6B6B', 'board-1', NOW(), NOW(), 0),
('list-2', '진행 중', '현재 작업 중인 항목들', 2, '#4ECDC4', 'board-1', NOW(), NOW(), 0),
('list-3', '검토 중', '검토가 필요한 항목들', 3, '#45B7D1', 'board-1', NOW(), NOW(), 0),
('list-4', '완료', '완료된 작업들', 4, '#96CEB4', 'board-1', NOW(), NOW(), 0),
('list-5', '요구사항 분석', '앱 요구사항 정리', 1, '#FFEAA7', 'board-2', NOW(), NOW(), 0),
('list-6', '설계', '앱 설계 및 아키텍처', 2, '#DDA0DD', 'board-2', NOW(), NOW(), 0),
('list-7', '개발', '실제 개발 작업', 3, '#98D8C8', 'board-2', NOW(), NOW(), 0),
('list-8', '테스트', '테스트 및 QA', 4, '#F06292', 'board-2', NOW(), NOW(), 0),
('list-9', '아이디어', '캠페인 아이디어 수집', 1, '#FFD93D', 'board-3', NOW(), NOW(), 0),
('list-10', '기획', '캠페인 기획 및 전략', 2, '#6BCF7F', 'board-3', NOW(), NOW(), 0),
('list-11', '실행', '캠페인 실행', 3, '#4D96FF', 'board-3', NOW(), NOW(), 0);

-- 신규 lists 테이블에도 동일 데이터 반영 (H2: UUID는 RANDOM_UUID())
INSERT INTO lists (id, workspace_id, board_id, title, position, color, created_at, updated_at) VALUES
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-1', '백로그', 1, '#FF6B6B', NOW(), NOW()),
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-1', '진행 중', 2, '#4ECDC4', NOW(), NOW()),
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-1', '검토 중', 3, '#45B7D1', NOW(), NOW()),
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-1', '완료', 4, '#96CEB4', NOW(), NOW()),
(RANDOM_UUID(), '22222222-2222-2222-2222-222222222222', 'board-2', '요구사항 분석', 1, '#FFEAA7', NOW(), NOW()),
(RANDOM_UUID(), '22222222-2222-2222-2222-222222222222', 'board-2', '설계', 2, '#DDA0DD', NOW(), NOW()),
(RANDOM_UUID(), '22222222-2222-2222-2222-222222222222', 'board-2', '개발', 3, '#98D8C8', NOW(), NOW()),
(RANDOM_UUID(), '22222222-2222-2222-2222-222222222222', 'board-2', '테스트', 4, '#F06292', NOW(), NOW()),
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-3', '아이디어', 1, '#FFD93D', NOW(), NOW()),
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-3', '기획', 2, '#6BCF7F', NOW(), NOW()),
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-3', '실행', 3, '#4D96FF', NOW(), NOW());

-- =====================================================
-- 6. 라벨 (workspace_id 포함)
-- =====================================================
INSERT INTO labels (label_id, board_id, name, color, created_at, updated_at, version, workspace_id) VALUES
('label-1', 'board-1', '긴급', '#FF4757', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('label-2', 'board-1', '버그', '#FF6348', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('label-3', 'board-1', '기능개발', '#1E90FF', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('label-4', 'board-1', 'UI/UX', '#9C88FF', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('label-11', 'board-1', 'iOS', '#007AFF', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('label-5', 'board-2', 'iOS', '#007AFF', NOW(), NOW(), 0, '22222222-2222-2222-2222-222222222222'),
('label-6', 'board-2', 'Android', '#34C759', NOW(), NOW(), 0, '22222222-2222-2222-2222-222222222222'),
('label-7', 'board-2', 'API', '#FF9500', NOW(), NOW(), 0, '22222222-2222-2222-2222-222222222222'),
('label-8', 'board-3', '소셜미디어', '#E91E63', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('label-9', 'board-3', '이메일', '#2196F3', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('label-10', 'board-3', '광고', '#FF5722', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111');

-- =====================================================
-- 7. 카드 (workspace_id 포함)
-- =====================================================
INSERT INTO cards (card_id, title, description, position, due_date, priority, archived, list_id, created_by, created_at, updated_at, version, workspace_id) VALUES
('card-1', '메인 페이지 디자인', '사용자 인터페이스 디자인 작업', 1, DATEADD('DAY', 7, NOW()), NULL, false, 'list-1', 'user-1', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-2', '데이터베이스 설계', 'ERD 작성 및 테이블 설계', 2, DATEADD('DAY', 5, NOW()), NULL, false, 'list-1', 'user-1', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-3', 'API 개발', 'RESTful API 엔드포인트 구현', 3, DATEADD('DAY', 10, NOW()), NULL, false, 'list-1', 'user-1', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-4', '단위 테스트 작성', '핵심 기능에 대한 단위 테스트', 1, DATEADD('DAY', 3, NOW()), NULL, false, 'list-2', 'user-2', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-5', '보안 테스트', '인증 및 권한 검증', 2, DATEADD('DAY', 5, NOW()), 'high', false, 'list-2', 'user-3', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-6', '성능 최적화', '데이터베이스 쿼리 최적화', 3, DATEADD('DAY', 8, NOW()), 'medium', false, 'list-2', 'user-2', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-7', '사용자 매뉴얼 작성', '시스템 사용법 가이드', 1, DATEADD('DAY', 2, NOW()), 'low', false, 'list-3', 'user-2', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-8', '배포 준비', '프로덕션 환경 배포 준비', 2, DATEADD('DAY', 1, NOW()), 'urgent', false, 'list-3', 'user-4', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-9', '모니터링 설정', '시스템 모니터링 도구 설정', 3, DATEADD('DAY', 4, NOW()), NULL, false, 'list-3', 'user-2', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-10', 'Android 개발', '모바일 앱 개발', 1, DATEADD('DAY', 15, NOW()), NULL, false, 'list-4', 'user-4', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-11', 'SNS 콘텐츠 기획', '소셜미디어 마케팅 콘텐츠', 1, DATEADD('DAY', 3, NOW()), NULL, false, 'list-5', 'user-5', NOW(), NOW(), 0, '22222222-2222-2222-2222-222222222222'),
('card-12', '브랜드 가이드라인', '브랜드 아이덴티티 가이드', 2, DATEADD('DAY', 7, NOW()), NULL, false, 'list-5', 'user-1', NOW(), NOW(), 0, '22222222-2222-2222-2222-222222222222'),
('card-14', '크리스마스 프로모션 아이디어', '연말 시즌 맞춤 프로모션 기획', 1, DATEADD('DAY', 5, NOW()), NULL, false, 'list-9', 'user-1', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-15', '인플루언서 협업 제안', '인기 인플루언서와의 협업 방안 검토', 2, DATEADD('DAY', 7, NOW()), NULL, false, 'list-9', 'user-5', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-16', '소셜미디어 캠페인 전략', '페이스북, 인스타그램 캠페인 기획', 1, DATEADD('DAY', 10, NOW()), NULL, false, 'list-10', 'user-1', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-17', '이메일 마케팅 시리즈', '고객 세그먼트별 이메일 캠페인 설계', 2, DATEADD('DAY', 8, NOW()), NULL, false, 'list-10', 'user-5', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-18', '온라인 광고 캠페인', '구글 애즈 및 페이스북 광고 집행', 1, DATEADD('DAY', 12, NOW()), NULL, false, 'list-11', 'user-1', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('card-19', '오프라인 이벤트 기획', '팝업 스토어 및 체험존 운영', 2, DATEADD('DAY', 15, NOW()), NULL, false, 'list-11', 'user-5', NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111');

-- =====================================================
-- 8. 카드 담당자
-- =====================================================
INSERT INTO card_members (card_id, user_id, assigned_at) VALUES
('card-1', 'user-2', NOW()),
('card-1', 'user-3', NOW()),
('card-2', 'user-1', NOW()),
('card-3', 'user-1', NOW()),
('card-4', 'user-2', NOW()),
('card-5', 'user-3', NOW()),
('card-7', 'user-2', NOW()),
('card-8', 'user-4', NOW()),
('card-9', 'user-2', NOW()),
('card-10', 'user-4', NOW()),
('card-11', 'user-5', NOW()),
('card-12', 'user-1', NOW()),
('card-14', 'user-1', NOW()),
('card-15', 'user-5', NOW()),
('card-16', 'user-1', NOW()),
('card-17', 'user-5', NOW()),
('card-18', 'user-1', NOW()),
('card-19', 'user-5', NOW());

-- =====================================================
-- 9. 카드-라벨 연결 (workspace_id 포함)
-- =====================================================
INSERT INTO card_labels (card_id, label_id, applied_at, workspace_id) VALUES
('card-1', 'label-4', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-1', 'label-3', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-2', 'label-3', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-3', 'label-3', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-4', 'label-3', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-5', 'label-1', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-5', 'label-2', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-6', 'label-3', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-7', 'label-4', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-7', 'label-11', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-8', 'label-1', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-9', 'label-3', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-9', 'label-11', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-10', 'label-3', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-11', 'label-7', NOW(), '22222222-2222-2222-2222-222222222222'),
('card-12', 'label-5', NOW(), '22222222-2222-2222-2222-222222222222'),
('card-14', 'label-8', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-15', 'label-8', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-16', 'label-8', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-17', 'label-9', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-18', 'label-10', NOW(), '11111111-1111-1111-1111-111111111111'),
('card-19', 'label-10', NOW(), '11111111-1111-1111-1111-111111111111');

-- =====================================================
-- 10. 댓글 (workspace_id 포함)
-- =====================================================
INSERT INTO comments (comment_id, card_id, author_id, content, edited, created_at, updated_at, version, workspace_id) VALUES
('comment-1', 'card-1', 'user-1', '디자인 초안을 검토해주세요. 메인 컬러는 어떤 것이 좋을까요?', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('comment-2', 'card-1', 'user-2', '파란색 계열이 좋을 것 같습니다. 브랜드 이미지와도 잘 맞을 것 같아요.', FALSE, DATEADD('HOUR', 1, NOW()), DATEADD('HOUR', 1, NOW()), 0, '11111111-1111-1111-1111-111111111111'),
('comment-3', 'card-2', 'user-3', 'OAuth 인증도 고려해보면 어떨까요?', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('comment-4', 'card-4', 'user-1', 'Swagger를 사용해서 API 문서를 자동 생성하면 좋겠습니다.', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('comment-5', 'card-4', 'user-2', '동의합니다. Swagger UI로 테스트도 쉽게 할 수 있겠네요.', FALSE, DATEADD('MINUTE', 30, NOW()), DATEADD('MINUTE', 30, NOW()), 0, '11111111-1111-1111-1111-111111111111'),
('comment-6', 'card-4', 'user-3', 'API 버전 관리도 고려해야 할 것 같습니다.', FALSE, DATEADD('HOUR', 2, NOW()), DATEADD('HOUR', 2, NOW()), 0, '11111111-1111-1111-1111-111111111111'),
('comment-7', 'card-5', 'user-1', 'OWASP Top 10 보안 취약점을 기준으로 체크해주세요.', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('comment-8', 'card-7', 'user-4', '사용자 경험을 중심으로 설계하면 좋겠습니다.', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('comment-9', 'card-7', 'user-2', '네, 사용성 테스트도 진행해보겠습니다.', FALSE, DATEADD('MINUTE', 45, NOW()), DATEADD('MINUTE', 45, NOW()), 0, '11111111-1111-1111-1111-111111111111'),
('comment-10', 'card-9', 'user-2', 'iOS 개발 가이드라인을 준수해서 개발하겠습니다.', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('comment-11', 'card-11', 'user-1', '타겟 고객층을 명확히 정의하고 콘텐츠를 기획해주세요.', FALSE, NOW(), NOW(), 0, '22222222-2222-2222-2222-222222222222'),
('comment-12', 'card-14', 'user-1', '크리스마스 시즌에 맞는 따뜻한 느낌의 프로모션이 좋을 것 같습니다.', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('comment-13', 'card-14', 'user-5', '할인율과 함께 선물 증정 이벤트도 고려해보면 어떨까요?', FALSE, DATEADD('HOUR', 2, NOW()), DATEADD('HOUR', 2, NOW()), 0, '11111111-1111-1111-1111-111111111111'),
('comment-14', 'card-16', 'user-1', '인스타그램 릴스와 페이스북 스토리 활용 방안을 구체적으로 기획해주세요.', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('comment-15', 'card-17', 'user-5', '신규 고객과 기존 고객을 구분해서 다른 메시지를 보내는 것이 좋겠습니다.', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111'),
('comment-16', 'card-18', 'user-1', '광고 예산 배분과 타겟팅 설정을 세밀하게 계획해주세요.', FALSE, NOW(), NOW(), 0, '11111111-1111-1111-1111-111111111111');

-- =====================================================
-- 11. 활동 로그 (신규 activities 사용)
-- =====================================================
INSERT INTO activities (id, workspace_id, board_id, user_id, type, payload_json, created_at) VALUES
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-1', 'user-1', 'BOARD_CREATE', '{"boardName":"웹 개발 프로젝트"}', DATEADD('DAY', -3, NOW())),
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-1', 'user-1', 'LIST_CREATE', '{"listName":"백로그"}', DATEADD('DAY', -3, NOW())),
(RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'board-1', 'user-2', 'CARD_ADD_COMMENT', '{"cardTitle":"메인 페이지 디자인"}', DATEADD('HOUR', -12, NOW())),
(RANDOM_UUID(), '22222222-2222-2222-2222-222222222222', 'board-2', 'user-2', 'BOARD_CREATE', '{"boardName":"모바일 앱 개발"}', DATEADD('DAY', -5, NOW()));

-- =====================================================
-- 12. 워크스페이스 멤버십
-- =====================================================
INSERT INTO workspace_members (workspace_id, user_id, role, invite_status, invited_by, invited_at, joined_at) VALUES
('11111111-1111-1111-1111-111111111111', 'user-1', 'OWNER', 'ACCEPTED', 'user-1', NOW(), NOW()),
('11111111-1111-1111-1111-111111111111', 'user-2', 'MEMBER', 'ACCEPTED', 'user-1', NOW(), NOW()),
('11111111-1111-1111-1111-111111111111', 'user-3', 'MEMBER', 'ACCEPTED', 'user-1', NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'user-2', 'OWNER', 'ACCEPTED', 'user-2', NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'user-4', 'MEMBER', 'ACCEPTED', 'user-2', NOW(), NOW());
