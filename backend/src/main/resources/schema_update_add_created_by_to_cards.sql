-- 카드 테이블에 생성자 정보 컬럼 추가
-- 기존 카드들의 경우 시스템 사용자로 설정 (필요시 실제 사용자 ID로 업데이트 필요)

-- 1. created_by 컬럼 추가 (기본값으로 시스템 사용자 ID 설정)
ALTER TABLE cards ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000';

-- 2. created_by 컬럼에 인덱스 추가 (조회 성능 향상)
CREATE INDEX idx_card_created_by ON cards(created_by);

-- 3. 기존 카드들의 created_by 값을 업데이트 (선택사항)
-- 실제 운영환경에서는 기존 카드들의 생성자를 적절히 설정해야 함
-- UPDATE cards SET created_by = '실제_사용자_ID' WHERE created_by = '00000000-0000-0000-0000-000000000000';

-- 4. 기본값 제거 (새로 생성되는 카드는 반드시 created_by 값을 가져야 함)
ALTER TABLE cards ALTER COLUMN created_by DROP DEFAULT;
