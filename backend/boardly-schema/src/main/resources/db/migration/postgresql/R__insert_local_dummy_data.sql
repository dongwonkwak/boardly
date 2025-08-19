-- =====================================================
-- Boardly Database - PostgreSQL 테스트 데이터 SQL
-- 멀티테넌시 및 권한/초대 스키마 반영
-- =====================================================

-- 기존 데이터 정리 (FK 순서 고려)
DELETE FROM activities;
DELETE FROM comments;
DELETE FROM card_labels;
DELETE FROM card_members;
DELETE FROM cards;
DELETE FROM labels;
DELETE FROM lists;
DELETE FROM board_members;
DELETE FROM boards;
DELETE FROM workspace_members;
DELETE FROM invitations;
DELETE FROM workspaces;
DELETE FROM users;

-- =====================================================
-- 1. 사용자
-- =====================================================
INSERT INTO users (id, email, first_name, last_name, password_hash, is_active, created_at, updated_at) VALUES
('user-1', 'test@example.com', 'John', 'Doe', '{noop}Password1!', TRUE, NOW(), NOW()),
('user-2', 'jane.smith@example.com', '제인', '스미스', '{noop}Password1!', TRUE, NOW(), NOW()),
('user-3', 'mike.wilson@example.com', 'Mike', 'Wilson', '{noop}Password1!', TRUE, NOW(), NOW()),
('user-4', 'sarah.jones@example.com', 'Sarah', 'Jones', '{noop}Password1!', TRUE, NOW(), NOW()),
('user-5', 'david.brown@example.com', 'David', 'Brown', '{noop}Password1!', TRUE, NOW(), NOW());

-- =====================================================
-- 2. 워크스페이스
-- =====================================================
INSERT INTO workspaces (id, name, description, type, owner_user_id, created_at, updated_at) VALUES
('11111111-1111-1111-1111-111111111111', '회사 워크스페이스', '웹/마케팅 팀 공동 워크스페이스', 'TEAM', 'user-1', NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', '모바일 워크스페이스', '모바일 앱 팀 전용 워크스페이스', 'TEAM', 'user-2', NOW(), NOW()),
('33333333-3333-3333-3333-333333333333', '개인 워크스페이스', '개인 프로젝트 관리', 'PERSONAL', 'user-3', NOW(), NOW());

-- =====================================================
-- 3. 워크스페이스 멤버십
-- =====================================================
INSERT INTO workspace_members (workspace_id, user_id, role, type, invited_by, invited_at, joined_at) VALUES
('11111111-1111-1111-1111-111111111111', 'user-1', 'OWNER', 'MEMBER', 'user-1', NOW(), NOW()),
('11111111-1111-1111-1111-111111111111', 'user-2', 'MEMBER', 'MEMBER', 'user-1', NOW(), NOW()),
('11111111-1111-1111-1111-111111111111', 'user-3', 'MEMBER', 'MEMBER', 'user-1', NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'user-2', 'OWNER', 'MEMBER', 'user-2', NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'user-4', 'MEMBER', 'MEMBER', 'user-2', NOW(), NOW()),
('33333333-3333-3333-3333-333333333333', 'user-3', 'OWNER', 'MEMBER', 'user-3', NOW(), NOW());

-- =====================================================
-- 4. 보드
-- =====================================================
INSERT INTO boards (id, workspace_id, title, description, is_public, is_starred, is_archived, created_at, updated_at) VALUES
('board-1', '11111111-1111-1111-1111-111111111111', '웹 개발 프로젝트', '회사 웹사이트 리뉴얼 프로젝트', TRUE, TRUE, FALSE, NOW(), NOW()),
('board-2', '22222222-2222-2222-2222-222222222222', '모바일 앱 개발', '신규 모바일 애플리케이션 개발', TRUE, FALSE, FALSE, NOW(), NOW()),
('board-3', '11111111-1111-1111-1111-111111111111', '마케팅 캠페인', '2024년 Q4 마케팅 캠페인 기획', TRUE, FALSE, FALSE, NOW(), NOW()),
('board-4', '33333333-3333-3333-3333-333333333333', '개인 프로젝트', '개인 학습 및 사이드 프로젝트', FALSE, TRUE, FALSE, NOW(), NOW());

-- =====================================================
-- 5. 보드 멤버
-- =====================================================
INSERT INTO board_members (board_id, user_id, role, invited_by, invited_at, joined_at, invitation_type) VALUES
('board-1', 'user-1', 'OWNER', 'user-1', NOW(), NOW(), 'INDIVIDUAL'),
('board-1', 'user-2', 'EDITOR', 'user-1', NOW(), NOW(), 'WORKSPACE_AUTO'),
('board-1', 'user-3', 'EDITOR', 'user-1', NOW(), NOW(), 'WORKSPACE_AUTO'),
('board-2', 'user-2', 'OWNER', 'user-2', NOW(), NOW(), 'INDIVIDUAL'),
('board-2', 'user-4', 'EDITOR', 'user-2', NOW(), NOW(), 'WORKSPACE_AUTO'),
('board-3', 'user-1', 'OWNER', 'user-1', NOW(), NOW(), 'INDIVIDUAL'),
('board-3', 'user-5', 'VIEWER', 'user-1', NOW(), NOW(), 'INDIVIDUAL'),
('board-4', 'user-3', 'OWNER', 'user-3', NOW(), NOW(), 'INDIVIDUAL');

-- =====================================================
-- 6. 리스트
-- =====================================================
INSERT INTO lists (id, workspace_id, board_id, title, position, color, created_at, updated_at) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '11111111-1111-1111-1111-111111111111', 'board-1', '백로그', 1, '#FF6B6B', NOW(), NOW()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '11111111-1111-1111-1111-111111111111', 'board-1', '진행 중', 2, '#4ECDC4', NOW(), NOW()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', '11111111-1111-1111-1111-111111111111', 'board-1', '검토 중', 3, '#45B7D1', NOW(), NOW()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', '11111111-1111-1111-1111-111111111111', 'board-1', '완료', 4, '#96CEB4', NOW(), NOW()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', '22222222-2222-2222-2222-222222222222', 'board-2', '요구사항 분석', 1, '#FFEAA7', NOW(), NOW()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', '22222222-2222-2222-2222-222222222222', 'board-2', '설계', 2, '#DDA0DD', NOW(), NOW()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', '22222222-2222-2222-2222-222222222222', 'board-2', '개발', 3, '#98D8C8', NOW(), NOW()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', '22222222-2222-2222-2222-222222222222', 'board-2', '테스트', 4, '#F06292', NOW(), NOW()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', '11111111-1111-1111-1111-111111111111', 'board-3', '아이디어', 1, '#FFD93D', NOW(), NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', '11111111-1111-1111-1111-111111111111', 'board-3', '기획', 2, '#6BCF7F', NOW(), NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '11111111-1111-1111-1111-111111111111', 'board-3', '실행', 3, '#4D96FF', NOW(), NOW()),
('cccccccc-cccc-cccc-cccc-ccccccccccc1', '33333333-3333-3333-3333-333333333333', 'board-4', '학습 목표', 1, '#FF9FF3', NOW(), NOW()),
('cccccccc-cccc-cccc-cccc-ccccccccccc2', '33333333-3333-3333-3333-333333333333', 'board-4', '진행 중', 2, '#54A0FF', NOW(), NOW()),
('cccccccc-cccc-cccc-cccc-ccccccccccc3', '33333333-3333-3333-3333-333333333333', 'board-4', '완료', 3, '#5F27CD', NOW(), NOW());

-- =====================================================
-- 7. 라벨
-- =====================================================
INSERT INTO labels (id, workspace_id, name, color, created_at) VALUES
('label-1', '11111111-1111-1111-1111-111111111111', '긴급', '#FF4757', NOW()),
('label-2', '11111111-1111-1111-1111-111111111111', '버그', '#FF6348', NOW()),
('label-3', '11111111-1111-1111-1111-111111111111', '기능개발', '#1E90FF', NOW()),
('label-4', '11111111-1111-1111-1111-111111111111', 'UI/UX', '#9C88FF', NOW()),
('label-5', '11111111-1111-1111-1111-111111111111', 'iOS', '#007AFF', NOW()),
('label-6', '22222222-2222-2222-2222-222222222222', 'Android', '#34C759', NOW()),
('label-7', '22222222-2222-2222-2222-222222222222', 'API', '#FF9500', NOW()),
('label-8', '11111111-1111-1111-1111-111111111111', '소셜미디어', '#E91E63', NOW()),
('label-9', '11111111-1111-1111-1111-111111111111', '이메일', '#2196F3', NOW()),
('label-10', '11111111-1111-1111-1111-111111111111', '광고', '#FF5722', NOW()),
('label-11', '33333333-3333-3333-3333-333333333333', '학습', '#00D2D3', NOW()),
('label-12', '33333333-3333-3333-3333-333333333333', '프로젝트', '#FF9F43', NOW());

-- =====================================================
-- 8. 카드
-- =====================================================
INSERT INTO cards (id, workspace_id, list_id, title, description, position, due_date, is_completed, created_at, updated_at) VALUES
('card-1', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '메인 페이지 디자인', '사용자 인터페이스 디자인 작업', 1, NOW() + INTERVAL '7 days', FALSE, NOW(), NOW()),
('card-2', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '데이터베이스 설계', 'ERD 작성 및 테이블 설계', 2, NOW() + INTERVAL '5 days', FALSE, NOW(), NOW()),
('card-3', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'API 개발', 'RESTful API 엔드포인트 구현', 3, NOW() + INTERVAL '10 days', FALSE, NOW(), NOW()),
('card-4', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '단위 테스트 작성', '핵심 기능에 대한 단위 테스트', 1, NOW() + INTERVAL '3 days', FALSE, NOW(), NOW()),
('card-5', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '보안 테스트', '인증 및 권한 검증', 2, NOW() + INTERVAL '5 days', FALSE, NOW(), NOW()),
('card-6', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '성능 최적화', '데이터베이스 쿼리 최적화', 3, NOW() + INTERVAL '8 days', FALSE, NOW(), NOW()),
('card-7', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', '사용자 매뉴얼 작성', '시스템 사용법 가이드', 1, NOW() + INTERVAL '2 days', FALSE, NOW(), NOW()),
('card-8', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', '배포 준비', '프로덕션 환경 배포 준비', 2, NOW() + INTERVAL '1 day', FALSE, NOW(), NOW()),
('card-9', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', '모니터링 설정', '시스템 모니터링 도구 설정', 3, NOW() + INTERVAL '4 days', FALSE, NOW(), NOW()),
('card-10', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'Android 개발', '모바일 앱 개발', 1, NOW() + INTERVAL '15 days', TRUE, NOW(), NOW()),
('card-11', '22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'SNS 콘텐츠 기획', '소셜미디어 마케팅 콘텐츠', 1, NOW() + INTERVAL '3 days', FALSE, NOW(), NOW()),
('card-12', '22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', '브랜드 가이드라인', '브랜드 아이덴티티 가이드', 2, NOW() + INTERVAL '7 days', FALSE, NOW(), NOW()),
('card-13', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', '크리스마스 프로모션 아이디어', '연말 시즌 맞춤 프로모션 기획', 1, NOW() + INTERVAL '5 days', FALSE, NOW(), NOW()),
('card-14', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', '인플루언서 협업 제안', '인기 인플루언서와의 협업 방안 검토', 2, NOW() + INTERVAL '7 days', FALSE, NOW(), NOW()),
('card-15', '11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', '소셜미디어 캠페인 전략', '페이스북, 인스타그램 캠페인 기획', 1, NOW() + INTERVAL '10 days', FALSE, NOW(), NOW()),
('card-16', '11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', '이메일 마케팅 시리즈', '고객 세그먼트별 이메일 캠페인 설계', 2, NOW() + INTERVAL '8 days', FALSE, NOW(), NOW()),
('card-17', '11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '온라인 광고 캠페인', '구글 애즈 및 페이스북 광고 집행', 1, NOW() + INTERVAL '12 days', FALSE, NOW(), NOW()),
('card-18', '11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '오프라인 이벤트 기획', '팝업 스토어 및 체험존 운영', 2, NOW() + INTERVAL '15 days', FALSE, NOW(), NOW()),
('card-19', '33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-ccccccccccc1', 'Spring Boot 마스터하기', 'Spring Boot 고급 기능 학습', 1, NOW() + INTERVAL '30 days', FALSE, NOW(), NOW()),
('card-20', '33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-ccccccccccc2', 'React 프로젝트', '개인 포트폴리오 웹사이트 개발', 1, NOW() + INTERVAL '20 days', FALSE, NOW(), NOW()),
('card-21', '33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-ccccccccccc3', 'Docker 기초 학습', '컨테이너 기술 기초 학습 완료', 1, NOW() - INTERVAL '5 days', TRUE, NOW(), NOW());

-- =====================================================
-- 9. 카드 담당자
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
('card-13', 'user-1', NOW()),
('card-14', 'user-5', NOW()),
('card-15', 'user-1', NOW()),
('card-16', 'user-5', NOW()),
('card-17', 'user-1', NOW()),
('card-18', 'user-5', NOW()),
('card-19', 'user-3', NOW()),
('card-20', 'user-3', NOW());

-- =====================================================
-- 10. 카드-라벨 연결
-- =====================================================
INSERT INTO card_labels (workspace_id, card_id, label_id) VALUES
('11111111-1111-1111-1111-111111111111', 'card-1', 'label-4'),
('11111111-1111-1111-1111-111111111111', 'card-1', 'label-3'),
('11111111-1111-1111-1111-111111111111', 'card-2', 'label-3'),
('11111111-1111-1111-1111-111111111111', 'card-3', 'label-3'),
('11111111-1111-1111-1111-111111111111', 'card-4', 'label-3'),
('11111111-1111-1111-1111-111111111111', 'card-5', 'label-1'),
('11111111-1111-1111-1111-111111111111', 'card-5', 'label-2'),
('11111111-1111-1111-1111-111111111111', 'card-6', 'label-3'),
('11111111-1111-1111-1111-111111111111', 'card-7', 'label-4'),
('11111111-1111-1111-1111-111111111111', 'card-7', 'label-5'),
('11111111-1111-1111-1111-111111111111', 'card-8', 'label-1'),
('11111111-1111-1111-1111-111111111111', 'card-9', 'label-3'),
('11111111-1111-1111-1111-111111111111', 'card-9', 'label-5'),
('11111111-1111-1111-1111-111111111111', 'card-10', 'label-3'),
('22222222-2222-2222-2222-222222222222', 'card-11', 'label-7'),
('22222222-2222-2222-2222-222222222222', 'card-12', 'label-6'),
('11111111-1111-1111-1111-111111111111', 'card-13', 'label-8'),
('11111111-1111-1111-1111-111111111111', 'card-14', 'label-8'),
('11111111-1111-1111-1111-111111111111', 'card-15', 'label-8'),
('11111111-1111-1111-1111-111111111111', 'card-16', 'label-9'),
('11111111-1111-1111-1111-111111111111', 'card-17', 'label-10'),
('11111111-1111-1111-1111-111111111111', 'card-18', 'label-10'),
('33333333-3333-3333-3333-333333333333', 'card-19', 'label-11'),
('33333333-3333-3333-3333-333333333333', 'card-20', 'label-12'),
('33333333-3333-3333-3333-333333333333', 'card-21', 'label-11');

-- =====================================================
-- 11. 댓글
-- =====================================================
INSERT INTO comments (id, workspace_id, card_id, user_id, content, created_at, updated_at) VALUES
('comment-1', '11111111-1111-1111-1111-111111111111', 'card-1', 'user-1', '디자인 초안을 검토해주세요. 메인 컬러는 어떤 것이 좋을까요?', NOW(), NOW()),
('comment-2', '11111111-1111-1111-1111-111111111111', 'card-1', 'user-2', '파란색 계열이 좋을 것 같습니다. 브랜드 이미지와도 잘 맞을 것 같아요.', NOW() + INTERVAL '1 hour', NOW() + INTERVAL '1 hour'),
('comment-3', '11111111-1111-1111-1111-111111111111', 'card-2', 'user-3', 'OAuth 인증도 고려해보면 어떨까요?', NOW(), NOW()),
('comment-4', '11111111-1111-1111-1111-111111111111', 'card-4', 'user-1', 'Swagger를 사용해서 API 문서를 자동 생성하면 좋겠습니다.', NOW(), NOW()),
('comment-5', '11111111-1111-1111-1111-111111111111', 'card-4', 'user-2', '동의합니다. Swagger UI로 테스트도 쉽게 할 수 있겠네요.', NOW() + INTERVAL '30 minutes', NOW() + INTERVAL '30 minutes'),
('comment-6', '11111111-1111-1111-1111-111111111111', 'card-4', 'user-3', 'API 버전 관리도 고려해야 할 것 같습니다.', NOW() + INTERVAL '2 hours', NOW() + INTERVAL '2 hours'),
('comment-7', '11111111-1111-1111-1111-111111111111', 'card-5', 'user-1', 'OWASP Top 10 보안 취약점을 기준으로 체크해주세요.', NOW(), NOW()),
('comment-8', '11111111-1111-1111-1111-111111111111', 'card-7', 'user-4', '사용자 경험을 중심으로 설계하면 좋겠습니다.', NOW(), NOW()),
('comment-9', '11111111-1111-1111-1111-111111111111', 'card-7', 'user-2', '네, 사용성 테스트도 진행해보겠습니다.', NOW() + INTERVAL '45 minutes', NOW() + INTERVAL '45 minutes'),
('comment-10', '11111111-1111-1111-1111-111111111111', 'card-9', 'user-2', 'iOS 개발 가이드라인을 준수해서 개발하겠습니다.', NOW(), NOW()),
('comment-11', '22222222-2222-2222-2222-222222222222', 'card-11', 'user-1', '타겟 고객층을 명확히 정의하고 콘텐츠를 기획해주세요.', NOW(), NOW()),
('comment-12', '11111111-1111-1111-1111-111111111111', 'card-13', 'user-1', '크리스마스 시즌에 맞는 따뜻한 느낌의 프로모션이 좋을 것 같습니다.', NOW(), NOW()),
('comment-13', '11111111-1111-1111-1111-111111111111', 'card-13', 'user-5', '할인율과 함께 선물 증정 이벤트도 고려해보면 어떨까요?', NOW() + INTERVAL '2 hours', NOW() + INTERVAL '2 hours'),
('comment-14', '11111111-1111-1111-1111-111111111111', 'card-15', 'user-1', '인스타그램 릴스와 페이스북 스토리 활용 방안을 구체적으로 기획해주세요.', NOW(), NOW()),
('comment-15', '11111111-1111-1111-1111-111111111111', 'card-16', 'user-5', '신규 고객과 기존 고객을 구분해서 다른 메시지를 보내는 것이 좋겠습니다.', NOW(), NOW()),
('comment-16', '11111111-1111-1111-1111-111111111111', 'card-17', 'user-1', '광고 예산 배분과 타겟팅 설정을 세밀하게 계획해주세요.', NOW(), NOW()),
('comment-17', '33333333-3333-3333-3333-333333333333', 'card-19', 'user-3', 'Spring Security와 JWT 인증도 함께 학습해보겠습니다.', NOW(), NOW()),
('comment-18', '33333333-3333-3333-3333-333333333333', 'card-20', 'user-3', 'TypeScript와 함께 사용하면 더 안전한 코드를 작성할 수 있을 것 같습니다.', NOW(), NOW());

-- =====================================================
-- 12. 활동 로그
-- =====================================================
INSERT INTO activities (id, workspace_id, board_id, user_id, type, payload_json, created_at) VALUES
('activity-1', '11111111-1111-1111-1111-111111111111', 'board-1', 'user-1', 'BOARD_CREATE', '{"boardName":"웹 개발 프로젝트"}', NOW() - INTERVAL '3 days'),
('activity-2', '11111111-1111-1111-1111-111111111111', 'board-1', 'user-1', 'LIST_CREATE', '{"listName":"백로그"}', NOW() - INTERVAL '3 days'),
('activity-3', '11111111-1111-1111-1111-111111111111', 'board-1', 'user-2', 'CARD_ADD_COMMENT', '{"cardTitle":"메인 페이지 디자인"}', NOW() - INTERVAL '12 hours'),
('activity-4', '22222222-2222-2222-2222-222222222222', 'board-2', 'user-2', 'BOARD_CREATE', '{"boardName":"모바일 앱 개발"}', NOW() - INTERVAL '5 days'),
('activity-5', '33333333-3333-3333-3333-333333333333', 'board-4', 'user-3', 'BOARD_CREATE', '{"boardName":"개인 프로젝트"}', NOW() - INTERVAL '1 day'),
('activity-6', '11111111-1111-1111-1111-111111111111', 'board-3', 'user-1', 'BOARD_CREATE', '{"boardName":"마케팅 캠페인"}', NOW() - INTERVAL '2 days');

-- =====================================================
-- 13. 초대 데이터
-- =====================================================
INSERT INTO invitations (id, type, target_id, email, role, status, invited_by, expires_at, created_at) VALUES
('invitation-1', 'WORKSPACE', '11111111-1111-1111-1111-111111111111', 'newmember@example.com', 'MEMBER', 'PENDING', 'user-1', NOW() + INTERVAL '7 days', NOW()),
('invitation-2', 'BOARD', 'board-1', 'guest@example.com', 'VIEWER', 'PENDING', 'user-1', NOW() + INTERVAL '3 days', NOW()),
('invitation-3', 'WORKSPACE', '22222222-2222-2222-2222-222222222222', 'developer@example.com', 'MEMBER', 'ACCEPTED', 'user-2', NOW() - INTERVAL '1 day', NOW() - INTERVAL '7 days');
