# 워크스페이스 설계 문서 - 권한 및 초대 시스템

## 1. 워크스페이스 타입

### 1.1 Personal 워크스페이스
- **목적**: 개인 프로젝트 관리
- **특징**: 
  - 개인 전용, 멤버 초대 불가
  - 사용자 혼자만 접근 가능
  - 개인 업무, 학습, 취미 프로젝트 등에 활용
- **기본 생성**: 회원가입 시 "My Workspace" 이름으로 자동 생성

### 1.2 Team 워크스페이스  
- **목적**: 팀 협업 프로젝트 관리
- **특징**:
  - 멤버 초대 및 협업 가능
  - 보드 공개/비공개 설정 지원
  - 실시간 동기화 지원 (P1)
  - 팀 프로젝트, 회사 업무 등에 활용

## 2. 보드 공개 설정

### 2.1 공개 보드 (Public Board)
- **접근 권한**: 워크스페이스의 모든 정식 멤버가 자동 접근
- **기본 권한**: Editor 권한 자동 부여
- **표시**: 워크스페이스 멤버의 보드 목록에 표시

### 2.2 비공개 보드 (Private Board)  
- **접근 권한**: 개별 초대받은 사용자만 접근 가능
- **권한 설정**: 초대 시 Editor/Viewer 권한 선택 가능
- **표시**: 초대받은 사용자의 보드 목록에만 표시

### 2.3 공개 설정 변경

#### 비공개 → 공개 변경
1. 워크스페이스의 모든 정식 멤버에게 Editor 권한 자동 부여
2. 기존 개별 초대받은 사용자들의 권한은 유지
3. **실시간 UI 업데이트**: 워크스페이스 멤버들의 보드 목록에 즉시 표시

#### 공개 → 비공개 변경
1. 기존 권한 유지 (자동 부여된 권한도 개별 초대 상태로 변환)
2. 새로운 멤버는 개별 초대를 통해서만 접근 가능

## 3. 사용자 타입 및 권한 체계

### 3.1 워크스페이스 역할

| 역할 | 설명 | 권한 | 표시 위치 |
|------|------|------|-----------|
| **OWNER** | 워크스페이스 소유자 | - 워크스페이스 관리 (설정, 삭제)<br>- 멤버 초대/제거<br>- 모든 보드에 대한 절대 권한<br>- 멤버 역할 변경 | 워크스페이스 멤버 목록 |
| **MEMBER** | 워크스페이스 정식 멤버 | - 보드 생성<br>- 공개 보드에 Editor 권한<br>- 비공개 보드는 개별 초대 필요<br>- 자신이 생성한 보드는 OWNER 권한 | 워크스페이스 멤버 목록 |
| **BOARD_ONLY** | 보드에만 초대받은 사용자 | - 보드 생성 불가<br>- 초대받은 보드만 접근<br>- 워크스페이스 정보 접근 불가 | 워크스페이스 멤버 목록 표시 안 함<br>해당 보드 멤버 목록에만 표시 |

### 3.2 보드 역할

| 역할 | 설명 | 권한 |
|------|------|------|
| **OWNER** | 보드 소유자 | - 보드 관리 (설정, 삭제, 공개/비공개 변경)<br>- 멤버 초대<br>- 모든 콘텐츠 권한 |
| **EDITOR** | 보드 편집자 | - 보드 정보 수정<br>- 콘텐츠 생성/수정/삭제<br>- 댓글 작성/수정/삭제 |
| **VIEWER** | 보드 조회자 | - 보드 및 콘텐츠 조회만 가능<br>- 수정/삭제 불가 |

### 3.3 권한 우선순위 규칙

**🏆 1순위: 워크스페이스 OWNER**
- 워크스페이스 내 모든 보드(공개/비공개)에 대한 절대 권한
- 보드별 역할에 관계없이 모든 작업 가능

**🥈 2순위: 워크스페이스 MEMBER**  
- 공개 보드: 자동 Editor 권한
- 비공개 보드: 개별 초대받은 경우만 접근

**🥉 3순위: BOARD_ONLY 사용자**
- 초대받은 보드에만 지정된 권한으로 접근
- 워크스페이스 레벨 권한 없음

## 4. 초대 시스템

### 4.1 워크스페이스 초대

**대상**: Team 워크스페이스만 가능  
**결과**: 초대받은 사용자는 워크스페이스 MEMBER가 되어 모든 공개 보드에 Editor 권한

**초대 방법**:
1. **이메일 초대**: 이메일 주소로 초대 링크 발송
2. **초대 링크**: 7일간 유효한 워크스페이스 초대 링크 생성

**초대 프로세스**:
```
1. 워크스페이스 OWNER가 멤버 초대
2. 초대받은 사용자는 회원가입 필수 (기존 사용자면 로그인)
3. 초대 수락 시 워크스페이스 MEMBER 역할 부여
4. 워크스페이스 내 모든 공개 보드에 자동 Editor 권한
5. 비공개 보드는 개별 초대 필요
```

### 4.2 보드 개별 초대

**대상**: 워크스페이스에 속하지 않은 외부 사용자 또는 워크스페이스 멤버  
**권한**: 초대 시 Editor/Viewer 역할 선택 가능

**초대 방법**:
1. **이메일 초대**: 이메일 주소로 보드 초대 링크 발송
2. **초대 링크**: 7일간 유효한 보드 초대 링크 생성

**초대 프로세스**:
```
외부 사용자의 경우:
1. 워크스페이스 OWNER 또는 보드 OWNER가 외부 사용자를 특정 보드에 초대
2. 초대받은 사용자는 회원가입 필수
3. 초대 수락 시:
   - BOARD_ONLY 타입으로 설정
   - 해당 보드에만 지정된 역할(EDITOR/VIEWER) 부여
   - 워크스페이스 정보 접근 불가

워크스페이스 멤버의 경우:
1. 비공개 보드에 워크스페이스 멤버를 개별 초대
2. Editor/Viewer 권한 선택 가능
3. 해당 멤버는 계속 워크스페이스 MEMBER 타입 유지
```

### 4.3 새 보드 생성 시 권한 자동 설정

#### Personal 워크스페이스에서 보드 생성
```
1. 생성자는 해당 보드의 OWNER가 됨
2. 공개/비공개 개념 없음 (소유자만 접근)
3. UI에서 공개 설정 옵션 표시되지 않음
4. 데이터베이스에서 is_public 값은 의미 없음 (항상 소유자만 접근)
```

#### Team 워크스페이스에서 보드 생성

**공개 보드 생성 시 (기본값)**:
```
1. 생성자는 해당 보드의 OWNER가 됨
2. 다른 워크스페이스 정식 멤버들은 자동으로 Editor 권한 부여
3. BOARD_ONLY 사용자들은 접근 불가
4. UI에서 공개 설정 선택 옵션 표시 (기본값: 공개)
```

**비공개 보드 생성 시**:
```
1. 생성자는 해당 보드의 OWNER가 됨
2. 다른 사용자들은 개별 초대를 통해서만 접근 가능
3. 워크스페이스 정식 멤버들도 초대받아야 접근 가능
```

#### 보드 생성 UI 플로우

**Personal 워크스페이스**:
```
[새 보드 만들기 모달]
┌─────────────────────────────────────┐
│ 보드 이름: [________________]       │
│ 설명: [____________________]        │
│                                     │
│ [취소] [보드 만들기]                │
└─────────────────────────────────────┘
```

**Team 워크스페이스**:
```
[새 보드 만들기 모달]
┌─────────────────────────────────────┐
│ 보드 이름: [________________]       │
│ 설명: [____________________]        │
│                                     │
│ 공개 설정:                          │
│ ● 공개 - 워크스페이스 멤버 모두 접근  │
│ ○ 비공개 - 초대받은 사용자만 접근    │
│                                     │
│ [취소] [보드 만들기]                │
└─────────────────────────────────────┘
```

## 5. 권한 매트릭스

| 기능 | 워크스페이스<br>OWNER | 워크스페이스<br>MEMBER | BOARD_ONLY | 보드<br>OWNER | 보드<br>EDITOR | 보드<br>VIEWER |
|:-----|:---------------------:|:---------------------:|:----------:|:--------------:|:---------------:|:---------------:|
| **워크스페이스 관리** |
| 워크스페이스 설정/삭제 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 워크스페이스 멤버 초대 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 워크스페이스 정보 조회 | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ |
| **보드 관리** |
| 보드 생성 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| 공개 보드 자동 접근 | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ |
| 보드 설정 (공개/비공개) | ✅ | ✅* | ❌ | ✅ | ❌ | ❌ |
| 보드 정보 수정 | ✅ | ✅* | ❌ | ✅ | ✅ | ❌ |
| 보드 삭제 | ✅ | ✅* | ❌ | ✅ | ❌ | ❌ |
| 보드 멤버 초대 | ✅ | ✅* | ❌ | ✅ | ❌ | ❌ |
| **콘텐츠 관리** |
| 콘텐츠 조회 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 콘텐츠 생성/수정 | ✅ | ✅ | ✅** | ✅ | ✅ | ❌ |
| 콘텐츠 삭제 | ✅ | ✅ | ✅** | ✅ | ✅ | ❌ |
| **활동 추적** |
| 활동 내역 조회 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

**\* 해당 보드에 접근 권한이 있는 경우에만**  
**\*\* Editor 권한으로 초대받은 경우에만**

## 6. 사용자 시나리오

### 6.1 팀 전체 합류 시나리오

```
1. 팀장이 "마케팅팀" Team 워크스페이스 생성
2. 팀장이 신입사원을 워크스페이스에 초대 (이메일 또는 링크)
3. 신입사원이 회원가입 후 초대 수락
4. 신입사원은 워크스페이스 MEMBER가 되어:
   - 모든 공개 보드(Q1 캠페인, 브랜딩 전략 등)에 Editor 권한
   - 비공개 보드(임원진 회의록)는 개별 초대 필요
5. 신입사원이 "개인 프로젝트" 공개 보드를 새로 생성
6. 다른 팀원들은 실시간으로 보드 목록에서 "개인 프로젝트" 확인 가능
7. 다른 팀원들은 자동으로 "개인 프로젝트" 보드에 Editor 권한 부여
```

### 6.2 외부 협력업체 참여 시나리오

```
1. 팀장이 외부 디자이너를 "브랜딩 프로젝트" 보드에만 Viewer로 초대 (이메일 또는 링크)
2. 외부 디자이너가 회원가입 후 초대 수락  
3. 외부 디자이너는 BOARD_ONLY 타입이 되어:
   - "브랜딩 프로젝트" 보드만 Viewer 권한으로 접근 가능
   - 다른 보드(Q1 캠페인, 개인 프로젝트 등)는 조회 불가
   - 워크스페이스 정보(이름, 설명, 멤버 목록) 접근 불가
   - 워크스페이스 멤버 목록에 표시되지 않음
4. "브랜딩 프로젝트" 보드의 멤버 목록에는 외부 디자이너 표시됨
```

### 6.3 비공개 보드 활용 시나리오

```
1. 팀장이 "임원진 회의록" 비공개 보드 생성
2. 워크스페이스의 다른 멤버들은 해당 보드를 볼 수 없음
3. 팀장이 부팀장을 "임원진 회의록" 보드에 Editor로 개별 초대
4. 부팀장은 해당 보드에 접근 가능하게 되고, 보드 목록에 표시됨
5. 나중에 팀장이 보드를 공개로 변경하면:
   - 워크스페이스의 모든 정식 멤버에게 Editor 권한 자동 부여
   - 멤버들의 보드 목록에 실시간으로 "임원진 회의록" 보드 표시
   - 부팀장의 기존 Editor 권한은 유지됨
```

### 6.4 권한 충돌 해결 예시

```
상황: 
- 김개발자: 워크스페이스 MEMBER
- "레거시 시스템" 공개 보드 → 자동 Editor 권한
- 보드 소유자가 김개발자를 해당 보드에 Viewer로 개별 초대 시도

결과:
- 워크스페이스 설정이 우선되어 김개발자는 Editor 권한 유지
- 보드별 Viewer 설정은 무시됨
```

## 7. MVP 범위 제한사항

### 7.1 보드 이동 불가
- Personal 워크스페이스 ↔ Team 워크스페이스 간 보드 이동 불가
- 복사/이동 기능은 P1 단계에서 고려

### 7.2 단순화된 초대 시스템
- 외부 사용자 초대 시 회원가입 필수
- 임시 접근이나 게스트 모드 없음
- 초대 링크 유효기간: 7일 고정

### 7.3 BOARD_ONLY 사용자 제한
- 워크스페이스 레벨 정보 접근 불가
- 보드 생성 권한 없음
- 워크스페이스 멤버 목록에 표시되지 않음

### 7.4 워크스페이스 삭제 제한
- 워크스페이스에 보드가 1개라도 있으면 삭제 불가
- 모든 보드를 먼저 삭제한 후 워크스페이스 삭제 가능
- 삭제 시도 시 "워크스페이스를 삭제하려면 먼저 모든 보드를 삭제해주세요" 오류 메시지 표시

## 8. API 설계 고려사항

### 8.1 권한 검증 로직
```javascript
function checkPermission(userId, workspaceId, boardId, permission) {
  // 1. 워크스페이스 OWNER 체크
  if (isWorkspaceOwner(userId, workspaceId)) {
    return true;
  }
  
  // 2. 워크스페이스 MEMBER 체크
  const workspaceMember = getWorkspaceMember(userId, workspaceId);
  if (workspaceMember && workspaceMember.type === 'MEMBER') {
    // 공개 보드인 경우 Editor 권한
    const board = getBoard(boardId);
    if (board.isPublic) {
      return hasEditorPermission(permission);
    }
    // 비공개 보드인 경우 개별 권한 체크
    return checkBoardPermission(userId, boardId, permission);
  }
  
  // 3. BOARD_ONLY 사용자 권한 체크
  if (workspaceMember && workspaceMember.type === 'BOARD_ONLY') {
    return checkBoardPermission(userId, boardId, permission);
  }
  
  return false;
}
```

### 8.2 보드 공개 설정 변경 로직
```javascript
function changeBoardVisibility(boardId, isPublic, userId) {
  const board = getBoard(boardId);
  const workspace = getWorkspace(board.workspaceId);
  
  // Personal 워크스페이스는 공개 설정 변경 불가
  if (workspace.type === 'PERSONAL') {
    throw new BadRequestException('개인 워크스페이스에서는 공개 설정을 변경할 수 없습니다');
  }
  
  // 권한 체크
  if (!canManageBoard(userId, boardId)) {
    throw new ForbiddenException('보드 설정을 변경할 권한이 없습니다');
  }
  
  if (isPublic && !board.isPublic) {
    // 비공개 → 공개: 워크스페이스 멤버들에게 자동 권한 부여
    const workspaceMembers = getWorkspaceMembers(board.workspaceId, 'MEMBER');
    workspaceMembers.forEach(member => {
      if (!hasBoardAccess(member.userId, boardId)) {
        addBoardMember(boardId, member.userId, 'EDITOR');
      }
    });
    
    // 실시간 UI 업데이트
    notifyWorkspaceMembers(board.workspaceId, {
      type: 'BOARD_MADE_PUBLIC',
      boardId: boardId,
      boardName: board.name
    });
  }
  
  updateBoard(boardId, { isPublic });
}
```

### 8.3 자동 권한 부여 로직
```javascript
function createBoard(userId, workspaceId, boardData) {
  const workspace = getWorkspace(workspaceId);
  const board = createBoardEntity(boardData);
  
  // 생성자를 보드 OWNER로 설정
  setBoardOwner(board.id, userId);
  
  // Personal 워크스페이스는 공개 설정 처리 안 함
  if (workspace.type === 'PERSONAL') {
    return board;
  }
  
  // Team 워크스페이스에서 공개 보드인 경우
  if (board.isPublic) {
    // 워크스페이스 정식 멤버들에게 Editor 권한 자동 부여
    const workspaceMembers = getWorkspaceMembers(workspaceId, 'MEMBER');
    workspaceMembers.forEach(member => {
      if (member.userId !== userId) { // 생성자 제외
        setBoardMember(board.id, member.userId, 'EDITOR');
      }
    });
    
    // 실시간 UI 업데이트
    notifyWorkspaceMembers(workspaceId, {
      type: 'NEW_PUBLIC_BOARD',
      boardId: board.id,
      boardName: board.name
    });
  }
  
  return board;
}
```

## 9. 데이터베이스 스키마

### 9.1 Workspaces 테이블 수정
```sql
CREATE TABLE workspaces (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  type ENUM('PERSONAL', 'TEAM') NOT NULL,
  owner_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (owner_id) REFERENCES users(id)
);
```

### 9.2 Boards 테이블 수정
```sql
CREATE TABLE boards (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  is_public BOOLEAN DEFAULT TRUE,
  is_starred BOOLEAN DEFAULT FALSE,
  workspace_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
);
```

### 9.3 Workspace_Members 테이블
사용자와 워크스페이스 간의 멤버십 관계 및 역할을 관리하는 테이블

```sql
CREATE TABLE workspace_members (
  user_id VARCHAR(255) NOT NULL,
  workspace_id VARCHAR(255) NOT NULL,
  role ENUM('OWNER', 'MEMBER') NOT NULL,
  type ENUM('MEMBER', 'BOARD_ONLY') NOT NULL DEFAULT 'MEMBER',
  joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  invited_by VARCHAR(255),
  invited_at TIMESTAMP,
  PRIMARY KEY (user_id, workspace_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
  FOREIGN KEY (invited_by) REFERENCES users(id)
);
```

**컬럼 설명**:
- `user_id`: 워크스페이스 멤버의 사용자 ID
- `workspace_id`: 워크스페이스 ID
- `role`: 워크스페이스에서의 역할
  - `OWNER`: 워크스페이스 소유자 (생성자, 관리 권한)
  - `MEMBER`: 워크스페이스 정식 멤버 (보드 생성 가능)
- `type`: 멤버 타입 구분
  - `MEMBER`: 워크스페이스 전체 접근 가능한 정식 멤버
  - `BOARD_ONLY`: 특정 보드에만 초대받은 제한된 멤버
- `joined_at`: 워크스페이스 가입 일시
- `invited_by`: 초대한 사용자 ID (자체 가입인 경우 NULL)
- `invited_at`: 초대받은 일시 (자체 가입인 경우 NULL)

**사용 사례**:
- Personal 워크스페이스: 소유자 자동 등록 (`role=OWNER`, `type=MEMBER`)
- Team 워크스페이스 초대: 정식 멤버 등록 (`role=MEMBER`, `type=MEMBER`)
- 보드 개별 초대: 제한된 멤버 등록 (`role=MEMBER`, `type=BOARD_ONLY`)

### 9.4 Board_Members 테이블
사용자와 보드 간의 접근 권한 및 역할을 관리하는 테이블

```sql
CREATE TABLE board_members (
  user_id VARCHAR(255) NOT NULL,
  board_id VARCHAR(255) NOT NULL,
  role ENUM('OWNER', 'EDITOR', 'VIEWER') NOT NULL,
  joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  invited_by VARCHAR(255),
  invited_at TIMESTAMP,
  invitation_type ENUM('WORKSPACE_AUTO', 'INDIVIDUAL') NOT NULL DEFAULT 'INDIVIDUAL',
  PRIMARY KEY (user_id, board_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (board_id) REFERENCES boards(id),
  FOREIGN KEY (invited_by) REFERENCES users(id)
);
```

**컬럼 설명**:
- `user_id`: 보드 멤버의 사용자 ID
- `board_id`: 보드 ID
- `role`: 보드에서의 역할
  - `OWNER`: 보드 소유자 (생성자, 모든 권한)
  - `EDITOR`: 편집자 (콘텐츠 생성/수정/삭제 가능)
  - `VIEWER`: 조회자 (읽기 전용)
- `joined_at`: 보드 접근 권한 부여 일시
- `invited_by`: 초대한 사용자 ID (시스템 자동 부여인 경우 NULL)
- `invited_at`: 개별 초대받은 일시 (자동 부여인 경우 NULL)
- `invitation_type`: 권한 부여 방식 구분
  - `WORKSPACE_AUTO`: 워크스페이스 멤버십으로 자동 부여된 권한
  - `INDIVIDUAL`: 사용자가 직접 초대하여 부여된 권한

**사용 사례**:
- 보드 생성: 생성자를 OWNER로 등록 (`invitation_type=INDIVIDUAL`)
- 공개 보드 자동 권한: 워크스페이스 멤버들에게 자동 부여 (`invitation_type=WORKSPACE_AUTO`)
- 개별 초대: 특정 사용자를 직접 초대 (`invitation_type=INDIVIDUAL`)

### 9.5 Invitations 테이블
워크스페이스 및 보드 초대 정보를 관리하는 테이블

```sql
CREATE TABLE invitations (
  id VARCHAR(255) PRIMARY KEY,
  type ENUM('WORKSPACE', 'BOARD') NOT NULL,
  target_id VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  invite_code VARCHAR(255) UNIQUE,
  role VARCHAR(50) NOT NULL,
  status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED') DEFAULT 'PENDING',
  invited_by VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  accepted_at TIMESTAMP NULL,
  FOREIGN KEY (invited_by) REFERENCES users(id),
  INDEX idx_invite_code (invite_code),
  INDEX idx_email_status (email, status),
  INDEX idx_expires_at (expires_at)
);
```

**컬럼 설명**:
- `id`: 초대 고유 식별자
- `type`: 초대 타입
  - `WORKSPACE`: 워크스페이스 초대
  - `BOARD`: 보드 개별 초대
- `target_id`: 초대 대상 ID (워크스페이스 ID 또는 보드 ID)
- `email`: 초대받을 사용자의 이메일 주소 (이메일 초대인 경우)
- `invite_code`: 초대 링크용 고유 코드 (링크 초대인 경우)
- `role`: 초대 시 부여될 역할
  - 워크스페이스 초대: `MEMBER`
  - 보드 초대: `OWNER`, `EDITOR`, `VIEWER`
- `status`: 초대 상태
  - `PENDING`: 초대 대기 중
  - `ACCEPTED`: 초대 수락됨
  - `DECLINED`: 초대 거절됨
  - `EXPIRED`: 초대 만료됨
- `invited_by`: 초대를 보낸 사용자 ID
- `expires_at`: 초대 만료 일시 (기본 7일)
- `created_at`: 초대 생성 일시
- `accepted_at`: 초대 수락 일시 (수락된 경우만)

**인덱스 설명**:
- `idx_invite_code`: 초대 링크 접근 시 빠른 조회를 위한 인덱스
- `idx_email_status`: 이메일별 초대 상태 조회용 복합 인덱스
- `idx_expires_at`: 만료된 초대 정리 작업용 인덱스

**사용 사례**:
- 이메일 초대: `email` 필드 사용, `invite_code` NULL
- 링크 초대: `invite_code` 필드 사용, `email` NULL
- 만료 처리: 배치 작업으로 `expires_at` 기준 상태 업데이트

## 10. 데이터 정합성 규칙

### 10.1 Workspace_Members 테이블 제약사항

**1. Personal 워크스페이스 규칙**
```sql
-- Personal 워크스페이스는 OWNER 1명만 허용
SELECT COUNT(*) FROM workspace_members wm
JOIN workspaces w ON wm.workspace_id = w.id
WHERE w.type = 'PERSONAL' AND wm.role = 'OWNER'
GROUP BY wm.workspace_id
HAVING COUNT(*) > 1; -- 이 쿼리 결과가 없어야 함
```

**2. 워크스페이스 OWNER 필수**
```sql
-- 모든 워크스페이스는 최소 1명의 OWNER 필요
SELECT w.id FROM workspaces w
LEFT JOIN workspace_members wm ON w.id = wm.workspace_id AND wm.role = 'OWNER'
WHERE wm.workspace_id IS NULL; -- 이 쿼리 결과가 없어야 함
```

**3. type과 role의 일관성**
```sql
-- BOARD_ONLY 타입은 항상 MEMBER 역할만 가능
SELECT * FROM workspace_members 
WHERE type = 'BOARD_ONLY' AND role = 'OWNER'; -- 이 쿼리 결과가 없어야 함
```

### 10.2 Board_Members 테이블 제약사항

**1. 보드 OWNER 필수**
```sql
-- 모든 보드는 최소 1명의 OWNER 필요
SELECT b.id FROM boards b
LEFT JOIN board_members bm ON b.id = bm.board_id AND bm.role = 'OWNER'
WHERE bm.board_id IS NULL; -- 이 쿼리 결과가 없어야 함
```

**2. WORKSPACE_AUTO 타입 제한**
```sql
-- WORKSPACE_AUTO는 워크스페이스 정식 멤버만 가능
SELECT bm.* FROM board_members bm
JOIN boards b ON bm.board_id = b.id
JOIN workspace_members wm ON bm.user_id = wm.user_id AND b.workspace_id = wm.workspace_id
WHERE bm.invitation_type = 'WORKSPACE_AUTO' 
AND wm.type = 'BOARD_ONLY'; -- 이 쿼리 결과가 없어야 함
```

**3. 공개 보드 접근 규칙**
```sql
-- 공개 보드에 접근하는 워크스페이스 MEMBER는 최소 EDITOR 권한 필요
SELECT bm.* FROM board_members bm
JOIN boards b ON bm.board_id = b.id
JOIN workspace_members wm ON bm.user_id = wm.user_id AND b.workspace_id = wm.workspace_id
WHERE b.is_public = TRUE 
AND wm.type = 'MEMBER' 
AND bm.role = 'VIEWER'
AND bm.invitation_type = 'WORKSPACE_AUTO'; -- 이 쿼리 결과가 없어야 함
```

### 10.3 Invitations 테이블 제약사항

**1. 이메일 또는 초대 코드 중 하나만 설정**
```sql
-- email과 invite_code가 동시에 NULL이거나 동시에 값이 있으면 안됨
SELECT * FROM invitations 
WHERE (email IS NULL AND invite_code IS NULL) 
OR (email IS NOT NULL AND invite_code IS NOT NULL); -- 이 쿼리 결과가 없어야 함
```

**2. 만료 시간 검증**
```sql
-- 만료 시간은 생성 시간보다 이후여야 함
SELECT * FROM invitations 
WHERE expires_at <= created_at; -- 이 쿼리 결과가 없어야 함
```

**3. 수락 시간 일관성**
```sql
-- ACCEPTED 상태인 경우 accepted_at이 설정되어야 함
SELECT * FROM invitations 
WHERE status = 'ACCEPTED' AND accepted_at IS NULL; -- 이 쿼리 결과가 없어야 함

-- ACCEPTED가 아닌 경우 accepted_at이 NULL이어야 함
SELECT * FROM invitations 
WHERE status != 'ACCEPTED' AND accepted_at IS NOT NULL; -- 이 쿼리 결과가 없어야 함
```

## 11. 배치 작업 가이드

### 11.1 만료된 초대 정리 작업

**실행 주기**: 매일 자정 실행

**1. 만료된 초대 상태 업데이트**
```sql
UPDATE invitations 
SET status = 'EXPIRED' 
WHERE status = 'PENDING' 
AND expires_at < NOW();
```

**2. 오래된 만료 초대 삭제 (선택사항)**
```sql
-- 30일 이상 된 만료/거절된 초대 삭제
DELETE FROM invitations 
WHERE status IN ('EXPIRED', 'DECLINED') 
AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

### 11.2 데이터 정합성 검증 작업

**실행 주기**: 매주 실행

**1. 고아 멤버십 정리**
```sql
-- 삭제된 워크스페이스의 멤버십 제거
DELETE wm FROM workspace_members wm
LEFT JOIN workspaces w ON wm.workspace_id = w.id
WHERE w.id IS NULL;

-- 삭제된 보드의 멤버십 제거
DELETE bm FROM board_members bm
LEFT JOIN boards b ON bm.board_id = b.id
WHERE b.id IS NULL;
```

**2. 권한 일관성 복구**
```sql
-- 공개 보드에서 워크스페이스 멤버의 누락된 권한 복구
INSERT INTO board_members (user_id, board_id, role, invitation_type, joined_at)
SELECT wm.user_id, b.id, 'EDITOR', 'WORKSPACE_AUTO', NOW()
FROM workspace_members wm
JOIN boards b ON wm.workspace_id = b.workspace_id
LEFT JOIN board_members bm ON wm.user_id = bm.user_id AND b.id = bm.board_id
WHERE wm.type = 'MEMBER'
AND b.is_public = TRUE
AND bm.user_id IS NULL;
```

### 11.3 통계 및 모니터링 작업

**실행 주기**: 매일 실행

**1. 사용 통계 수집**
```sql
-- 일일 활성 사용자 수
SELECT COUNT(DISTINCT user_id) as daily_active_users
FROM (
  SELECT user_id FROM workspace_members WHERE DATE(joined_at) = CURDATE()
  UNION
  SELECT user_id FROM board_members WHERE DATE(joined_at) = CURDATE()
  UNION  
  SELECT invited_by as user_id FROM invitations WHERE DATE(created_at) = CURDATE()
) as active_users;

-- 워크스페이스별 멤버 수 통계
SELECT w.type, COUNT(wm.user_id) as member_count
FROM workspaces w
LEFT JOIN workspace_members wm ON w.id = wm.workspace_id
GROUP BY w.type;
```

**2. 이상 데이터 모니터링**
```sql
-- OWNER가 없는 워크스페이스 알림
SELECT w.id, w.name, w.type 
FROM workspaces w
LEFT JOIN workspace_members wm ON w.id = wm.workspace_id AND wm.role = 'OWNER'
WHERE wm.workspace_id IS NULL;

-- 과도한 초대 시도 모니터링 (1일 50건 이상)
SELECT invited_by, COUNT(*) as invite_count
FROM invitations 
WHERE DATE(created_at) = CURDATE()
GROUP BY invited_by
HAVING COUNT(*) > 50;
```

### 11.4 성능 최적화 작업

**실행 주기**: 월 1회 실행

**1. 인덱스 최적화 분석**
```sql
-- 자주 사용되는 쿼리 패턴 분석을 위한 실행 계획 검토
EXPLAIN SELECT * FROM workspace_members 
WHERE user_id = 'user_123';

EXPLAIN SELECT * FROM board_members 
WHERE board_id = 'board_456' AND invitation_type = 'WORKSPACE_AUTO';
```

**2. 테이블 크기 모니터링**
```sql
SELECT 
  table_name,
  table_rows,
  ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'boardly'
AND table_name IN ('workspace_members', 'board_members', 'invitations')
ORDER BY (data_length + index_length) DESC;
```

## 10. 향후 확장 고려사항 (P1)

### 10.1 고급 권한 제어
- 워크스페이스 MEMBER의 보드별 권한 개별 설정
- 커스텀 역할 정의
- 보드별 권한 템플릿

### 10.2 보드 이동/복사
- Personal ↔ Team 워크스페이스 간 보드 이동
- 보드 템플릿 및 복사 기능
- 워크스페이스 간 보드 공유

### 10.3 고급 초대 기능
- 임시 접근 링크 (회원가입 없이 제한된 시간 접근)
- 게스트 모드 지원
- 대량 초대 기능 (CSV 업로드 등)
- 초대 링크 커스터마이징 (유효기간, 사용 횟수 제한)

### 10.4 실시간 협업 강화
- 멤버 온라인 상태 표시
- 실시간 커서 및 편집 상태 표시
- 동시 편집 충돌 해결 메커니즘

### 10.5 고급 알림 시스템
- 보드 공개 설정 변경 알림
- 멤버 초대/제거 알림
- 권한 변경 알림
- 이메일/푸시 알림 설정