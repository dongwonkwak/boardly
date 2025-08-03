-- =====================================================
-- Boardly Database - PostgreSQL 테스트 데이터 SQL
-- =====================================================

-- 테스트 데이터 삭제 (기존 데이터가 있다면)
DELETE FROM user_activity;
DELETE FROM comments;
DELETE FROM card_labels;
DELETE FROM card_members;
DELETE FROM cards;
DELETE FROM labels;
DELETE FROM board_lists;
DELETE FROM board_members;
DELETE FROM boards;
DELETE FROM users;

-- =====================================================
-- 1. 사용자 테스트 데이터
-- =====================================================
INSERT INTO users (user_id, email, hashed_password, first_name, last_name, is_active, created_at, updated_at, version) VALUES
('user-1', 'test@example.com', '{noop}Password1!', 'John', 'Doe', TRUE, NOW(), NOW(), 0),
('user-2', 'jane.smith@example.com', '{noop}Password1!', '제인', '스미스', TRUE, NOW(), NOW(), 0),
('user-3', 'mike.wilson@example.com', '{noop}Password1!', 'Mike', 'Wilson', TRUE, NOW(), NOW(), 0),
('user-4', 'sarah.jones@example.com', '{noop}Password1!', 'Sarah', 'Jones', TRUE, NOW(), NOW(), 0),
('user-5', 'david.brown@example.com', '{noop}Password1!', 'David', 'Brown', TRUE, NOW(), NOW(), 0);

-- =====================================================
-- 2. 보드 테스트 데이터
-- =====================================================
INSERT INTO boards (board_id, title, description, is_archived, owner_id, is_starred, created_at, updated_at, version) VALUES
('board-1', '웹 개발 프로젝트', '회사 웹사이트 리뉴얼 프로젝트', FALSE, 'user-1', TRUE, NOW(), NOW(), 0),
('board-2', '모바일 앱 개발', '신규 모바일 애플리케이션 개발', FALSE, 'user-2', FALSE, NOW(), NOW(), 0),
('board-3', '마케팅 캠페인', '2024년 Q4 마케팅 캠페인 기획', FALSE, 'user-1', FALSE, NOW(), NOW(), 0),
('board-4', '인사 관리', 'HR 관련 업무 관리', FALSE, 'user-3', TRUE, NOW(), NOW(), 0);

-- =====================================================
-- 3. 보드 멤버 테스트 데이터
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
-- 4. 보드 리스트 테스트 데이터
-- =====================================================
INSERT INTO board_lists (list_id, title, description, position, color, board_id, created_at, updated_at, version) VALUES
-- 웹 개발 프로젝트 보드 리스트
('list-1', '백로그', '해야 할 일 목록', 1, '#FF6B6B', 'board-1', NOW(), NOW(), 0),
('list-2', '진행 중', '현재 작업 중인 항목들', 2, '#4ECDC4', 'board-1', NOW(), NOW(), 0),
('list-3', '검토 중', '검토가 필요한 항목들', 3, '#45B7D1', 'board-1', NOW(), NOW(), 0),
('list-4', '완료', '완료된 작업들', 4, '#96CEB4', 'board-1', NOW(), NOW(), 0),

-- 모바일 앱 개발 보드 리스트
('list-5', '요구사항 분석', '앱 요구사항 정리', 1, '#FFEAA7', 'board-2', NOW(), NOW(), 0),
('list-6', '설계', '앱 설계 및 아키텍처', 2, '#DDA0DD', 'board-2', NOW(), NOW(), 0),
('list-7', '개발', '실제 개발 작업', 3, '#98D8C8', 'board-2', NOW(), NOW(), 0),
('list-8', '테스트', '테스트 및 QA', 4, '#F06292', 'board-2', NOW(), NOW(), 0),

-- 마케팅 캠페인 보드 리스트
('list-9', '아이디어', '캠페인 아이디어 수집', 1, '#FFD93D', 'board-3', NOW(), NOW(), 0),
('list-10', '기획', '캠페인 기획 및 전략', 2, '#6BCF7F', 'board-3', NOW(), NOW(), 0),
('list-11', '실행', '캠페인 실행', 3, '#4D96FF', 'board-3', NOW(), NOW(), 0);

-- =====================================================
-- 5. 라벨 테스트 데이터
-- =====================================================
INSERT INTO labels (label_id, board_id, name, color, created_at, updated_at, version) VALUES
-- 웹 개발 프로젝트 라벨
('label-1', 'board-1', '긴급', '#FF4757', NOW(), NOW(), 0),
('label-2', 'board-1', '버그', '#FF6348', NOW(), NOW(), 0),
('label-3', 'board-1', '기능개발', '#1E90FF', NOW(), NOW(), 0),
('label-4', 'board-1', 'UI/UX', '#9C88FF', NOW(), NOW(), 0),

-- 모바일 앱 개발 라벨
('label-5', 'board-2', 'iOS', '#007AFF', NOW(), NOW(), 0),
('label-6', 'board-2', 'Android', '#34C759', NOW(), NOW(), 0),
('label-7', 'board-2', 'API', '#FF9500', NOW(), NOW(), 0),

-- 마케팅 캠페인 라벨
('label-8', 'board-3', '소셜미디어', '#E91E63', NOW(), NOW(), 0),
('label-9', 'board-3', '이메일', '#2196F3', NOW(), NOW(), 0),
('label-10', 'board-3', '광고', '#FF5722', NOW(), NOW(), 0);

-- =====================================================
-- 6. 카드 테스트 데이터 (PostgreSQL INTERVAL 사용)
-- =====================================================
INSERT INTO cards (card_id, title, description, position, due_date, start_date, archived, priority, is_completed, list_id, comments_count, attachments_count, labels_count, created_at, updated_at, version) VALUES
-- 웹 개발 프로젝트 카드들
('card-1', '메인 페이지 디자인', '새로운 메인 페이지 디자인 작업', 1, NOW() + INTERVAL '7 days', NOW() - INTERVAL '2 days', FALSE, 'high', FALSE, 'list-1', 2, 0, 2, NOW(), NOW(), 0),
('card-2', '로그인 기능 구현', '사용자 로그인/로그아웃 기능 개발', 2, NOW() + INTERVAL '3 days', NOW() - INTERVAL '1 day', FALSE, 'urgent', FALSE, 'list-1', 1, 0, 1, NOW(), NOW(), 0),
('card-3', '데이터베이스 설계', '프로젝트 데이터베이스 스키마 설계', 1, NULL, NOW() - INTERVAL '3 days', FALSE, 'medium', TRUE, 'list-2', 0, 0, 1, NOW(), NOW(), 0),
('card-4', 'API 문서 작성', 'REST API 문서화 작업', 2, NOW() + INTERVAL '5 days', NOW() - INTERVAL '1 day', FALSE, 'medium', FALSE, 'list-2', 3, 0, 0, NOW(), NOW(), 0),
('card-5', '보안 테스트', '웹사이트 보안 취약점 테스트', 1, NULL, NOW() - INTERVAL '5 days', FALSE, 'high', FALSE, 'list-3', 1, 0, 2, NOW(), NOW(), 0),
('card-6', '배포 환경 구축', '프로덕션 서버 환경 구축', 1, NULL, NOW() - INTERVAL '7 days', FALSE, 'low', TRUE, 'list-4', 0, 0, 0, NOW(), NOW(), 0),

-- 모바일 앱 개발 카드들
('card-7', '앱 와이어프레임', '모바일 앱 화면 구성 설계', 1, NOW() + INTERVAL '10 days', NOW() - INTERVAL '4 days', FALSE, 'medium', FALSE, 'list-5', 2, 0, 1, NOW(), NOW(), 0),
('card-8', '사용자 스토리 작성', '앱 기능별 사용자 스토리 정리', 2, NULL, NOW() - INTERVAL '2 days', FALSE, 'low', TRUE, 'list-5', 0, 0, 0, NOW(), NOW(), 0),
('card-9', 'iOS 네이티브 개발', 'iOS 앱 네이티브 개발', 1, NOW() + INTERVAL '14 days', NOW() - INTERVAL '6 days', FALSE, 'high', FALSE, 'list-7', 1, 0, 1, NOW(), NOW(), 0),
('card-10', 'Android 개발', 'Android 앱 개발', 2, NOW() + INTERVAL '14 days', NOW() - INTERVAL '6 days', FALSE, 'high', FALSE, 'list-7', 0, 0, 1, NOW(), NOW(), 0),

-- 마케팅 캠페인 카드들
('card-11', 'SNS 콘텐츠 기획', '인스타그램, 페이스북 콘텐츠 기획', 1, NOW() + INTERVAL '2 days', NOW() - INTERVAL '1 day', FALSE, 'urgent', FALSE, 'list-9', 1, 0, 1, NOW(), NOW(), 0),
('card-12', '이메일 템플릿 제작', '뉴스레터 이메일 템플릿 디자인', 1, NULL, NOW() - INTERVAL '3 days', FALSE, 'medium', FALSE, 'list-10', 0, 0, 1, NOW(), NOW(), 0),

-- 시작일/마감일이 null인 카드들 (테스트용)
('card-14', '아이디어 브레인스토밍', '새로운 기능 아이디어 수집', 3, NULL, NULL, FALSE, 'low', FALSE, 'list-1', 0, 0, 0, NOW(), NOW(), 0),
('card-15', '문서 정리', '프로젝트 문서 정리 작업', 4, NULL, NULL, FALSE, 'medium', FALSE, 'list-1', 0, 0, 0, NOW(), NOW(), 0),
('card-16', '회의 준비', '다음 회의 자료 준비', 5, NOW() + INTERVAL '1 day', NULL, FALSE, 'high', FALSE, 'list-2', 0, 0, 0, NOW(), NOW(), 0),
('card-17', '코드 리뷰', '팀원 코드 리뷰', 3, NULL, NOW() - INTERVAL '1 day', FALSE, 'medium', FALSE, 'list-2', 0, 0, 0, NOW(), NOW(), 0),
('card-18', '테스트 케이스 작성', '단위 테스트 케이스 작성', 2, NULL, NULL, FALSE, 'low', FALSE, 'list-5', 0, 0, 0, NOW(), NOW(), 0),
('card-19', '성능 최적화', '앱 성능 최적화 작업', 3, NOW() + INTERVAL '5 days', NULL, FALSE, 'high', FALSE, 'list-7', 0, 0, 0, NOW(), NOW(), 0),
('card-20', '사용자 피드백 수집', '사용자 피드백 수집 및 분석', 2, NULL, NULL, FALSE, 'medium', FALSE, 'list-9', 0, 0, 0, NOW(), NOW(), 0);

-- =====================================================
-- 7. 카드 멤버 테스트 데이터 (카드 담당자)
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
('card-12', 'user-1', NOW());

-- =====================================================
-- 8. 카드 라벨 연결 테스트 데이터
-- =====================================================
INSERT INTO card_labels (card_id, label_id, applied_at) VALUES
-- 웹 개발 프로젝트 카드 라벨
('card-1', 'label-4', NOW()), -- UI/UX
('card-1', 'label-3', NOW()), -- 기능개발
('card-2', 'label-3', NOW()), -- 기능개발
('card-3', 'label-3', NOW()), -- 기능개발
('card-5', 'label-1', NOW()), -- 긴급
('card-5', 'label-2', NOW()), -- 버그

-- 모바일 앱 개발 카드 라벨
('card-7', 'label-5', NOW()), -- iOS
('card-9', 'label-5', NOW()), -- iOS
('card-10', 'label-6', NOW()), -- Android

-- 마케팅 캠페인 카드 라벨
('card-11', 'label-8', NOW()), -- 소셜미디어
('card-12', 'label-9', NOW()); -- 이메일

-- =====================================================
-- 9. 댓글 테스트 데이터 (PostgreSQL INTERVAL 사용)
-- =====================================================
INSERT INTO comments (comment_id, card_id, author_id, content, edited, created_at, updated_at, version) VALUES
('comment-1', 'card-1', 'user-1', '디자인 초안을 검토해주세요. 메인 컬러는 어떤 것이 좋을까요?', FALSE, NOW(), NOW(), 0),
('comment-2', 'card-1', 'user-2', '파란색 계열이 좋을 것 같습니다. 브랜드 이미지와도 잘 맞을 것 같아요.', FALSE, NOW() + INTERVAL '1 hour', NOW() + INTERVAL '1 hour', 0),
('comment-3', 'card-2', 'user-3', 'OAuth 인증도 고려해보면 어떨까요?', FALSE, NOW(), NOW(), 0),
('comment-4', 'card-4', 'user-1', 'Swagger를 사용해서 API 문서를 자동 생성하면 좋겠습니다.', FALSE, NOW(), NOW(), 0),
('comment-5', 'card-4', 'user-2', '동의합니다. Swagger UI로 테스트도 쉽게 할 수 있겠네요.', FALSE, NOW() + INTERVAL '30 minutes', NOW() + INTERVAL '30 minutes', 0),
('comment-6', 'card-4', 'user-3', 'API 버전 관리도 고려해야 할 것 같습니다.', FALSE, NOW() + INTERVAL '2 hours', NOW() + INTERVAL '2 hours', 0),
('comment-7', 'card-5', 'user-1', 'OWASP Top 10 보안 취약점을 기준으로 체크해주세요.', FALSE, NOW(), NOW(), 0),
('comment-8', 'card-7', 'user-4', '사용자 경험을 중심으로 설계하면 좋겠습니다.', FALSE, NOW(), NOW(), 0),
('comment-9', 'card-7', 'user-2', '네, 사용성 테스트도 진행해보겠습니다.', FALSE, NOW() + INTERVAL '45 minutes', NOW() + INTERVAL '45 minutes', 0),
('comment-10', 'card-9', 'user-2', 'iOS 개발 가이드라인을 준수해서 개발하겠습니다.', FALSE, NOW(), NOW(), 0),
('comment-11', 'card-11', 'user-1', '타겟 고객층을 명확히 정의하고 콘텐츠를 기획해주세요.', FALSE, NOW(), NOW(), 0);

-- =====================================================
-- 10. 사용자 활동 테스트 데이터 (PostgreSQL INTERVAL 사용)
-- =====================================================
INSERT INTO user_activity (activity_id, actor_id, board_id, list_id, card_id, activity_type, actor_first_name, actor_last_name, actor_profile_image_url, payload, created_at) VALUES
('activity-1', 'user-1', 'board-1', NULL, NULL, 'BOARD_CREATE', 'John', 'Doe', NULL, '{"boardName": "웹 개발 프로젝트"}', NOW() - INTERVAL '3 days'),
('activity-2', 'user-1', 'board-1', 'list-1', NULL, 'LIST_CREATE', 'John', 'Doe', NULL, '{"listName": "백로그", "boardName": "웹 개발 프로젝트"}', NOW() - INTERVAL '3 days'),
('activity-3', 'user-1', 'board-1', 'list-1', 'card-1', 'CARD_CREATE', 'John', 'Doe', NULL, '{"cardTitle": "메인 페이지 디자인", "listName": "백로그"}', NOW() - INTERVAL '2 days'),
('activity-4', 'user-2', 'board-1', NULL, 'card-1', 'CARD_ASSIGN_MEMBER', 'Jane', 'Smith', NULL, '{"cardTitle": "메인 페이지 디자인", "memberFirstName": "Jane", "memberLastName": "Smith"}', NOW() - INTERVAL '2 days'),
('activity-5', 'user-1', 'board-1', NULL, 'card-2', 'CARD_CREATE', 'John', 'Doe', NULL, '{"cardTitle": "로그인 기능 구현", "listName": "백로그"}', NOW() - INTERVAL '1 day'),
('activity-6', 'user-1', 'board-1', NULL, 'card-3', 'CARD_MOVE', 'John', 'Doe', NULL, '{"cardTitle": "데이터베이스 설계", "sourceListName": "백로그", "destListName": "진행 중"}', NOW() - INTERVAL '1 day'),
('activity-7', 'user-2', 'board-1', NULL, 'card-1', 'CARD_ADD_COMMENT', 'Jane', 'Smith', NULL, '{"cardTitle": "메인 페이지 디자인"}', NOW() - INTERVAL '12 hours'),
('activity-8', 'user-3', 'board-1', NULL, 'card-2', 'CARD_ADD_COMMENT', 'Mike', 'Wilson', NULL, '{"cardTitle": "로그인 기능 구현"}', NOW() - INTERVAL '8 hours'),
('activity-9', 'user-1', 'board-1', NULL, 'card-4', 'CARD_ADD_COMMENT', 'John', 'Doe', NULL, '{"cardTitle": "API 문서 작성"}', NOW() - INTERVAL '6 hours'),
('activity-10', 'user-2', 'board-2', NULL, NULL, 'BOARD_CREATE', 'Jane', 'Smith', NULL, '{"boardName": "모바일 앱 개발"}', NOW() - INTERVAL '5 days'),
('activity-11', 'user-2', 'board-2', 'list-5', 'card-7', 'CARD_CREATE', 'Jane', 'Smith', NULL, '{"cardTitle": "앱 와이어프레임", "listName": "요구사항 분석"}', NOW() - INTERVAL '4 days'),
('activity-12', 'user-4', 'board-2', NULL, 'card-7', 'CARD_ASSIGN_MEMBER', 'Sarah', 'Jones', NULL, '{"cardTitle": "앱 와이어프레임", "memberFirstName": "Sarah", "memberLastName": "Jones"}', NOW() - INTERVAL '4 days'),
('activity-13', 'user-2', 'board-2', NULL, 'card-9', 'CARD_MOVE', 'Jane', 'Smith', NULL, '{"cardTitle": "iOS 네이티브 개발", "sourceListName": "설계", "destListName": "개발"}', NOW() - INTERVAL '3 hours'),
('activity-14', 'user-1', 'board-3', NULL, NULL, 'BOARD_CREATE', 'John', 'Doe', NULL, '{"boardName": "마케팅 캠페인"}', NOW() - INTERVAL '6 days'),
('activity-15', 'user-5', 'board-3', NULL, 'card-11', 'CARD_ASSIGN_MEMBER', 'David', 'Brown', NULL, '{"cardTitle": "SNS 콘텐츠 기획", "memberFirstName": "David", "memberLastName": "Brown"}', NOW() - INTERVAL '2 days'),
('activity-16', 'user-1', 'board-1', NULL, 'card-1', 'CARD_ADD_LABEL', 'John', 'Doe', NULL, '{"cardTitle": "메인 페이지 디자인", "labelName": "UI/UX"}', NOW() - INTERVAL '1 day'),
('activity-17', 'user-1', 'board-1', NULL, 'card-5', 'CARD_SET_DUE_DATE', 'John', 'Doe', NULL, '{"cardTitle": "보안 테스트", "dueDate": "2024-08-05"}', NOW() - INTERVAL '4 hours'),
('activity-18', 'user-3', 'board-1', 'list-2', NULL, 'LIST_RENAME', 'Mike', 'Wilson', NULL, '{"oldName": "리뷰", "newName": "검토 중"}', NOW() - INTERVAL '2 hours'),
('activity-19', 'user-2', 'board-2', NULL, 'card-10', 'CARD_UPDATE_DESCRIPTION', 'Jane', 'Smith', NULL, '{"cardTitle": "Android 개발"}', NOW() - INTERVAL '1 hour'),
('activity-20', 'user-5', 'board-3', NULL, 'card-11', 'CARD_ADD_COMMENT', 'David', 'Brown', NULL, '{"cardTitle": "SNS 콘텐츠 기획"}', NOW() - INTERVAL '30 minutes');

-- =====================================================
-- 데이터 일관성 업데이트
-- =====================================================

-- 카드의 댓글 수 업데이트
UPDATE cards SET comments_count = (
    SELECT COUNT(*) FROM comments WHERE comments.card_id = cards.card_id
);

-- 카드의 라벨 수 업데이트  
UPDATE cards SET labels_count = (
    SELECT COUNT(*) FROM card_labels WHERE card_labels.card_id = cards.card_id
);

-- 보드 리스트의 위치 정렬 확인을 위한 추가 데이터
INSERT INTO board_lists (list_id, title, description, position, color, board_id, created_at, updated_at, version) VALUES
('list-12', '아카이브', '완료된 오래된 작업들', 5, '#95A5A6', 'board-1', NOW(), NOW(), 0);

-- 아카이브된 카드 예시
INSERT INTO cards (card_id, title, description, position, due_date, start_date, archived, priority, is_completed, list_id, comments_count, attachments_count, labels_count, created_at, updated_at, version) VALUES
('card-13', '구 버전 호환성 검사', '이전 버전과의 호환성 확인 (완료됨)', 1, NULL, NOW() - INTERVAL '15 days', TRUE, 'low', TRUE, 'list-12', 0, 0, 0, NOW() - INTERVAL '10 days', NOW(), 0);

-- 추가 보드 멤버 권한 예시
INSERT INTO board_members (member_id, board_id, user_id, role, is_active, created_at, updated_at, version) VALUES
('member-9', 'board-1', 'user-4', 'VIEWER', TRUE, NOW(), NOW(), 0),
('member-10', 'board-2', 'user-1', 'MEMBER', FALSE, NOW(), NOW(), 0); -- 비활성 멤버 예시 