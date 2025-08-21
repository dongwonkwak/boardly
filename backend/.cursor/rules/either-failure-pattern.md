# 🔧 Either<Failure, T> 사용 규칙

## Either 리턴 타입 적용 범위
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

## 예외 처리 절대 금지 규칙
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

## Failure 타입별 사용법
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

## Either 체이닝 패턴
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

## Controller에서 Either 처리
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
