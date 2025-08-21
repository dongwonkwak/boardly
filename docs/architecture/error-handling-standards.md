# 에러 처리 표준

## 개요

Boardly 프로젝트는 일관된 에러 처리 방식을 통해 예측 가능하고 안정적인 API를 제공합니다. 이 문서는 도메인 레이어부터 프레젠테이션 레이어까지의 에러 처리 방법을 정의합니다.

## 에러 처리 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                Presentation Layer (API)                     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              ApiFailureHandler                      │   │
│  │  - Failure → HTTP Status Code 매핑                 │   │
│  │  - ErrorResponse 생성                              │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Application Layer (UseCase)                    │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Either<Failure, Success>               │   │
│  │  - 도메인 로직 실행 결과 처리                        │   │
│  │  - Failure 반환 또는 Success 반환                   │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                Domain Layer                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    Failure                          │   │
│  │  - InputError, NotFound, PermissionDenied 등       │   │
│  │  - 비즈니스 로직 실패 표현                           │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Failure 클래스 구조

### 기본 Failure 타입

```java
public abstract class Failure {
    private final String message;
    
    // 400 Bad Request - 입력 검증 실패
    public static class InputError extends Failure {
        private final String errorCode;
        private final List<FieldViolation> violations;
    }
    
    // 403 Forbidden - 권한 부족
    public static class PermissionDenied extends Failure {
        private final String errorCode;
        private final Object context;
    }
    
    // 404 Not Found - 리소스 미발견
    public static class NotFound extends Failure {
        private final String errorCode;
        private final Object context;
    }
    
    // 409 Conflict - 리소스 충돌
    public static class ResourceConflict extends Failure {
        private final String errorCode;
        private final Object context;
    }
    
    // 412 Precondition Failed - 전제 조건 실패
    public static class PreconditionFailed extends Failure {
        private final String errorCode;
        private final Object context;
    }
    
    // 422 Unprocessable Entity - 비즈니스 룰 위반
    public static class BusinessRuleViolation extends Failure {
        private final String errorCode;
        private final Object context;
    }
    
    // 500 Internal Server Error - 내부 서버 오류
    public static class InternalError extends Failure {
        private final String errorCode;
        private final Object context;
    }
}
```

### Factory Methods

```java
// 입력 검증 실패
public static InputError ofInputError(String message, String errorCode, List<FieldViolation> violations)
public static InputError ofInputError(String message)
public static InputError ofValidation(String message, List<FieldViolation> violations)

// 권한 거부
public static PermissionDenied ofPermissionDenied(String message, String errorCode, Object context)
public static PermissionDenied ofPermissionDenied(String message)

// 리소스 미발견
public static NotFound ofNotFound(String message, String errorCode, Object context)
public static NotFound ofNotFound(String message)

// 리소스 충돌
public static ResourceConflict ofResourceConflict(String message, String errorCode, Object context)
public static ResourceConflict ofConflict(String message)

// 비즈니스 룰 위반
public static BusinessRuleViolation ofBusinessRuleViolation(String message, String errorCode, Object context)
public static BusinessRuleViolation ofBusinessRuleViolation(String message)

// 내부 서버 오류
public static InternalError ofInternalError(String message, String errorCode, Object context)
public static InternalError ofInternalServerError(String message)
```

## HTTP 상태 코드 매핑

| Failure 타입 | HTTP 상태 코드 | 설명 |
|-------------|---------------|------|
| `InputError` | 400 Bad Request | 입력 검증 실패 |
| `PermissionDenied` | 403 Forbidden | 권한 부족 |
| `NotFound` | 404 Not Found | 리소스 미발견 |
| `ResourceConflict` | 409 Conflict | 리소스 충돌 |
| `PreconditionFailed` | 412 Precondition Failed | 전제 조건 실패 |
| `BusinessRuleViolation` | 422 Unprocessable Entity | 비즈니스 룰 위반 |
| `InternalError` | 500 Internal Server Error | 내부 서버 오류 |

## 레이어별 에러 처리

### 1. Domain Layer

도메인 레이어에서는 비즈니스 로직 실패를 `Failure` 객체로 표현합니다.

```java
public class Board {
    public Either<Failure, Board> addCard(Card card) {
        if (isArchived()) {
            return Either.left(Failure.ofBusinessRuleViolation(
                "아카이브된 보드에는 카드를 추가할 수 없습니다.",
                "BOARD_ARCHIVED",
                Map.of("boardId", id, "status", "ARCHIVED")
            ));
        }
        
        if (cards.size() >= MAX_CARDS) {
            return Either.left(Failure.ofBusinessRuleViolation(
                "보드의 최대 카드 수를 초과했습니다.",
                "CARD_LIMIT_EXCEEDED",
                Map.of("currentCount", cards.size(), "maxCount", MAX_CARDS)
            ));
        }
        
        List<Card> newCards = new ArrayList<>(cards);
        newCards.add(card);
        return Either.right(new Board(id, name, newCards));
    }
}
```

### 2. Application Layer

애플리케이션 레이어에서는 도메인 로직의 결과를 처리하고 적절한 `Failure`를 반환합니다.

```java
@Service
@Transactional
public class AddCardToBoardUseCase {
    private final BoardRepository boardRepository;
    
    public Either<Failure, Board> execute(AddCardCommand command) {
        return boardRepository.findById(command.getBoardId())
            .map(board -> board.addCard(command.getCard())
                .peek(updatedBoard -> boardRepository.save(updatedBoard)))
            .orElse(Either.left(Failure.ofNotFound(
                "보드를 찾을 수 없습니다.",
                "BOARD_NOT_FOUND",
                Map.of("boardId", command.getBoardId())
            )));
    }
}
```

### 3. Presentation Layer

프레젠테이션 레이어에서는 `ApiFailureHandler`를 통해 `Failure`를 HTTP 응답으로 변환합니다.

```java
@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final AddCardToBoardUseCase addCardUseCase;
    private final ApiFailureHandler failureHandler;
    
    @PostMapping("/{boardId}/cards")
    public ResponseEntity<?> addCard(
            @PathVariable String boardId,
            @RequestBody AddCardRequest request) {
        
        AddCardCommand command = AddCardCommand.of(boardId, request.getCardData());
        
        return addCardUseCase.execute(command)
            .map(board -> ResponseEntity.ok(BoardResponse.from(board)))
            .getOrElse(failure -> failureHandler.handleFailure(failure));
    }
}
```

## ApiFailureHandler 구현

```java
@Component
public class ApiFailureHandler {
    
    public ResponseEntity<ErrorResponse> handleFailure(Failure failure) {
        return switch (failure) {
            case Failure.InputError inputError -> handleInputError(inputError);
            case Failure.PermissionDenied permissionDenied -> handlePermissionDenied(permissionDenied);
            case Failure.NotFound notFound -> handleNotFound(notFound);
            case Failure.ResourceConflict resourceConflict -> handleResourceConflict(resourceConflict);
            case Failure.PreconditionFailed preconditionFailed -> handlePreconditionFailed(preconditionFailed);
            case Failure.BusinessRuleViolation businessRuleViolation -> handleBusinessRuleViolation(businessRuleViolation);
            case Failure.InternalError internalError -> handleInternalError(internalError);
            default -> handleUnknownFailure(failure);
        };
    }
    
    private ResponseEntity<ErrorResponse> handleInputError(Failure.InputError inputError) {
        log.warn("입력 오류: errorCode={}, message={}, violations={}",
                inputError.getErrorCode(), inputError.getMessage(), inputError.getViolations().size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.validation(inputError));
    }
    
    private ResponseEntity<ErrorResponse> handlePermissionDenied(Failure.PermissionDenied permissionDenied) {
        log.warn("권한 거부: errorCode={}, message={}", 
                permissionDenied.getErrorCode(), permissionDenied.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.forbidden(permissionDenied));
    }
    
    private ResponseEntity<ErrorResponse> handleNotFound(Failure.NotFound notFound) {
        log.debug("리소스 미발견: errorCode={}, message={}", 
                notFound.getErrorCode(), notFound.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.notFound(notFound));
    }
    
    private ResponseEntity<ErrorResponse> handleResourceConflict(Failure.ResourceConflict resourceConflict) {
        log.warn("리소스 충돌: errorCode={}, message={}", 
                resourceConflict.getErrorCode(), resourceConflict.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.conflict(resourceConflict));
    }
    
    private ResponseEntity<ErrorResponse> handleBusinessRuleViolation(Failure.BusinessRuleViolation businessRuleViolation) {
        log.warn("비즈니스 룰 위반: errorCode={}, message={}", 
                businessRuleViolation.getErrorCode(), businessRuleViolation.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.businessRuleViolation(businessRuleViolation));
    }
    
    private ResponseEntity<ErrorResponse> handleInternalError(Failure.InternalError internalError) {
        log.error("내부 서버 오류: errorCode={}, message={}", 
                internalError.getErrorCode(), internalError.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.internal(internalError));
    }
}
```

## 에러 코드 표준

### 일반 에러 코드

| 코드 | 설명 | 사용 예시 |
|------|------|-----------|
| `VALIDATION_ERROR` | 입력 검증 실패 | 필수 필드 누락, 형식 오류 |
| `INVALID_INPUT` | 잘못된 입력 | 잘못된 데이터 타입 |
| `PERMISSION_DENIED` | 권한 부족 | 읽기 전용 리소스 수정 |
| `NOT_FOUND` | 리소스 미발견 | 존재하지 않는 ID 조회 |
| `RESOURCE_CONFLICT` | 리소스 충돌 | 중복 생성 시도 |
| `BUSINESS_RULE_VIOLATION` | 비즈니스 룰 위반 | 도메인 규칙 위반 |
| `INTERNAL_ERROR` | 내부 서버 오류 | 예상치 못한 오류 |

### 도메인별 에러 코드

#### 보드 관련
- `BOARD_NOT_FOUND`: 보드를 찾을 수 없음
- `BOARD_ARCHIVED`: 아카이브된 보드
- `BOARD_NAME_DUPLICATE`: 보드 이름 중복
- `BOARD_ACCESS_DENIED`: 보드 접근 권한 없음

#### 카드 관련
- `CARD_NOT_FOUND`: 카드를 찾을 수 없음
- `CARD_LIMIT_EXCEEDED`: 카드 개수 제한 초과
- `CARD_STATUS_INVALID`: 카드 상태가 유효하지 않음

#### 사용자 관련
- `USER_NOT_FOUND`: 사용자를 찾을 수 없음
- `USER_EMAIL_DUPLICATE`: 이메일 중복
- `USER_INACTIVE`: 비활성 사용자

## 로깅 전략

### 로그 레벨

| Failure 타입 | 로그 레벨 | 이유 |
|-------------|----------|------|
| `InputError` | WARN | 클라이언트 오류이지만 주의 필요 |
| `PermissionDenied` | WARN | 보안 관련 이슈 |
| `NotFound` | DEBUG | 일반적인 상황 |
| `ResourceConflict` | WARN | 비즈니스 로직 충돌 |
| `BusinessRuleViolation` | WARN | 비즈니스 규칙 위반 |
| `InternalError` | ERROR | 시스템 오류 |

### 로그 메시지 형식

```java
// 입력 오류
log.warn("입력 오류: errorCode={}, message={}, violations={}",
        inputError.getErrorCode(), inputError.getMessage(), inputError.getViolations().size());

// 권한 거부
log.warn("권한 거부: errorCode={}, message={}, context={}",
        permissionDenied.getErrorCode(), permissionDenied.getMessage(), permissionDenied.getContext());

// 내부 서버 오류
log.error("내부 서버 오류: errorCode={}, message={}, context={}",
        internalError.getErrorCode(), internalError.getMessage(), internalError.getContext());
```

## 테스트 전략

### 1. Domain Layer 테스트

```java
@Test
void 보드가_아카이브된_경우_카드_추가_실패() {
    // given
    Board archivedBoard = Board.create("테스트 보드").archive();
    Card card = Card.create("테스트 카드");
    
    // when
    Either<Failure, Board> result = archivedBoard.addCard(card);
    
    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft())
        .isInstanceOf(Failure.BusinessRuleViolation.class)
        .satisfies(failure -> {
            assertThat(failure.getErrorCode()).isEqualTo("BOARD_ARCHIVED");
            assertThat(failure.getMessage()).contains("아카이브된 보드");
        });
}
```

### 2. UseCase 테스트

```java
@Test
void 존재하지_않는_보드에_카드_추가_시_NotFound_반환() {
    // given
    String nonExistentBoardId = "non-existent-id";
    AddCardCommand command = AddCardCommand.of(nonExistentBoardId, cardData);
    
    when(boardRepository.findById(nonExistentBoardId))
        .thenReturn(Optional.empty());
    
    // when
    Either<Failure, Board> result = useCase.execute(command);
    
    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft())
        .isInstanceOf(Failure.NotFound.class)
        .satisfies(failure -> {
            assertThat(failure.getErrorCode()).isEqualTo("BOARD_NOT_FOUND");
        });
}
```

### 3. Controller 테스트

```java
@Test
void 입력_검증_실패_시_400_반환() {
    // given
    AddCardRequest invalidRequest = new AddCardRequest(""); // 빈 제목
    
    // when
    ResponseEntity<?> response = controller.addCard("board-id", invalidRequest);
    
    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody())
        .isInstanceOf(ErrorResponse.class)
        .satisfies(error -> {
            assertThat(error.getError().getCode()).isEqualTo("VALIDATION_ERROR");
        });
}
```

## 모범 사례

### 1. 명확한 에러 메시지

```java
// 좋은 예
Failure.ofInputError("보드 이름은 1자 이상 100자 이하여야 합니다.")

// 나쁜 예
Failure.ofInputError("Invalid input")
```

### 2. 적절한 컨텍스트 제공

```java
// 좋은 예
Failure.ofBusinessRuleViolation(
    "보드의 최대 카드 수를 초과했습니다.",
    "CARD_LIMIT_EXCEEDED",
    Map.of("currentCount", cards.size(), "maxCount", MAX_CARDS)
)

// 나쁜 예
Failure.ofBusinessRuleViolation("카드 추가 실패")
```

### 3. 일관된 에러 코드 사용

```java
// 도메인별로 일관된 에러 코드 사용
public static final String BOARD_NOT_FOUND = "BOARD_NOT_FOUND";
public static final String CARD_NOT_FOUND = "CARD_NOT_FOUND";
public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
```

### 4. 적절한 로그 레벨 사용

```java
// 클라이언트 오류는 WARN, 시스템 오류는 ERROR
log.warn("입력 오류: {}", inputError.getMessage());
log.error("내부 서버 오류: {}", internalError.getMessage(), exception);
```

## 결론

일관된 에러 처리 방식을 사용하면 클라이언트가 예측 가능한 방식으로 오류를 처리할 수 있고, 개발자는 효율적으로 디버깅할 수 있습니다. 이 문서의 가이드를 따라 모든 레이어에서 동일한 에러 처리 패턴을 적용하세요.
