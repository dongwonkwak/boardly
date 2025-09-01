# Boardly - 백엔드 API 구현 가이드

> 📘 **참고**: 이 문서는 API 설계 원칙의 구체적인 백엔드 구현 방법을 다룹니다.  
> API 설계 원칙은 `api-design-principles.md` 문서를 참조하세요.

## 아키텍처 기반 구현

### Hexagonal Architecture 기반 UseCase → Controller 흐름

**핵심 구현 원칙**:
- UseCase는 `Either<Failure, Domain>` 타입 반환
- **검증은 Service 레벨에서만 수행** (Controller에서 검증 안함)
- Controller는 HTTP 관심사만 처리 (상태 코드, 헤더 등)
- 도메인 객체를 응답 DTO로 변환
- **글로벌 에러 처리 사용하지 않음** (Controller에서 직접 처리)

### UseCase 서비스 패턴

> 📘 **참고**: UseCase 서비스 패턴은 백엔드의 `usecase-service-pattern.mdc` 문서 참조

**UseCase Interface 정의:**
```java
// port/in 패키지
public interface CreateWorkspaceUseCase {
    Either<Failure, Workspace> execute(CreateWorkspaceCommand command);
}

public interface UpdateWorkspaceUseCase {
    Either<Failure, Workspace> execute(UpdateWorkspaceCommand command);
}

public interface DeleteWorkspaceUseCase {
    Either<Failure, Void> execute(DeleteWorkspaceCommand command);
}

public interface GetWorkspaceUseCase {
    Either<Failure, Workspace> findById(WorkspaceId workspaceId);
}
```

**Service 구현체 예시:**
```java
@UseCase(category = "CRUD")
@Service
@Transactional
@RequiredArgsConstructor
public class WorkspaceCrudService implements 
    CreateWorkspaceUseCase,
    UpdateWorkspaceUseCase,
    DeleteWorkspaceUseCase,
    GetWorkspaceUseCase {
    
    private final CreateWorkspaceCommandValidator createValidator;
    private final UpdateWorkspaceCommandValidator updateValidator;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceDomainService domainService;
    
    @Override
    public Either<Failure, Workspace> execute(CreateWorkspaceCommand command) {
        // 1. 입력 검증
        Validation<Seq<String>, CreateWorkspaceCommand> validation = createValidator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofInputError(validation.getError().asJava()));
        }
        
        // 2. 비즈니스 룰 검증
        Either<Failure, Void> businessValidation = domainService.validateWorkspaceCreation(command);
        if (businessValidation.isLeft()) {
            return businessValidation.map(v -> null);
        }
        
        // 3. 도메인 객체 생성
        Workspace workspace = Workspace.create(command);
        
        // 4. 저장
        return workspaceRepository.save(workspace);
    }
    
    @Override
    public Either<Failure, Workspace> execute(UpdateWorkspaceCommand command) {
        Validation<Seq<String>, UpdateWorkspaceCommand> validation = updateValidator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofInputError(validation.getError().asJava()));
        }
        
        return workspaceRepository.findById(command.workspaceId())
            .map(workspace -> {
                workspace.updateName(command.name());
                workspace.updateDescription(command.description());
                return workspaceRepository.save(workspace);
            })
            .orElse(Either.left(Failure.ResourceNotFound("Workspace not found")));
    }
    
    @Override
    public Either<Failure, Void> execute(DeleteWorkspaceCommand command) {
        return workspaceRepository.findById(command.workspaceId())
            .map(workspace -> {
                workspaceRepository.delete(workspace.getId());
                return Either.<Failure, Void>right(null);
            })
            .orElse(Either.left(Failure.ResourceNotFound("Workspace not found")));
    }
    
    @Override
    public Either<Failure, Workspace> findById(WorkspaceId workspaceId) {
        return workspaceRepository.findById(workspaceId)
            .map(Either::<Failure, Workspace>right)
            .orElse(Either.left(Failure.ResourceNotFound("Workspace not found")));
    }
}
```

## 데이터 검증 구현

### Service 레벨 검증 전략

**핵심 원칙**:
- **Controller에서는 검증하지 않음**
- **Service(UseCase) 레벨에서만 검증 수행**
- Vavr Validation을 통한 함수형 검증
- 입력 검증과 비즈니스 룰 검증 분리

**검증 흐름**:
1. Controller: HTTP 요청 → Command 객체 변환 (검증 없음)
2. Service: Command 객체 검증 → 비즈니스 룰 검증 → 도메인 객체 처리
3. Controller: Service 결과 → HTTP 응답 변환

### Vavr Validation 구현 예시

```java
public class WorkspaceValidationService {
    
    public static Validation<String, String> validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Validation.invalid("워크스페이스 이름은 필수입니다.");
        }
        if (name.length() > 100) {
            return Validation.invalid("워크스페이스 이름은 100자 이하여야 합니다.");
        }
        return Validation.valid(name);
    }
    
    public static Validation<String, String> validateDescription(String description) {
        if (description != null && description.length() > 500) {
            return Validation.invalid("워크스페이스 설명은 500자 이하여야 합니다.");
        }
        return Validation.valid(description);
    }
    
    public static Validation<String, UserId> validateOwnerId(UserId ownerId) {
        if (ownerId == null) {
            return Validation.invalid("소유자 ID는 필수입니다.");
        }
        return Validation.valid(ownerId);
    }
}
```

### 응답 데이터 설계 및 구현

**민감한 정보 제외 원칙:**
```java
// 민감한 정보 제외하고 필요한 정보만 노출
public class UserResponse {
    private String id;           // usr_01ARZ3NDEKTSV4RRFFQ69G5FAV
    private String email;
    private String displayName;
    private Instant createdAt;
    // password, refreshToken 필드 제외
    
    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId().getValue())
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
```

**응답 DTO 변환 패턴:**
```java
public class WorkspaceResponse {
    private String id;
    private String name;
    private String description;
    private WorkspaceType type;
    private Instant createdAt;
    private UserSummary owner;
    
    public static WorkspaceResponse from(Workspace workspace) {
        return WorkspaceResponse.builder()
            .id(workspace.getId().getValue())
            .name(workspace.getName())
            .description(workspace.getDescription())
            .type(workspace.getType())
            .createdAt(workspace.getCreatedAt())
            .owner(UserSummary.from(workspace.getOwner()))
            .build();
    }
}
```

## Controller 구현

### Controller 레벨 에러 처리

> 📘 **프로젝트 정책**: 글로벌 에러 핸들러 사용하지 않음. 각 Controller에서 직접 에러 처리 수행.

**Controller에서 직접 에러 처리 예시:**
```java
@RestController
@RequestMapping("/api/v1/workspaces")
@Tag(name = "Workspace", description = "워크스페이스 관리 API")
public class WorkspaceController {
    
    private final CreateWorkspaceUseCase createWorkspaceUseCase;
    private final UpdateWorkspaceUseCase updateWorkspaceUseCase;
    private final DeleteWorkspaceUseCase deleteWorkspaceUseCase;
    private final GetWorkspaceUseCase getWorkspaceUseCase;
    
    @PostMapping
    @Operation(summary = "워크스페이스 생성", description = "새로운 워크스페이스를 생성합니다")
    public ResponseEntity<?> createWorkspace(@RequestBody CreateWorkspaceRequest request, 
                                           HttpServletRequest httpRequest) {
        // 1. Request → Command 변환 (검증 없음)
        CreateWorkspaceCommand command = CreateWorkspaceCommand.builder()
            .name(request.getName())
            .description(request.getDescription())
            .type(request.getType())
            .ownerId(getCurrentUserId())
            .build();
        
        // 2. UseCase 호출
        Either<Failure, Workspace> result = createWorkspaceUseCase.execute(command);
        
        // 3. 결과에 따른 HTTP 응답 처리
        return result.fold(
            failure -> switch (failure.getType()) {
                case INPUT_ERROR -> ResponseEntity.badRequest()
                    .body(ErrorResponse.of(failure, httpRequest.getRequestURI()));
                case BUSINESS_ERROR -> ResponseEntity.unprocessableEntity()
                    .body(ErrorResponse.of(failure, httpRequest.getRequestURI()));
                case NOT_FOUND -> ResponseEntity.notFound().build();
                case PERMISSION_DENIED -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(failure, httpRequest.getRequestURI()));
                default -> ResponseEntity.internalServerError()
                    .body(ErrorResponse.of(failure, httpRequest.getRequestURI()));
            },
            workspace -> ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(WorkspaceResponse.from(workspace)))
        );
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "워크스페이스 조회", description = "특정 워크스페이스를 조회합니다")
    public ResponseEntity<?> getWorkspace(@PathVariable String id, HttpServletRequest httpRequest) {
        WorkspaceId workspaceId = WorkspaceId.from(id);
        Either<Failure, Workspace> result = getWorkspaceUseCase.execute(workspaceId);
        
        return result.fold(
            failure -> switch (failure.getType()) {
                case NOT_FOUND -> ResponseEntity.notFound().build();
                case PERMISSION_DENIED -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(failure, httpRequest.getRequestURI()));
                default -> ResponseEntity.internalServerError()
                    .body(ErrorResponse.of(failure, httpRequest.getRequestURI()));
            },
            workspace -> ResponseEntity.ok()
                .body(SuccessResponse.of(WorkspaceResponse.from(workspace)))
        );
    }
}
```

### 응답 래퍼 클래스 구현

**성공 응답 래퍼:**
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse<T> {
    private final T data;
    private final MetaInfo meta;
    
    private SuccessResponse(T data, MetaInfo meta) {
        this.data = data;
        this.meta = meta;
    }
    
    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(data, MetaInfo.create());
    }
    
    public static <T> SuccessResponse<T> of(T data, String apiVersion) {
        return new SuccessResponse<>(data, MetaInfo.create(apiVersion));
    }
    
    // getters
    public T getData() { return data; }
    public MetaInfo getMeta() { return meta; }
}
```

**에러 응답 래퍼:**
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final ErrorInfo error;
    private final MetaInfo meta;
    
    private ErrorResponse(ErrorInfo error, MetaInfo meta) {
        this.error = error;
        this.meta = meta;
    }
    
    public static ErrorResponse of(Failure failure, String path) {
        ErrorInfo errorInfo = ErrorInfo.builder()
            .code(failure.getCode())
            .message(failure.getMessage())
            .context(failure.getContext())
            .details(failure.getDetails())
            .build();
            
        MetaInfo metaInfo = MetaInfo.builder()
            .timestamp(Instant.now())
            .path(path)
            .apiVersion("v1")
            .requestId(generateRequestId())
            .build();
            
        return new ErrorResponse(errorInfo, metaInfo);
    }
    
    // getters
    public ErrorInfo getError() { return error; }
    public MetaInfo getMeta() { return meta; }
}
```

**Meta 정보 클래스:**
```java
public class MetaInfo {
    private final Instant timestamp;
    private final String apiVersion;
    private final String requestId;
    private final String path;
    private final String etag;
    
    public static MetaInfo create() {
        return MetaInfo.builder()
            .timestamp(Instant.now())
            .apiVersion("v1")
            .requestId(generateRequestId())
            .build();
    }
    
    public static MetaInfo create(String apiVersion) {
        return MetaInfo.builder()
            .timestamp(Instant.now())
            .apiVersion(apiVersion)
            .requestId(generateRequestId())
            .build();
    }
    
    private static String generateRequestId() {
        return "req-" + UUID.randomUUID().toString();
    }
    
    // getters and builder
}
```

## API 문서화 구현

### OpenAPI 3.0 스펙 자동 생성

**SpringDoc OpenAPI 설정:**
```java
@OpenAPIDefinition(
    info = @Info(
        title = "Boardly API",
        version = "v1",
        description = "칸반 보드 기반 협업 프로젝트 관리 서비스"
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "개발 서버"),
        @Server(url = "https://api.boardly.example.com", description = "운영 서버")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearerAuth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
```

### 주석 기반 문서화 예시

**상세한 API 문서화:**
```java
@RestController
@RequestMapping("/api/v1/workspaces")
@Tag(name = "Workspace", description = "워크스페이스 관리 API")
public class WorkspaceController {
    
    @Operation(
        summary = "워크스페이스 생성", 
        description = "새로운 워크스페이스를 생성합니다. 사용자당 최대 5개까지 생성 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "워크스페이스 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkspaceResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 (입력 검증 실패)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "422", 
            description = "비즈니스 룰 위반 (예: 워크스페이스 한도 초과)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping
    public ResponseEntity<?> createWorkspace(
            @Parameter(description = "워크스페이스 생성 요청", required = true)
            @Valid @RequestBody CreateWorkspaceRequest request,
            HttpServletRequest httpRequest) {
        // 구현
    }
}
```

## 성능 최적화 구현

### 페이징 구현

**페이징 요청 파라미터:**
```java
public class PageRequest {
    @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private Integer page = 1;
    
    @Parameter(description = "페이지 크기", example = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    private Integer size = 20;
    
    @Parameter(description = "정렬 기준", example = "name,asc")
    private String sort;
    
    // getters, setters
}
```

**페이징 응답 구조:**
```java
public class PagedResponse<T> {
    private List<T> items;
    private PaginationInfo pagination;
    
    public static <T> PagedResponse<T> of(List<T> items, long totalElements, int page, int size) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        PaginationInfo pagination = PaginationInfo.builder()
            .page(page)
            .size(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .hasNext(page < totalPages)
            .hasPrevious(page > 1)
            .first(page == 1)
            .last(page == totalPages)
            .build();
            
        return new PagedResponse<>(items, pagination);
    }
}
```

### 필드 선택 (Sparse Fieldsets) 구현

```java
@GetMapping
public ResponseEntity<?> getWorkspaces(
        @RequestParam(required = false) Set<String> fields,
        PageRequest pageRequest) {
    
    Either<Failure, Page<Workspace>> result = getWorkspacesUseCase.execute(pageRequest);
    
    return result.fold(
        failure -> handleFailure(failure),
        page -> {
            List<WorkspaceResponse> responses = page.getContent().stream()
                .map(workspace -> WorkspaceResponse.from(workspace, fields))
                .collect(Collectors.toList());
                
            return ResponseEntity.ok()
                .body(SuccessResponse.of(PagedResponse.of(responses, page)));
        }
    );
}
```

### 조건부 요청 (ETag) 구현

```java
@GetMapping("/{id}")
public ResponseEntity<?> getWorkspace(
        @PathVariable String id,
        @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
    
    WorkspaceId workspaceId = WorkspaceId.from(id);
    Either<Failure, Workspace> result = getWorkspaceUseCase.execute(workspaceId);
    
    return result.fold(
        failure -> handleFailure(failure),
        workspace -> {
            String etag = generateETag(workspace);
            
            // If-None-Match 헤더 확인
            if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            
            return ResponseEntity.ok()
                .eTag(etag)
                .body(SuccessResponse.of(WorkspaceResponse.from(workspace)));
        }
    );
}

private String generateETag(Workspace workspace) {
    String content = workspace.getId() + ":" + workspace.getUpdatedAt();
    return "\"" + DigestUtils.md5DigestAsHex(content.getBytes()) + "\"";
}
```

## 테스트 구현

### Controller 테스트

```java
@WebMvcTest(WorkspaceController.class)
class WorkspaceControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CreateWorkspaceUseCase createWorkspaceUseCase;
    
    @Test
    void createWorkspace_Success() throws Exception {
        // Given
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
            .name("테스트 워크스페이스")
            .description("테스트 설명")
            .type(WorkspaceType.TEAM)
            .build();
            
        Workspace workspace = Workspace.builder()
            .id(WorkspaceId.generate())
            .name("테스트 워크스페이스")
            .description("테스트 설명")
            .type(WorkspaceType.TEAM)
            .build();
            
        when(createWorkspaceUseCase.execute(any())).thenReturn(Either.right(workspace));
        
        // When & Then
        mockMvc.perform(post("/api/v1/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("테스트 워크스페이스"))
            .andExpected(jsonPath("$.meta.apiVersion").value("v1"));
    }
    
    @Test
    void createWorkspace_ValidationError() throws Exception {
        // Given
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
            .name("") // 빈 이름
            .build();
            
        Failure failure = Failure.ofInputError("VALIDATION_ERROR", "입력 데이터가 유효하지 않습니다.");
        when(createWorkspaceUseCase.execute(any())).thenReturn(Either.left(failure));
        
        // When & Then
        mockMvc.perform(post("/api/v1/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
```

### UseCase 테스트

```java
class WorkspaceCrudServiceTest {
    
    @Mock
    private CreateWorkspaceCommandValidator createValidator;
    
    @Mock
    private UpdateWorkspaceCommandValidator updateValidator;
    
    @Mock
    private WorkspaceRepository workspaceRepository;
    
    @Mock
    private WorkspaceDomainService domainService;
    
    @InjectMocks
    private WorkspaceCrudService service;
    
    @Test
    void createWorkspace_Success() {
        // Given
        CreateWorkspaceCommand command = CreateWorkspaceCommand.builder()
            .name("테스트 워크스페이스")
            .description("테스트 설명")
            .type(WorkspaceType.TEAM)
            .ownerId(UserId.generate())
            .build();
            
        when(createValidator.validate(command)).thenReturn(Validation.valid(command));
        when(domainService.validateWorkspaceCreation(command)).thenReturn(Either.right(null));
        when(workspaceRepository.save(any())).thenReturn(Either.right(any(Workspace.class)));
        
        // When
        Either<Failure, Workspace> result = service.execute(command);
        
        // Then
        assertThat(result.isRight()).isTrue();
    }
    
    @Test
    void createWorkspace_ValidationError() {
        // Given
        CreateWorkspaceCommand command = CreateWorkspaceCommand.builder()
            .name("") // 빈 이름
            .build();
            
        when(createValidator.validate(command))
            .thenReturn(Validation.invalid(Seq.of("이름은 필수입니다.")));
        
        // When
        Either<Failure, Workspace> result = service.execute(command);
        
        // Then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getType()).isEqualTo(FailureType.INPUT_ERROR);
    }
}
```

## 구현 체크리스트

### Controller 구현
- [ ] UseCase 의존성 주입
- [ ] Request → Command 변환 (검증 없음)
- [ ] Either 결과를 HTTP 응답으로 변환
- [ ] 적절한 HTTP 상태 코드 설정
- [ ] OpenAPI 어노테이션 추가

### UseCase 구현
- [ ] Either<Failure, Domain> 반환 타입
- [ ] Vavr Validation을 통한 입력 검증
- [ ] 비즈니스 룰 검증 분리
- [ ] Repository를 통한 데이터 처리

### 응답 DTO 구현
- [ ] 도메인 객체 → DTO 변환 메서드
- [ ] 민감한 정보 제외
- [ ] 필드 선택 지원 (선택적)

### 테스트 구현
- [ ] Controller 테스트 (성공/실패 케이스)
- [ ] UseCase 테스트 (검증 로직 포함)
- [ ] 통합 테스트 (필요시)