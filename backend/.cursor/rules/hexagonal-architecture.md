# 🏗️ 헥사고날 아키텍처 레이어별 규칙

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

## Domain 레이어 (도메인)
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

## Application 레이어
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

## Infrastructure 레이어 (Adapter)
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

## API 레이어 (Controller)
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
