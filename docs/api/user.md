# User API 문서

## 개요

사용자 정보의 조회, 수정 및 등록을 관리하는 API입니다. 사용자 프로필 관리 기능을 제공합니다.

## 엔드포인트

### PUT /api/users

사용자를 업데이트합니다.

#### 요청 데이터

```json
{
  "firstName": "개발",
  "lastName": "김"
}
```

#### 응답

**HTTP Status**: 200 OK  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "userId": "user_123",
  "email": "dev@example.com",
  "firstName": "개발",
  "lastName": "김",
  "isActive": true
}
```

### POST /api/users/register

사용자를 등록합니다.

#### 요청 데이터

```json
{
  "email": "dev@example.com",
  "password": "securePassword123",
  "firstName": "개발",
  "lastName": "김"
}
```

#### 응답

**HTTP Status**: 201 Created  
**Content-Type**: application/json

#### 응답 데이터 구조

```json
{
  "userId": "user_123",
  "email": "dev@example.com",
  "firstName": "개발",
  "lastName": "김",
  "isActive": true
}
```

## 데이터 모델

### User 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `userId` | string | Y | 사용자 고유 식별자 |
| `email` | string | Y | 사용자 이메일 |
| `firstName` | string | Y | 사용자 이름 |
| `lastName` | string | Y | 사용자 성 |
| `isActive` | boolean | Y | 계정 활성화 상태 |

### UpdateUserRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `firstName` | string | N | 사용자 이름 |
| `lastName` | string | N | 사용자 성 |

### RegisterUserRequest 객체

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `email` | string | Y | 사용자 이메일 |
| `password` | string | Y | 사용자 비밀번호 |
| `firstName` | string | Y | 사용자 이름 |
| `lastName` | string | Y | 사용자 성 |

## 에러 응답

### 400 Bad Request
```json
{
  "code": "BAD_REQUEST",
  "message": "입력 값이 유효하지 않습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/users",
  "details": [
    {
      "field": "firstName",
      "message": "이름은 필수입니다.",
      "rejectedValue": ""
    }
  ]
}
```

### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/users"
}
```

### 409 Conflict
```json
{
  "code": "CONFLICT",
  "message": "이미 사용 중인 이메일입니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/users/register"
}
```

### 500 Internal Server Error
```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "서버 오류가 발생했습니다.",
  "timestamp": "2025-01-20T15:30:00Z",
  "path": "/api/users"
}
```

## 구현 참고사항

### 프론트엔드 처리

1. **폼 검증**: 이메일 형식, 비밀번호 강도 검증
2. **다국어 처리**: 이름과 성을 언어별로 조합 (한국어: 성+이름, 영어: 이름+성)
3. **실시간 검증**: 이메일 중복 확인 실시간 검증

### 백엔드 구현

1. **인증**: JWT Bearer Token을 통한 사용자 인증
2. **트랜잭션**: 사용자 등록 시 원자적 처리

### 보안 고려사항

- **비밀번호 정책**: 최소 8자, 영문/숫자/특수문자 조합
- **이메일 인증**: 회원가입 후 이메일 인증 절차
- **세션 관리**: JWT 토큰 만료 시간 설정
- **데이터 암호화**: 민감한 사용자 정보 암호화 저장

## 예시 cURL 요청

### 사용자 정보 수정
```bash
curl -X PUT "https://api.boardly.com/api/users" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "개발",
    "lastName": "김"
  }'
```

### 사용자 등록
```bash
curl -X POST "https://api.boardly.com/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "dev@example.com",
    "password": "securePassword123",
    "firstName": "개발",
    "lastName": "김"
  }'
``` 