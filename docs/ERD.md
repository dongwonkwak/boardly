# Boardly Project - ERD (Entity Relationship Diagram)

```mermaid
erDiagram
    USER {
        varchar user_id PK "UUID, 사용자 고유 식별자"
        varchar email UK "NOT NULL, 이메일 주소 (최대 100자)"
        varchar password "NOT NULL, BCrypt 암호화된 비밀번호 (8-20자)"
        varchar first_name "NOT NULL, 이름 (1-50자, 한글/영문)"
        varchar last_name "NOT NULL, 성 (1-50자, 한글/영문)"
        varchar language "DEFAULT 'en', 사용자 언어 설정 (en/ko)"
        datetime created_at "NOT NULL, 생성일시"
        datetime updated_at "NOT NULL, 수정일시"
        boolean is_active "DEFAULT true, 계정 활성화 상태"
    }

    BOARD {
        varchar board_id PK "UUID, 보드 고유 식별자"
        varchar user_id FK "NOT NULL, 보드 소유자"
        varchar title "NOT NULL, 보드 제목 (1-100자)"
        text description "보드 설명 (최대 500자)"
        datetime created_at "NOT NULL, 생성일시"
        datetime updated_at "NOT NULL, 수정일시"
        boolean is_archived "DEFAULT false, 보드 아카이브 상태"
    }

    BOARD_LIST {
        varchar list_id PK "UUID, 리스트 고유 식별자"
        varchar board_id FK "NOT NULL, 소속 보드"
        varchar title "NOT NULL, 리스트 제목 (1-100자)"
        int position "NOT NULL, 리스트 순서 (0부터 시작)"
        varchar color "DEFAULT '#0079BF', 리스트 헤더 색상"
        datetime created_at "NOT NULL, 생성일시"
        datetime updated_at "NOT NULL, 수정일시"
    }

    CARD {
        varchar card_id PK "UUID, 카드 고유 식별자"
        varchar list_id FK "NOT NULL, 소속 리스트"
        varchar title "NOT NULL, 카드 제목 (1-200자)"
        text description "카드 설명 (최대 2000자, 마크다운 지원)"
        int position "NOT NULL, 카드 순서 (0부터 시작)"
        datetime created_at "NOT NULL, 생성일시"
        datetime updated_at "NOT NULL, 수정일시"
        boolean is_archived "DEFAULT false, 카드 아카이브 상태"
    }

    %% 사용자 활동 로그 (확장 기능)
    USER_ACTIVITY {
        varchar activity_id PK "UUID, 활동 로그 고유 식별자"
        varchar user_id FK "NOT NULL, 활동 수행자"
        varchar board_id FK "보드 ID (선택사항)"
        varchar list_id FK "리스트 ID (선택사항)"
        varchar card_id FK "카드 ID (선택사항)"
        varchar activity_type "NOT NULL, 활동 유형 (CREATE/UPDATE/DELETE/MOVE)"
        varchar entity_type "NOT NULL, 엔티티 유형 (BOARD/LIST/CARD)"
        text description "활동 설명"
        json metadata "추가 메타데이터 (JSON)"
        datetime created_at "NOT NULL, 활동 발생일시"
    }

    %% 기본 관계 정의
    USER ||--o{ BOARD : "owns"
    BOARD ||--o{ BOARD_LIST : "contains"
    BOARD_LIST ||--o{ CARD : "contains"
    USER ||--o{ USER_ACTIVITY : "performs"

    %% 확장 기능용 테이블 (Phase 2)
    BOARD_MEMBER {
        varchar board_member_id PK "UUID, 보드 멤버 고유 식별자"
        varchar board_id FK "NOT NULL, 보드 ID"
        varchar user_id FK "NOT NULL, 사용자 ID"
        varchar role "NOT NULL, 권한 (OWNER/MEMBER/VIEWER)"
        datetime created_at "NOT NULL, 초대일시"
        datetime updated_at "NOT NULL, 수정일시"
        boolean is_active "DEFAULT true, 멤버 활성화 상태"
    }

    CARD_COMMENT {
        varchar comment_id PK "UUID, 댓글 고유 식별자"
        varchar card_id FK "NOT NULL, 카드 ID"
        varchar user_id FK "NOT NULL, 작성자 ID"
        text content "NOT NULL, 댓글 내용"
        datetime created_at "NOT NULL, 작성일시"
        datetime updated_at "NOT NULL, 수정일시"
        boolean is_edited "DEFAULT false, 수정 여부"
    }

    LABEL {
        varchar label_id PK "UUID, 라벨 고유 식별자"
        varchar board_id FK "NOT NULL, 보드 ID"
        varchar name "NOT NULL, 라벨명 (1-50자)"
        varchar color "NOT NULL, 라벨 색상 (HEX 코드)"
        datetime created_at "NOT NULL, 생성일시"
        datetime updated_at "NOT NULL, 수정일시"
    }

    CARD_LABEL {
        varchar card_id FK "NOT NULL, 카드 ID"
        varchar label_id FK "NOT NULL, 라벨 ID"
        datetime created_at "NOT NULL, 적용일시"
    }

    %% 파일 첨부 (확장 기능)
    CARD_ATTACHMENT {
        varchar attachment_id PK "UUID, 첨부파일 고유 식별자"
        varchar card_id FK "NOT NULL, 카드 ID"
        varchar user_id FK "NOT NULL, 업로드한 사용자"
        varchar file_name "NOT NULL, 파일명"
        varchar file_path "NOT NULL, 파일 저장 경로"
        varchar file_type "NOT NULL, 파일 MIME 타입"
        bigint file_size "NOT NULL, 파일 크기 (bytes)"
        datetime created_at "NOT NULL, 업로드일시"
    }

    %% 카드 체크리스트 (확장 기능)
    CARD_CHECKLIST {
        varchar checklist_id PK "UUID, 체크리스트 고유 식별자"
        varchar card_id FK "NOT NULL, 카드 ID"
        varchar title "NOT NULL, 체크리스트 제목"
        int position "NOT NULL, 체크리스트 순서"
        datetime created_at "NOT NULL, 생성일시"
        datetime updated_at "NOT NULL, 수정일시"
    }

    CARD_CHECKLIST_ITEM {
        varchar item_id PK "UUID, 체크리스트 아이템 고유 식별자"
        varchar checklist_id FK "NOT NULL, 체크리스트 ID"
        varchar content "NOT NULL, 아이템 내용"
        boolean is_completed "DEFAULT false, 완료 여부"
        int position "NOT NULL, 아이템 순서"
        datetime created_at "NOT NULL, 생성일시"
        datetime updated_at "NOT NULL, 수정일시"
    }

    %% 확장 관계
    USER ||--o{ BOARD_MEMBER : "joins"
    BOARD ||--o{ BOARD_MEMBER : "includes"
    USER ||--o{ CARD_COMMENT : "writes"
    CARD ||--o{ CARD_COMMENT : "has"
    BOARD ||--o{ LABEL : "defines"
    CARD ||--o{ CARD_LABEL : "tagged_with"
    LABEL ||--o{ CARD_LABEL : "applied_to"
    CARD ||--o{ CARD_ATTACHMENT : "has"
    USER ||--o{ CARD_ATTACHMENT : "uploads"
    CARD ||--o{ CARD_CHECKLIST : "has"
    CARD_CHECKLIST ||--o{ CARD_CHECKLIST_ITEM : "contains"
```

## 데이터베이스 제약 조건

### 1. 사용자 제약 조건
- **이메일**: 유효한 이메일 형식, 중복 불가
- **비밀번호**: 8-20자, 영문+숫자+특수문자 포함
- **이름/성**: 1-50자, 한글/영문만 허용

### 2. 보드 제약 조건
- **사용자당 보드 수**: 최대 50개
- **보드 제목**: 1-100자, 필수
- **보드 설명**: 최대 500자

### 3. 리스트 제약 조건
- **보드당 리스트 수**: 최대 20개
- **리스트 제목**: 1-100자, 필수
- **리스트 순서**: 0부터 시작하는 정수

### 4. 카드 제약 조건
- **리스트당 카드 수**: 최대 100개
- **카드 제목**: 1-200자, 필수
- **카드 설명**: 최대 2000자, 마크다운 지원
- **카드 순서**: 0부터 시작하는 정수

### 5. 인덱스 최적화
```sql
-- 성능 최적화를 위한 인덱스
CREATE INDEX idx_board_user_id ON BOARD(user_id);
CREATE INDEX idx_board_list_board_id ON BOARD_LIST(board_id);
CREATE INDEX idx_board_list_position ON BOARD_LIST(board_id, position);
CREATE INDEX idx_card_list_id ON CARD(list_id);
CREATE INDEX idx_card_position ON CARD(list_id, position);
CREATE INDEX idx_user_activity_user_id ON USER_ACTIVITY(user_id);
CREATE INDEX idx_user_activity_created_at ON USER_ACTIVITY(created_at);
```

## 데이터 무결성 규칙

### 1. CASCADE 삭제 규칙
- 사용자 삭제 시 → 소유한 모든 보드 삭제
- 보드 삭제 시 → 포함된 모든 리스트 삭제
- 리스트 삭제 시 → 포함된 모든 카드 삭제
- 카드 삭제 시 → 관련 댓글, 첨부파일, 라벨 연결 삭제

### 2. 외래키 제약 조건
- 모든 FK는 NOT NULL (필수 관계)
- 참조 무결성 보장
- 순환 참조 방지

### 3. 데이터 검증
- 입력 데이터 길이 및 형식 검증
- 비즈니스 로직 검증 (제한 개수 등)
- 중복 데이터 방지

---

**문서 버전**: v1.1  
**최종 수정일**: 2025년 1월 17일  
**작성자**: Boardly 개발팀