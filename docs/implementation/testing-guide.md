# 테스트 작성 가이드

## 개요

Boardly 프로젝트는 테스트 주도 개발(TDD)과 품질 보증을 위해 체계적인 테스트 전략을 사용합니다. 이 문서는 각 레이어별 테스트 작성 방법과 메서드별 테스트 파일 분리 원칙을 설명합니다.

## 테스트 전략

### 1. 테스트 피라미드

```
        /\
       /  \     E2E Tests (소수)
      /____\
     /      \   Integration Tests (적당)
    /________\
   /          \ Unit Tests (다수)
  /____________\
```

### 2. 테스트 분류

| 테스트 유형 | 목적 | 실행 속도 | 의존성 |
|------------|------|-----------|--------|
| Unit Test | 개별 컴포넌트 검증 | 빠름 | Mock 사용 |
| Integration Test | 컴포넌트 간 연동 검증 | 보통 | 실제 의존성 |
| E2E Test | 전체 시스템 검증 | 느림 | 전체 시스템 |

## 테스트 파일 구조

### 1. 메서드별 테스트 파일 분리

사용자가 선호하는 방식에 따라 각 메서드별로 별도 테스트 파일을 생성합니다.

```
src/test/java/com/boardly/application/
├── CreateBoardUseCaseTest.java
├── UpdateBoardUseCaseTest.java
├── DeleteBoardUseCaseTest.java
└── GetBoardListUseCaseTest.java
```

### 2. 테스트 클래스 네이밍

```java
// UseCase 테스트
CreateBoardUseCaseTest
UpdateBoardUseCaseTest
DeleteBoardUseCaseTest

// Controller 테스트
BoardControllerTest
CardControllerTest

// Repository 테스트
JpaBoardRepositoryTest
JpaCardRepositoryTest

// Domain 테스트
BoardTest
CardTest
```

## Unit Test 작성

### 1. UseCase Unit Test

```java
@ExtendWith(MockitoExtension.class)
class CreateBoardUseCaseTest {
    
    @Mock
    private BoardRepository boardRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private CreateBoardUseCase useCase;
    
    @Test
    @DisplayName("보드 생성 성공")
    void 보드_생성_성공() {
        // given
        CreateBoardCommand command = CreateBoardCommand.of("테스트 보드", "테스트 설명", userId);
        Board expectedBoard = Board.create("테스트 보드", "테스트 설명");
        
        when(boardRepository.save(any(Board.class))).thenReturn(expectedBoard);
        
        // when
        Board result = useCase.execute(command);
        
        // then
        assertThat(result).isEqualTo(expectedBoard);
        verify(boardRepository).save(any(Board.class));
        verify(eventPublisher).publish(any(BoardCreatedEvent.class));
    }
    
    @Test
    @DisplayName("보드 이름 중복 시 실패")
    void 보드_이름_중복_시_실패() {
        // given
        CreateBoardCommand command = CreateBoardCommand.of("중복 보드", "설명", userId);
        
        when(boardRepository.existsByName("중복 보드", userId)).thenReturn(true);
        
        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BoardNameDuplicateException.class)
                .hasMessage("동일한 이름의 보드가 이미 존재합니다.");
    }
    
    @Test
    @DisplayName("입력 검증 실패 시 InputError 반환")
    void 입력_검증_실패_시_InputError_반환() {
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
}
```

### 2. Controller Unit Test

```java
@ExtendWith(MockitoExtension.class)
class BoardControllerTest {
    
    @Mock
    private CreateBoardUseCase createBoardUseCase;
    
    @Mock
    private ApiFailureHandler failureHandler;
    
    @InjectMocks
    private BoardController controller;
    
    @Test
    @DisplayName("보드 생성 API 성공")
    void 보드_생성_API_성공() {
        // given
        CreateBoardRequest request = CreateBoardRequest.builder()
                .name("테스트 보드")
                .description("테스트 설명")
                .build();
        
        Board board = Board.create("테스트 보드", "테스트 설명");
        BoardResponse expectedResponse = BoardResponse.from(board);
        
        when(createBoardUseCase.execute(any(CreateBoardCommand.class)))
                .thenReturn(Either.right(board));
        
        // when
        ResponseEntity<BoardResponse> response = controller.createBoard(request);
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }
    
    @Test
    @DisplayName("보드 생성 API 실패 시 에러 응답")
    void 보드_생성_API_실패_시_에러_응답() {
        // given
        CreateBoardRequest request = CreateBoardRequest.builder()
                .name("")
                .description("테스트 설명")
                .build();
        
        Failure.InputError failure = Failure.ofInputError("보드 이름은 필수입니다.");
        ErrorResponse errorResponse = ErrorResponse.validation(failure);
        
        when(createBoardUseCase.execute(any(CreateBoardCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.badRequest().body(errorResponse));
        
        // when
        ResponseEntity<?> response = controller.createBoard(request);
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorResponse);
    }
}
```

### 3. Domain Model Test

```java
class BoardTest {
    
    @Test
    @DisplayName("보드 생성 성공")
    void 보드_생성_성공() {
        // given
        String name = "테스트 보드";
        String description = "테스트 설명";
        
        // when
        Board board = Board.create(name, description);
        
        // then
        assertThat(board.getName().getValue()).isEqualTo(name);
        assertThat(board.getDescription().getValue()).isEqualTo(description);
        assertThat(board.getCards()).isEmpty();
    }
    
    @Test
    @DisplayName("카드 추가 성공")
    void 카드_추가_성공() {
        // given
        Board board = Board.create("테스트 보드", "설명");
        Card card = Card.create("테스트 카드", "카드 설명");
        
        // when
        Board updatedBoard = board.addCard(card);
        
        // then
        assertThat(updatedBoard.getCards()).hasSize(1);
        assertThat(updatedBoard.getCards().get(0)).isEqualTo(card);
        assertThat(board.getCards()).isEmpty(); // 원본은 변경되지 않음
    }
    
    @Test
    @DisplayName("아카이브된 보드에 카드 추가 시 실패")
    void 아카이브된_보드에_카드_추가_시_실패() {
        // given
        Board board = Board.create("테스트 보드", "설명").archive();
        Card card = Card.create("테스트 카드", "카드 설명");
        
        // when
        Either<Failure, Board> result = board.addCard(card);
        
        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft())
                .isInstanceOf(Failure.BusinessRuleViolation.class)
                .satisfies(failure -> {
                    assertThat(failure.getErrorCode()).isEqualTo("BOARD_ARCHIVED");
                    assertThat(failure.getMessage()).contains("아카이브된 보드");
                });
    }
}
```

## Integration Test 작성

### 1. UseCase Integration Test

```java
@SpringBootTest
@Transactional
class CreateBoardUseCaseIntegrationTest {
    
    @Autowired
    private CreateBoardUseCase useCase;
    
    @Autowired
    private BoardRepository boardRepository;
    
    @MockBean
    private EventPublisher eventPublisher;
    
    @Test
    @DisplayName("보드 생성 통합 테스트")
    void 보드_생성_통합_테스트() {
        // given
        CreateBoardCommand command = CreateBoardCommand.of("통합 테스트 보드", "설명", userId);
        
        // when
        Board result = useCase.execute(command);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getName().getValue()).isEqualTo("통합 테스트 보드");
        
        Board savedBoard = boardRepository.findById(result.getId()).orElse(null);
        assertThat(savedBoard).isNotNull();
        assertThat(savedBoard.getName().getValue()).isEqualTo("통합 테스트 보드");
        
        verify(eventPublisher).publish(any(BoardCreatedEvent.class));
    }
    
    @Test
    @DisplayName("중복 이름으로 보드 생성 시 실패")
    void 중복_이름으로_보드_생성_시_실패() {
        // given
        CreateBoardCommand command1 = CreateBoardCommand.of("중복 보드", "설명1", userId);
        CreateBoardCommand command2 = CreateBoardCommand.of("중복 보드", "설명2", userId);
        
        // when
        useCase.execute(command1); // 첫 번째는 성공
        
        // then
        assertThatThrownBy(() -> useCase.execute(command2))
                .isInstanceOf(BoardNameDuplicateException.class)
                .hasMessage("동일한 이름의 보드가 이미 존재합니다.");
    }
}
```

### 2. Repository Integration Test

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaBoardRepositoryTest {
    
    @Autowired
    private BoardRepository boardRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @DisplayName("보드 저장 및 조회")
    void 보드_저장_및_조회() {
        // given
        Board board = Board.create("테스트 보드", "테스트 설명");
        
        // when
        Board savedBoard = boardRepository.save(board);
        Optional<Board> foundBoard = boardRepository.findById(savedBoard.getId());
        
        // then
        assertThat(foundBoard).isPresent();
        assertThat(foundBoard.get().getName().getValue()).isEqualTo("테스트 보드");
    }
    
    @Test
    @DisplayName("사용자별 보드 목록 조회")
    void 사용자별_보드_목록_조회() {
        // given
        UserId userId1 = UserId.of("user1");
        UserId userId2 = UserId.of("user2");
        
        Board board1 = Board.create("보드1", "설명1");
        Board board2 = Board.create("보드2", "설명2");
        Board board3 = Board.create("보드3", "설명3");
        
        boardRepository.saveAll(Arrays.asList(board1, board2, board3));
        
        // when
        List<Board> user1Boards = boardRepository.findByUserId(userId1);
        List<Board> user2Boards = boardRepository.findByUserId(userId2);
        
        // then
        assertThat(user1Boards).hasSize(2);
        assertThat(user2Boards).hasSize(1);
    }
}
```

### 3. Controller Integration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BoardControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private BoardRepository boardRepository;
    
    @Test
    @DisplayName("보드 생성 API 통합 테스트")
    void 보드_생성_API_통합_테스트() {
        // given
        CreateBoardRequest request = CreateBoardRequest.builder()
                .name("API 테스트 보드")
                .description("API 테스트 설명")
                .build();
        
        // when
        ResponseEntity<BoardResponse> response = restTemplate.postForEntity(
                "/api/boards",
                request,
                BoardResponse.class
        );
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("API 테스트 보드");
        
        // 데이터베이스 확인
        Board savedBoard = boardRepository.findById(response.getBody().getId()).orElse(null);
        assertThat(savedBoard).isNotNull();
        assertThat(savedBoard.getName().getValue()).isEqualTo("API 테스트 보드");
    }
    
    @Test
    @DisplayName("잘못된 요청 시 400 응답")
    void 잘못된_요청_시_400_응답() {
        // given
        CreateBoardRequest request = CreateBoardRequest.builder()
                .name("") // 빈 이름
                .description("설명")
                .build();
        
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/boards",
                request,
                ErrorResponse.class
        );
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError().getCode()).isEqualTo("VALIDATION_ERROR");
    }
}
```

## TestContainers를 사용한 테스트

### 1. PostgreSQL TestContainers 설정

```java
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BoardControllerWithTestContainersTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("boardly_test")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("실제 PostgreSQL에서 보드 생성 테스트")
    void 실제_PostgreSQL에서_보드_생성_테스트() {
        // given
        CreateBoardRequest request = CreateBoardRequest.builder()
                .name("TestContainers 테스트 보드")
                .description("TestContainers 테스트 설명")
                .build();
        
        // when
        ResponseEntity<BoardResponse> response = restTemplate.postForEntity(
                "/api/boards",
                request,
                BoardResponse.class
        );
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("TestContainers 테스트 보드");
    }
}
```

## 테스트 데이터 관리

### 1. Test Fixture

```java
@ExtendWith(MockitoExtension.class)
class CreateBoardUseCaseTest {
    
    private static final UserId TEST_USER_ID = UserId.of("test-user");
    
    @Test
    @DisplayName("보드 생성 성공")
    void 보드_생성_성공() {
        // given
        CreateBoardCommand command = createValidCommand();
        Board expectedBoard = createExpectedBoard();
        
        when(boardRepository.save(any(Board.class))).thenReturn(expectedBoard);
        
        // when
        Board result = useCase.execute(command);
        
        // then
        assertThat(result).isEqualTo(expectedBoard);
    }
    
    private CreateBoardCommand createValidCommand() {
        return CreateBoardCommand.of("테스트 보드", "테스트 설명", TEST_USER_ID);
    }
    
    private Board createExpectedBoard() {
        return Board.create("테스트 보드", "테스트 설명");
    }
}
```

### 2. Test Data Builder

```java
public class BoardTestDataBuilder {
    
    private String name = "테스트 보드";
    private String description = "테스트 설명";
    private UserId ownerId = UserId.of("test-user");
    private List<Card> cards = new ArrayList<>();
    
    public BoardTestDataBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public BoardTestDataBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public BoardTestDataBuilder ownerId(UserId ownerId) {
        this.ownerId = ownerId;
        return this;
    }
    
    public BoardTestDataBuilder addCard(Card card) {
        this.cards.add(card);
        return this;
    }
    
    public Board build() {
        Board board = Board.create(name, description);
        cards.forEach(board::addCard);
        return board;
    }
    
    public CreateBoardCommand buildCommand() {
        return CreateBoardCommand.of(name, description, ownerId);
    }
    
    public static BoardTestDataBuilder aBoard() {
        return new BoardTestDataBuilder();
    }
}

// 사용 예시
@Test
void 보드_생성_성공() {
    // given
    CreateBoardCommand command = BoardTestDataBuilder.aBoard()
            .name("특별한 보드")
            .description("특별한 설명")
            .buildCommand();
    
    // when & then
    // ...
}
```

## 테스트 실행 전략

### 1. 테스트 실행 순서

```bash
# 1. Unit Tests (빠름)
./gradlew test

# 2. Integration Tests (보통)
./gradlew integrationTest

# 3. E2E Tests (느림)
./gradlew e2eTest
```

### 2. CI/CD 파이프라인

```yaml
# .github/workflows/test.yml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run Unit Tests
      run: ./gradlew test
    
    - name: Run Integration Tests
      run: ./gradlew integrationTest
    
    - name: Run E2E Tests
      run: ./gradlew e2eTest
```

## 모범 사례

### 1. 테스트 네이밍

```java
// 좋은 예: 명확하고 설명적인 이름
@Test
void 보드_이름이_빈_문자열일_때_생성_실패() { }

@Test
void 사용자가_최대_보드_개수를_초과했을_때_생성_실패() { }

// 나쁜 예: 모호한 이름
@Test
void test1() { }

@Test
void shouldFail() { }
```

### 2. Given-When-Then 구조

```java
@Test
void 보드_생성_성공() {
    // Given: 테스트 준비
    CreateBoardCommand command = CreateBoardCommand.of("테스트 보드", "설명", userId);
    Board expectedBoard = Board.create("테스트 보드", "설명");
    when(boardRepository.save(any(Board.class))).thenReturn(expectedBoard);
    
    // When: 테스트 실행
    Board result = useCase.execute(command);
    
    // Then: 결과 검증
    assertThat(result).isEqualTo(expectedBoard);
    verify(boardRepository).save(any(Board.class));
}
```

### 3. 테스트 격리

```java
@ExtendWith(MockitoExtension.class)
class CreateBoardUseCaseTest {
    
    @Mock
    private BoardRepository boardRepository;
    
    @InjectMocks
    private CreateBoardUseCase useCase;
    
    @BeforeEach
    void setUp() {
        // 각 테스트 전에 Mock 초기화
        reset(boardRepository);
    }
    
    @Test
    void 테스트1() { }
    
    @Test
    void 테스트2() { }
}
```

### 4. Assertion 메시지

```java
// 좋은 예: 명확한 실패 메시지
assertThat(result.getName().getValue())
    .as("보드 이름이 일치해야 합니다")
    .isEqualTo("테스트 보드");

// 나쁜 예: 기본 메시지만 사용
assertThat(result.getName().getValue()).isEqualTo("테스트 보드");
```

## 결론

체계적인 테스트 전략을 통해 코드 품질을 보장하고 리팩토링을 안전하게 수행할 수 있습니다. 메서드별 테스트 파일 분리 원칙을 따라 유지보수하기 쉬운 테스트 코드를 작성하세요.
