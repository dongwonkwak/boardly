# API 응답 형식 표준

## 개요

Boardly API는 일관된 응답 형식을 제공하여 클라이언트 개발의 편의성을 높이고 오류 처리를 표준화합니다. 이 문서는 성공 응답과 오류 응답의 형식을 정의합니다.

## 기본 응답 구조

### 성공 응답

모든 성공 응답은 다음과 같은 기본 구조를 따릅니다:

```json
{
  "data": {
    // 실제 응답 데이터
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "version": "1.0.0"
  }
}
```

### 오류 응답

모든 오류 응답은 다음과 같은 기본 구조를 따릅니다:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "사용자 친화적 오류 메시지",
    "details": [
      {
        "field": "fieldName",
        "message": "필드별 오류 메시지",
        "rejectedValue": "거부된 값"
      }
    ],
    "context": {
      // 추가 컨텍스트 정보
    }
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "path": "/api/boards",
    "version": "1.0.0"
  }
}
```

## HTTP 상태 코드

### 성공 응답 (2xx)

| 상태 코드 | 설명 | 사용 예시 |
|-----------|------|-----------|
| 200 | OK | 리소스 조회 성공 |
| 201 | Created | 리소스 생성 성공 |
| 204 | No Content | 삭제 성공 (응답 본문 없음) |

### 클라이언트 오류 (4xx)

| 상태 코드 | 설명 | 사용 예시 |
|-----------|------|-----------|
| 400 | Bad Request | 입력 검증 실패 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 부족 |
| 404 | Not Found | 리소스 미발견 |
| 409 | Conflict | 리소스 충돌 |
| 412 | Precondition Failed | 전제 조건 실패 |
| 422 | Unprocessable Entity | 비즈니스 룰 위반 |

### 서버 오류 (5xx)

| 상태 코드 | 설명 | 사용 예시 |
|-----------|------|-----------|
| 500 | Internal Server Error | 내부 서버 오류 |

## 응답 예시

### 1. 보드 생성 성공 (201 Created)

```json
{
  "data": {
    "id": "01HQ8K2N3M4P5Q6R7S8T9U0V1",
    "name": "프로젝트 관리",
    "description": "프로젝트 진행 상황을 관리하는 보드",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z",
    "owner": {
      "id": "01HQ8K2N3M4P5Q6R7S8T9U0V2",
      "name": "홍길동",
      "email": "hong@example.com"
    }
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "version": "1.0.0"
  }
}
```

### 2. 보드 목록 조회 성공 (200 OK)

```json
{
  "data": {
    "boards": [
      {
        "id": "01HQ8K2N3M4P5Q6R7S8T9U0V1",
        "name": "프로젝트 관리",
        "description": "프로젝트 진행 상황을 관리하는 보드",
        "createdAt": "2024-01-15T10:30:00Z",
        "cardCount": 5
      },
      {
        "id": "01HQ8K2N3M4P5Q6R7S8T9U0V3",
        "name": "개발 작업",
        "description": "개발 작업을 관리하는 보드",
        "createdAt": "2024-01-15T11:00:00Z",
        "cardCount": 3
      }
    ],
    "pagination": {
      "page": 1,
      "size": 10,
      "totalElements": 2,
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false
    }
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "version": "1.0.0"
  }
}
```

### 3. 입력 검증 실패 (400 Bad Request)

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력 데이터가 유효하지 않습니다.",
    "details": [
      {
        "field": "name",
        "message": "보드 이름은 1자 이상 100자 이하여야 합니다.",
        "rejectedValue": ""
      },
      {
        "field": "description",
        "message": "보드 설명은 500자 이하여야 합니다.",
        "rejectedValue": "매우 긴 설명..."
      }
    ]
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "path": "/api/boards",
    "version": "1.0.0"
  }
}
```

### 4. 리소스 미발견 (404 Not Found)

```json
{
  "error": {
    "code": "BOARD_NOT_FOUND",
    "message": "요청한 보드를 찾을 수 없습니다.",
    "context": {
      "boardId": "01HQ8K2N3M4P5Q6R7S8T9U0V1"
    }
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "path": "/api/boards/01HQ8K2N3M4P5Q6R7S8T9U0V1",
    "version": "1.0.0"
  }
}
```

### 5. 권한 거부 (403 Forbidden)

```json
{
  "error": {
    "code": "PERMISSION_DENIED",
    "message": "이 보드에 대한 수정 권한이 없습니다.",
    "context": {
      "boardId": "01HQ8K2N3M4P5Q6R7S8T9U0V1",
      "requiredPermission": "WRITE",
      "userPermission": "READ"
    }
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "path": "/api/boards/01HQ8K2N3M4P5Q6R7S8T9U0V1",
    "version": "1.0.0"
  }
}
```

### 6. 비즈니스 룰 위반 (422 Unprocessable Entity)

```json
{
  "error": {
    "code": "BOARD_ARCHIVED",
    "message": "아카이브된 보드는 수정할 수 없습니다.",
    "context": {
      "boardId": "01HQ8K2N3M4P5Q6R7S8T9U0V1",
      "boardStatus": "ARCHIVED"
    }
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "path": "/api/boards/01HQ8K2N3M4P5Q6R7S8T9U0V1",
    "version": "1.0.0"
  }
}
```

## 페이지네이션

페이지네이션이 있는 응답은 다음과 같은 구조를 따릅니다:

```json
{
  "data": {
    "items": [
      // 실제 데이터 항목들
    ],
    "pagination": {
      "page": 1,
      "size": 10,
      "totalElements": 100,
      "totalPages": 10,
      "hasNext": true,
      "hasPrevious": false,
      "first": true,
      "last": false
    }
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "version": "1.0.0"
  }
}
```

## 구현 가이드

### 1. 성공 응답 DTO

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
        String path;
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
```

### 2. 오류 응답 DTO

```java
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

### 3. Controller에서 사용

```java
@RestController
@RequestMapping("/api/boards")
public class BoardController {
    
    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(
            @RequestBody CreateBoardRequest request,
            HttpServletRequest httpRequest) {
        
        CreateBoardCommand command = CreateBoardCommand.of(request.getName());
        Board board = createBoardUseCase.execute(command);
        
        ApiResponse<BoardResponse> response = ApiResponse.success(
                BoardResponse.from(board)
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, 
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorResponse.ErrorInfo.builder()
                        .code("VALIDATION_ERROR")
                        .message("입력 데이터가 유효하지 않습니다.")
                        .details(ex.getViolations())
                        .build())
                .meta(ErrorResponse.MetaInfo.builder()
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .version("1.0.0")
                        .build())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}
```

## 오류 코드 표준

### 일반 오류 코드

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

### 도메인별 오류 코드

#### 보드 관련
- `BOARD_NOT_FOUND`
- `BOARD_ARCHIVED`
- `BOARD_NAME_DUPLICATE`
- `BOARD_ACCESS_DENIED`

#### 카드 관련
- `CARD_NOT_FOUND`
- `CARD_LIMIT_EXCEEDED`
- `CARD_STATUS_INVALID`

#### 사용자 관련
- `USER_NOT_FOUND`
- `USER_EMAIL_DUPLICATE`
- `USER_INACTIVE`

## 헤더 표준

### 응답 헤더

| 헤더 | 설명 | 예시 |
|------|------|------|
| `Content-Type` | 응답 콘텐츠 타입 | `application/json` |
| `X-Request-ID` | 요청 추적 ID | `req-12345678-1234-1234-1234-123456789abc` |
| `X-Rate-Limit-Remaining` | 남은 요청 수 | `99` |
| `X-Rate-Limit-Reset` | 요청 제한 리셋 시간 | `1642233600` |

### 요청 헤더

| 헤더 | 설명 | 예시 |
|------|------|------|
| `Authorization` | 인증 토큰 | `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` |
| `Content-Type` | 요청 콘텐츠 타입 | `application/json` |
| `Accept` | 응답 형식 선호도 | `application/json` |
| `If-Match` | 조건부 요청 | `"abc123"` |

## 버전 관리

API 버전은 URL 경로에 포함됩니다:

```
/api/v1/boards
/api/v2/boards
```

또는 헤더를 통한 버전 관리:

```
Accept: application/vnd.boardly.v1+json
```

## 결론

일관된 API 응답 형식을 사용하면 클라이언트 개발의 편의성을 높이고, 오류 처리를 표준화할 수 있습니다. 이 문서의 가이드를 따라 모든 API 엔드포인트에서 동일한 응답 구조를 유지하세요.
