# 헥사고날 아키텍처 가이드

## 개요

Boardly 프로젝트는 헥사고날 아키텍처(포트와 어댑터 패턴)를 기반으로 설계되었습니다. 이 문서는 각 레이어의 역할과 책임, 그리고 레이어 간 상호작용 방법을 설명합니다.

## 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   REST API  │  │   Web UI    │  │   Mobile    │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  Use Cases  │  │  Commands   │  │   Queries   │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   Entities  │  │   Services  │  │  Value Obj  │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  Database   │  │   External  │  │   Message   │        │
│  │  Adapters   │  │   Services  │  │   Queues    │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

## 레이어별 역할과 책임

### 1. Domain Layer (`boardly-domain`)

**역할**: 비즈니스 로직의 핵심

**주요 구성요소**:
- **Entities**: 비즈니스 객체 (Board, Card, User 등)
- **Value Objects**: 불변 객체 (Email, BoardId 등)
- **Domain Services**: 도메인 로직을 캡슐화하는 서비스
- **Repositories**: 도메인 객체 저장소 인터페이스
- **Domain Events**: 도메인 이벤트 정의

**원칙**:
- 외부 의존성 없음
- 순수한 비즈니스 로직만 포함
- 인프라스트럭처 레이어에 대한 의존성 없음

**예시**:
```java
// Entity
public class Board {
    private BoardId id;
    private BoardName name;
    private List<Card> cards;
    
    public void addCard(Card card) {
        // 비즈니스 로직
    }
}

// Repository Interface
public interface BoardRepository {
    Board save(Board board);
    Optional<Board> findById(BoardId id);
}
```

### 2. Application Layer (`boardly-application`)

**역할**: 유스케이스 구현 및 도메인 조율

**주요 구성요소**:
- **Use Cases**: 비즈니스 유스케이스 구현
- **Commands**: 명령 객체
- **Queries**: 조회 객체
- **Application Services**: 애플리케이션 서비스

**원칙**:
- 도메인 레이어만 의존
- 트랜잭션 경계 정의
- 도메인 객체 조율

**예시**:
```java
@Service
@Transactional
public class CreateBoardUseCase {
    private final BoardRepository boardRepository;
    
    public Board execute(CreateBoardCommand command) {
        Board board = Board.create(command.getName());
        return boardRepository.save(board);
    }
}
```

### 3. Infrastructure Layer (`boardly-infrastructure`)

**역할**: 외부 시스템과의 연동

**주요 구성요소**:
- **Repository Implementations**: 데이터베이스 접근 구현
- **External Service Adapters**: 외부 API 연동
- **Message Brokers**: 메시지 큐 연동
- **Configuration**: 설정 관리

**원칙**:
- 도메인과 애플리케이션 레이어의 인터페이스 구현
- 기술적 세부사항 캡슐화

**예시**:
```java
@Repository
public class JpaBoardRepository implements BoardRepository {
    private final BoardJpaRepository jpaRepository;
    
    @Override
    public Board save(Board board) {
        BoardEntity entity = BoardMapper.toEntity(board);
        BoardEntity saved = jpaRepository.save(entity);
        return BoardMapper.toDomain(saved);
    }
}
```

### 4. Presentation Layer (`boardly-api`)

**역할**: 사용자 인터페이스 제공

**주요 구성요소**:
- **REST Controllers**: HTTP API 엔드포인트
- **DTOs**: 데이터 전송 객체
- **Request/Response Models**: 요청/응답 모델
- **Exception Handlers**: 예외 처리

**원칙**:
- 애플리케이션 레이어만 의존
- 사용자 입력 검증
- 응답 형식 표준화

**예시**:
```java
@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final CreateBoardUseCase createBoardUseCase;
    
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@RequestBody CreateBoardRequest request) {
        CreateBoardCommand command = CreateBoardCommand.of(request.getName());
        Board board = createBoardUseCase.execute(command);
        return ResponseEntity.ok(BoardResponse.from(board));
    }
}
```

## 의존성 방향

```
Presentation Layer
       ↓
Application Layer
       ↓
Domain Layer
       ↑
Infrastructure Layer
```

**중요한 원칙**:
- 의존성은 항상 안쪽(도메인)을 향함
- 바깥쪽 레이어는 안쪽 레이어의 인터페이스만 알 수 있음
- 안쪽 레이어는 바깥쪽 레이어를 전혀 모름

## 모듈 구조

```
boardly/
├── boardly-domain/           # 도메인 레이어
├── boardly-application/      # 애플리케이션 레이어
├── boardly-infrastructure/   # 인프라스트럭처 레이어
├── boardly-api/             # 프레젠테이션 레이어
├── boardly-app/             # 애플리케이션 설정
└── boardly-schema/          # 데이터베이스 스키마
```

## 포트와 어댑터 패턴

### 포트 (Ports)
- **Inbound Ports**: 애플리케이션이 외부로부터 받는 요청
- **Outbound Ports**: 애플리케이션이 외부로 보내는 요청

### 어댑터 (Adapters)
- **Primary Adapters**: 외부에서 애플리케이션을 호출하는 어댑터 (REST API, CLI 등)
- **Secondary Adapters**: 애플리케이션이 외부를 호출하는 어댑터 (Database, External API 등)

## 테스트 전략

### 1. Domain Layer
- **Unit Tests**: 도메인 로직 단위 테스트
- **Mock 없음**: 순수한 비즈니스 로직만 테스트

### 2. Application Layer
- **Integration Tests**: UseCase와 Repository 통합 테스트
- **Mock Repository**: 외부 의존성 모킹

### 3. Infrastructure Layer
- **Integration Tests**: 실제 데이터베이스 연동 테스트
- **TestContainers**: 격리된 환경에서 테스트

### 4. Presentation Layer
- **Unit Tests**: Controller 단위 테스트
- **Integration Tests**: 전체 API 스택 테스트

## 모범 사례

### 1. 도메인 객체 설계
```java
// 좋은 예: 불변성과 캡슐화
public class Board {
    private final BoardId id;
    private final BoardName name;
    private final List<Card> cards;
    
    public Board addCard(Card card) {
        List<Card> newCards = new ArrayList<>(cards);
        newCards.add(card);
        return new Board(id, name, newCards);
    }
}

// 나쁜 예: 가변 상태
public class Board {
    private List<Card> cards;
    
    public void addCard(Card card) {
        cards.add(card); // 가변 상태 변경
    }
}
```

### 2. UseCase 설계
```java
// 좋은 예: 명확한 책임 분리
@Service
@Transactional
public class CreateBoardUseCase {
    private final BoardRepository boardRepository;
    private final EventPublisher eventPublisher;
    
    public Board execute(CreateBoardCommand command) {
        Board board = Board.create(command.getName());
        Board saved = boardRepository.save(board);
        eventPublisher.publish(new BoardCreatedEvent(saved.getId()));
        return saved;
    }
}
```

### 3. Repository 패턴
```java
// 좋은 예: 도메인 중심 인터페이스
public interface BoardRepository {
    Board save(Board board);
    Optional<Board> findById(BoardId id);
    List<Board> findByUserId(UserId userId);
}

// 나쁜 예: 기술 중심 인터페이스
public interface BoardRepository {
    BoardEntity save(BoardEntity entity);
    Optional<BoardEntity> findById(Long id);
}
```

## 결론

헥사고날 아키텍처는 비즈니스 로직을 기술적 세부사항으로부터 분리하여 유지보수성과 테스트 가능성을 높입니다. 이 가이드를 따라 일관된 아키텍처를 유지하면서 확장 가능한 시스템을 구축할 수 있습니다.
