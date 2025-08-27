# Boardly - API 설계 원칙

## RESTful API 설계 원칙

### 1. URL 설계 규칙

**리소스 중심의 URL 구조**
```
# 올바른 예시
GET    /api/workspaces                    # 워크스페이스 목록
POST   /api/workspaces                    # 워크스페이스 생성
GET    /api/workspaces/{id}               # 특정 워크스페이스 조회
PUT    /api/workspaces/{id}               # 워크스페이스 수정
DELETE /api/workspaces/{id}               # 워크스페이스 삭제

# 중첩 리소스
GET    /api/workspaces/{id}/boards        # 워크스페이스의 보드 목록
POST   /api/workspaces/{id}/boards        # 워크스페이스에 보드 생성
GET    /api/boards/{id}/lists             # 보드의 리스트 목록
POST   /api/lists/{id}/cards              # 리스트에 카드 생성

# 잘못된 예시 (동사 사용)
POST   /api/workspaces/create             # ❌
POST   /api/cards/move                    # ❌
GET    /api/users/getProfile              # ❌
```

**URL 명명 규칙**
- 명사 사용 (동사 금지)
- 복수형 사용 (`/users`, `/workspaces`)
- 소문자 + 하이픈 (`/workspace-members`)
- 계층 구조 반영

### 2. HTTP 메서드 활용

| 메서드 | 목적 | 멱등성 | 캐시 가능 |
|--------|------|--------|-----------|
| GET | 조회 | ✅ | ✅ |
| POST | 생성 | ❌ | ❌ |
| PUT | 전체 수정 | ✅ | ❌ |
| PATCH | 부분 수정 | ❌ | ❌ |
| DELETE | 삭제 | ✅ | ❌ |

**특수 동작 처리**
```
# 카드 이동 (PATCH 사용)
PATCH  /api/cards/{id}/position
Body: { listId: "list-2", position: 3 }

# 멤버 초대 (POST 사용)  
POST   /api/workspaces/{id}/invitations
Body: { email: "user@example.com", role: "member" }

# 복합 검색 (GET with Query Parameters)
GET    /api/search?q=keyword&type=card&workspace=123
```

### 3. 응답 구조 표준화

**성공 응답**
```json
{
  "data": {
    "id": "01HQ8K2N3M4P5Q6R7S8T9U0V1",
    "name": "개발팀",
    "description": "개발팀 워크스페이스",
    "createdAt": "2025-01-01T00:00:00Z",
    "owner": {
      "id": "01HQ8K2N3M4P5Q6R7S8T9U0V2",
      "name": "홍길동",
      "email": "hong@example.com"
    }
  },
  "meta": {
    "timestamp": "2025-01-01T00:00:00Z",
    "version": "1.0.0"
  }
}
```

**목록 응답 (페이징)**
```json
{
  "data": {
    "workspaces": [
      { "id": "01HQ8K2N3M4P5Q6R7S8T9U0V1", "name": "워크스페이스1" },
      { "id": "01HQ8K2N3M4P5Q6R7S8T9U0V3", "name": "워크스페이스2" }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 45,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false,
      "first": true,
      "last": false
    }
  },
  "meta": {
    "timestamp": "2025-01-01T00:00:00Z",
    "version": "1.0.0"
  }
}
```

**에러 응답**
```json
{
  "error": {
    "code": "WORKSPACE_NOT_FOUND",
    "message": "워크스페이스를 찾을 수 없습니다.",
    "context": {
      "workspaceId": "01HQ8K2N3M4P5Q6R7S8T9U0V1"
    }
  },
  "meta": {
    "timestamp": "2025-01-01T00:00:00Z",
    "path": "/api/workspaces/01HQ8K2N3M4P5Q6R7S8T9U0V1",
    "version": "1.0.0"
  }
}
```

**입력 검증 실패 응답**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력 데이터가 유효하지 않습니다.",
    "details": [
      {
        "field": "name",
        "message": "워크스페이스 이름은 1자 이상 100자 이하여야 합니다.",
        "rejectedValue": ""
      },
      {
        "field": "description", 
        "message": "워크스페이스 설명은 500자 이하여야 합니다.",
        "rejectedValue": "매우 긴 설명..."
      }
    ]
  },
  "meta": {
    "timestamp": "2025-01-01T00:00:00Z",
    "path": "/api/workspaces",
    "version": "1.0.0"
  }
}
```

### 4. HTTP 상태 코드 가이드

**성공 (2xx)**
- `200 OK`: 성공적인 GET, PUT, PATCH
- `201 Created`: 성공적인 POST (리소스 생성)
- `204 No Content`: 성공적인 DELETE

**클라이언트 에러 (4xx)**
- `400 Bad Request`: 입력 검증 실패
- `401 Unauthorized`: 인증 실패  
- `403 Forbidden`: 권한 부족
- `404 Not Found`: 리소스 없음
- `409 Conflict`: 리소스 충돌 (예: 중복 이메일)
- `412 Precondition Failed`: 전제 조건 실패 (예: ETag 불일치)
- `422 Unprocessable Entity`: 비즈니스 룰 위반

**서버 에러 (5xx)**
- `500 Internal Server Error`: 서버 내부 오류

### 5. API 버전 관리

**URL Path 버전 관리 (채택)**
```
/api/v1/workspaces
/api/v2/workspaces
```

**Header 버전 관리 (고려사항)**
```
Accept: application/vnd.boardly.v1+json
```

## 인증 및 보안

### JWT 토큰 기반 인증
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### CORS 설정
```yaml
# application.yml
cors:
  allowed-origins: 
    - http://localhost:3000
    - https://boardly.example.com
  allowed-methods:
    - GET
    - POST
    - PUT
    - PATCH
    - DELETE
    - OPTIONS
  allowed-headers:
    - Authorization
    - Content-Type
    - X-Request-ID
  exposed-headers:
    - X-Request-ID
    - X-Rate-Limit-Remaining
    - X-Rate-Limit-Reset
```

### Rate Limiting
```
# 응답 헤더 예시
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1609459200
X-Request-ID: req-12345678-1234-1234-1234-123456789abc
```

## 데이터 검증

### Vavr Validation을 통한 요청 데이터 검증

**Validation 클래스 예시**
```java
public class CreateWorkspaceRequestValidator {
    
    public static Validation<Seq<String>, CreateWorkspaceCommand> validate(
            CreateWorkspaceRequest request) {
        
        return Validation
            .combine(
                validateName(request.getName()),
                validateDescription(request.getDescription())
            )
            .ap(CreateWorkspaceCommand::new);
    }
    
    private static Validation<String, String> validateName(String name) {
        return name == null || name.trim().isEmpty() 
            ? Validation.invalid("워크스페이스 이름은 필수입니다")
            : name.length() > 100
                ? Validation.invalid("이름은 100자 이하여야 합니다") 
                : Validation.valid(name.trim());
    }
    
    private static Validation<String, String> validateDescription(String description) {
        if (description == null) {
            return Validation.valid(null);
        }
        return description.length() > 500
            ? Validation.invalid("설명은 500자 이하여야 합니다")
            : Validation.valid(description);
    }
}
```

**Controller에서 Validation 사용**
```java
@PostMapping
public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
        @RequestBody CreateWorkspaceRequest request) {
    
    return CreateWorkspaceRequestValidator.validate(request)
        .fold(
            errors -> {
                List<FieldViolation> violations = errors.map(error -> 
                    FieldViolation.builder()
                        .field("name") // 실제로는 필드명 매핑 로직 필요
                        .message(error)
                        .rejectedValue(request.getName())
                        .build()
                ).toJavaList();
                
                throw new ValidationException(violations);
            },
            command -> {
                Workspace workspace = workspaceService.createWorkspace(command);
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(WorkspaceResponse.from(workspace)));
            }
        );
}
```

**도메인 객체 검증**
```java
public class Workspace {
    
    public static Validation<Seq<String>, Workspace> create(
            WorkspaceId id, 
            String name, 
            String description, 
            UserId ownerId) {
        
        return Validation
            .combine(
                validateId(id),
                validateName(name),
                validateDescription(description),
                validateOwnerId(ownerId)
            )
            .ap(Workspace::new);
    }
    
    private static Validation<String, WorkspaceId> validateId(WorkspaceId id) {
        return id == null 
            ? Validation.invalid("워크스페이스 ID는 필수입니다")
            : Validation.valid(id);
    }
    
    private static Validation<String, String> validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Validation.invalid("워크스페이스 이름은 필수입니다");
        }
        if (name.length() > 100) {
            return Validation.invalid("워크스페이스 이름은 100자 이하여야 합니다");
        }
        return Validation.valid(name.trim());
    }
}
```

### 응답 데이터 검증
```java
// 민감한 정보 제외
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private String profileImageUrl;
    // password, refreshToken 필드 제외
    
    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId().getValue())
            .email(user.getEmail())
            .name(user.getName())
            .profileImageUrl(user.getProfileImageUrl())
            .build();
    }
}
```

## API 문서화

### OpenAPI 3.0 스펙
```yaml
# 자동 생성되는 OpenAPI 스펙 예시
/api/workspaces:
  get:
    summary: 워크스페이스 목록 조회
    tags: [Workspace]
    security:
      - bearerAuth: []
    parameters:
      - name: page
        in: query
        schema:
          type: integer
          default: 1
    responses:
      200:
        description: 성공
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/WorkspaceListResponse'
```

### 주석 기반 문서화
```java
@RestController
@RequestMapping("/api/workspaces")
@Tag(name = "Workspace", description = "워크스페이스 관리 API")
public class WorkspaceController {
    
    @Operation(summary = "워크스페이스 생성", description = "새로운 워크스페이스를 생성합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request) {
        // 구현
    }
}
```

## 성능 최적화

### 페이징
```
GET /api/workspaces?page=1&size=20&sort=name,asc
```

### 필드 선택 (Sparse Fieldsets)
```
GET /api/workspaces?fields=id,name,createdAt
```

### 관계 데이터 로딩
```
GET /api/workspaces/{id}?include=boards,members
```

### 조건부 요청 (ETag)
```
# 응답 헤더
ETag: "33a64df551425fcc55e4d42a148795d9f25f89d4"

# 클라이언트 요청
If-None-Match: "33a64df551425fcc55e4d42a148795d9f25f89d4"

# 304 Not Modified 응답
```

## 에러 처리 전략

### 표준 에러 코드 체계

**일반 에러 코드**
| 코드 | 설명 | HTTP 상태 코드 |
|------|------|----------------|
| `VALIDATION_ERROR` | 입력 검증 실패 | 400 |
| `UNAUTHORIZED` | 인증 실패 | 401 |
| `PERMISSION_DENIED` | 권한 부족 | 403 |
| `NOT_FOUND` | 리소스 미발견 | 404 |
| `RESOURCE_CONFLICT` | 리소스 충돌 | 409 |
| `PRECONDITION_FAILED` | 전제 조건 실패 | 412 |
| `BUSINESS_RULE_VIOLATION` | 비즈니스 룰 위반 | 422 |
| `INTERNAL_ERROR` | 내부 서버 오류 | 500 |

**도메인별 에러 코드**
```
# 워크스페이스 관련
WORKSPACE_NOT_FOUND
WORKSPACE_ACCESS_DENIED  
WORKSPACE_NAME_DUPLICATE
WORKSPACE_ARCHIVED

# 보드 관련
BOARD_NOT_FOUND
BOARD_ACCESS_DENIED
BOARD_ARCHIVED
BOARD_LIMIT_EXCEEDED

# 카드 관련
CARD_NOT_FOUND
CARD_POSITION_INVALID
CARD_STATUS_INVALID
CARD_LIMIT_EXCEEDED

# 사용자 관련
USER_NOT_FOUND
USER_EMAIL_DUPLICATE
USER_INACTIVE
USER_NOT_WORKSPACE_MEMBER
```

### 전역 에러 핸들러
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        ErrorResponse response = ErrorResponse.builder()
            .error(ErrorInfo.builder()
                .code("VALIDATION_ERROR")
                .message("입력 데이터가 유효하지 않습니다.")
                .details(ex.getViolations())
                .build())
            .meta(MetaInfo.builder()
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .version("1.0.0")
                .build())
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkspaceNotFound(
            WorkspaceNotFoundException ex, HttpServletRequest request) {
        
        ErrorResponse response = ErrorResponse.builder()
            .error(ErrorInfo.builder()
                .code("WORKSPACE_NOT_FOUND")
                .message("워크스페이스를 찾을 수 없습니다.")
                .context(Map.of("workspaceId", ex.getWorkspaceId()))
                .build())
            .meta(MetaInfo.builder()
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .version("1.0.0")
                .build())
            .build();
            
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            BusinessRuleViolationException ex, HttpServletRequest request) {
        
        ErrorResponse response = ErrorResponse.builder()
            .error(ErrorInfo.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .context(ex.getContext())
                .build())
            .meta(MetaInfo.builder()
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .version("1.0.0")
                .build())
            .build();
            
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }
}
```

### 응답 DTO 구조
```java
@Value
@Builder
public class ApiResponse<T> {
    T data;
    MetaInfo meta;
    
    @Value
    @Builder
    public static class MetaInfo {
        Instant timestamp;
        String version;
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .data(data)
            .meta(MetaInfo.builder()
                .timestamp(Instant.now())
                .version("1.0.0")
                .build())
            .build();
    }
}

@Value
@Builder
public class ErrorResponse {
    ErrorInfo error;
    MetaInfo meta;
    
    @Value
    @Builder
    public static class ErrorInfo {
        String code;
        String message;
        List<FieldViolation> details;
        Object context;
    }
    
    @Value
    @Builder
    public static class FieldViolation {
        String field;
        String message;
        Object rejectedValue;
    }
    
    @Value
    @Builder
    public static class MetaInfo {
        Instant timestamp;
        String path;
        String version;
    }
}
```

## API 설계 체크리스트

### 설계 단계
- [ ] RESTful URL 구조 준수
- [ ] 적절한 HTTP 메서드 선택
- [ ] 일관된 응답 구조
- [ ] 적절한 HTTP 상태 코드
- [ ] 보안 고려사항 반영

### 구현 단계  
- [ ] 요청/응답 검증 구현
- [ ] 에러 처리 구현
- [ ] OpenAPI 문서 자동 생성
- [ ] 테스트 케이스 작성

### 배포 단계
- [ ] API 문서 배포
- [ ] 모니터링 설정
- [ ] 로깅 설정
- [ ] 성능 테스트