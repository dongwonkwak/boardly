# UseCase 패턴 가이드

## 개요

Boardly 프로젝트는 UseCase 패턴을 통해 비즈니스 로직을 캡슐화하고 도메인 모델과 프레젠테이션 레이어를 분리합니다. 이 문서는 UseCase의 설계 원칙과 구현 방법을 설명합니다.

## UseCase 패턴의 목적

### 1. 비즈니스 로직 캡슐화
- 하나의 비즈니스 유스케이스를 하나의 클래스로 표현
- 도메인 로직을 애플리케이션 레이어에서 조율

### 2. 도메인 모델 중심 설계
- UseCase는 도메인 모델 객체를 반환
- 프레젠테이션 레이어에서 DTO로 변환

### 3. 테스트 용이성
- 각 UseCase를 독립적으로 테스트 가능
- 비즈니스 로직의 단위 테스트 작성 용이

## UseCase 구조

### 기본 구조

```java
@Service
@Transactional
public class CreateBoardUseCase {
    private final BoardRepository boardRepository;
    private final EventPublisher eventPublisher;
    
    public Board execute(CreateBoardCommand command) {
        // 1. 입력 검증
        validateCommand(command);
        
        // 2. 도메인 로직 실행
        Board board = Board.create(command.getName(), command.getDescription());
        
        // 3. 저장
        Board savedBoard = boardRepository.save(board);
        
        // 4. 이벤트 발행
        eventPublisher.publish(new BoardCreatedEvent(savedBoard.getId()));
        
        // 5. 도메인 모델 반환
        return savedBoard;
    }
    
    private void validateCommand(CreateBoardCommand command) {
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("보드 이름은 필수입니다.");
        }
    }
}
```

### Command 객체

```java
@Value
@Builder
public class CreateBoardCommand {
    String name;
    String description;
    UserId ownerId;
    
    public static CreateBoardCommand of(String name, String description, UserId ownerId) {
        return CreateBoardCommand.builder()
                .name(name)
                .description(description)
                .ownerId(ownerId)
                .build();
    }
    
    public static CreateBoardCommand from(CreateBoardRequest request, UserId ownerId) {
        return CreateBoardCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(ownerId)
                .build();
    }
}
```

## UseCase 분류

### 1. Command UseCase (명령)

상태를 변경하는 UseCase

```java
@Service
@Transactional
public class UpdateBoardUseCase {
    private final BoardRepository boardRepository;
    private final PermissionService permissionService;
    
    public Board execute(UpdateBoardCommand command) {
        // 1. 권한 검증
        if (!permissionService.canEditBoard(command.getUserId(), command.getBoardId())) {
            throw new PermissionDeniedException("보드 수정 권한이 없습니다.");
        }
        
        // 2. 도메인 객체 조회
        Board board = boardRepository.findById(command.getBoardId())
                .orElseThrow(() -> new BoardNotFoundException("보드를 찾을 수 없습니다."));
        
        // 3. 도메인 로직 실행
        Board updatedBoard = board.update(command.getName(), command.getDescription());
        
        // 4. 저장
        return boardRepository.save(updatedBoard);
    }
}
```

### 2. Query UseCase (조회)

데이터를 조회하는 UseCase

```java
@Service
@Transactional(readOnly = true)
public class GetBoardDetailUseCase {
    private final BoardRepository boardRepository;
    private final PermissionService permissionService;
    
    public BoardDetail execute(GetBoardDetailQuery query) {
        // 1. 권한 검증
        if (!permissionService.canViewBoard(query.getUserId(), query.getBoardId())) {
            throw new PermissionDeniedException("보드 조회 권한이 없습니다.");
        }
        
        // 2. 도메인 객체 조회
        Board board = boardRepository.findByIdWithCards(query.getBoardId())
                .orElseThrow(() -> new BoardNotFoundException("보드를 찾을 수 없습니다."));
        
        // 3. 도메인 모델 반환 (BoardDetail은 도메인 모델)
        return BoardDetail.from(board);
    }
}
```

### 3. Complex UseCase (복합)

여러 도메인 객체를 조율하는 UseCase

```java
@Service
@Transactional
public class MoveCardUseCase {
    private final BoardRepository boardRepository;
    private final CardRepository cardRepository;
    private final PermissionService permissionService;
    
    public Board execute(MoveCardCommand command) {
        // 1. 권한 검증
        if (!permissionService.canEditBoard(command.getUserId(), command.getSourceBoardId())) {
            throw new PermissionDeniedException("소스 보드 수정 권한이 없습니다.");
        }
        if (!permissionService.canEditBoard(command.getUserId(), command.getTargetBoardId())) {
            throw new PermissionDeniedException("타겟 보드 수정 권한이 없습니다.");
        }
        
        // 2. 도메인 객체 조회
        Board sourceBoard = boardRepository.findById(command.getSourceBoardId())
                .orElseThrow(() -> new BoardNotFoundException("소스 보드를 찾을 수 없습니다."));
        Board targetBoard = boardRepository.findById(command.getTargetBoardId())
                .orElseThrow(() -> new BoardNotFoundException("타겟 보드를 찾을 수 없습니다."));
        Card card = cardRepository.findById(command.getCardId())
                .orElseThrow(() -> new CardNotFoundException("카드를 찾을 수 없습니다."));
        
        // 3. 도메인 로직 실행
        Board updatedSourceBoard = sourceBoard.removeCard(card);
        Board updatedTargetBoard = targetBoard.addCard(card);
        
        // 4. 저장
        boardRepository.save(updatedSourceBoard);
        boardRepository.save(updatedTargetBoard);
        
        // 5. 도메인 모델 반환
        return updatedTargetBoard;
    }
}
```

## 도메인 모델 반환 원칙

### 1. 도메인 모델 직접 반환

```java
// 좋은 예: 도메인 모델 반환
@Service
public class CreateBoardUseCase {
    public Board execute(CreateBoardCommand command) {
        Board board = Board.create(command.getName());
        return boardRepository.save(board);
    }
}

// 나쁜 예: DTO 반환
@Service
public class CreateBoardUseCase {
    public BoardResponse execute(CreateBoardCommand command) {
        Board board = Board.create(command.getName());
        Board saved = boardRepository.save(board);
        return BoardResponse.from(saved); // UseCase에서 DTO 변환
    }
}
```

### 2. 프레젠테이션 레이어에서 DTO 변환

```java
@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final CreateBoardUseCase createBoardUseCase;
    
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@RequestBody CreateBoardRequest request) {
        CreateBoardCommand command = CreateBoardCommand.from(request, getCurrentUserId());
        Board board = createBoardUseCase.execute(command);
        
        // 프레젠테이션 레이어에서 DTO 변환
        BoardResponse response = BoardResponse.from(board);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

## Error Handling

### 1. Either 패턴 사용

```java
@Service
@Transactional
public class CreateBoardUseCase {
    private final BoardRepository boardRepository;
    
    public Either<Failure, Board> execute(CreateBoardCommand command) {
        // 1. 입력 검증
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            return Either.left(Failure.ofInputError("보드 이름은 필수입니다."));
        }
        
        // 2. 비즈니스 룰 검증
        if (boardRepository.existsByName(command.getName(), command.getOwnerId())) {
            return Either.left(Failure.ofResourceConflict(
                "동일한 이름의 보드가 이미 존재합니다.",
                "BOARD_NAME_DUPLICATE",
                Map.of("name", command.getName())
            ));
        }
        
        // 3. 도메인 로직 실행
        Board board = Board.create(command.getName(), command.getDescription());
        Board savedBoard = boardRepository.save(board);
        
        return Either.right(savedBoard);
    }
}
```

### 2. Exception 사용

```java
@Service
@Transactional
public class CreateBoardUseCase {
    private final BoardRepository boardRepository;
    
    public Board execute(CreateBoardCommand command) {
        // 1. 입력 검증
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("보드 이름은 필수입니다.");
        }
        
        // 2. 비즈니스 룰 검증
        if (boardRepository.existsByName(command.getName(), command.getOwnerId())) {
            throw new BoardNameDuplicateException("동일한 이름의 보드가 이미 존재합니다.");
        }
        
        // 3. 도메인 로직 실행
        Board board = Board.create(command.getName(), command.getDescription());
        return boardRepository.save(board);
    }
}
```

## 의존성 주입

### 1. 생성자 주입

```java
@Service
@Transactional
public class CreateBoardUseCase {
    private final BoardRepository boardRepository;
    private final EventPublisher eventPublisher;
    private final PermissionService permissionService;
    
    public CreateBoardUseCase(
            BoardRepository boardRepository,
            EventPublisher eventPublisher,
            PermissionService permissionService) {
        this.boardRepository = boardRepository;
        this.eventPublisher = eventPublisher;
        this.permissionService = permissionService;
    }
    
    public Board execute(CreateBoardCommand command) {
        // UseCase 로직
    }
}
```

### 2. 인터페이스 의존

```java
@Service
@Transactional
public class CreateBoardUseCase {
    private final BoardRepository boardRepository; // 인터페이스에 의존
    
    public CreateBoardUseCase(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }
}
```

## 테스트 전략

### 1. Unit Test

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
    void 보드_이름_중복_시_실패() {
        // given
        CreateBoardCommand command = CreateBoardCommand.of("중복 보드", "설명", userId);
        
        when(boardRepository.existsByName("중복 보드", userId)).thenReturn(true);
        
        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BoardNameDuplicateException.class)
                .hasMessage("동일한 이름의 보드가 이미 존재합니다.");
    }
}
```

### 2. Integration Test

```java
@SpringBootTest
@Transactional
class CreateBoardUseCaseIntegrationTest {
    
    @Autowired
    private CreateBoardUseCase useCase;
    
    @Autowired
    private BoardRepository boardRepository;
    
    @Test
    void 보드_생성_통합_테스트() {
        // given
        CreateBoardCommand command = CreateBoardCommand.of("통합 테스트 보드", "설명", userId);
        
        // when
        Board result = useCase.execute(command);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("통합 테스트 보드");
        
        Board savedBoard = boardRepository.findById(result.getId()).orElse(null);
        assertThat(savedBoard).isNotNull();
        assertThat(savedBoard.getName()).isEqualTo("통합 테스트 보드");
    }
}
```

## 모범 사례

### 1. 단일 책임 원칙

```java
// 좋은 예: 하나의 UseCase가 하나의 비즈니스 유스케이스만 처리
@Service
public class CreateBoardUseCase {
    public Board execute(CreateBoardCommand command) {
        // 보드 생성만 담당
    }
}

@Service
public class UpdateBoardUseCase {
    public Board execute(UpdateBoardCommand command) {
        // 보드 수정만 담당
    }
}

// 나쁜 예: 하나의 UseCase가 여러 작업을 처리
@Service
public class BoardManagementUseCase {
    public Board createBoard(CreateBoardCommand command) { /* ... */ }
    public Board updateBoard(UpdateBoardCommand command) { /* ... */ }
    public void deleteBoard(DeleteBoardCommand command) { /* ... */ }
}
```

### 2. 명확한 네이밍

```java
// 좋은 예: 명확한 동사 + 명사
CreateBoardUseCase
UpdateBoardUseCase
DeleteBoardUseCase
MoveCardUseCase
AssignCardToUserUseCase

// 나쁜 예: 모호한 이름
BoardUseCase
BoardService
BoardManager
```

### 3. 불변성 유지

```java
// 좋은 예: Command 객체는 불변
@Value
@Builder
public class CreateBoardCommand {
    String name;
    String description;
    UserId ownerId;
}

// 나쁜 예: 가변 Command
public class CreateBoardCommand {
    private String name;
    private String description;
    
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
}
```

### 4. 트랜잭션 경계 명확화

```java
// 좋은 예: 트랜잭션 경계가 명확
@Service
@Transactional
public class CreateBoardUseCase {
    public Board execute(CreateBoardCommand command) {
        // 모든 작업이 하나의 트랜잭션에서 실행
    }
}

// 나쁜 예: 트랜잭션 경계가 불분명
@Service
public class CreateBoardUseCase {
    @Transactional
    public Board execute(CreateBoardCommand command) {
        // 메서드 레벨 트랜잭션은 클래스 레벨보다 덜 명확
    }
}
```

## 성능 고려사항

### 1. 읽기 전용 트랜잭션

```java
@Service
@Transactional(readOnly = true)
public class GetBoardListUseCase {
    public List<Board> execute(GetBoardListQuery query) {
        // 읽기 전용 트랜잭션으로 성능 최적화
        return boardRepository.findByUserId(query.getUserId());
    }
}
```

### 2. 배치 처리

```java
@Service
@Transactional
public class BulkCreateCardsUseCase {
    public List<Card> execute(BulkCreateCardsCommand command) {
        List<Card> cards = command.getCardData().stream()
                .map(Card::create)
                .collect(Collectors.toList());
        
        // 배치 저장으로 성능 최적화
        return cardRepository.saveAll(cards);
    }
}
```

## 결론

UseCase 패턴을 통해 비즈니스 로직을 명확하게 캡슐화하고, 도메인 모델 중심의 설계를 유지할 수 있습니다. 이 문서의 가이드를 따라 일관된 UseCase 구현을 통해 유지보수성과 테스트 가능성을 높이세요.
