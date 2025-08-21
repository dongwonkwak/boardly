# 입력 검증 표준

## 개요

Boardly 프로젝트는 일관된 입력 검증 방식을 통해 데이터 무결성을 보장하고 안전한 API를 제공합니다. 이 문서는 각 레이어에서의 입력 검증 방법과 `Failure.ofInputError`를 사용한 검증 실패 처리 방법을 설명합니다.

## 검증 레이어

### 1. Presentation Layer (API)
- HTTP 요청 데이터 검증
- Bean Validation 어노테이션 사용
- 기본적인 형식 검증

### 2. Application Layer (UseCase)
- 비즈니스 규칙 검증
- 도메인 로직 전 검증
- 복합 검증 로직

### 3. Domain Layer
- 도메인 객체 생성 시 검증
- 불변성 보장
- 도메인 규칙 검증

## 검증 방법

### 1. Bean Validation (Presentation Layer)

```java
@RestController
@RequestMapping("/api/boards")
public class BoardController {
    
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(
            @Valid @RequestBody CreateBoardRequest request) {
        // Bean Validation이 자동으로 검증
        CreateBoardCommand command = CreateBoardCommand.from(request, getCurrentUserId());
        Board board = createBoardUseCase.execute(command);
        return ResponseEntity.ok(BoardResponse.from(board));
    }
}

@Value
@Builder
public class CreateBoardRequest {
    @NotBlank(message = "보드 이름은 필수입니다.")
    @Size(min = 1, max = 100, message = "보드 이름은 1자 이상 100자 이하여야 합니다.")
    String name;
    
    @Size(max = 500, message = "보드 설명은 500자 이하여야 합니다.")
    String description;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 16진수 형식(#RRGGBB)이어야 합니다.")
    String color;
}
```

### 2. UseCase 검증 (Application Layer)

```java
@Service
@Transactional
public class CreateBoardUseCase {
    private final BoardRepository boardRepository;
    
    public Either<Failure, Board> execute(CreateBoardCommand command) {
        // 1. 입력 검증
        Either<Failure, Void> validationResult = validateCommand(command);
        if (validationResult.isLeft()) {
            return Either.left(validationResult.getLeft());
        }
        
        // 2. 비즈니스 규칙 검증
        Either<Failure, Void> businessValidationResult = validateBusinessRules(command);
        if (businessValidationResult.isLeft()) {
            return Either.left(businessValidationResult.getLeft());
        }
        
        // 3. 도메인 로직 실행
        Board board = Board.create(command.getName(), command.getDescription());
        Board savedBoard = boardRepository.save(board);
        
        return Either.right(savedBoard);
    }
    
    private Either<Failure, Void> validateCommand(CreateBoardCommand command) {
        List<Failure.FieldViolation> violations = new ArrayList<>();
        
        // 이름 검증
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            violations.add(Failure.FieldViolation.builder()
                    .field("name")
                    .message("보드 이름은 필수입니다.")
                    .rejectedValue(command.getName())
                    .build());
        } else if (command.getName().length() > 100) {
            violations.add(Failure.FieldViolation.builder()
                    .field("name")
                    .message("보드 이름은 100자 이하여야 합니다.")
                    .rejectedValue(command.getName())
                    .build());
        }
        
        // 설명 검증
        if (command.getDescription() != null && command.getDescription().length() > 500) {
            violations.add(Failure.FieldViolation.builder()
                    .field("description")
                    .message("보드 설명은 500자 이하여야 합니다.")
                    .rejectedValue(command.getDescription())
                    .build());
        }
        
        if (!violations.isEmpty()) {
            return Either.left(Failure.ofInputError(
                "입력 데이터가 유효하지 않습니다.",
                "VALIDATION_ERROR",
                violations
            ));
        }
        
        return Either.right(null);
    }
    
    private Either<Failure, Void> validateBusinessRules(CreateBoardCommand command) {
        // 중복 이름 검증
        if (boardRepository.existsByName(command.getName(), command.getOwnerId())) {
            return Either.left(Failure.ofResourceConflict(
                "동일한 이름의 보드가 이미 존재합니다.",
                "BOARD_NAME_DUPLICATE",
                Map.of("name", command.getName())
            ));
        }
        
        // 사용자별 보드 개수 제한 검증
        long userBoardCount = boardRepository.countByUserId(command.getOwnerId());
        if (userBoardCount >= MAX_BOARDS_PER_USER) {
            return Either.left(Failure.ofBusinessRuleViolation(
                "사용자당 최대 보드 개수를 초과했습니다.",
                "BOARD_LIMIT_EXCEEDED",
                Map.of("currentCount", userBoardCount, "maxCount", MAX_BOARDS_PER_USER)
            ));
        }
        
        return Either.right(null);
    }
}
```

### 3. 도메인 객체 검증 (Domain Layer)

```java
public class Board {
    private final BoardId id;
    private final BoardName name;
    private final BoardDescription description;
    private final List<Card> cards;
    
    public static Board create(String name, String description) {
        // 도메인 객체 생성 시 검증
        BoardName boardName = BoardName.of(name);
        BoardDescription boardDescription = BoardDescription.of(description);
        
        return new Board(
            BoardId.generate(),
            boardName,
            boardDescription,
            new ArrayList<>()
        );
    }
    
    private Board(BoardId id, BoardName name, BoardDescription description, List<Card> cards) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cards = new ArrayList<>(cards);
    }
}

@Value
public class BoardName {
    String value;
    
    public static BoardName of(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("보드 이름은 필수입니다.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("보드 이름은 100자 이하여야 합니다.");
        }
        return new BoardName(name.trim());
    }
}

@Value
public class BoardDescription {
    String value;
    
    public static BoardDescription of(String description) {
        if (description == null) {
            return new BoardDescription("");
        }
        if (description.length() > 500) {
            throw new IllegalArgumentException("보드 설명은 500자 이하여야 합니다.");
        }
        return new BoardDescription(description.trim());
    }
}
```

## 검증 어노테이션 표준

### 1. 기본 검증 어노테이션

```java
@Value
@Builder
public class CreateBoardRequest {
    // 필수 필드
    @NotBlank(message = "보드 이름은 필수입니다.")
    String name;
    
    // 길이 제한
    @Size(min = 1, max = 100, message = "보드 이름은 1자 이상 100자 이하여야 합니다.")
    String name;
    
    @Size(max = 500, message = "보드 설명은 500자 이하여야 합니다.")
    String description;
    
    // 이메일 형식
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email;
    
    // 정규식 패턴
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 16진수 형식(#RRGGBB)이어야 합니다.")
    String color;
    
    // 숫자 범위
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    @Max(value = 1000, message = "페이지 번호는 1000 이하여야 합니다.")
    Integer page;
    
    // 날짜 형식
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate;
    
    // 중첩 객체 검증
    @Valid
    List<@Valid CardRequest> cards;
}
```

### 2. 커스텀 검증 어노테이션

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BoardNameValidator.class)
public @interface ValidBoardName {
    String message() default "유효하지 않은 보드 이름입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class BoardNameValidator implements ConstraintValidator<ValidBoardName, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        // 특수 문자 제한
        if (value.matches(".*[<>\"'&].*")) {
            return false;
        }
        
        // 예약어 제한
        List<String> reservedWords = Arrays.asList("admin", "system", "root");
        if (reservedWords.contains(value.toLowerCase())) {
            return false;
        }
        
        return true;
    }
}
```

## Failure.ofInputError 사용법

### 1. 기본 사용법

```java
// 단일 오류
return Either.left(Failure.ofInputError("보드 이름은 필수입니다."));

// 여러 필드 오류
List<Failure.FieldViolation> violations = Arrays.asList(
    Failure.FieldViolation.builder()
        .field("name")
        .message("보드 이름은 필수입니다.")
        .rejectedValue(null)
        .build(),
    Failure.FieldViolation.builder()
        .field("description")
        .message("보드 설명은 500자 이하여야 합니다.")
        .rejectedValue("매우 긴 설명...")
        .build()
);

return Either.left(Failure.ofInputError(
    "입력 데이터가 유효하지 않습니다.",
    "VALIDATION_ERROR",
    violations
));
```

### 2. 검증 헬퍼 메서드

```java
@Component
public class ValidationHelper {
    
    public static Either<Failure, Void> validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return Either.left(Failure.ofInputError(
                String.format("%s는 필수입니다.", fieldName),
                "REQUIRED_FIELD_MISSING",
                List.of(Failure.FieldViolation.builder()
                    .field(fieldName)
                    .message(String.format("%s는 필수입니다.", fieldName))
                    .rejectedValue(value)
                    .build())
            ));
        }
        return Either.right(null);
    }
    
    public static Either<Failure, Void> validateLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            return Either.left(Failure.ofInputError(
                String.format("%s는 %d자 이하여야 합니다.", fieldName, maxLength),
                "FIELD_TOO_LONG",
                List.of(Failure.FieldViolation.builder()
                    .field(fieldName)
                    .message(String.format("%s는 %d자 이하여야 합니다.", fieldName, maxLength))
                    .rejectedValue(value)
                    .build())
            ));
        }
        return Either.right(null);
    }
    
    public static Either<Failure, Void> validatePattern(String value, String fieldName, String pattern, String message) {
        if (value != null && !value.matches(pattern)) {
            return Either.left(Failure.ofInputError(
                message,
                "INVALID_PATTERN",
                List.of(Failure.FieldViolation.builder()
                    .field(fieldName)
                    .message(message)
                    .rejectedValue(value)
                    .build())
            ));
        }
        return Either.right(null);
    }
}
```

### 3. UseCase에서 사용

```java
@Service
@Transactional
public class CreateBoardUseCase {
    
    public Either<Failure, Board> execute(CreateBoardCommand command) {
        // 검증 헬퍼 사용
        Either<Failure, Void> nameValidation = ValidationHelper.validateRequired(command.getName(), "name");
        if (nameValidation.isLeft()) {
            return Either.left(nameValidation.getLeft());
        }
        
        Either<Failure, Void> nameLengthValidation = ValidationHelper.validateLength(command.getName(), "name", 100);
        if (nameLengthValidation.isLeft()) {
            return Either.left(nameLengthValidation.getLeft());
        }
        
        Either<Failure, Void> descriptionValidation = ValidationHelper.validateLength(command.getDescription(), "description", 500);
        if (descriptionValidation.isLeft()) {
            return Either.left(descriptionValidation.getLeft());
        }
        
        // 도메인 로직 실행
        Board board = Board.create(command.getName(), command.getDescription());
        return Either.right(boardRepository.save(board));
    }
}
```

## 검증 전략

### 1. 계층별 검증 책임

| 레이어 | 검증 내용 | 방법 |
|--------|-----------|------|
| Presentation | 기본 형식 검증 | Bean Validation |
| Application | 비즈니스 규칙 검증 | UseCase 내 검증 로직 |
| Domain | 도메인 규칙 검증 | 도메인 객체 생성 시 |

### 2. 검증 순서

```java
public Either<Failure, Board> execute(CreateBoardCommand command) {
    // 1. 기본 형식 검증 (Presentation Layer에서 이미 처리됨)
    
    // 2. 비즈니스 규칙 검증
    Either<Failure, Void> businessValidation = validateBusinessRules(command);
    if (businessValidation.isLeft()) {
        return Either.left(businessValidation.getLeft());
    }
    
    // 3. 도메인 객체 생성 (도메인 레이어에서 검증)
    Board board = Board.create(command.getName(), command.getDescription());
    
    // 4. 저장
    return Either.right(boardRepository.save(board));
}
```

## 테스트 전략

### 1. Bean Validation 테스트

```java
@ExtendWith(MockitoExtension.class)
class CreateBoardRequestValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void 유효한_요청_검증_성공() {
        // given
        CreateBoardRequest request = CreateBoardRequest.builder()
                .name("테스트 보드")
                .description("테스트 설명")
                .build();
        
        // when
        Set<ConstraintViolation<CreateBoardRequest>> violations = validator.validate(request);
        
        // then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void 이름_누락_시_검증_실패() {
        // given
        CreateBoardRequest request = CreateBoardRequest.builder()
                .name("")
                .description("테스트 설명")
                .build();
        
        // when
        Set<ConstraintViolation<CreateBoardRequest>> violations = validator.validate(request);
        
        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("보드 이름은 필수입니다.");
    }
}
```

### 2. UseCase 검증 테스트

```java
@ExtendWith(MockitoExtension.class)
class CreateBoardUseCaseValidationTest {
    
    @Mock
    private BoardRepository boardRepository;
    
    @InjectMocks
    private CreateBoardUseCase useCase;
    
    @Test
    void 이름_누락_시_InputError_반환() {
        // given
        CreateBoardCommand command = CreateBoardCommand.of("", "설명", userId);
        
        // when
        Either<Failure, Board> result = useCase.execute(command);
        
        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft())
                .isInstanceOf(Failure.InputError.class)
                .satisfies(failure -> {
                    assertThat(failure.getErrorCode()).isEqualTo("VALIDATION_ERROR");
                    assertThat(failure.getViolations()).hasSize(1);
                    assertThat(failure.getViolations().get(0).getField()).isEqualTo("name");
                });
    }
    
    @Test
    void 중복_이름_시_ResourceConflict_반환() {
        // given
        CreateBoardCommand command = CreateBoardCommand.of("중복 보드", "설명", userId);
        when(boardRepository.existsByName("중복 보드", userId)).thenReturn(true);
        
        // when
        Either<Failure, Board> result = useCase.execute(command);
        
        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft())
                .isInstanceOf(Failure.ResourceConflict.class)
                .satisfies(failure -> {
                    assertThat(failure.getErrorCode()).isEqualTo("BOARD_NAME_DUPLICATE");
                });
    }
}
```

## 모범 사례

### 1. 명확한 오류 메시지

```java
// 좋은 예
Failure.ofInputError("보드 이름은 1자 이상 100자 이하여야 합니다.")

// 나쁜 예
Failure.ofInputError("Invalid input")
```

### 2. 적절한 필드 정보 제공

```java
// 좋은 예
Failure.FieldViolation.builder()
    .field("name")
    .message("보드 이름은 필수입니다.")
    .rejectedValue(null)
    .build()

// 나쁜 예
Failure.FieldViolation.builder()
    .field("")
    .message("오류가 발생했습니다.")
    .rejectedValue(null)
    .build()
```

### 3. 일관된 검증 순서

```java
// 1. 필수 필드 검증
// 2. 형식 검증
// 3. 길이 검증
// 4. 비즈니스 규칙 검증
// 5. 도메인 객체 생성
```

### 4. 성능 고려

```java
// 좋은 예: 조기 반환으로 불필요한 검증 방지
public Either<Failure, Board> execute(CreateBoardCommand command) {
    // 1. 필수 검증
    if (command.getName() == null || command.getName().trim().isEmpty()) {
        return Either.left(Failure.ofInputError("보드 이름은 필수입니다."));
    }
    
    // 2. 비즈니스 검증 (필수 검증 통과 후에만)
    if (boardRepository.existsByName(command.getName(), command.getOwnerId())) {
        return Either.left(Failure.ofResourceConflict("중복된 이름입니다."));
    }
    
    // 3. 도메인 로직 실행
    Board board = Board.create(command.getName(), command.getDescription());
    return Either.right(boardRepository.save(board));
}
```

## 결론

일관된 입력 검증 방식을 사용하면 데이터 무결성을 보장하고 안전한 API를 제공할 수 있습니다. `Failure.ofInputError`를 적절히 활용하여 명확하고 사용자 친화적인 오류 메시지를 제공하세요.
