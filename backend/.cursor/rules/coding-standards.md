# Java/Spring Boot 멀티모듈 + 헥사고날 아키텍처 코딩 컨벤션

## 📂 모듈 구조 및 네이밍

### 모듈 네이밍 규칙
```
boardly/
├── boardly-domain/              # 도메인 핵심 로직 (엔티티, 도메인 서비스)
├── boardly-application/         # Use Case 구현체, Command, Validator
├── boardly-infrastructure/      # 외부 시스템 연동 (JPA, 메시징 등)
├── boardly-api/                # REST API 컨트롤러
├── boardly-app/                # 메인 애플리케이션 (설정, 실행)
└── boardly-scheme/             # 데이터베이스 스키마 관리
```

### 패키지 구조
```java
com.boardly
├── domain                       # boardly-domain 모듈
│   ├── model
│   │   ├── user               # 사용자 도메인
│   │   │   ├── User.java
│   │   │   ├── UserId.java    # Value Object
│   │   │   └── UserStatus.java
│   │   └── board              # 게시판 도메인
│   │       ├── Board.java
│   │       ├── BoardId.java   # Value Object
│   │       └── Comment.java
│   ├── service                # 도메인 서비스
│   │   ├── UserDomainService.java
│   │   └── BoardDomainService.java
│   └── repository             # 리포지토리 인터페이스 (아웃바운드 포트)
│       ├── UserRepository.java
│       └── BoardRepository.java
├── application                  # boardly-application 모듈
│   ├── port
│   │   └── in                 # 인바운드 포트 (Use Case 인터페이스)
│   │       ├── user
│   │       │   ├── CreateUserUseCase.java
│   │       │   └── FindUserUseCase.java
│   │       └── board
│   │           ├── CreateBoardUseCase.java
│   │           └── DeleteBoardUseCase.java
│   ├── usecase                # Use Case 구현체
│   │   ├── user
│   │   │   ├── CreateUserUseCaseImpl.java
│   │   │   └── FindUserUseCaseImpl.java
│   │   └── board
│   │       ├── CreateBoardUseCaseImpl.java
│   │       └── DeleteBoardUseCaseImpl.java
│   ├── command                # Command 객체
│   │   ├── user
│   │   │   ├── CreateUserCommand.java
│   │   │   └── UpdateUserCommand.java
│   │   └── board
│   │       ├── CreateBoardCommand.java
│   │       └── DeleteBoardCommand.java
│   └── validator              # 입력값 검증
│       ├── common
│       │   ├── CommonFieldValidator.java
│       │   └── ValidationHelper.java
│       ├── user
│       │   ├── CreateUserCommandValidator.java
│       │   └── UpdateUserCommandValidator.java
│       └── board
│           ├── CreateBoardCommandValidator.java
│           └── DeleteBoardCommandValidator.java
├── infrastructure              # boardly-infrastructure 모듈
│   ├── persistence
│   │   ├── user
│   │   │   ├── UserEntity.java
│   │   │   ├── UserJpaRepository.java
│   │   │   └── UserRepositoryImpl.java
│   │   └── board
│   │       ├── BoardEntity.java
│   │       ├── BoardJpaRepository.java
│   │       └── BoardRepositoryImpl.java
│   └── messaging
│       ├── event
│       └── publisher
└── api                         # boardly-api 모듈
    ├── controller
    │   ├── user
    │   │   └── UserController.java
    │   └── board
    │       └── BoardController.java
    ├── dto
    │   ├── request
    │   │   ├── CreateUserRequest.java
    │   │   └── CreateBoardRequest.java
    │   └── response
    │       ├── UserResponse.java
    │       └── BoardResponse.java
    └── config
        ├── WebConfig.java
        └── SecurityConfig.java
```

## 🏗️ 헥사고날 아키텍처 레이어별 규칙

### Domain 레이어 (도메인)
```java
// ✅ 좋은 예시 - 도메인 엔티티 (순수 자바 객체)
public class User {
    private final UserId id;
    private String email;
    private String password;
    private UserStatus status;
    
    // 도메인 로직
    public Either<Failure, Void> changePassword(String newPassword) {
        return validatePassword(newPassword)
            .map(validPassword -> {
                this.password = encryptPassword(validPassword);
                return null;
            });
    }
    
    private Either<Failure, String> validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return Either.left(Failure.BusinessRuleViolation("user.password.invalid"));
        }
        return Either.right(password);
    }
}

// ✅ 좋은 예시 - Value Object (ID)
public record UserId(Long value) {
    public UserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("UserId must be positive");
        }
    }
    
    public static Either<String, UserId> create(Long value) {
        try {
            return Either.right(new UserId(value));
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }
    
    public static UserId of(Long value) {
        return new UserId(value);
    }
}

// ✅ 좋은 예시 - 인바운드 포트 (UseCase 인터페이스)
public interface CreateUserUseCase {
    Either<Failure, User> createUser(CreateUserCommand command);
}

// ✅ 좋은 예시 - 아웃바운드 포트 (Repository 인터페이스)
public interface UserRepository {
    Optional<User> findById(UserId id);                    // 단순 조회
    Optional<User> findByEmail(String email);              // 단순 조회
    Either<Failure, User> save(User user);                 // 제약 조건 위반 가능
    Either<Failure, User> update(User user);               // 낙관적 락킹 실패 가능
    Either<Failure, Void> deleteById(UserId id);           // 참조 무결성 위반 가능
    boolean existsByEmail(String email);                   // 단순 존재 여부
}
```

### Application 레이어
```java
// ✅ 좋은 예시 - Use Case 구현체 (모든 메서드는 Either 리턴)
@Component
@Transactional
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements CreateUserUseCase {
    
    private final CreateUserCommandValidator validator;
    private final UserRepository userRepository;
    private final ValidationMessageResolver messageResolver;
    
    @Override
    public Either<Failure, User> createUser(CreateUserCommand command) {
        // 1. 입력값 검증
        Validation<Seq<String>, CreateUserCommand> validation = validator.validate(command);
        if (validation.isInvalid()) {
            List<String> errors = validation.getError().asJava();
            return Either.left(Failure.InputError(errors));
        }
        
        CreateUserCommand validCommand = validation.get();
        
        // 2. 비즈니스 로직 실행 (Either 체이닝)
        return checkEmailDuplication(validCommand.email())
            .flatMap(email -> createAndSaveUser(validCommand));
    }
    
    private Either<Failure, String> checkEmailDuplication(String email) {
        return userRepository.existsByEmail(email)
            ? Either.left(Failure.ResourceConflict(
                messageResolver.getMessage("user.email.already.exists")))
            : Either.right(email);
    }
    
    private Either<Failure, User> createAndSaveUser(CreateUserCommand command) {
        User user = User.builder()
            .id(UserId.generate())  // 새 ID 생성
            .email(command.email())
            .firstName(command.firstName())
            .lastName(command.lastName())
            .password(encryptPassword(command.password()))
            .build();
            
        return userRepository.save(user);
    }
}

// ✅ 좋은 예시 - Command 객체
public record CreateUserCommand(
    String email,
    String firstName,
    String lastName,
    String password
) {}

// ✅ 좋은 예시 - Vavr Validator
@Component
@RequiredArgsConstructor
public class CreateUserCommandValidator {
    
    private final CommonFieldValidator commonValidator;
    
    public Validation<Seq<String>, CreateUserCommand> validate(CreateUserCommand command) {
        return Validation.combine(
            commonValidator.validateEmail(command.email()),
            commonValidator.validateName(command.firstName(), "firstName"),
            commonValidator.validateName(command.lastName(), "lastName"),
            commonValidator.validatePassword(command.password())
        ).ap(CreateUserCommand::new);
    }
}
```

### Infrastructure 레이어 (Adapter)
```java
// ✅ 좋은 예시 - JPA 엔티티 (Infrastructure 레이어)
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {
    @Id
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String firstName;
    
    // 도메인 모델로 변환
    public User toDomain() {
        return User.builder()
            .id(UserId.of(this.id))
            .email(this.email)
            .firstName(this.firstName)
            .lastName(this.lastName)
            .build();
    }
    
    // 도메인 모델에서 엔티티 생성
    public static UserEntity from(User user) {
        return UserEntity.builder()
            .id(user.getId().value())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .build();
    }
}

// ✅ 좋은 예시 - Repository 구현체
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository userJpaRepository;
    private final ValidationMessageResolver messageResolver;
    
    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.value())
            .map(UserEntity::toDomain);
    }
    
    @Override
    public Either<Failure, User> save(User user) {
        try {
            UserEntity entity = UserEntity.from(user);
            UserEntity savedEntity = userJpaRepository.save(entity);
            return Either.right(savedEntity.toDomain());
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("email")) {
                return Either.left(Failure.ResourceConflict(
                    messageResolver.getMessage("user.email.already.exists")));
            }
            return Either.left(Failure.InternalError(
                messageResolver.getMessage("user.save.failed")));
        }
    }
}
```

### API 레이어 (Controller)
```java
// ✅ 좋은 예시 - REST 컨트롤러
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final CreateUserUseCase createUserUseCase;
    
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        CreateUserCommand command = CreateUserCommand.builder()
            .email(request.email())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .password(request.password())
            .build();
            
        return createUserUseCase.createUser(command)
            .map(user -> ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(user)))
            .getOrElseGet(failure -> ResponseEntity
                .status(failure.getHttpStatus())
                .body(ErrorResponse.from(failure)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return UserId.create(id)
            .flatMap(userId -> findUserUseCase.findById(userId))
            .map(user -> ResponseEntity.ok(UserResponse.from(user)))
            .getOrElseGet(failure -> ResponseEntity
                .status(failure.getHttpStatus())
                .body(ErrorResponse.from(failure)));
    }
}
```

## 📝 네이밍 컨벤션

### 클래스 네이밍
```java
// 도메인 모델
public class User { }                    // 명사, PascalCase
public record UserId(Long value) { }     // 명사 + Id

// Use Case (인바운드 포트)
public interface CreateUserUseCase { }   // 동사 + 명사 + UseCase
public interface FindUserUseCase { }

// 리포지토리 (아웃바운드 포트)
public interface UserRepository { }      // 명사 + Repository

// 서비스
public class UserDomainService { }       // 명사 + DomainService
public class CreateUserUseCaseImpl { }   // UseCase 구현체는 Impl 접미사

// 컨트롤러 (항상 ResponseEntity<?> 리턴)
public class UserController { }          // 명사 + Controller
// 모든 메서드는 public ResponseEntity<?> methodName() 형식

// DTO
public record CreateUserCommand() { }    // 동사 + 명사 + Command
public record UserResponse() { }         // 명사 + Response
public record CreateUserRequest() { }    // 동사 + 명사 + Request

// Validator
public class CreateUserCommandValidator { }  // Command명 + Validator
public class CommonFieldValidator { }        // 공통 검증은 Common + 대상 + Validator
```

### 메서드 네이밍
```java
// ✅ 좋은 예시
public User createUser(CreateUserCommand command) { }    // create, update, delete
public Optional<User> findById(Long id) { }              // find, get
public List<User> findAllByStatus(UserStatus status) { } // findAllBy, findBy
public boolean existsByEmail(String email) { }           // existsBy
public void validateUser(User user) { }                  // validate
public String encryptPassword(String password) { }       // 동사 + 명사

// ❌ 나쁜 예시
public User makeUser() { }              // 모호한 동사
public User getUserData() { }           // 불필요한 접미사
public boolean checkEmail() { }         // check 대신 exists 사용
```

### 변수 네이밍
```java
// ✅ 좋은 예시
private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;
private static final int MAX_LOGIN_ATTEMPTS = 3;
private static final String DEFAULT_USER_ROLE = "USER";

List<User> activeUsers = findActiveUsers();
Optional<User> foundUser = userRepository.findById(id);

// ❌ 나쁜 예시
private final UserRepository repo;      // 축약 금지
private final PasswordEncoder pwdEnc;   // 축약 금지
List<User> list = findActiveUsers();    // 모호한 네이밍
```

## 🎯 어노테이션 사용 규칙

### 스프링 어노테이션
```java
// Core 레이어 - 스프링 어노테이션 최소화
@Component  // 도메인 서비스에만 사용
public class UserDomainService { }

// Application 레이어
@Service    // Use Case 구현체
@Transactional
@RequiredArgsConstructor
public class CreateUserUseCaseImpl { }

// Adapter 레이어
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController { }

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl { }
```

### Vavr Validation 사용 규칙
```java
// ✅ Command별 Validator 생성
@Component
@RequiredArgsConstructor
public class CreateUserCommandValidator {
    
    private final CommonFieldValidator commonValidator;
    
    public Validation<Seq<String>, CreateUserCommand> validate(CreateUserCommand command) {
        return Validation.combine(
            commonValidator.validateEmail(command.email()),
            commonValidator.validateName(command.firstName(), "firstName"),
            commonValidator.validateName(command.lastName(), "lastName"),
            commonValidator.validatePassword(command.password())
        ).ap(CreateUserCommand::new);
    }
}

// ✅ 공통 필드 Validator
@Component
@RequiredArgsConstructor
public class CommonFieldValidator {
    
    private final ValidationMessageResolver messageResolver;
    
    public Validation<String, String> validateEmail(String email) {
        return Validation.combine(
            notBlank(email, "user.email.required"),
            emailFormat(email, "user.email.invalid"),
            maxLength(email, 100, "user.email.too.long")
        ).ap((e1, e2, e3) -> email);
    }
    
    public Validation<String, String> validatePassword(String password) {
        return Validation.combine(
            notBlank(password, "user.password.required"),
            minLength(password, 8, "user.password.too.short"),
            hasUpperCase(password, "user.password.no.uppercase"),
            hasDigit(password, "user.password.no.digit")
        ).ap((p1, p2, p3, p4) -> password);
    }
    
    public Validation<String, String> validateName(String name, String fieldType) {
        return Validation.combine(
            notBlank(name, "user." + fieldType + ".required"),
            maxLength(name, 50, "user." + fieldType + ".too.long"),
            noSpecialChars(name, "user." + fieldType + ".invalid.chars")
        ).ap((n1, n2, n3) -> name);
    }
    
    // 기본 검증 메서드들
    private Validation<String, String> notBlank(String value, String messageKey) {
        return value != null && !value.trim().isEmpty() 
            ? Validation.valid(value)
            : Validation.invalid(messageResolver.getMessage(messageKey));
    }
    
    private Validation<String, String> emailFormat(String email, String messageKey) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            ? Validation.valid(email)
            : Validation.invalid(messageResolver.getMessage(messageKey));
    }
    
    private Validation<String, String> minLength(String value, int minLength, String messageKey) {
        return value != null && value.length() >= minLength
            ? Validation.valid(value)
            : Validation.invalid(messageResolver.getMessage(messageKey));
    }
    
    private Validation<String, String> maxLength(String value, int maxLength, String messageKey) {
        return value != null && value.length() <= maxLength
            ? Validation.valid(value)
            : Validation.invalid(messageResolver.getMessage(messageKey));
    }
}

// ✅ ID Value Object별 검증이 필요한 경우 (선택적)
@Component
@RequiredArgsConstructor
public class DeleteBoardCommandValidator {
    
    public Validation<Seq<String>, DeleteBoardCommand> validate(DeleteBoardCommand command) {
        return Validation.combine(
            validateUserId(command.userId()),
            validateBoardId(command.boardId())
        ).ap(DeleteBoardCommand::new);
    }
    
    private Validation<String, UserId> validateUserId(UserId userId) {
        return userId != null 
            ? Validation.valid(userId)
            : Validation.invalid("User ID is required");
    }
    
    private Validation<String, BoardId> validateBoardId(BoardId boardId) {
        return boardId != null 
            ? Validation.valid(boardId)
            : Validation.invalid("Board ID is required");
    }
}

## 🔧 예외 처리 규칙

### Either<Failure, T> 사용시 예외 금지
```java
// ✅ 좋은 예시 - 모든 오류를 Either.left()로 처리
@Override
public Either<Failure, User> createUser(CreateUserCommand command) {
    // 입력값 검증 실패 → Either.left()
    if (command.email() == null || command.email().isEmpty()) {
        return Either.left(Failure.InputError(
            messageResolver.getMessage("user.email.required")));
    }
    
    // NullPointerException 대신 Either.left()
    if (command == null) {
        return Either.left(Failure.InputError(
            messageResolver.getMessage("command.required")));
    }
    
    // IllegalArgumentException 대신 Either.left()
    if (command.password() != null && command.password().length() < 8) {
        return Either.left(Failure.BusinessRuleViolation(
            messageResolver.getMessage("user.password.too.short")));
    }
    
    return Either.right(createdUser);
}

// ❌ 나쁜 예시 - Either 사용시 예외 던지기 금지
@Override
public Either<Failure, User> createUser(CreateUserCommand command) {
    if (command == null) {
        throw new IllegalArgumentException("Command cannot be null");  // ❌ 금지!
    }
    
    if (command.email() == null) {
        throw new NullPointerException("Email is required");           // ❌ 금지!
    }
    
    if (userRepository.existsByEmail(command.email())) {
        throw new DuplicateEmailException("Email exists");             // ❌ 금지!
    }
    
    return Either.right(user);
}
```

### Failure 클래스 기반 오류 처리
```java
// ✅ ValidationMessageResolver 사용한 메시지 처리
@Component
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements CreateUserUseCase {
    
    private final ValidationMessageResolver messageResolver;
    
    @Override
    public Either<Failure, User> createUser(CreateUserCommand command) {
        // Failure.InputError - 400 Bad Request
        if (hasInvalidInput(command)) {
            return Either.left(Failure.InputError(
                messageResolver.getMessage("user.input.invalid")));
        }
        
        // Failure.PermissionDenied - 403 Forbidden
        if (!hasPermission(command.requesterId())) {
            return Either.left(Failure.PermissionDenied(
                messageResolver.getMessage("user.create.permission.denied")));
        }
        
        // Failure.NotFound - 404 Not Found
        if (!departmentExists(command.departmentId())) {
            return Either.left(Failure.NotFound(
                messageResolver.getMessage("department.not.found")));
        }
        
        // Failure.ResourceConflict - 409 Conflict
        if (userRepository.existsByEmail(command.email())) {
            return Either.left(Failure.ResourceConflict(
                messageResolver.getMessage("user.email.already.exists")));
        }
        
        // Failure.PreconditionFailed - 412 Precondition Failed
        if (versionMismatch(command)) {
            return Either.left(Failure.PreconditionFailed(
                messageResolver.getMessage("user.version.mismatch")));
        }
        
        // Failure.BusinessRuleViolation - 422 Unprocessable Entity
        if (violatesBusinessRule(command)) {
            return Either.left(Failure.BusinessRuleViolation(
                messageResolver.getMessage("user.business.rule.violation")));
        }
        
        // 기타 예상하지 못한 오류는 InternalError로 처리
        try {
            return createAndSaveUser(command);
        } catch (Exception e) {
            log.error("Unexpected error during user creation", e);
            return Either.left(Failure.InternalError(
                messageResolver.getMessage("user.create.internal.error")));
        }
    }
}
```

### 글로벌 예외 처리 없음
```java
// ❌ 글로벌 예외 처리 사용 안함
// @RestControllerAdvice 사용하지 않음
// @ExceptionHandler 사용하지 않음

// ✅ Controller에서 Either 결과만 처리
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    // 모든 메서드는 Either 결과를 직접 처리
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        CreateUserCommand command = CreateUserCommand.from(request);
        
        return createUserUseCase.createUser(command)
            .map(user -> ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(user)))
            .getOrElseGet(failure -> ResponseEntity
                .status(failure.getHttpStatus())
                .body(ErrorResponse.from(failure)));
    }
}
```

### messages.properties 다국어 메시지
```properties
# 입력 오류
user.email.required=이메일은 필수입니다
user.email.invalid=올바른 이메일 형식이 아닙니다
user.password.required=비밀번호는 필수입니다
user.password.too.short=비밀번호는 최소 8자 이상이어야 합니다

# 비즈니스 오류
user.email.already.exists=이미 존재하는 이메일입니다
user.not.found=사용자를 찾을 수 없습니다
user.create.permission.denied=사용자 생성 권한이 없습니다

# 내부 오류
user.create.internal.error=사용자 생성 중 오류가 발생했습니다
user.save.failed=사용자 저장에 실패했습니다
```

## 📊 테스트 코드 규칙

### 테스트 클래스 네이밍
```java
// Unit Test
public class UserTest { }                    // 클래스명 + Test
public class CreateUserUseCaseImplTest { }   // 클래스명 + Test

// Integration Test
public class UserControllerIntegrationTest { }   // 클래스명 + IntegrationTest
public class UserRepositoryIntegrationTest { }
```

### 테스트 메서드 네이밍 (한글 허용)
```java
@Test
void 사용자_생성_성공() {
    // given
    CreateUserCommand command = CreateUserCommand.builder()
        .email("test@example.com")
        .password("password123")
        .build();
    
    // when
    UserResponse response = createUserUseCase.createUser(command);
    
    // then
    assertThat(response.email()).isEqualTo("test@example.com");
}

@Test
void 중복된_이메일로_사용자_생성시_예외발생() {
    // given - when - then
}
```

## 🗄️ 데이터베이스 관련 규칙

### 엔티티 vs 도메인 분리
```java
// JPA 엔티티 (persistence-adapter 모듈)
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    // 도메인 모델로 변환
    public User toDomain() {
        return User.builder()
            .id(this.id)
            .email(this.email)
            .build();
    }
    
    // 도메인 모델에서 엔티티 생성
    public static UserEntity from(User user) {
        return UserEntity.builder()
            .id(user.getId())
            .email(user.getEmail())
            .build();
    }
}
```

## 📋 코드 리뷰 체크리스트

### 필수 확인 사항
- [ ] 헥사고날 아키텍처 레이어 의존성 규칙 준수
- [ ] Core 레이어에 외부 의존성 없음
- [ ] 인터페이스를 통한 의존성 역전 적용
- [ ] 비즈니스 로직이 도메인 레이어에 위치
- [ ] 예외 처리 적절히 구현
- [ ] 테스트 코드 작성 (커버리지 80% 이상)
- [ ] API 문서화 (Swagger/OpenAPI)

### 성능 및 보안
- [ ] N+1 쿼리 문제 없음
- [ ] 트랜잭션 범위 적절히 설정
- [ ] 민감 정보 로깅 방지
- [ ] SQL Injection 방어
- [ ] 입력 값 검증 구현

## 🔗 의존성 규칙

```java
// ✅ 허용되는 의존성 방향
boardly-api → boardly-application → boardly-domain
boardly-infrastructure → boardly-domain
boardly-application → boardly-domain

// ❌ 금지된 의존성
boardly-domain → boardly-application (X)
boardly-domain → boardly-infrastructure (X)
boardly-domain → boardly-api (X)
boardly-infrastructure → boardly-application (X)
boardly-infrastructure → boardly-api (X)
```

### 모듈별 의존성 세부 규칙

#### boardly-domain 모듈
```java
// ✅ 허용되는 의존성
- Java 표준 라이브러리
- Vavr (Either, Validation 등)
- 스프링 어노테이션 최소한 사용 (@Component만 도메인 서비스에)

// ❌ 금지된 의존성
- JPA 어노테이션 (@Entity, @Table 등)
- Spring Web 관련 (@Controller, @RestController 등)
- 외부 라이브러리 (Jackson, Hibernate 등)
```

#### boardly-application 모듈
```java
// ✅ 허용되는 의존성
- boardly-domain 모듈
- Spring Core (@Component, @Service, @Transactional)
- Vavr (Validation, Either)
- ValidationMessageResolver

// ❌ 금지된 의존성
- JPA 관련 (@Entity, @Repository)
- Spring Web (@Controller, @RequestMapping)
- HTTP 관련 라이브러리
```

#### boardly-infrastructure 모듈
```java
// ✅ 허용되는 의존성
- boardly-domain 모듈
- JPA/Hibernate
- Spring Data JPA
- 외부 시스템 연동 라이브러리
- 메시징 라이브러리

// ❌ 금지된 의존성
- boardly-application 모듈
- Spring Web 관련
```

#### boardly-api 모듈
```java
// ✅ 허용되는 의존성
- boardly-application 모듈
- Spring Web (@RestController, @RequestMapping)
- Validation 라이브러리
- JSON 처리 라이브러리

// ❌ 금지된 의존성
- boardly-domain 직접 의존
- boardly-infrastructure 직접 의존
- 데이터베이스 관련 라이브러리
```

## 🔧 Either<Failure, T> 사용 규칙

### Either 리턴 타입 적용 범위
```java
// ✅ UseCase - 모든 public 메서드는 Either 리턴
public interface CreateUserUseCase {
    Either<Failure, User> createUser(CreateUserCommand command);
}

public interface FindUserUseCase {
    Either<Failure, User> findById(UserId id);           // 조회도 Either
    Either<Failure, List<User>> findAll();              // 목록 조회도 Either
}

// ✅ Repository - 작업 유형별 구분
public interface UserRepository {
    // 단순 조회 - Optional 사용
    Optional<User> findById(UserId id);
    List<User> findAll();
    boolean existsByEmail(String email);
    
    // 생성/수정/삭제 - Either 사용 (제약 조건, 무결성 검증)
    Either<Failure, User> save(User user);
    Either<Failure, User> update(User user);
    Either<Failure, Void> deleteById(UserId id);
    
    // 복잡한 조회 (권한 체크 포함) - Either 사용
    Either<Failure, User> findByIdWithPermission(UserId id, UserId requesterId);
}
```

### 예외 처리 절대 금지 규칙
```java
// ✅ 좋은 예시 - Either로 모든 오류 처리
@Override
public Either<Failure, User> createUser(CreateUserCommand command) {
    // 1. 입력값 검증 실패 → Either.left()
    Validation<Seq<String>, CreateUserCommand> validation = validator.validate(command);
    if (validation.isInvalid()) {
        List<String> errors = validation.getError().asJava();
        return Either.left(Failure.InputError(errors));
    }
    
    // 2. 비즈니스 룰 위반 → Either.left()
    if (userRepository.existsByEmail(command.email())) {
        return Either.left(Failure.ResourceConflict(
            messageResolver.getMessage("user.email.already.exists")));
    }
    
    // 3. 정상 처리 → Either.right()
    User user = createUser(command);
    return userRepository.save(user);
}

// ❌ 나쁜 예시 - 예외 던지기 금지
@Override
public Either<Failure, User> createUser(CreateUserCommand command) {
    if (command.email() == null) {
        throw new IllegalArgumentException("Email is required");  // ❌ 예외 던지기 금지!
    }
    
    if (userRepository.existsByEmail(command.email())) {
        throw new DuplicateEmailException("Email exists");        // ❌ 예외 던지기 금지!
    }
    
    return Either.right(user);
}
```

### Failure 타입별 사용법
```java
// ✅ Failure 타입 활용 예시
@Override
public Either<Failure, User> updateUser(UpdateUserCommand command) {
    // 400 - 입력 형식 오류
    if (command.userId() == null) {
        return Either.left(Failure.InputError(
            messageResolver.getMessage("user.id.required")));
    }
    
    // 404 - 리소스 미발견
    Optional<User> userOpt = userRepository.findById(command.userId());
    if (userOpt.isEmpty()) {
        return Either.left(Failure.NotFound(
            messageResolver.getMessage("user.not.found")));
    }
    
    User user = userOpt.get();
    
    // 403 - 권한 거부
    if (!hasPermissionToUpdate(command.requesterId(), user)) {
        return Either.left(Failure.PermissionDenied(
            messageResolver.getMessage("user.update.permission.denied")));
    }
    
    // 409 - 리소스 충돌 (이메일 중복)
    if (!user.getEmail().equals(command.email()) && 
        userRepository.existsByEmail(command.email())) {
        return Either.left(Failure.ResourceConflict(
            messageResolver.getMessage("user.email.already.exists")));
    }
    
    // 412 - 전제 조건 실패 (버전 불일치)
    if (!user.getVersion().equals(command.version())) {
        return Either.left(Failure.PreconditionFailed(
            messageResolver.getMessage("user.version.mismatch")));
    }
    
    // 422 - 비즈니스 룰 위반
    if (user.getStatus() == UserStatus.DELETED) {
        return Either.left(Failure.BusinessRuleViolation(
            messageResolver.getMessage("user.deleted.cannot.update")));
    }
    
    // 정상 처리
    user.updateInfo(command.email(), command.firstName(), command.lastName());
    return userRepository.update(user);
}
```

### Either 체이닝 패턴
```java
// ✅ Either 체이닝으로 깔끔한 처리
@Override
public Either<Failure, Board> createBoard(CreateBoardCommand command) {
    return validator.validate(command)
        .toEither()
        .mapLeft(errors -> Failure.InputError(errors.asJava()))
        .flatMap(validCommand -> validateUserExists(validCommand.userId()))
        .flatMap(userId -> validateUserPermission(userId))
        .flatMap(userId -> createAndSaveBoard(command))
        .flatMap(board -> publishBoardCreatedEvent(board));
}

private Either<Failure, UserId> validateUserExists(UserId userId) {
    return userRepository.findById(userId)
        .map(user -> Either.<Failure, UserId>right(userId))
        .orElse(Either.left(Failure.NotFound(
            messageResolver.getMessage("user.not.found"))));
}
```

### Controller에서 Either 처리
```java
// ✅ Controller에서 Either 결과 처리 (항상 ResponseEntity<?> 리턴)
@PostMapping
public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
    CreateUserCommand command = CreateUserCommand.from(request);
    
    return createUserUseCase.createUser(command)
        .map(user -> ResponseEntity.status(HttpStatus.CREATED)
            .body(UserResponse.from(user)))
        .getOrElseGet(failure -> ResponseEntity
            .status(failure.getHttpStatus())
            .body(ErrorResponse.from(failure)));
}

@GetMapping("/{id}")
public ResponseEntity<?> getUser(@PathVariable Long id) {
    return UserId.create(id)
        .flatMap(userId -> findUserUseCase.findById(userId))
        .map(user -> ResponseEntity.ok(UserResponse.from(user)))
        .getOrElseGet(failure -> ResponseEntity
            .status(failure.getHttpStatus())
            .body(ErrorResponse.from(failure)));
}

@PutMapping("/{id}")
public ResponseEntity<?> updateUser(
        @PathVariable Long id, 
        @RequestBody UpdateUserRequest request) {
    
    return UserId.create(id)
        .map(userId -> UpdateUserCommand.builder()
            .userId(userId)
            .email(request.email())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .build())
        .flatMap(command -> updateUserUseCase.updateUser(command))
        .map(user -> ResponseEntity.ok(UserResponse.from(user)))
        .getOrElseGet(failure -> ResponseEntity
            .status(failure.getHttpStatus())
            .body(ErrorResponse.from(failure)));
}

@DeleteMapping("/{id}")
public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    return UserId.create(id)
        .flatMap(userId -> deleteUserUseCase.deleteUser(userId))
        .map(result -> ResponseEntity.noContent().build())
        .getOrElseGet(failure -> ResponseEntity
            .status(failure.getHttpStatus())
            .body(ErrorResponse.from(failure)));
}
```

이 컨벤션을 통해 일관성 있고 유지보수 가능한 헥사고날 아키텍처 기반의 스프링 부트 애플리케이션을 개발할 수 있습니다.
