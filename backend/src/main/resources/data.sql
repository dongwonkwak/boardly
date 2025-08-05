-- =====================================================
-- Boardly Database - 테스트 데이터 SQL
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
-- 6. 카드 테스트 데이터
-- =====================================================
INSERT INTO cards (card_id, title, description, position, due_date, priority, archived, list_id, created_at, updated_at, version) VALUES
-- 웹 개발 프로젝트 카드들 (우선순위 미지정)
('card-1', '메인 페이지 디자인', '사용자 인터페이스 디자인 작업', 1, DATEADD('DAY', 7, NOW()), NULL, false, 'list-1', NOW(), NOW(), 0),
('card-2', '데이터베이스 설계', 'ERD 작성 및 테이블 설계', 2, DATEADD('DAY', 5, NOW()), NULL, false, 'list-1', NOW(), NOW(), 0),
('card-3', 'API 개발', 'RESTful API 엔드포인트 구현', 3, DATEADD('DAY', 10, NOW()), NULL, false, 'list-1', NOW(), NOW(), 0),
('card-4', '단위 테스트 작성', '핵심 기능에 대한 단위 테스트', 1, DATEADD('DAY', 3, NOW()), NULL, false, 'list-2', NOW(), NOW(), 0),
('card-5', '보안 테스트', '인증 및 권한 검증', 2, DATEADD('DAY', 5, NOW()), 'high', false, 'list-2', NOW(), NOW(), 0),
('card-6', '성능 최적화', '데이터베이스 쿼리 최적화', 3, DATEADD('DAY', 8, NOW()), 'medium', false, 'list-2', NOW(), NOW(), 0),
('card-7', '사용자 매뉴얼 작성', '시스템 사용법 가이드', 1, DATEADD('DAY', 2, NOW()), 'low', false, 'list-3', NOW(), NOW(), 0),
('card-8', '배포 준비', '프로덕션 환경 배포 준비', 2, DATEADD('DAY', 1, NOW()), 'urgent', false, 'list-3', NOW(), NOW(), 0),
('card-9', '모니터링 설정', '시스템 모니터링 도구 설정', 3, DATEADD('DAY', 4, NOW()), NULL, false, 'list-3', NOW(), NOW(), 0),
('card-10', 'Android 개발', '모바일 앱 개발', 1, DATEADD('DAY', 15, NOW()), NULL, false, 'list-4', NOW(), NOW(), 0),
('card-11', 'SNS 콘텐츠 기획', '소셜미디어 마케팅 콘텐츠', 1, DATEADD('DAY', 3, NOW()), NULL, false, 'list-5', NOW(), NOW(), 0),
('card-12', '브랜드 가이드라인', '브랜드 아이덴티티 가이드', 2, DATEADD('DAY', 7, NOW()), NULL, false, 'list-5', NOW(), NOW(), 0);

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
-- 9. 댓글 테스트 데이터
-- =====================================================
INSERT INTO comments (comment_id, card_id, author_id, content, edited, created_at, updated_at, version) VALUES
('comment-1', 'card-1', 'user-1', '디자인 초안을 검토해주세요. 메인 컬러는 어떤 것이 좋을까요?', FALSE, NOW(), NOW(), 0),
('comment-2', 'card-1', 'user-2', '파란색 계열이 좋을 것 같습니다. 브랜드 이미지와도 잘 맞을 것 같아요.', FALSE, DATEADD('HOUR', 1, NOW()), DATEADD('HOUR', 1, NOW()), 0),
('comment-3', 'card-2', 'user-3', 'OAuth 인증도 고려해보면 어떨까요?', FALSE, NOW(), NOW(), 0),
('comment-4', 'card-4', 'user-1', 'Swagger를 사용해서 API 문서를 자동 생성하면 좋겠습니다.', FALSE, NOW(), NOW(), 0),
('comment-5', 'card-4', 'user-2', '동의합니다. Swagger UI로 테스트도 쉽게 할 수 있겠네요.', FALSE, DATEADD('MINUTE', 30, NOW()), DATEADD('MINUTE', 30, NOW()), 0),
('comment-6', 'card-4', 'user-3', 'API 버전 관리도 고려해야 할 것 같습니다.', FALSE, DATEADD('HOUR', 2, NOW()), DATEADD('HOUR', 2, NOW()), 0),
('comment-7', 'card-5', 'user-1', 'OWASP Top 10 보안 취약점을 기준으로 체크해주세요.', FALSE, NOW(), NOW(), 0),
('comment-8', 'card-7', 'user-4', '사용자 경험을 중심으로 설계하면 좋겠습니다.', FALSE, NOW(), NOW(), 0),
('comment-9', 'card-7', 'user-2', '네, 사용성 테스트도 진행해보겠습니다.', FALSE, DATEADD('MINUTE', 45, NOW()), DATEADD('MINUTE', 45, NOW()), 0),
('comment-10', 'card-9', 'user-2', 'iOS 개발 가이드라인을 준수해서 개발하겠습니다.', FALSE, NOW(), NOW(), 0),
('comment-11', 'card-11', 'user-1', '타겟 고객층을 명확히 정의하고 콘텐츠를 기획해주세요.', FALSE, NOW(), NOW(), 0);

-- =====================================================
-- 10. 사용자 활동 테스트 데이터
-- =====================================================
INSERT INTO user_activity (activity_id, actor_id, board_id, list_id, card_id, activity_type, actor_first_name, actor_last_name, actor_profile_image_url, payload, created_at) VALUES
('activity-1', 'user-1', 'board-1', NULL, NULL, 'BOARD_CREATE', 'John', 'Doe', NULL, '{"boardName": "웹 개발 프로젝트"}', DATEADD('DAY', -3, NOW())),
('activity-2', 'user-1', 'board-1', 'list-1', NULL, 'LIST_CREATE', 'John', 'Doe', NULL, '{"listName": "백로그", "boardName": "웹 개발 프로젝트"}', DATEADD('DAY', -3, NOW())),
('activity-3', 'user-1', 'board-1', 'list-1', 'card-1', 'CARD_CREATE', 'John', 'Doe', NULL, '{"cardTitle": "메인 페이지 디자인", "listName": "백로그"}', DATEADD('DAY', -2, NOW())),
('activity-4', 'user-2', 'board-1', NULL, 'card-1', 'CARD_ASSIGN_MEMBER', 'Jane', 'Smith', NULL, '{"cardTitle": "메인 페이지 디자인", "memberFirstName": "Jane", "memberLastName": "Smith"}', DATEADD('DAY', -2, NOW())),
('activity-5', 'user-1', 'board-1', NULL, 'card-2', 'CARD_CREATE', 'John', 'Doe', NULL, '{"cardTitle": "로그인 기능 구현", "listName": "백로그"}', DATEADD('DAY', -1, NOW())),
('activity-6', 'user-1', 'board-1', NULL, 'card-3', 'CARD_MOVE', 'John', 'Doe', NULL, '{"cardTitle": "데이터베이스 설계", "sourceListName": "백로그", "destListName": "진행 중"}', DATEADD('DAY', -1, NOW())),
('activity-7', 'user-2', 'board-1', NULL, 'card-1', 'CARD_ADD_COMMENT', 'Jane', 'Smith', NULL, '{"cardTitle": "메인 페이지 디자인"}', DATEADD('HOUR', -12, NOW())),
('activity-8', 'user-3', 'board-1', NULL, 'card-2', 'CARD_ADD_COMMENT', 'Mike', 'Wilson', NULL, '{"cardTitle": "로그인 기능 구현"}', DATEADD('HOUR', -8, NOW())),
('activity-9', 'user-1', 'board-1', NULL, 'card-4', 'CARD_ADD_COMMENT', 'John', 'Doe', NULL, '{"cardTitle": "API 문서 작성"}', DATEADD('HOUR', -6, NOW())),
('activity-10', 'user-2', 'board-2', NULL, NULL, 'BOARD_CREATE', 'Jane', 'Smith', NULL, '{"boardName": "모바일 앱 개발"}', DATEADD('DAY', -5, NOW())),
('activity-11', 'user-2', 'board-2', 'list-5', 'card-7', 'CARD_CREATE', 'Jane', 'Smith', NULL, '{"cardTitle": "앱 와이어프레임", "listName": "요구사항 분석"}', DATEADD('DAY', -4, NOW())),
('activity-12', 'user-4', 'board-2', NULL, 'card-7', 'CARD_ASSIGN_MEMBER', 'Sarah', 'Jones', NULL, '{"cardTitle": "앱 와이어프레임", "memberFirstName": "Sarah", "memberLastName": "Jones"}', DATEADD('DAY', -4, NOW())),
('activity-13', 'user-2', 'board-2', NULL, 'card-9', 'CARD_MOVE', 'Jane', 'Smith', NULL, '{"cardTitle": "iOS 네이티브 개발", "sourceListName": "설계", "destListName": "개발"}', DATEADD('HOUR', -3, NOW())),
('activity-14', 'user-1', 'board-3', NULL, NULL, 'BOARD_CREATE', 'John', 'Doe', NULL, '{"boardName": "마케팅 캠페인"}', DATEADD('DAY', -6, NOW())),
('activity-15', 'user-5', 'board-3', NULL, 'card-11', 'CARD_ASSIGN_MEMBER', 'David', 'Brown', NULL, '{"cardTitle": "SNS 콘텐츠 기획", "memberFirstName": "David", "memberLastName": "Brown"}', DATEADD('DAY', -2, NOW())),
('activity-16', 'user-1', 'board-1', NULL, 'card-1', 'CARD_ADD_LABEL', 'John', 'Doe', NULL, '{"cardTitle": "메인 페이지 디자인", "labelName": "UI/UX"}', DATEADD('DAY', -1, NOW())),
('activity-17', 'user-1', 'board-1', NULL, 'card-5', 'CARD_SET_DUE_DATE', 'John', 'Doe', NULL, '{"cardTitle": "보안 테스트", "dueDate": "2024-08-05"}', DATEADD('HOUR', -4, NOW())),
('activity-18', 'user-3', 'board-1', 'list-2', NULL, 'LIST_RENAME', 'Mike', 'Wilson', NULL, '{"oldName": "리뷰", "newName": "검토 중"}', DATEADD('HOUR', -2, NOW())),
('activity-19', 'user-2', 'board-2', NULL, 'card-10', 'CARD_UPDATE_DESCRIPTION', 'Jane', 'Smith', NULL, '{"cardTitle": "Android 개발"}', DATEADD('HOUR', -1, NOW())),
('activity-20', 'user-5', 'board-3', NULL, 'card-11', 'CARD_ADD_COMMENT', 'David', 'Brown', NULL, '{"cardTitle": "SNS 콘텐츠 기획"}', DATEADD('MINUTE', -30, NOW()));



-- 보드 리스트의 위치 정렬 확인을 위한 추가 데이터
INSERT INTO board_lists (list_id, title, description, position, color, board_id, created_at, updated_at, version) VALUES
('list-12', '아카이브', '완료된 오래된 작업들', 5, '#95A5A6', 'board-1', NOW(), NOW(), 0);

-- 아카이브된 카드 예시
INSERT INTO cards (card_id, title, description, position, due_date, priority, archived, list_id, created_at, updated_at, version) VALUES
('card-13', '구 버전 호환성 검사', '이전 버전과의 호환성 확인 (완료됨)', 1, NULL, NULL, TRUE, 'list-12', DATEADD('DAY', -10, NOW()), NOW(), 0);

-- 추가 보드 멤버 권한 예시
INSERT INTO board_members (member_id, board_id, user_id, role, is_active, created_at, updated_at, version) VALUES
('member-9', 'board-1', 'user-4', 'VIEWER', TRUE, NOW(), NOW(), 0),
('member-10', 'board-2', 'user-1', 'MEMBER', FALSE, NOW(), NOW(), 0); -- 비활성 멤버 예시