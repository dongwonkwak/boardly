# Boardly 도메인 ID 체계 명세서

## 개요

Boardly 프로젝트에서는 모든 도메인 엔티티의 ID를 **Prefix + ULID** 형식으로 통일하여 사용합니다.

### ULID (Universally Unique Lexicographically Sortable Identifier)

- **길이**: 26자 (Base32 인코딩)
- **문자 집합**: `0123456789ABCDEFGHJKMNPQRSTVWXYZ` (32개 문자, I/L/O/U 제외)
- **특징**:
  - 시간 기반 정렬 가능 (타임스탬프 포함)
  - 대소문자 구분 없음
  - URL 안전 (특수문자 없음)
  - UUID보다 짧고 읽기 쉬움

### ID 구조

```
{prefix}{ulid}
```

- **전체 길이**: 29자 (prefix 3자 + ULID 26자)
- **데이터베이스 타입**: `VARCHAR(32)`
- **정규식 패턴**: `^{prefix}[0-9A-HJKMNP-TV-Z]{26}$`

---

## 도메인별 ID 정의

### 1. 사용자 (User)

- **Prefix**: `usr`
- **패턴**: `usr{ulid}`
- **예시**: `usr01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^usr[0-9A-HJKMNP-TV-Z]{26}$`

### 2. 워크스페이스 (Workspace)

- **Prefix**: `wsp`
- **패턴**: `wsp{ulid}`
- **예시**: `wsp01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^wsp[0-9A-HJKMNP-TV-Z]{26}$`

### 3. 보드 (Board)

- **Prefix**: `brd`
- **패턴**: `brd{ulid}`
- **예시**: `brd01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^brd[0-9A-HJKMNP-TV-Z]{26}$`

### 4. 리스트/컬럼 (List)

- **Prefix**: `lst`
- **패턴**: `lst{ulid}`
- **예시**: `lst01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^lst[0-9A-HJKMNP-TV-Z]{26}$`

### 5. 카드 (Card)

- **Prefix**: `crd`
- **패턴**: `crd{ulid}`
- **예시**: `crd01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^crd[0-9A-HJKMNP-TV-Z]{26}$`

### 6. 라벨 (Label)

- **Prefix**: `lbl`
- **패턴**: `lbl{ulid}`
- **예시**: `lbl01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^lbl[0-9A-HJKMNP-TV-Z]{26}$`

### 7. 댓글 (Comment)

- **Prefix**: `cmt`
- **패턴**: `cmt{ulid}`
- **예시**: `cmt01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^cmt[0-9A-HJKMNP-TV-Z]{26}$`

### 8. 첨부파일 (Attachment)

- **Prefix**: `att`
- **패턴**: `att{ulid}`
- **예시**: `att01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^att[0-9A-HJKMNP-TV-Z]{26}$`

### 9. 활동 로그 (Activity Log)

- **Prefix**: `log`
- **패턴**: `log{ulid}`
- **예시**: `log01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^log[0-9A-HJKMNP-TV-Z]{26}$`

### 9. 체크리스트 (Checklist)

- **Prefix**: `chl`
- **패턴**: `chl{ulid}`
- **예시**: `chl01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^chl[0-9A-HJKMNP-TV-Z]{26}$`

### 10. 체크리스트 아이템 (Checklist Item)

- **Prefix**: `chi`
- **패턴**: `chi{ulid}`
- **예시**: `chi01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^chi[0-9A-HJKMNP-TV-Z]{26}$`

### 11. 초대 (Invitation)

- **Prefix**: `inv`
- **패턴**: `inv{ulid}`
- **예시**: `inv01ARZ3NDEKTSV4RRFFQ69G5FAV`
- **정규식**: `^inv[0-9A-HJKMNP-TV-Z]{26}$`

---

## 데이터베이스 제약조건

각 테이블의 ID 컬럼에는 다음과 같은 CHECK 제약조건을 적용합니다:

```sql
-- 예시: users 테이블
CONSTRAINT users_id_chk CHECK (id ~ '^usr[0-9A-HJKMNP-TV-Z]{26}$')

-- 예시: workspaces 테이블  
CONSTRAINT workspaces_id_chk CHECK (id ~ '^wsp[0-9A-HJKMNP-TV-Z]{26}$')

-- 예시: boards 테이블
CONSTRAINT boards_id_chk CHECK (id ~ '^brd[0-9A-HJKMNP-TV-Z]{26}$')
```

### 현재 누락된 제약조건

다음 테이블들에는 ID 제약조건이 추가되어야 합니다:

```sql
-- labels 테이블
ALTER TABLE labels ADD CONSTRAINT labels_id_chk 
CHECK (id ~ '^lbl[0-9A-HJKMNP-TV-Z]{26}$');

-- comments 테이블
ALTER TABLE comments ADD CONSTRAINT comments_id_chk 
CHECK (id ~ '^cmt[0-9A-HJKMNP-TV-Z]{26}$');

-- attachments 테이블
ALTER TABLE attachments ADD CONSTRAINT attachments_id_chk 
CHECK (id ~ '^att[0-9A-HJKMNP-TV-Z]{26}$');

-- activity_logs 테이블
ALTER TABLE activity_logs ADD CONSTRAINT activity_logs_id_chk 
CHECK (id ~ '^log[0-9A-HJKMNP-TV-Z]{26}$');

-- invites 테이블
ALTER TABLE invites ADD CONSTRAINT invites_id_chk 
CHECK (id ~ '^inv[0-9A-HJKMNP-TV-Z]{26}$');

-- checklists 테이블 (추가)
ALTER TABLE checklists ADD CONSTRAINT checklists_id_chk 
CHECK (id ~ '^chl[0-9A-HJKMNP-TV-Z]{26}$');

-- checklist_items 테이블 (추가)
ALTER TABLE checklist_items ADD CONSTRAINT checklist_items_id_chk 
CHECK (id ~ '^chi[0-9A-HJKMNP-TV-Z]{26}$');
```

---

## 백엔드 구현 가이드

### 1. 공통 상수 정의

`boardly-shared` 모듈에서 ID Prefix 상수를 정의합니다:

```java
public final class DomainIdPrefixes {
    public static final String USER = "usr";
    public static final String WORKSPACE = "wsp";
    public static final String BOARD = "brd";
    public static final String LIST = "lst";
    public static final String CARD = "crd";
    public static final String CHECKLIST = "chl";
    public static final String CHECKLIST_ITEM = "chi";
    public static final String LABEL = "lbl";
    public static final String COMMENT = "cmt";
    public static final String ATTACHMENT = "att";
    public static final String ACTIVITY_LOG = "log";
    public static final String INVITATION = "inv";
    
    private DomainIdPrefixes() {
        throw new UnsupportedOperationException("Utility class");
    }
}
```

### 2. 도메인 ID 클래스

각 도메인의 ID 클래스에서 prefix를 사용합니다:

```java
@Embeddable
public class UserId extends EntityId {
    private static final String PREFIX = DomainIdPrefixes.USER;
    
    protected UserId() {
        super();
    }
    
    private UserId(String value) {
        super(value);
    }
    
    public static UserId generate() {
        return new UserId(PREFIX + Ulid.fast().toString());
    }
    
    public static UserId of(String value) {
        validatePrefix(value, PREFIX);
        return new UserId(value);
    }
}
```

### 3. 검증 메서드

```java
public abstract class EntityId {
    protected static void validatePrefix(String value, String expectedPrefix) {
        if (value == null || !value.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                "Invalid ID format. Expected prefix: " + expectedPrefix);
        }
        
        if (!value.matches("^" + expectedPrefix + "[0-9A-HJKMNP-TV-Z]{26}$")) {
            throw new IllegalArgumentException(
                "Invalid ID format. Expected: " + expectedPrefix + "{26-char-ulid}");
        }
    }
}
```

---

## 프론트엔드 구현 가이드

### 1. TypeScript 타입 정의

```typescript
// types/domain-ids.ts
export type DomainId<T extends string> = `${T}${string}`;

export type UserId = DomainId<'usr'>;
export type WorkspaceId = DomainId<'wsp'>;
export type BoardId = DomainId<'brd'>;
export type ListId = DomainId<'lst'>;
export type CardId = DomainId<'crd'>;
export type ChecklistId = DomainId<'chl'>;
export type ChecklistItemId = DomainId<'chi'>;
export type LabelId = DomainId<'lbl'>;
export type CommentId = DomainId<'cmt'>;
export type AttachmentId = DomainId<'att'>;
export type ActivityLogId = DomainId<'log'>;
export type InvitationId = DomainId<'inv'>;
```

### 2. 검증 함수

```typescript
// utils/id-validation.ts
export const ID_PATTERNS = {
  usr: /^usr[0-9A-HJKMNP-TV-Z]{26}$/,
  wsp: /^wsp[0-9A-HJKMNP-TV-Z]{26}$/,
  brd: /^brd[0-9A-HJKMNP-TV-Z]{26}$/,
  lst: /^lst[0-9A-HJKMNP-TV-Z]{26}$/,
  crd: /^crd[0-9A-HJKMNP-TV-Z]{26}$/,
  chl: /^chl[0-9A-HJKMNP-TV-Z]{26}$/,
  chi: /^chi[0-9A-HJKMNP-TV-Z]{26}$/,
  lbl: /^lbl[0-9A-HJKMNP-TV-Z]{26}$/,
  cmt: /^cmt[0-9A-HJKMNP-TV-Z]{26}$/,
  att: /^att[0-9A-HJKMNP-TV-Z]{26}$/,
  log: /^log[0-9A-HJKMNP-TV-Z]{26}$/,
  inv: /^inv[0-9A-HJKMNP-TV-Z]{26}$/,
} as const;

export function validateDomainId<T extends keyof typeof ID_PATTERNS>(
  id: string, 
  prefix: T
): id is DomainId<T> {
  return ID_PATTERNS[prefix].test(id);
}
```

---

## 마이그레이션 가이드

기존 데이터베이스에 누락된 제약조건을 추가하는 마이그레이션:

### 파일: `V1_2__add_missing_id_constraints.sql`

```sql
-- =============================================================
-- Migration: Add missing ID constraints for domain entities
-- Version: V1.2
-- =============================================================

-- labels 테이블 ID 제약조건 추가
ALTER TABLE labels ADD CONSTRAINT labels_id_chk 
CHECK (id ~ '^lbl[0-9A-HJKMNP-TV-Z]{26}$');

-- comments 테이블 ID 제약조건 추가
ALTER TABLE comments ADD CONSTRAINT comments_id_chk 
CHECK (id ~ '^cmt[0-9A-HJKMNP-TV-Z]{26}$');

-- attachments 테이블 ID 제약조건 추가
ALTER TABLE attachments ADD CONSTRAINT attachments_id_chk 
CHECK (id ~ '^att[0-9A-HJKMNP-TV-Z]{26}$');

-- activity_logs 테이블 ID 제약조건 추가
ALTER TABLE activity_logs ADD CONSTRAINT activity_logs_id_chk 
CHECK (id ~ '^log[0-9A-HJKMNP-TV-Z]{26}$');

-- invites 테이블 ID 제약조건 추가
ALTER TABLE invites ADD CONSTRAINT invites_id_chk 
CHECK (id ~ '^inv[0-9A-HJKMNP-TV-Z]{26}$');

-- checklists 테이블 ID 제약조건 추가
ALTER TABLE checklists ADD CONSTRAINT checklists_id_chk 
CHECK (id ~ '^chl[0-9A-HJKMNP-TV-Z]{26}$');

-- checklist_items 테이블 ID 제약조건 추가
ALTER TABLE checklist_items ADD CONSTRAINT checklist_items_id_chk 
CHECK (id ~ '^chi[0-9A-HJKMNP-TV-Z]{26}$');
```

---

## 합성키 사용 가이드

### 1. 합성키 사용 원칙

일부 도메인 엔티티는 비즈니스 특성상 별도의 대리키 대신 자연키의 조합(합성키)을 사용합니다.

### 2. 합성키 사용 케이스

다음 테이블들은 합성키를 사용합니다:

| 테이블 | 합성키 구성 | 도메인 엔티티 |
|--------|-------------|---------------|
| `workspace_members` | `(workspace_id, user_id)` | `WorkspaceMembership` |
| `board_members` | `(board_id, user_id)` | `BoardMembership` |
| `card_members` | `(card_id, user_id)` | `CardAssignee` |
| `card_labels` | `(card_id, label_id)` | `CardLabel` |

### 3. 합성키 도메인 엔티티 구현

```java
// 합성키 사용 예시: WorkspaceMembership
@EqualsAndHashCode(of = {"workspaceId", "userId"})
public class WorkspaceMembership {
    // 합성키 구성 요소들 (별도 ID 클래스 없음)
    private WorkspaceId workspaceId;  // PK 일부
    private UserId userId;            // PK 일부
    
    // 기타 속성들
    private WorkspaceRole role;
    private Instant joinedAt;
    
    @Builder
    public WorkspaceMembership(WorkspaceId workspaceId, UserId userId, 
                             WorkspaceRole role, Instant joinedAt) {
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }
    
    public static WorkspaceMembership create(WorkspaceId workspaceId, UserId userId, WorkspaceRole role) {
        return WorkspaceMembership.builder()
                .workspaceId(workspaceId)
                .userId(userId)
                .role(role)
                .joinedAt(Instant.now())
                .build();
    }
}
```

### 4. 합성키 vs 대리키 선택 기준

#### 합성키 사용 조건:
- 자연키 조합이 의미적으로 명확한 경우
- 관계 엔티티(Many-to-Many)인 경우
- 자연키가 변경되지 않는 경우

#### 대리키 사용 조건:
- 단일 독립 엔티티인 경우
- 비즈니스 로직이 복잡한 경우
- 엔티티 식별이 복잡한 경우

### 5. 데이터베이스 스키마

합성키 테이블의 스키마 예시:

```sql
-- workspace_members 테이블
CREATE TABLE workspace_members (
  workspace_id   VARCHAR(32) NOT NULL REFERENCES workspaces(id),
  user_id        VARCHAR(32) NOT NULL REFERENCES users(id),
  role           VARCHAR(16) NOT NULL,
  joined_at      TIMESTAMPTZ,
  -- 기타 컬럼들...
  PRIMARY KEY (workspace_id, user_id)  -- 합성키
);

-- card_members 테이블  
CREATE TABLE card_members (
  card_id        VARCHAR(32) NOT NULL REFERENCES cards(id),
  user_id        VARCHAR(32) NOT NULL REFERENCES users(id),
  assigned_at    TIMESTAMPTZ,
  -- 기타 컬럼들...
  PRIMARY KEY (card_id, user_id)  -- 합성키
);
```

---

## 테스트 가이드

### 1. 단위 테스트 예시

```java
@Test
void shouldGenerateValidUserId() {
    // given & when
    UserId userId = UserId.generate();
    
    // then
    assertThat(userId.getValue())
        .matches("^usr[0-9A-HJKMNP-TV-Z]{26}$")
        .hasSize(29); // usr + 26자 ULID
}

@Test
void shouldValidateUserIdFormat() {
    // given
    String validId = "usr01ARZ3NDEKTSV4RRFFQ69G5FAV";
    String invalidId = "invalid_id";
    
    // when & then
    assertDoesNotThrow(() -> UserId.of(validId));
    assertThatThrownBy(() -> UserId.of(invalidId))
        .isInstanceOf(IllegalArgumentException.class);
}
```

### 2. 통합 테스트

```java
@Test
void shouldEnforceIdConstraintInDatabase() {
    // given
    User user = User.builder()
        .id(UserId.of("invalid_id")) // 잘못된 형식
        .email(Email.of("test@example.com"))
        .build();
    
    // when & then
    assertThatThrownBy(() -> userRepository.save(user))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("users_id_chk");
}
```

---

## 모니터링 및 운영

### 1. ID 형식 검증 로그

잘못된 ID 형식이 감지되면 로그를 남깁니다:

```java
@Component
public class IdValidationAspect {
    
    @Before("@annotation(ValidateDomainId)")
    public void validateId(JoinPoint joinPoint) {
        // ID 검증 로직
        if (!isValidFormat(id)) {
            log.error("Invalid domain ID format detected: {}", id);
            // 메트릭 수집, 알림 등
        }
    }
}
```

### 2. 메트릭 수집

- ID 생성 속도
- 형식 오류 발생 빈도
- ULID 충돌 발생 여부 (거의 없지만 모니터링)

---

## 자주 묻는 질문 (FAQ)

### Q1. 왜 UUID 대신 ULID를 사용하나요?

**A:** ULID는 다음과 같은 장점이 있습니다:
- 시간 기반 정렬 가능 (생성 순서 유지)
- 더 짧고 읽기 쉬움 (26자 vs 36자)
- URL 안전 (하이픈 없음)
- 대소문자 구분 없음

### Q2. Prefix를 변경할 수 있나요?

**A:** 가능하지만 다음 사항을 고려해야 합니다:
- 기존 데이터 마이그레이션 필요
- 데이터베이스 제약조건 수정
- 백엔드/프론트엔드 코드 업데이트
- API 응답 형식 변경

### Q3. ID 길이를 늘려야 하는 경우가 있나요?

**A:** 현재 32자(VARCHAR(32))로 충분하지만, 미래에 Prefix가 길어지거나 추가 정보가 필요한 경우 마이그레이션을 통해 변경 가능합니다.

### Q4. ULID 충돌 가능성은?

**A:** ULID는 타임스탬프(48비트) + 랜덤(80비트)으로 구성되어 실질적으로 충돌 가능성이 없습니다. 같은 밀리초에 생성되더라도 랜덤 부분이 다릅니다.

---

## 참고 자료

- [ULID 스펙](https://github.com/ulid/spec)
- [PostgreSQL 정규식 문서](https://www.postgresql.org/docs/current/functions-matching.html)
- [Boardly ERD 문서](./erd.md)
- [Boardly 스키마 문서](./boardly-schema.md)

---

**문서 버전**: 1.1  
**최종 수정일**: 2024-12-20  
**작성자**: Boardly 개발팀

### 변경 이력
- **v1.1 (2024-12-20)**: 체크리스트 관련 prefix 추가, 합성키 사용 가이드 추가
- **v1.0 (2024-12-19)**: 초기 문서 작성
