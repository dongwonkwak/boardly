# Boardly 에러 코드 명세서

## 개요

Boardly API에서 사용하는 표준 에러 코드 체계를 정의합니다. 일관된 에러 처리를 통해 클라이언트에서 예측 가능한 에러 핸들링이 가능하도록 합니다.

## 에러 코드 명명 규칙

### 구조
```
{DOMAIN}_{ERROR_TYPE}
```

### 명명 규칙
- **대문자 사용**: 모든 에러 코드는 대문자로 작성
- **언더스코어 구분**: 단어 간 언더스코어 사용
- **도메인 접두사**: 관련 도메인을 접두사로 사용
- **구체적 명명**: 에러 상황을 명확히 표현

### 에러 타입 분류
- `NOT_FOUND`: 리소스 미발견
- `ACCESS_DENIED`: 접근 권한 없음
- `DUPLICATE`: 중복 데이터
- `INVALID`: 잘못된 데이터/상태
- `EXCEEDED`: 제한 초과
- `ARCHIVED`: 아카이브된 리소스
- `EXPIRED`: 만료된 리소스

---

## 표준 에러 코드

### HTTP 상태 코드 매핑

| 에러 코드 | 설명 | HTTP 상태 |
|----------|------|-----------|
| `VALIDATION_ERROR` | 입력 검증 실패 | 400 |
| `UNAUTHORIZED` | 인증 실패 | 401 |
| `PERMISSION_DENIED` | 권한 부족 | 403 |
| `NOT_FOUND` | 리소스 미발견 | 404 |
| `RESOURCE_CONFLICT` | 리소스 충돌 | 409 |
| `PRECONDITION_FAILED` | 전제 조건 실패 | 412 |
| `BUSINESS_RULE_VIOLATION` | 비즈니스 룰 위반 | 422 |
| `RATE_LIMIT_EXCEEDED` | 요청 한도 초과 | 429 |
| `INTERNAL_ERROR` | 내부 서버 오류 | 500 |

---

## 도메인별 에러 코드

### 사용자 (User) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `USER_NOT_FOUND` | 사용자 미발견 | 404 | 존재하지 않는 사용자 ID로 조회 시 |
| `USER_EMAIL_DUPLICATE` | 이메일 중복 | 409 | 이미 존재하는 이메일로 회원가입 시 |
| `USER_INACTIVE` | 비활성 사용자 | 403 | 비활성화된 사용자 계정 접근 시 |
| `USER_NOT_WORKSPACE_MEMBER` | 워크스페이스 멤버 아님 | 403 | 워크스페이스 멤버가 아닌 사용자가 접근 시 |

### 워크스페이스 (Workspace) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `WORKSPACE_NOT_FOUND` | 워크스페이스 미발견 | 404 | 존재하지 않는 워크스페이스 ID로 조회 시 |
| `WORKSPACE_ACCESS_DENIED` | 워크스페이스 접근 권한 없음 | 403 | 멤버가 아닌 사용자가 워크스페이스 접근 시 |
| `WORKSPACE_NAME_DUPLICATE` | 워크스페이스 이름 중복 | 409 | 같은 소유자가 동일 이름의 워크스페이스 생성 시 |
| `WORKSPACE_ARCHIVED` | 아카이브된 워크스페이스 | 410 | 아카이브된 워크스페이스 접근 시 |
| `WORKSPACE_LIMIT_EXCEEDED` | 워크스페이스 개수 제한 초과 | 422 | 사용자당 최대 5개 제한 초과 시 |

### 보드 (Board) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `BOARD_NOT_FOUND` | 보드 미발견 | 404 | 존재하지 않는 보드 ID로 조회 시 |
| `BOARD_ACCESS_DENIED` | 보드 접근 권한 없음 | 403 | 보드 멤버가 아닌 사용자가 접근 시 |
| `BOARD_ARCHIVED` | 아카이브된 보드 | 410 | 아카이브된 보드 접근 시 |
| `BOARD_LIMIT_EXCEEDED` | 보드 개수 제한 초과 | 422 | 워크스페이스당 최대 20개 제한 초과 시 |
| `BOARD_NAME_DUPLICATE` | 보드 이름 중복 | 409 | 같은 워크스페이스 내 동일 이름 보드 생성 시 |

### 리스트/컬럼 (List) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `LIST_NOT_FOUND` | 리스트 미발견 | 404 | 존재하지 않는 리스트 ID로 조회 시 |
| `LIST_ACCESS_DENIED` | 리스트 접근 권한 없음 | 403 | 보드 접근 권한이 없는 리스트 조회 시 |
| `LIST_ARCHIVED` | 아카이브된 리스트 | 410 | 아카이브된 리스트 접근 시 |
| `LIST_POSITION_INVALID` | 잘못된 리스트 위치 | 422 | 유효하지 않은 position 값 설정 시 |
| `LIST_NAME_DUPLICATE` | 리스트 이름 중복 | 409 | 같은 보드 내 동일 이름 리스트 생성 시 |
| `LIST_LIMIT_EXCEEDED` | 리스트 개수 제한 초과 | 422 | 보드당 최대 리스트 개수 초과 시 |

### 카드 (Card) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `CARD_NOT_FOUND` | 카드 미발견 | 404 | 존재하지 않는 카드 ID로 조회 시 |
| `CARD_ACCESS_DENIED` | 카드 접근 권한 없음 | 403 | 보드 접근 권한이 없는 카드 조회 시 |
| `CARD_POSITION_INVALID` | 잘못된 카드 위치 | 422 | 유효하지 않은 position 값 설정 시 |
| `CARD_STATUS_INVALID` | 잘못된 카드 상태 | 422 | 허용되지 않는 상태로 변경 시 |
| `CARD_LIMIT_EXCEEDED` | 카드 개수 제한 초과 | 422 | 리스트당 최대 카드 개수 초과 시 |
| `CARD_ARCHIVED` | 아카이브된 카드 | 410 | 아카이브된 카드 접근 시 |

### 라벨 (Label) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `LABEL_NOT_FOUND` | 라벨 미발견 | 404 | 존재하지 않는 라벨 ID로 조회 시 |
| `LABEL_ACCESS_DENIED` | 라벨 접근 권한 없음 | 403 | 보드 접근 권한이 없는 라벨 조회 시 |
| `LABEL_NAME_DUPLICATE` | 라벨 이름 중복 | 409 | 같은 보드 내 동일 이름 라벨 생성 시 |
| `LABEL_COLOR_INVALID` | 잘못된 색상 코드 | 422 | 유효하지 않은 hex 색상 코드 입력 시 |
| `LABEL_LIMIT_EXCEEDED` | 라벨 개수 제한 초과 | 422 | 보드당 최대 라벨 개수 초과 시 |

### 댓글 (Comment) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `COMMENT_NOT_FOUND` | 댓글 미발견 | 404 | 존재하지 않는 댓글 ID로 조회 시 |
| `COMMENT_ACCESS_DENIED` | 댓글 접근 권한 없음 | 403 | 카드 접근 권한이 없는 댓글 조회 시 |
| `COMMENT_DELETED` | 삭제된 댓글 | 410 | 삭제된 댓글 접근 시 |
| `COMMENT_EMPTY_CONTENT` | 빈 댓글 내용 | 422 | 내용이 없는 댓글 작성 시 |
| `COMMENT_TOO_LONG` | 댓글 길이 초과 | 422 | 최대 1000자 초과 시 |

### 첨부파일 (Attachment) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `ATTACHMENT_NOT_FOUND` | 첨부파일 미발견 | 404 | 존재하지 않는 첨부파일 ID로 조회 시 |
| `ATTACHMENT_ACCESS_DENIED` | 첨부파일 접근 권한 없음 | 403 | 카드 접근 권한이 없는 첨부파일 조회 시 |
| `ATTACHMENT_SIZE_EXCEEDED` | 파일 크기 제한 초과 | 422 | 최대 50MB 초과 시 |
| `ATTACHMENT_TYPE_NOT_ALLOWED` | 허용되지 않은 파일 형식 | 422 | 금지된 파일 확장자 업로드 시 |
| `ATTACHMENT_UPLOAD_FAILED` | 파일 업로드 실패 | 500 | 저장소 업로드 오류 시 |
| `ATTACHMENT_LIMIT_EXCEEDED` | 첨부파일 개수 제한 초과 | 422 | 카드당 최대 첨부파일 개수 초과 시 |

### 활동 로그 (Activity Log) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `ACTIVITY_LOG_NOT_FOUND` | 활동 로그 미발견 | 404 | 존재하지 않는 로그 ID로 조회 시 |
| `ACTIVITY_LOG_ACCESS_DENIED` | 활동 로그 접근 권한 없음 | 403 | 보드 접근 권한이 없는 로그 조회 시 |
| `ACTIVITY_LOG_RETENTION_EXCEEDED` | 보관 기간 초과 | 410 | 보관 기간(90일) 초과 로그 조회 시 |

### 초대 (Invite) 관련

| 에러 코드 | 설명 | HTTP 상태 | 발생 상황 |
|----------|------|-----------|----------|
| `INVITE_NOT_FOUND` | 초대 미발견 | 404 | 존재하지 않는 초대 ID/토큰으로 조회 시 |
| `INVITE_EXPIRED` | 초대 만료 | 410 | 만료된 초대 링크 접근 시 |
| `INVITE_ALREADY_ACCEPTED` | 이미 수락된 초대 | 409 | 이미 수락된 초대 재수락 시도 시 |
| `INVITE_ALREADY_DECLINED` | 이미 거절된 초대 | 409 | 이미 거절된 초대 수락 시도 시 |
| `INVITE_INVALID_TOKEN` | 잘못된 초대 토큰 | 400 | 유효하지 않은 초대 토큰 사용 시 |
| `INVITE_TARGET_ALREADY_MEMBER` | 이미 멤버인 사용자 초대 | 409 | 이미 멤버인 사용자 초대 시 |
| `INVITE_SELF_INVITE_NOT_ALLOWED` | 자신을 초대하는 것 불허 | 422 | 자기 자신을 초대 시도 시 |
| `INVITE_PERMISSION_DENIED` | 초대 권한 없음 | 403 | 초대 권한이 없는 사용자가 초대 시도 시 |

---

## 에러 응답 구조

### 표준 에러 응답

```json
{
  "error": {
    "code": "WORKSPACE_NOT_FOUND",
    "message": "워크스페이스를 찾을 수 없습니다.",
    "context": {
      "workspaceId": "wsp_01ARZ3NDEKTSV4RRFFQ69G5FAV"
    }
  },
  "meta": {
    "timestamp": "2025-01-01T12:34:56Z",
    "path": "/api/v1/workspaces/wsp_01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "apiVersion": "v1",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

### 입력 검증 실패 응답

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
    "timestamp": "2025-01-01T12:34:56Z",
    "path": "/api/v1/workspaces",
    "apiVersion": "v1",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

---

## 에러 코드 사용 가이드

### 백엔드 구현

**Failure 클래스 예시**
```java
public abstract class Failure {
    public static Failure ofNotFound(String code, String message) {
        return new NotFoundFailure(code, message);
    }
    
    public static Failure ofValidation(String code, String message, Map<String, Object> context) {
        return new ValidationFailure(code, message, context);
    }
    
    public static Failure ofBusinessRule(String code, String message) {
        return new BusinessRuleFailure(code, message);
    }
}
```

**UseCase에서 사용 예시**
```java
public Either<Failure, Workspace> getWorkspace(WorkspaceId id) {
    Optional<Workspace> workspace = workspaceRepository.findById(id);
    
    if (workspace.isEmpty()) {
        return Either.left(Failure.ofNotFound(
            "WORKSPACE_NOT_FOUND", 
            "워크스페이스를 찾을 수 없습니다."
        ));
    }
    
    return Either.right(workspace.get());
}
```

### 프론트엔드 활용

**에러 핸들링 예시**
```typescript
const handleApiError = (error: ApiError) => {
  switch (error.code) {
    case 'WORKSPACE_NOT_FOUND':
      toast.error('워크스페이스를 찾을 수 없습니다.');
      navigate('/workspaces');
      break;
      
    case 'WORKSPACE_ACCESS_DENIED':
      toast.error('워크스페이스에 접근할 권한이 없습니다.');
      break;
      
    case 'VALIDATION_ERROR':
      showValidationErrors(error.details);
      break;
      
    default:
      toast.error('알 수 없는 오류가 발생했습니다.');
  }
};
```

---

## 확장 가이드

### 새로운 에러 코드 추가

1. **명명 규칙 준수**: `{DOMAIN}_{ERROR_TYPE}` 형식
2. **HTTP 상태 코드 매핑**: 적절한 4xx/5xx 코드 선택
3. **문서 업데이트**: 이 명세서에 추가
4. **다국어 메시지**: 한국어/영어 메시지 정의
5. **테스트 케이스**: 에러 상황 테스트 추가

### 에러 메시지 국제화

```
# messages/ErrorMessages.properties
WORKSPACE_NOT_FOUND=워크스페이스를 찾을 수 없습니다.
WORKSPACE_ACCESS_DENIED=워크스페이스에 접근할 권한이 없습니다.

# messages/ErrorMessages_en.properties  
WORKSPACE_NOT_FOUND=Workspace not found.
WORKSPACE_ACCESS_DENIED=Access denied to workspace.
```

---

**문서 버전**: 1.0  
**최종 수정일**: 2024-12-19  
**작성자**: Boardly 개발팀
