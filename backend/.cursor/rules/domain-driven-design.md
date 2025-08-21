# 도메인 주도 설계(DDD) 원칙 및 규칙

## 1. 전략적 설계 (Strategic Design) 규칙

### 1.1 도메인 모델링
- **규칙 1**: 비즈니스 도메인을 우선으로 설계하라
  - 기술적 관심사보다 비즈니스 로직을 중심으로 모델링
  - 도메인 전문가와 지속적으로 소통하여 모델을 발전시킬 것

- **규칙 2**: 유비쿼터스 언어(Ubiquitous Language)를 구축하라
  - 개발팀과 도메인 전문가가 동일한 용어 사용
  - 코드, 문서, 대화에서 일관된 언어 사용
  - 새로운 통찰이 생기면 언어를 발전시킬 것

### 1.2 바운디드 컨텍스트 (Bounded Context)
- **규칙 3**: 명확한 경계를 설정하라
  - 각 바운디드 컨텍스트는 하나의 일관된 모델을 가져야 함
  - 컨텍스트 간의 경계를 명시적으로 정의
  - 같은 용어라도 컨텍스트마다 다른 의미를 가질 수 있음을 인정

- **규칙 4**: 컨텍스트 맵을 유지하라
  - 바운디드 컨텍스트 간의 관계를 시각화
  - 통합 패턴을 명시적으로 정의 (Shared Kernel, Customer-Supplier 등)

## 2. 전술적 설계 (Tactical Design) 규칙

### 2.1 엔티티 (Entity)
- **규칙 5**: 식별성이 중요한 객체만 엔티티로 설계하라
  - 생명주기 동안 추적되어야 하는 객체
  - 고유한 식별자를 반드시 가져야 함
  - 속성보다 식별성이 더 중요

```java
// 좋은 예시 - Entity
public class User {
    private final UserId id;  // 고유 식별자
    private String name;
    private Email email;
    
    public User(UserId id, String name, Email email) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
    }
    
    // 비즈니스 로직
    public void changeEmail(Email newEmail) {
        validateEmailChange(newEmail);
        this.email = newEmail;
        // 도메인 이벤트 발행
        DomainEvents.publish(new UserEmailChanged(this.id, newEmail));
    }
}
```

### 2.2 값 객체 (Value Object)
- **규칙 6**: 불변성을 보장하라
  - 생성 후 상태가 변경되지 않아야 함
  - 변경이 필요하면 새로운 인스턴스를 생성
  - 값의 동등성은 모든 속성으로 판단

- **규칙 7**: 도메인 개념을 명시적으로 표현하라
  - 원시 타입 대신 의미 있는 값 객체 사용
  - 예: String email → Email 객체

```java
// 좋은 예시 - Value Object
public class Email {
    private final String value;
    
    public Email(String value) {
        validate(value);
        this.value = value;
    }
    
    private void validate(String value) {
        if (value == null || !value.contains("@")) {
            throw new InvalidEmailException("Invalid email format: " + value);
        }
    }
    
    // 불변성 보장
    public String getValue() {
        return value;
    }
    
    // 값 객체 동등성
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

// 나쁜 예시 - 원시 타입 집착
public class User {
    private String email;  // String 대신 Email 값 객체 사용해야 함
}
```

### 2.3 애그리게이트 (Aggregate)
- **규칙 8**: 일관성 경계를 명확히 하라
  - 트랜잭션 일관성이 필요한 객체들을 그룹화
  - 애그리게이트 루트를 통해서만 내부 객체에 접근
  - 애그리게이트는 가능한 한 작게 유지

- **규칙 9**: 애그리게이트 간 참조는 ID로만 하라
  - 직접 객체 참조 대신 식별자 사용
  - 애그리게이트 간의 결합도 최소화

```java
// 좋은 예시 - Aggregate
public class Board {  // Aggregate Root
    private final BoardId id;
    private String title;
    private UserId ownerId;  // 다른 애그리게이트는 ID로만 참조
    private List<BoardMember> members;  // 내부 엔티티
    
    public Board(BoardId id, String title, UserId ownerId) {
        this.id = id;
        this.title = title;
        this.ownerId = ownerId;
        this.members = new ArrayList<>();
    }
    
    // 애그리게이트 루트를 통한 접근
    public void addMember(UserId userId, BoardRole role) {
        validateMemberAddition(userId, role);
        BoardMember member = new BoardMember(userId, role);
        this.members.add(member);
        
        DomainEvents.publish(new BoardMemberAdded(this.id, userId, role));
    }
    
    // 불변 리스트 반환으로 캡슐화 보장
    public List<BoardMember> getMembers() {
        return Collections.unmodifiableList(members);
    }
}

// 나쁜 예시 - 직접 객체 참조
public class Board {
    private User owner;  // User 애그리게이트 직접 참조 - 피해야 함
}
```

### 2.4 도메인 서비스 (Domain Service)
- **규칙 10**: 엔티티나 값 객체에 자연스럽게 속하지 않는 도메인 로직만 포함하라
  - 여러 애그리게이트에 걸친 비즈니스 규칙
  - 상태를 가지지 않는 순수한 도메인 로직
  - 도메인 전문가가 이해할 수 있는 개념

```java
// 좋은 예시 - Domain Service
public class BoardAccessPolicyService {
    
    public boolean canUserAccessBoard(User user, Board board) {
        // 여러 애그리게이트에 걸친 비즈니스 규칙
        if (board.getOwnerId().equals(user.getId())) {
            return true;
        }
        
        return board.getMembers().stream()
            .anyMatch(member -> member.getUserId().equals(user.getId())
                && member.hasReadAccess());
    }
    
    public BoardRole calculateUserRole(User user, Board board) {
        // 복잡한 도메인 로직
        if (board.getOwnerId().equals(user.getId())) {
            return BoardRole.OWNER;
        }
        
        return board.getMembers().stream()
            .filter(member -> member.getUserId().equals(user.getId()))
            .map(BoardMember::getRole)
            .findFirst()
            .orElse(BoardRole.NONE);
    }
}
```

### 2.5 저장소 (Repository)
- **규칙 11**: 컬렉션처럼 동작하게 설계하라
  - 도메인 모델 관점에서 설계
  - 영속성 관심사를 숨김
  - 애그리게이트 루트에 대해서만 저장소 제공

```java
// 좋은 예시 - Repository 인터페이스 (도메인 계층)
public interface UserRepository {
    Optional<User> findById(UserId userId);
    Optional<User> findByEmail(Email email);
    void save(User user);
    void delete(User user);
    List<User> findByRole(UserRole role);
}

// 나쁜 예시 - 기술적 관심사 노출
public interface UserRepository {
    User findUserByIdWithFetch(String userId);  // 기술적 세부사항 노출
    void saveWithTransaction(User user);         // 영속성 관심사 노출
}
```

### 2.6 도메인 이벤트 (Domain Event)
- **규칙 12**: 도메인에서 발생한 중요한 사건을 이벤트로 표현하라
  - 도메인 전문가가 관심 있어하는 사건
  - 불변 객체로 설계
  - 과거형으로 명명 (UserRegistered, OrderCompleted)

```java
// 좋은 예시 - Domain Event
public class UserEmailChanged implements DomainEvent {
    private final UserId userId;
    private final Email oldEmail;
    private final Email newEmail;
    private final Instant occurredOn;
    
    public UserEmailChanged(UserId userId, Email oldEmail, Email newEmail) {
        this.userId = userId;
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
        this.occurredOn = Instant.now();
    }
    
    // 불변 객체
    public UserId getUserId() { return userId; }
    public Email getOldEmail() { return oldEmail; }
    public Email getNewEmail() { return newEmail; }
    public Instant getOccurredOn() { return occurredOn; }
}

// 도메인 이벤트 발행
public class User {
    public void changeEmail(Email newEmail) {
        Email oldEmail = this.email;
        this.email = newEmail;
        
        // 도메인 이벤트 발행
        DomainEvents.publish(new UserEmailChanged(this.id, oldEmail, newEmail));
    }
}
```

## 3. 아키텍처 규칙

### 3.1 계층화 아키텍처
- **규칙 13**: 의존성 방향을 준수하라
  - 도메인 계층은 다른 계층에 의존하지 않음
  - 응용 서비스는 도메인에만 의존
  - 인프라스트럭처는 가장 바깥쪽 계층

- **규칙 14**: 도메인 로직을 도메인 계층에 집중시켜라
  - 비즈니스 규칙은 도메인 모델에 위치
  - 응용 서비스는 얇게 유지 (조정 역할만)

```java
// 좋은 예시 - 도메인 로직이 도메인 모델에 위치
public class User {
    public void changePassword(String currentPassword, String newPassword) {
        if (!this.password.matches(currentPassword)) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        if (newPassword.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters");
        }
        
        this.password = Password.encode(newPassword);
        DomainEvents.publish(new UserPasswordChanged(this.id));
    }
}

// 나쁜 예시 - 응용 서비스에 도메인 로직
@Service
public class UserApplicationService {
    public void changePassword(UserId userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId);
        
        // 도메인 로직이 응용 서비스에 위치 - 잘못됨
        if (!user.getPassword().matches(currentPassword)) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        if (newPassword.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters");
        }
        
        user.setPassword(Password.encode(newPassword));
        userRepository.save(user);
    }
}
```

### 3.2 헥사고날 아키텍처
- **규칙 15**: 포트와 어댑터 패턴을 적용하라
  - 도메인을 중심으로 외부 시스템과 분리
  - 인터페이스(포트)와 구현체(어댑터) 분리

## 4. 구현 규칙

### 4.1 테스트
- **규칙 16**: 도메인 로직을 철저히 테스트하라
  - 비즈니스 규칙에 대한 단위 테스트 작성
  - 도메인 모델의 동작을 검증
  - 모킹보다는 실제 도메인 객체 사용

```java
// 좋은 예시 - 도메인 로직 테스트
class UserTest {
    
    @Test
    void should_change_email_when_valid_email_provided() {
        // Given
        User user = new User(
            new UserId("user-123"),
            "John Doe",
            new Email("old@example.com")
        );
        Email newEmail = new Email("new@example.com");
        
        // When
        user.changeEmail(newEmail);
        
        // Then
        assertThat(user.getEmail()).isEqualTo(newEmail);
    }
    
    @Test
    void should_throw_exception_when_invalid_email_provided() {
        // Given
        User user = new User(
            new UserId("user-123"),
            "John Doe",
            new Email("old@example.com")
        );
        
        // When & Then
        assertThatThrownBy(() -> user.changeEmail(new Email("invalid-email")))
            .isInstanceOf(InvalidEmailException.class);
    }
}
```

### 4.2 리팩토링
- **규칙 17**: 지속적으로 모델을 개선하라
  - 새로운 도메인 통찰이 생기면 코드에 반영
  - 복잡성이 증가하면 모델을 단순화
  - 중복 제거보다 명확성을 우선시

### 4.3 성능
- **규칙 18**: 성능 최적화는 도메인 무결성을 해치지 않는 선에서 하라
  - 도메인 모델의 순수성을 유지
  - 성능 문제는 인프라스트럭처 계층에서 해결
  - CQRS 패턴 고려

## 5. 팀 협업 규칙

### 5.1 도메인 전문가와의 협업
- **규칙 19**: 정기적으로 도메인 전문가와 소통하라
  - 이벤트 스토밍 세션 진행
  - 모델의 변화를 함께 검토
  - 도메인 지식을 지속적으로 학습

### 5.2 코드 리뷰
- **규칙 20**: DDD 원칙 준수를 리뷰 기준에 포함하라
  - 도메인 로직이 올바른 위치에 있는지 확인
  - 유비쿼터스 언어 사용 여부 점검
  - 애그리게이트 경계가 적절한지 검토

## 6. 안티 패턴 (하지 말아야 할 것들)

- **금지사항 1**: 빈약한 도메인 모델 (Anemic Domain Model) 만들지 말 것
```java
// 나쁜 예시 - Anemic Domain Model
public class User {
    private String id;
    private String name;
    private String email;
    
    // getter, setter만 있고 비즈니스 로직 없음
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    // ...
}

// 좋은 예시 - Rich Domain Model
public class User {
    private final UserId id;
    private String name;
    private Email email;
    
    // 비즈니스 로직 포함
    public void changeEmail(Email newEmail) {
        validateEmailChange(newEmail);
        this.email = newEmail;
        DomainEvents.publish(new UserEmailChanged(this.id, newEmail));
    }
}
```

- **금지사항 2**: 기술적 관심사를 도메인에 섞지 말 것
```java
// 나쁜 예시 - 기술적 관심사 혼재
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;  // JPA 어노테이션이 도메인에 섞임
    
    public void changeEmail(String newEmail) {
        // HTTP 호출이 도메인에 위치 - 잘못됨
        httpClient.post("/notification", emailChangeData);
    }
}
```

- **금지사항 3**: 모든 곳에 DDD를 적용하려 하지 말 것 (복잡한 도메인에만 적용)
- **금지사항 4**: 큰 애그리게이트를 만들지 말 것
- **금지사항 5**: 데이터베이스 스키마부터 설계하지 말 것

## 7. AI 코딩 가이드라인

### DDD 원칙을 따르는 코드 생성 시 준수사항
1. **도메인 모델 중심**: 기술보다 비즈니스 로직을 우선
2. **유비쿼터스 언어**: 도메인 용어를 클래스명, 메서드명에 반영
3. **애그리게이트 경계**: 트랜잭션 일관성 경계를 명확히 설정
4. **값 객체 활용**: 원시 타입 대신 의미 있는 값 객체 사용
5. **도메인 이벤트**: 중요한 비즈니스 사건을 이벤트로 표현
6. **순수 도메인**: 외부 의존성 없는 순수한 도메인 모델 구현

### 금지사항
1. **도메인에 인프라스트럭처 코드 포함 금지**
2. **빈약한 도메인 모델 생성 금지**
3. **큰 애그리게이트 설계 금지**
4. **기술적 관심사와 도메인 로직 혼재 금지**

## 8. 체크리스트

### 설계 검토 시 확인사항
- [ ] 비즈니스 규칙이 도메인 모델에 표현되어 있는가?
- [ ] 유비쿼터스 언어가 코드에 반영되어 있는가?
- [ ] 애그리게이트 경계가 명확한가?
- [ ] 도메인 서비스의 책임이 명확한가?
- [ ] 의존성 방향이 올바른가?
- [ ] 도메인 이벤트가 적절히 활용되고 있는가?
- [ ] 값 객체가 적절히 사용되고 있는가?
- [ ] 엔티티와 값 객체의 구분이 명확한가?
- [ ] 도메인 로직이 도메인 계층에 위치하는가?
- [ ] 애그리게이트 간 참조가 ID로 이루어지는가?

이러한 규칙들을 점진적으로 적용하며, 팀의 상황과 도메인의 복잡성에 따라 유연하게 조정하여 사용하시기 바랍니다.
