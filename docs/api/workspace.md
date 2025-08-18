# 워크스페이스 API 명세서

## 1. 워크스페이스 관리 API

### 2.1 워크스페이스 목록 조회

**GET** `/api/workspaces`

**설명**: 현재 사용자가 속한 모든 워크스페이스 목록을 조회합니다.

**Request Headers**:
```
Authorization: Bearer <jwt_token>
Accept-Language: ko-KR,en;q=0.9
```

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| `type` | string | N | 워크스페이스 타입 필터 (`PERSONAL`, `TEAM`) | 전체 |
| `page` | number | N | 페이지 번호 | 1 |
| `limit` | number | N | 페이지당 항목 수 | 20 |

**Response** (200 OK):
```json
{
  "workspaces": [
    {
      "id": "ws_12345",
      "name": "My Workspace",
      "description": null,
      "type": "PERSONAL",
      "role": "OWNER",
      "memberType": "MEMBER",
      "memberCount": 1,
      "boardCount": 5,
      "createdAt": "2025-08-01T09:00:00Z",
      "updatedAt": "2025-08-15T14:30:00Z"
    },
    {
      "id": "ws_67890",
      "name": "마케팅팀",
      "description": "마케팅 프로젝트 관리 워크스페이스",
      "type": "TEAM",
      "role": "MEMBER",
      "memberType": "MEMBER",
      "memberCount": 8,
      "boardCount": 12,
      "createdAt": "2025-07-15T10:00:00Z",
      "updatedAt": "2025-08-17T16:20:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 2,
    "hasNext": false,
    "hasPrev": false
  }
}
```

### 2.2 워크스페이스 상세 조회

**GET** `/api/workspaces/{workspaceId}`

**설명**: 특정 워크스페이스의 상세 정보를 조회합니다.

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `workspaceId` | string | Y | 워크스페이스 ID |

**Response** (200 OK):
```json
{
  "id": "ws_67890",
  "name": "마케팅팀",
  "description": "마케팅 프로젝트 관리 워크스페이스",
  "type": "TEAM",
  "owner": {
    "id": "user_123",
    "firstName": "길동",
    "lastName": "홍",
    "email": "hong@example.com"
  },
  "myRole": "MEMBER",
  "myMemberType": "MEMBER",
  "memberCount": 8,
  "boardCount": 12,
  "publicBoardCount": 10,
  "privateBoardCount": 2,
  "createdAt": "2025-07-15T10:00:00Z",
  "updatedAt": "2025-08-17T16:20:00Z"
}
```

**Error Responses**:
- `404 NOT_FOUND`: 워크스페이스를 찾을 수 없음
- `403 FORBIDDEN`: 워크스페이스에 접근 권한이 없음

### 2.3 워크스페이스 생성

**POST** `/api/workspaces`

**설명**: 새로운 워크스페이스를 생성합니다.

**Request Body**:
```json
{
  "name": "개발팀",
  "description": "개발 프로젝트 관리 워크스페이스",
  "type": "TEAM"
}
```

**Request Body Schema**:
| 필드 | 타입 | 필수 | 제약사항 | 설명 |
|------|------|------|----------|------|
| `name` | string | Y | 1-100자 | 워크스페이스 이름 |
| `description` | string | N | 최대 500자 | 워크스페이스 설명 |
| `type` | string | Y | `PERSONAL`, `TEAM` | 워크스페이스 타입 |

**Response** (201 CREATED):
```json
{
  "id": "ws_new123",
  "name": "개발팀",
  "description": "개발 프로젝트 관리 워크스페이스",
  "type": "TEAM",
  "role": "OWNER",
  "memberType": "MEMBER",
  "memberCount": 1,
  "boardCount": 0,
  "createdAt": "2025-08-18T10:30:00Z",
  "updatedAt": "2025-08-18T10:30:00Z"
}
```

**Error Responses**:
- `400 BAD_REQUEST`: 잘못된 요청 데이터
- `422 UNPROCESSABLE_ENTITY`: 워크스페이스 수 제한 초과 (10개)

### 2.4 워크스페이스 수정

**PUT** `/api/workspaces/{workspaceId}`

**설명**: 워크스페이스 정보를 수정합니다. (OWNER만 가능)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `workspaceId` | string | Y | 워크스페이스 ID |

**Request Body**:
```json
{
  "name": "마케팅팀 (2025)",
  "description": "2025년 마케팅 프로젝트 관리"
}
```

**Response** (200 OK):
```json
{
  "id": "ws_67890",
  "name": "마케팅팀 (2025)",
  "description": "2025년 마케팅 프로젝트 관리",
  "type": "TEAM",
  "role": "OWNER",
  "memberType": "MEMBER",
  "memberCount": 8,
  "boardCount": 12,
  "createdAt": "2025-07-15T10:00:00Z",
  "updatedAt": "2025-08-18T10:30:00Z"
}
```

**Error Responses**:
- `403 FORBIDDEN`: 워크스페이스 수정 권한이 없음
- `404 NOT_FOUND`: 워크스페이스를 찾을 수 없음

### 2.5 워크스페이스 삭제

**DELETE** `/api/workspaces/{workspaceId}`

**설명**: 워크스페이스를 삭제합니다. 모든 보드가 삭제되어야 가능합니다. (OWNER만 가능)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `workspaceId` | string | Y | 워크스페이스 ID |

**Request Body**:
```json
{
  "confirmationName": "마케팅팀"
}
```

**Response** (204 NO_CONTENT):
빈 응답

**Error Responses**:
- `403 FORBIDDEN`: 워크스페이스 삭제 권한이 없음
- `409 CONFLICT`: 워크스페이스에 보드가 남아있음
- `400 BAD_REQUEST`: 확인용 워크스페이스 이름이 일치하지 않음

## 2. 워크스페이스 멤버 관리 API

### 2.1 워크스페이스 멤버 목록 조회

**GET** `/api/workspaces/{workspaceId}/members`

**설명**: 워크스페이스의 멤버 목록을 조회합니다. (BOARD_ONLY 멤버는 제외)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `workspaceId` | string | Y | 워크스페이스 ID |

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| `role` | string | N | 역할 필터 (`OWNER`, `MEMBER`) | 전체 |
| `page` | number | N | 페이지 번호 | 1 |
| `limit` | number | N | 페이지당 항목 수 | 20 |

**Response** (200 OK):
```json
{
  "members": [
    {
      "id": "user_123",
      "firstName": "길동",
      "lastName": "홍",
      "email": "hong@example.com",
      "role": "OWNER",
      "memberType": "MEMBER",
      "joinedAt": "2025-07-15T10:00:00Z",
      "lastActiveAt": "2025-08-18T09:45:00Z",
      "isActive": true,
      "invitedBy": null
    },
    {
      "id": "user_456",
      "firstName": "영희",
      "lastName": "김",
      "email": "kim@example.com",
      "role": "MEMBER",
      "memberType": "MEMBER",
      "joinedAt": "2025-07-20T14:00:00Z",
      "lastActiveAt": "2025-08-17T18:30:00Z",
      "isActive": true,
      "invitedBy": {
        "id": "user_123",
        "firstName": "길동",
        "lastName": "홍"
      }
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 8,
    "hasNext": false,
    "hasPrev": false
  }
}
```

### 2.2 워크스페이스 멤버 초대

**POST** `/api/workspaces/{workspaceId}/invitations`

**설명**: 워크스페이스에 새 멤버를 초대합니다. (OWNER만 가능)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `workspaceId` | string | Y | 워크스페이스 ID |

**Request Body**:
```json
{
  "inviteType": "EMAIL",
  "email": "newuser@example.com",
  "role": "MEMBER",
  "message": "마케팅팀 워크스페이스에 초대합니다."
}
```

**Request Body Schema**:
| 필드 | 타입 | 필수 | 제약사항 | 설명 |
|------|------|------|----------|------|
| `inviteType` | string | Y | `EMAIL`, `LINK` | 초대 방식 |
| `email` | string | 조건부 | 유효한 이메일 | 이메일 초대시 필수 |
| `role` | string | Y | `MEMBER` | 초대받을 사용자의 역할 |
| `message` | string | N | 최대 200자 | 초대 메시지 |

**Response** (201 CREATED):

**이메일 초대인 경우**:
```json
{
  "invitationId": "inv_12345",
  "type": "WORKSPACE",
  "inviteType": "EMAIL",
  "email": "newuser@example.com",
  "role": "MEMBER",
  "status": "PENDING",
  "expiresAt": "2025-08-25T10:30:00Z",
  "createdAt": "2025-08-18T10:30:00Z"
}
```

**링크 초대인 경우**:
```json
{
  "invitationId": "inv_67890",
  "type": "WORKSPACE",
  "inviteType": "LINK",
  "inviteCode": "abc123def456",
  "inviteUrl": "https://boardly.com/invite/abc123def456",
  "role": "MEMBER",
  "status": "PENDING",
  "expiresAt": "2025-08-25T10:30:00Z",
  "createdAt": "2025-08-18T10:30:00Z"
}
```

**Error Responses**:
- `403 FORBIDDEN`: 멤버 초대 권한이 없음
- `409 CONFLICT`: 이미 워크스페이스 멤버임
- `422 UNPROCESSABLE_ENTITY`: Personal 워크스페이스는 초대 불가

### 2.3 워크스페이스 멤버 역할 변경

**PUT** `/api/workspaces/{workspaceId}/members/{userId}/role`

**설명**: 워크스페이스 멤버의 역할을 변경합니다. (OWNER만 가능)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `workspaceId` | string | Y | 워크스페이스 ID |
| `userId` | string | Y | 대상 사용자 ID |

**Request Body**:
```json
{
  "role": "OWNER"
}
```

**Response** (200 OK):
```json
{
  "id": "user_456",
  "firstName": "영희",
  "lastName": "김",
  "email": "kim@example.com",
  "role": "OWNER",
  "memberType": "MEMBER",
  "joinedAt": "2025-07-20T14:00:00Z",
  "lastActiveAt": "2025-08-17T18:30:00Z",
  "isActive": true
}
```

**Error Responses**:
- `403 FORBIDDEN`: 역할 변경 권한이 없음
- `404 NOT_FOUND`: 멤버를 찾을 수 없음
- `422 UNPROCESSABLE_ENTITY`: BOARD_ONLY 멤버는 OWNER 역할 불가

### 2.4 워크스페이스 멤버 제거

**DELETE** `/api/workspaces/{workspaceId}/members/{userId}`

**설명**: 워크스페이스에서 멤버를 제거합니다. (OWNER만 가능)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `workspaceId` | string | Y | 워크스페이스 ID |
| `userId` | string | Y | 제거할 사용자 ID |

**Response** (204 NO_CONTENT):
빈 응답

**Error Responses**:
- `403 FORBIDDEN`: 멤버 제거 권한이 없음
- `404 NOT_FOUND`: 멤버를 찾을 수 없음
- `409 CONFLICT`: 자기 자신은 제거할 수 없음

## 3. 초대 관리 API

### 3.1 워크스페이스 초대 목록 조회

**GET** `/api/workspaces/{workspaceId}/invitations`

**설명**: 워크스페이스의 초대 목록을 조회합니다. (OWNER만 가능)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `workspaceId` | string | Y | 워크스페이스 ID |

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| `status` | string | N | 상태 필터 (`PENDING`, `ACCEPTED`, `DECLINED`, `EXPIRED`) | 전체 |
| `page` | number | N | 페이지 번호 | 1 |
| `limit` | number | N | 페이지당 항목 수 | 20 |

**Response** (200 OK):
```json
{
  "invitations": [
    {
      "id": "inv_12345",
      "type": "WORKSPACE",
      "inviteType": "EMAIL",
      "email": "newuser@example.com",
      "role": "MEMBER",
      "status": "PENDING",
      "invitedBy": {
        "id": "user_123",
        "firstName": "길동",
        "lastName": "홍"
      },
      "expiresAt": "2025-08-25T10:30:00Z",
      "createdAt": "2025-08-18T10:30:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "hasNext": false,
    "hasPrev": false
  }
}
```

### 3.2 초대 취소

**DELETE** `/api/workspaces/{workspaceId}/invitations/{invitationId}`

**설명**: 대기 중인 초대를 취소합니다. (OWNER만 가능)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `workspaceId` | string | Y | 워크스페이스 ID |
| `invitationId` | string | Y | 초대 ID |

**Response** (204 NO_CONTENT):
빈 응답

**Error Responses**:
- `403 FORBIDDEN`: 초대 취소 권한이 없음
- `404 NOT_FOUND`: 초대를 찾을 수 없음
- `409 CONFLICT`: 이미 수락되거나 만료된 초대

### 3.3 초대 수락

**POST** `/api/invitations/{inviteCode}/accept`

**설명**: 초대 코드로 초대를 수락합니다.

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `inviteCode` | string | Y | 초대 코드 |

**Response** (200 OK):
```json
{
  "workspace": {
    "id": "ws_67890",
    "name": "마케팅팀",
    "description": "마케팅 프로젝트 관리 워크스페이스",
    "type": "TEAM"
  },
  "membership": {
    "role": "MEMBER",
    "memberType": "MEMBER",
    "joinedAt": "2025-08-18T10:30:00Z"
  }
}
```

**Error Responses**:
- `404 NOT_FOUND`: 유효하지 않은 초대 코드
- `409 CONFLICT`: 이미 워크스페이스 멤버임
- `410 GONE`: 만료된 초대

### 3.4 초대 거절

**POST** `/api/invitations/{inviteCode}/decline`

**설명**: 초대 코드로 초대를 거절합니다.

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `inviteCode` | string | Y | 초대 코드 |

**Response** (204 NO_CONTENT):
빈 응답

**Error Responses**:
- `404 NOT_FOUND`: 유효하지 않은 초대 코드
- `410 GONE`: 만료된 초대

## 4. 에러 코드 정의

| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| `WORKSPACE_NOT_FOUND` | 404 | 워크스페이스를 찾을 수 없음 |
| `WORKSPACE_ACCESS_DENIED` | 403 | 워크스페이스 접근 권한 없음 |
| `WORKSPACE_LIMIT_EXCEEDED` | 422 | 워크스페이스 생성 제한 초과 |
| `WORKSPACE_HAS_BOARDS` | 409 | 워크스페이스에 보드가 남아있어 삭제 불가 |
| `WORKSPACE_NAME_MISMATCH` | 400 | 삭제 확인용 이름이 일치하지 않음 |
| `PERSONAL_WORKSPACE_INVITE_NOT_ALLOWED` | 422 | Personal 워크스페이스는 초대 불가 |
| `MEMBER_NOT_FOUND` | 404 | 워크스페이스 멤버를 찾을 수 없음 |
| `MEMBER_ALREADY_EXISTS` | 409 | 이미 워크스페이스 멤버임 |
| `CANNOT_REMOVE_SELF` | 409 | 자기 자신은 제거할 수 없음 |
| `BOARD_ONLY_CANNOT_BE_OWNER` | 422 | BOARD_ONLY 멤버는 OWNER 역할 불가 |
| `INVITATION_NOT_FOUND` | 404 | 초대를 찾을 수 없음 |
| `INVITATION_EXPIRED` | 410 | 만료된 초대 |
| `INVITATION_ALREADY_PROCESSED` | 409 | 이미 처리된 초대 |
| `INVALID_INVITE_CODE` | 404 | 유효하지 않은 초대 코드 |

## 5. 사용 예시

### 5.1 팀 워크스페이스 생성 및 멤버 초대 플로우

```bash
# 1. 팀 워크스페이스 생성
curl -X POST https://api.boardly.com/v1/api/workspaces \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "개발팀",
    "description": "소프트웨어 개발 프로젝트",
    "type": "TEAM"
  }'

# 2. 멤버 초대 (이메일)
curl -X POST https://api.boardly.com/v1/api/workspaces/ws_new123/invitations \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "inviteType": "EMAIL",
    "email": "developer@example.com",
    "role": "MEMBER",
    "message": "개발팀에 합류해주세요!"
  }'

# 3. 멤버 초대 (링크)
curl -X POST https://api.boardly.com/v1/api/workspaces/ws_new123/invitations \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "inviteType": "LINK",
    "role": "MEMBER"
  }'

# 4. 초대 수락 (초대받은 사용자)
curl -X POST https://api.boardly.com/v1/api/invitations/abc123def456/accept \
  -H "Authorization: Bearer <new_user_jwt_token>"
```

### 5.2 워크스페이스 관리 플로우

```bash
# 1. 워크스페이스 목록 조회
curl -X GET https://api.boardly.com/v1/api/workspaces \
  -H "Authorization: Bearer <jwt_token>"

# 2. 특정 워크스페이스 상세 조회
curl -X GET https://api.boardly.com/v1/api/workspaces/ws_67890 \
  -H "Authorization: Bearer <jwt_token>"

# 3. 워크스페이스 멤버 목록 조회
curl -X GET https://api.boardly.com/v1/api/workspaces/ws_67890/members \
  -H "Authorization: Bearer <jwt_token>"

# 4. 멤버 역할 변경
curl -X PUT https://api.boardly.com/v1/api/workspaces/ws_67890/members/user_456/role \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{"role": "OWNER"}'

# 5. 워크스페이스 삭제 (모든 보드 삭제 후)
curl -X DELETE https://api.boardly.com/v1/api/workspaces/ws_67890 \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{"confirmationName": "마케팅팀"}'
```