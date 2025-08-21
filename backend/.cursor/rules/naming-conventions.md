# 📝 네이밍 컨벤션

## 클래스 네이밍
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

## 메서드 네이밍
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

## 변수 네이밍
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

# 🎯 어노테이션 사용 규칙

## 스프링 어노테이션
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

## Vavr Validation 사용 규칙
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
```

# 📊 테스트 코드 규칙

## 테스트 클래스 네이밍
```java
// Unit Test
public class UserTest { }                    // 클래스명 + Test
public class CreateUserUseCaseImplTest { }   // 클래스명 + Test

// Integration Test
public class UserControllerIntegrationTest { }   // 클래스명 + IntegrationTest
public class UserRepositoryIntegrationTest { }
```

## 테스트 메서드 네이밍 (한글 허용)
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

# 🗄️ 데이터베이스 관련 규칙

## 엔티티 vs 도메인 분리
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

# 🔧 예외 처리 규칙

## Either<Failure, T> 사용시 예외 금지
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

## Failure 클래스 기반 오류 처리
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

## 글로벌 예외 처리 없음
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

## messages.properties 다국어 메시지
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
