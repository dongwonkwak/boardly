# Activity

## 개요

이 문서는 보드 애플리케이션의 활동(Activity) 데이터 구조와 타입을 정의합니다.

## 기본 데이터 구조

### Activity 객체 포맷

```json
{
  "id": "activity_9c8b7a6d",
  "type": "CARD_CREATE",
  "actor": {
    "id": "user_gildong",
    "firstName": "길동",
    "lastName": "홍",
    "profileImageUrl": "https://placehold.co/40x40/0284C7/FFFFFF?text=홍"
  },
  "timestamp": "2025-01-17T13:56:00.123Z",
  "payload": {
    "listName": "To Do",
    "cardTitle": "새 기능 기획",
    "listId": "list_123",
    "cardId": "card_456"
  }
}
```

### 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | string | 활동의 고유 식별자 |
| `type` | string | 활동 타입 (프론트엔드에서 활동 종류 식별용) |
| `actor` | object | 활동을 수행한 사용자 정보 |
| `timestamp` | string | 활동 발생 시간 (ISO 8601 형식, Spring Boot 3 Instant 직렬화) |
| `payload` | object | 활동 메시지 생성에 필요한 변수들 |

### Actor 객체 구조

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | string | 사용자 고유 식별자 |
| `firstName` | string | 사용자 이름 |
| `lastName` | string | 사용자 성 |
| `profileImageUrl` | string | 프로필 이미지 URL |

**참고**: `firstName`과 `lastName`이 분리되어 있어, 프론트엔드에서 다국어 지원 시 유연하게 이름을 조합할 수 있습니다.
- 한국어: `{{actorLastName}}{{actorFirstName}}`
- 영어: `{{actorFirstName}} {{actorLastName}}`

**Timestamp 형식**: Spring Boot 3의 `Instant` 객체는 ISO 8601 형식으로 직렬화됩니다.
- 예시: `"2025-01-17T13:56:00.123Z"`
- 밀리초 단위까지 포함된 정확한 시간 정보
- UTC 시간대 기준 (Z 접미사)

## 활동 타입별 상세 정의

### 1. 카드 관련 활동

#### 1.1 카드 생성 (CARD_CREATE)

**타입**: `CARD_CREATE`

**설명**: 새로운 카드가 생성되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "listName": "To Do",
  "cardTitle": "새 기능 기획",
  "listId": "list_123",
  "cardId": "card_456"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `listName` | string | 카드가 생성된 리스트 이름 |
| `cardTitle` | string | 생성된 카드의 제목 |
| `listId` | string | 카드가 생성된 리스트 ID |
| `cardId` | string | 새로 생성된 카드 ID |

**예시 메시지**: "홍길동님이 'To Do' 리스트에 '새 기능 기획' 카드를 생성했습니다."

#### 1.2 카드 이동 (CARD_MOVE)

**타입**: `CARD_MOVE`

**설명**: 카드가 다른 리스트로 이동되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "새 기능 기획",
  "sourceListName": "To Do",
  "destListName": "In Progress",
  "cardId": "card_456",
  "sourceListId": "list_123",
  "destListId": "list_789"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 이동된 카드의 제목 |
| `sourceListName` | string | 이동 전 리스트 이름 |
| `destListName` | string | 이동 후 리스트 이름 |
| `cardId` | string | 이동된 카드 ID |
| `sourceListId` | string | 이동 전 리스트 ID |
| `destListId` | string | 이동 후 리스트 ID |

**예시 메시지**: "홍길동님이 '새 기능 기획' 카드를 'To Do'에서 'In Progress'로 옮겼습니다."

#### 1.3 카드 이름 변경 (CARD_RENAME)

**타입**: `CARD_RENAME`

**설명**: 카드의 제목이 변경되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "oldTitle": "새 기능 기획",
  "newTitle": "사용자 인증 기능 구현",
  "cardId": "card_456"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `oldTitle` | string | 변경 전 카드 제목 |
| `newTitle` | string | 변경 후 카드 제목 |
| `cardId` | string | 카드 ID |

**예시 메시지**: "홍길동님이 카드의 이름을 '새 기능 기획'에서 '사용자 인증 기능 구현'으로 변경했습니다."

#### 1.4 카드 보관 (CARD_ARCHIVE)

**타입**: `CARD_ARCHIVE`

**설명**: 카드가 보관되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "사용자 인증 기능 구현",
  "cardId": "card_456",
  "listName": "Done"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 보관된 카드의 제목 |
| `cardId` | string | 카드 ID |
| `listName` | string | 보관 전 리스트 이름 |

**예시 메시지**: "홍길동님이 '사용자 인증 기능 구현' 카드를 보관했습니다."

#### 1.5 카드 삭제 (CARD_DELETE)

**타입**: `CARD_DELETE`

**설명**: 카드가 삭제되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "사용자 인증 기능 구현",
  "listName": "Done",
  "cardId": "card_456",
  "listId": "list_123"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 삭제된 카드의 제목 |
| `listName` | string | 삭제 전 리스트 이름 |
| `cardId` | string | 카드 ID |
| `listId` | string | 리스트 ID |

**예시 메시지**: "홍길동님이 'Done' 리스트에서 '사용자 인증 기능 구현' 카드를 삭제했습니다."

#### 1.6 카드 멤버 할당 (CARD_ASSIGN_MEMBER)

**타입**: `CARD_ASSIGN_MEMBER`

**설명**: 카드에 멤버가 할당되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "사용자 인증 기능 구현",
  "cardId": "card_456",
  "memberId": "user_jane",
  "memberFirstName": "제인",
  "memberLastName": "김"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 카드 제목 |
| `cardId` | string | 카드 ID |
| `memberId` | string | 할당된 멤버 ID |
| `memberFirstName` | string | 할당된 멤버 이름 |
| `memberLastName` | string | 할당된 멤버 성 |

**예시 메시지**: "홍길동님이 '사용자 인증 기능 구현' 카드에 김제인님을 멤버로 추가했습니다."

#### 1.7 카드 멤버 제외 (CARD_UNASSIGN_MEMBER)

**타입**: `CARD_UNASSIGN_MEMBER`

**설명**: 카드에서 멤버가 제외되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "사용자 인증 기능 구현",
  "cardId": "card_456",
  "memberId": "user_jane",
  "memberFirstName": "제인",
  "memberLastName": "김"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 카드 제목 |
| `cardId` | string | 카드 ID |
| `memberId` | string | 제외된 멤버 ID |
| `memberFirstName` | string | 제외된 멤버 이름 |
| `memberLastName` | string | 제외된 멤버 성 |

**예시 메시지**: "홍길동님이 '사용자 인증 기능 구현' 카드에서 김제인님을 제외했습니다."

#### 1.8 카드 마감일 설정 (CARD_SET_DUE_DATE)

**타입**: `CARD_SET_DUE_DATE`

**설명**: 카드에 마감일이 설정되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "사용자 인증 기능 구현",
  "cardId": "card_456",
  "dueDate": "2025-08-15"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 카드 제목 |
| `cardId` | string | 카드 ID |
| `dueDate` | string | 설정된 마감일 (YYYY-MM-DD 형식) |

**예시 메시지**: "홍길동님이 '사용자 인증 기능 구현' 카드의 마감일을 2025-08-15로 설정했습니다."

#### 1.9 카드 댓글 추가 (CARD_ADD_COMMENT)

**타입**: `CARD_ADD_COMMENT`

**설명**: 카드에 댓글이 추가되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "사용자 인증 기능 구현",
  "cardId": "card_456",
  "commentId": "comment_789"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 카드 제목 |
| `cardId` | string | 카드 ID |
| `commentId` | string | 댓글 ID |

**예시 메시지**: "홍길동님이 '사용자 인증 기능 구현' 카드에 댓글을 남겼습니다."

#### 1.10 카드 첨부파일 추가 (CARD_ADD_ATTACHMENT)

**타입**: `CARD_ADD_ATTACHMENT`

**설명**: 카드에 첨부파일이 추가되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "사용자 인증 기능 구현",
  "cardId": "card_456",
  "fileName": "auth_design.pdf",
  "fileId": "file_123"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 카드 제목 |
| `cardId` | string | 카드 ID |
| `fileName` | string | 첨부된 파일명 |
| `fileId` | string | 파일 ID |

**예시 메시지**: "홍길동님이 '사용자 인증 기능 구현' 카드에 'auth_design.pdf' 파일을 첨부했습니다."

#### 1.11 카드 체크리스트 추가 (CARD_ADD_CHECKLIST)

**타입**: `CARD_ADD_CHECKLIST`

**설명**: 카드에 체크리스트가 추가되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "사용자 인증 기능 구현",
  "cardId": "card_456",
  "checklistName": "구현 단계",
  "checklistId": "checklist_123"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 카드 제목 |
| `cardId` | string | 카드 ID |
| `checklistName` | string | 체크리스트 이름 |
| `checklistId` | string | 체크리스트 ID |

**예시 메시지**: "홍길동님이 '사용자 인증 기능 구현' 카드에 '구현 단계' 체크리스트를 추가했습니다."

#### 1.12 카드 복제 (CARD_DUPLICATE)

**타입**: `CARD_DUPLICATE`

**설명**: 카드가 복제되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "originalCardTitle": "사용자 인증 기능 구현",
  "newCardTitle": "사용자 인증 기능 구현 복사본",
  "originalCardId": "card_456",
  "newCardId": "card_789",
  "listName": "To Do"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `originalCardTitle` | string | 원본 카드 제목 |
| `newCardTitle` | string | 새로 생성된 카드 제목 |
| `originalCardId` | string | 원본 카드 ID |
| `newCardId` | string | 새로 생성된 카드 ID |
| `listName` | string | 카드가 생성된 리스트 이름 |

**예시 메시지**: "홍길동님이 '사용자 인증 기능 구현' 카드를 복제하여 '사용자 인증 기능 구현 복사본' 카드를 생성했습니다."

#### 1.13 카드 설명 수정 (CARD_UPDATE_DESCRIPTION)

**타입**: `CARD_UPDATE_DESCRIPTION`

**설명**: 카드의 설명이 수정되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "사용자 인증 기능 구현",
  "cardId": "card_456",
  "oldDescription": "기존 설명",
  "newDescription": "새로운 설명"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 카드 제목 |
| `cardId` | string | 카드 ID |
| `oldDescription` | string | 수정 전 설명 |
| `newDescription` | string | 수정 후 설명 |

**예시 메시지**: "홍길동님이 '사용자 인증 기능 구현' 카드의 설명을 수정했습니다."

#### 1.14 카드 제목 수정 (CARD_RENAME)

**타입**: `CARD_RENAME`

**설명**: 카드의 제목만 수정되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "oldTitle": "사용자 인증 기능 구현",
  "newTitle": "JWT 인증 시스템 구현",
  "cardId": "card_456"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `oldTitle` | string | 수정 전 카드 제목 |
| `newTitle` | string | 수정 후 카드 제목 |
| `cardId` | string | 카드 ID |

**예시 메시지**: "홍길동님이 카드의 이름을 '사용자 인증 기능 구현'에서 'JWT 인증 시스템 구현'으로 변경했습니다."

#### 1.15 카드 마감일 제거 (CARD_REMOVE_DUE_DATE)

**타입**: `CARD_REMOVE_DUE_DATE`

**설명**: 카드의 마감일이 제거되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "JWT 인증 시스템 구현",
  "cardId": "card_456",
  "removedDueDate": "2025-08-15"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 카드 제목 |
| `cardId` | string | 카드 ID |
| `removedDueDate` | string | 제거된 마감일 |

**예시 메시지**: "홍길동님이 'JWT 인증 시스템 구현' 카드의 마감일을 제거했습니다."

#### 1.16 카드 보관 해제 (CARD_UNARCHIVE)

**타입**: `CARD_UNARCHIVE`

**설명**: 카드가 보관에서 복원되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "cardTitle": "JWT 인증 시스템 구현",
  "cardId": "card_456",
  "listName": "Done"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `cardTitle` | string | 복원된 카드의 제목 |
| `cardId` | string | 카드 ID |
| `listName` | string | 복원된 리스트 이름 |

**예시 메시지**: "홍길동님이 'JWT 인증 시스템 구현' 카드를 보관에서 복원했습니다."

### 2. 리스트 관련 활동

#### 2.1 리스트 생성 (LIST_CREATE)

**타입**: `LIST_CREATE`

**설명**: 새로운 리스트가 생성되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "listName": "Backlog",
  "listId": "list_456",
  "boardName": "프로젝트 A",
  "color": "#FF6B6B"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `listName` | string | 생성된 리스트 이름 |
| `listId` | string | 리스트 ID |
| `boardName` | string | 보드 이름 |
| `color` | string | 리스트 색상 (선택사항) |

**예시 메시지**: "홍길동님이 '프로젝트 A' 보드에 'Backlog' 리스트를 생성했습니다."

#### 2.2 리스트 이름 변경 (LIST_RENAME)

**타입**: `LIST_RENAME`

**설명**: 리스트의 이름이 변경되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "oldName": "Backlog",
  "newName": "To Do",
  "listId": "list_456",
  "boardName": "프로젝트 A"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `oldName` | string | 변경 전 리스트 이름 |
| `newName` | string | 변경 후 리스트 이름 |
| `listId` | string | 리스트 ID |
| `boardName` | string | 보드 이름 |

**예시 메시지**: "홍길동님이 리스트의 이름을 'Backlog'에서 'To Do'로 변경했습니다."

#### 2.3 리스트 색상 변경 (LIST_CHANGE_COLOR)

**타입**: `LIST_CHANGE_COLOR`

**설명**: 리스트의 색상이 변경되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "listName": "To Do",
  "listId": "list_456",
  "oldColor": "#FF6B6B",
  "newColor": "#4ECDC4"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `listName` | string | 리스트 이름 |
| `listId` | string | 리스트 ID |
| `oldColor` | string | 변경 전 색상 |
| `newColor` | string | 변경 후 색상 |

**예시 메시지**: "홍길동님이 'To Do' 리스트의 색상을 변경했습니다."

#### 2.4 리스트 보관 (LIST_ARCHIVE)

**타입**: `LIST_ARCHIVE`

**설명**: 리스트가 보관되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "listName": "Done",
  "listId": "list_456",
  "boardName": "프로젝트 A"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `listName` | string | 보관된 리스트 이름 |
| `listId` | string | 리스트 ID |
| `boardName` | string | 보드 이름 |

**예시 메시지**: "홍길동님이 'Done' 리스트를 보관했습니다."

#### 2.5 리스트 보관 해제 (LIST_UNARCHIVE)

**타입**: `LIST_UNARCHIVE`

**설명**: 리스트가 보관에서 복원되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "listName": "Done",
  "listId": "list_456",
  "boardName": "프로젝트 A"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `listName` | string | 복원된 리스트 이름 |
| `listId` | string | 리스트 ID |
| `boardName` | string | 보드 이름 |

**예시 메시지**: "홍길동님이 'Done' 리스트를 보관에서 복원했습니다."

#### 2.6 리스트 이동 (LIST_MOVE)

**타입**: `LIST_MOVE`

**설명**: 리스트가 이동되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "listName": "In Progress",
  "listId": "list_456",
  "boardName": "프로젝트 A",
  "oldPosition": 1,
  "newPosition": 2
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `listName` | string | 이동된 리스트 이름 |
| `listId` | string | 리스트 ID |
| `boardName` | string | 보드 이름 |
| `oldPosition` | number | 이동 전 위치 |
| `newPosition` | number | 이동 후 위치 |

**예시 메시지**: "홍길동님이 'In Progress' 리스트를 옮겼습니다."

#### 2.7 리스트 삭제 (LIST_DELETE)

**타입**: `LIST_DELETE`

**설명**: 리스트가 삭제되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "listName": "Done",
  "listId": "list_456",
  "boardName": "프로젝트 A",
  "cardCount": 5
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `listName` | string | 삭제된 리스트 이름 |
| `listId` | string | 리스트 ID |
| `boardName` | string | 보드 이름 |
| `cardCount` | number | 삭제된 카드 수 |

**예시 메시지**: "홍길동님이 'Done' 리스트와 5개의 카드를 삭제했습니다."

### 3. 보드 관련 활동

#### 3.1 보드 생성 (BOARD_CREATE)

**타입**: `BOARD_CREATE`

**설명**: 새로운 보드가 생성되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "boardName": "프로젝트 A",
  "boardId": "board_123",
  "description": "새로운 프로젝트 보드"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `boardName` | string | 생성된 보드 이름 |
| `boardId` | string | 보드 ID |
| `description` | string | 보드 설명 (선택사항) |

**예시 메시지**: "홍길동님이 '프로젝트 A' 보드를 생성했습니다."

#### 3.2 보드 복제 (BOARD_DUPLICATE)

**타입**: `BOARD_DUPLICATE`

**설명**: 보드가 복제되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "originalBoardName": "프로젝트 A",
  "newBoardName": "프로젝트 A 복사본",
  "originalBoardId": "board_123",
  "newBoardId": "board_456"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `originalBoardName` | string | 원본 보드 이름 |
| `newBoardName` | string | 새로 생성된 보드 이름 |
| `originalBoardId` | string | 원본 보드 ID |
| `newBoardId` | string | 새로 생성된 보드 ID |

**예시 메시지**: "홍길동님이 '프로젝트 A' 보드를 복제하여 '프로젝트 A 복사본' 보드를 생성했습니다."

#### 3.3 보드 이름 변경 (BOARD_RENAME)

**타입**: `BOARD_RENAME`

**설명**: 보드의 이름이 변경되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "oldName": "프로젝트 A",
  "newName": "웹 애플리케이션 개발",
  "boardId": "board_123"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `oldName` | string | 변경 전 보드 이름 |
| `newName` | string | 변경 후 보드 이름 |
| `boardId` | string | 보드 ID |

**예시 메시지**: "홍길동님이 보드의 이름을 '프로젝트 A'에서 '웹 애플리케이션 개발'로 변경했습니다."

#### 3.4 보드 설명 수정 (BOARD_UPDATE_DESCRIPTION)

**타입**: `BOARD_UPDATE_DESCRIPTION`

**설명**: 보드의 설명이 수정되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "boardName": "웹 애플리케이션 개발",
  "boardId": "board_123",
  "oldDescription": "기존 설명",
  "newDescription": "새로운 설명"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `boardName` | string | 보드 이름 |
| `boardId` | string | 보드 ID |
| `oldDescription` | string | 수정 전 설명 |
| `newDescription` | string | 수정 후 설명 |

**예시 메시지**: "홍길동님이 '웹 애플리케이션 개발' 보드의 설명을 수정했습니다."

#### 3.5 보드 멤버 추가 (BOARD_ADD_MEMBER)

**타입**: `BOARD_ADD_MEMBER`

**설명**: 보드에 새로운 멤버가 추가되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "memberId": "user_jane",
  "memberFirstName": "제인",
  "memberLastName": "김",
  "boardName": "웹 애플리케이션 개발"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `memberId` | string | 추가된 멤버 ID |
| `memberFirstName` | string | 추가된 멤버 이름 |
| `memberLastName` | string | 추가된 멤버 성 |
| `boardName` | string | 보드 이름 |

**예시 메시지**: "홍길동님이 김제인님을 보드에 초대했습니다."

#### 3.6 보드 멤버 제거 (BOARD_REMOVE_MEMBER)

**타입**: `BOARD_REMOVE_MEMBER`

**설명**: 보드에서 멤버가 제거되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "memberId": "user_jane",
  "memberFirstName": "제인",
  "memberLastName": "김",
  "boardName": "웹 애플리케이션 개발"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `memberId` | string | 제거된 멤버 ID |
| `memberFirstName` | string | 제거된 멤버 이름 |
| `memberLastName` | string | 제거된 멤버 성 |
| `boardName` | string | 보드 이름 |

**예시 메시지**: "홍길동님이 보드에서 김제인님을 제외했습니다."

#### 3.7 보드 아카이브 (BOARD_ARCHIVE)

**타입**: `BOARD_ARCHIVE`

**설명**: 보드가 아카이브되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "boardName": "웹 애플리케이션 개발",
  "boardId": "board_123",
  "listCount": 5,
  "cardCount": 25
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `boardName` | string | 아카이브된 보드 이름 |
| `boardId` | string | 보드 ID |
| `listCount` | number | 아카이브된 리스트 수 |
| `cardCount` | number | 아카이브된 카드 수 |

**예시 메시지**: "홍길동님이 '웹 애플리케이션 개발' 보드를 아카이브했습니다."

#### 3.8 보드 언아카이브 (BOARD_UNARCHIVE)

**타입**: `BOARD_UNARCHIVE`

**설명**: 보드가 아카이브에서 복원되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "boardName": "웹 애플리케이션 개발",
  "boardId": "board_123"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `boardName` | string | 복원된 보드 이름 |
| `boardId` | string | 보드 ID |

**예시 메시지**: "홍길동님이 '웹 애플리케이션 개발' 보드를 아카이브에서 복원했습니다."

#### 3.9 보드 삭제 (BOARD_DELETE)

**타입**: `BOARD_DELETE`

**설명**: 보드가 삭제되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "boardName": "웹 애플리케이션 개발",
  "boardId": "board_123",
  "listCount": 5,
  "cardCount": 25
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `boardName` | string | 삭제된 보드 이름 |
| `boardId` | string | 보드 ID |
| `listCount` | number | 삭제된 리스트 수 |
| `cardCount` | number | 삭제된 카드 수 |

**예시 메시지**: "홍길동님이 '웹 애플리케이션 개발' 보드와 5개의 리스트, 25개의 카드를 삭제했습니다."

### 4. 사용자 관련 활동

#### 4.1 사용자 프로필 수정 (USER_UPDATE_PROFILE)

**타입**: `USER_UPDATE_PROFILE`

**설명**: 사용자의 프로필 정보가 수정되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "oldFirstName": "길동",
  "newFirstName": "길동",
  "oldLastName": "홍",
  "newLastName": "김",
  "userId": "user_gildong"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `oldFirstName` | string | 수정 전 이름 |
| `newFirstName` | string | 수정 후 이름 |
| `oldLastName` | string | 수정 전 성 |
| `newLastName` | string | 수정 후 성 |
| `userId` | string | 사용자 ID |

**예시 메시지**: "홍길동님이 프로필 정보를 수정했습니다."

#### 4.2 사용자 언어 설정 변경 (USER_CHANGE_LANGUAGE)

**타입**: `USER_CHANGE_LANGUAGE`

**설명**: 사용자의 언어 설정이 변경되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "oldLanguage": "ko",
  "newLanguage": "en",
  "userId": "user_gildong"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `oldLanguage` | string | 변경 전 언어 코드 |
| `newLanguage` | string | 변경 후 언어 코드 |
| `userId` | string | 사용자 ID |

**예시 메시지**: "홍길동님이 언어 설정을 변경했습니다."

#### 4.3 사용자 비밀번호 변경 (USER_CHANGE_PASSWORD)

**타입**: `USER_CHANGE_PASSWORD`

**설명**: 사용자의 비밀번호가 변경되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "userId": "user_gildong"
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `userId` | string | 사용자 ID |

**예시 메시지**: "홍길동님이 비밀번호를 변경했습니다."

#### 4.4 사용자 계정 삭제 (USER_DELETE_ACCOUNT)

**타입**: `USER_DELETE_ACCOUNT`

**설명**: 사용자 계정이 삭제되었을 때 발생하는 활동

**Payload 구조**:
```json
{
  "userId": "user_gildong",
  "boardCount": 10,
  "listCount": 50,
  "cardCount": 200
}
```

**Payload 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `userId` | string | 삭제된 사용자 ID |
| `boardCount` | number | 삭제된 보드 수 |
| `listCount` | number | 삭제된 리스트 수 |
| `cardCount` | number | 삭제된 카드 수 |

**예시 메시지**: "홍길동님이 계정을 삭제했습니다. (10개 보드, 50개 리스트, 200개 카드 삭제)"

## 국제화(i18n) 메시지 포맷

프론트엔드에서 활동 피드를 렌더링할 때 사용할 수 있는 다국어 메시지 템플릿입니다.

### 카드 관련 활동

| 활동 타입 | 한국어 (ko) | 영어 (en) | 필요 변수 |
|-----------|-------------|-----------|-----------|
| `CARD_CREATE` | `{{actorLastName}}{{actorFirstName}}님이 {{listName}} 리스트에 {{cardTitle}} 카드를 추가했습니다.` | `{{actorFirstName}} {{actorLastName}} added the card {{cardTitle}} to the {{listName}} list.` | `actorLastName`, `actorFirstName`, `listName`, `cardTitle` |
| `CARD_MOVE` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드를 {{sourceListName}}에서 {{destListName}}(으)로 옮겼습니다.` | `{{actorFirstName}} {{actorLastName}} moved the card {{cardTitle}} from {{sourceListName}} to {{destListName}}.` | `actorLastName`, `actorFirstName`, `cardTitle`, `sourceListName`, `destListName` |
| `CARD_RENAME` | `{{actorLastName}}{{actorFirstName}}님이 카드의 이름을 {{oldTitle}}에서 {{newTitle}}(으)로 변경했습니다.` | `{{actorFirstName}} {{actorLastName}} renamed the card from {{oldTitle}} to {{newTitle}}.` | `actorLastName`, `actorFirstName`, `oldTitle`, `newTitle` |
| `CARD_ARCHIVE` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드를 보관했습니다.` | `{{actorFirstName}} {{actorLastName}} archived the card {{cardTitle}}.` | `actorLastName`, `actorFirstName`, `cardTitle` |
| `CARD_DELETE` | `{{actorLastName}}{{actorFirstName}}님이 {{listName}} 리스트에서 {{cardTitle}} 카드를 삭제했습니다.` | `{{actorFirstName}} {{actorLastName}} deleted the card {{cardTitle}} from the list {{listName}}.` | `actorLastName`, `actorFirstName`, `listName`, `cardTitle` |
| `CARD_ASSIGN_MEMBER` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드에 {{memberLastName}}{{memberFirstName}}님을 멤버로 추가했습니다.` | `{{actorFirstName}} {{actorLastName}} assigned {{memberFirstName}} {{memberLastName}} to the card {{cardTitle}}.` | `actorLastName`, `actorFirstName`, `cardTitle`, `memberLastName`, `memberFirstName` |
| `CARD_UNASSIGN_MEMBER` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드에서 {{memberLastName}}{{memberFirstName}}님을 제외했습니다.` | `{{actorFirstName}} {{actorLastName}} unassigned {{memberFirstName}} {{memberLastName}} from the card {{cardTitle}}.` | `actorLastName`, `actorFirstName`, `cardTitle`, `memberLastName`, `memberFirstName` |
| `CARD_SET_DUE_DATE` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드의 마감일을 {{dueDate}}(으)로 설정했습니다.` | `{{actorFirstName}} {{actorLastName}} set the due date for the card {{cardTitle}} to {{dueDate}}.` | `actorLastName`, `actorFirstName`, `cardTitle`, `dueDate` |
| `CARD_ADD_COMMENT` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드에 댓글을 남겼습니다.` | `{{actorFirstName}} {{actorLastName}} commented on the card {{cardTitle}}.` | `actorLastName`, `actorFirstName`, `cardTitle` |
| `CARD_ADD_ATTACHMENT` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드에 {{fileName}} 파일을 첨부했습니다.` | `{{actorFirstName}} {{actorLastName}} attached the file {{fileName}} to the card {{cardTitle}}.` | `actorLastName`, `actorFirstName`, `cardTitle`, `fileName` |
| `CARD_ADD_CHECKLIST` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드에 {{checklistName}} 체크리스트를 추가했습니다.` | `{{actorFirstName}} {{actorLastName}} added the checklist {{checklistName}} to the card {{cardTitle}}.` | `actorLastName`, `actorFirstName`, `cardTitle`, `checklistName` |
| `CARD_DUPLICATE` | `{{actorLastName}}{{actorFirstName}}님이 {{originalCardTitle}} 카드를 복제하여 {{newCardTitle}} 카드를 생성했습니다.` | `{{actorFirstName}} {{actorLastName}} duplicated the card {{originalCardTitle}} to create the card {{newCardTitle}}.` | `actorLastName`, `actorFirstName`, `originalCardTitle`, `newCardTitle` |
| `CARD_UPDATE_DESCRIPTION` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드의 설명을 수정했습니다.` | `{{actorFirstName}} {{actorLastName}} updated the description of the card {{cardTitle}}.` | `actorLastName`, `actorFirstName`, `cardTitle` |
| `CARD_RENAME` | `{{actorLastName}}{{actorFirstName}}님이 카드의 이름을 {{oldTitle}}에서 {{newTitle}}(으)로 변경했습니다.` | `{{actorFirstName}} {{actorLastName}} renamed the card from {{oldTitle}} to {{newTitle}}.` | `actorLastName`, `actorFirstName`, `oldTitle`, `newTitle` |
| `CARD_REMOVE_DUE_DATE` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드의 마감일을 제거했습니다.` | `{{actorFirstName}} {{actorLastName}} removed the due date from the card {{cardTitle}}.` | `actorLastName`, `actorFirstName`, `cardTitle` |
| `CARD_UNARCHIVE` | `{{actorLastName}}{{actorFirstName}}님이 {{cardTitle}} 카드를 보관에서 복원했습니다.` | `{{actorFirstName}} {{actorLastName}} unarchived the card {{cardTitle}}.` | `actorLastName`, `actorFirstName`, `cardTitle` |

### 리스트 관련 활동

| 활동 타입 | 한국어 (ko) | 영어 (en) | 필요 변수 |
|-----------|-------------|-----------|-----------|
| `LIST_CREATE` | `{{actorLastName}}{{actorFirstName}}님이 {{listName}} 리스트를 생성했습니다.` | `{{actorFirstName}} {{actorLastName}} created the list {{listName}}.` | `actorLastName`, `actorFirstName`, `listName` |
| `LIST_RENAME` | `{{actorLastName}}{{actorFirstName}}님이 리스트의 이름을 {{oldName}}에서 {{newName}}(으)로 변경했습니다.` | `{{actorFirstName}} {{actorLastName}} renamed the list from {{oldName}} to {{newName}}.` | `actorLastName`, `actorFirstName`, `oldName`, `newName` |
| `LIST_ARCHIVE` | `{{actorLastName}}{{actorFirstName}}님이 {{listName}} 리스트를 보관했습니다.` | `{{actorFirstName}} {{actorLastName}} archived the list {{listName}}.` | `actorLastName`, `actorFirstName`, `listName` |
| `LIST_MOVE` | `{{actorLastName}}{{actorFirstName}}님이 {{listName}} 리스트를 옮겼습니다.` | `{{actorFirstName}} {{actorLastName}} moved the list {{listName}}.` | `actorLastName`, `actorFirstName`, `listName` |
| `LIST_CHANGE_COLOR` | `{{actorLastName}}{{actorFirstName}}님이 {{listName}} 리스트의 색상을 변경했습니다.` | `{{actorFirstName}} {{actorLastName}} changed the color of the list {{listName}}.` | `actorLastName`, `actorFirstName`, `listName` |
| `LIST_UNARCHIVE` | `{{actorLastName}}{{actorFirstName}}님이 {{listName}} 리스트를 보관에서 복원했습니다.` | `{{actorFirstName}} {{actorLastName}} unarchived the list {{listName}}.` | `actorLastName`, `actorFirstName`, `listName` |
| `LIST_DELETE` | `{{actorLastName}}{{actorFirstName}}님이 {{listName}} 리스트와 {{cardCount}}개의 카드를 삭제했습니다.` | `{{actorFirstName}} {{actorLastName}} deleted the list {{listName}} and {{cardCount}} cards.` | `actorLastName`, `actorFirstName`, `listName`, `cardCount` |

### 보드 관련 활동

| 활동 타입 | 한국어 (ko) | 영어 (en) | 필요 변수 |
|-----------|-------------|-----------|-----------|
| `BOARD_CREATE` | `{{actorLastName}}{{actorFirstName}}님이 {{boardName}} 보드를 생성했습니다.` | `{{actorFirstName}} {{actorLastName}} created the board {{boardName}}.` | `actorLastName`, `actorFirstName`, `boardName` |
| `BOARD_RENAME` | `{{actorLastName}}{{actorFirstName}}님이 보드의 이름을 {{oldName}}에서 {{newName}}(으)로 변경했습니다.` | `{{actorFirstName}} {{actorLastName}} renamed the board from {{oldName}} to {{newName}}.` | `actorLastName`, `actorFirstName`, `oldName`, `newName` |
| `BOARD_ARCHIVE` | `{{actorLastName}}{{actorFirstName}}님이 {{boardName}} 보드를 보관했습니다.` | `{{actorFirstName}} {{actorLastName}} archived the board {{boardName}}.` | `actorLastName`, `actorFirstName`, `boardName` |
| `BOARD_MOVE` | `{{actorLastName}}{{actorFirstName}}님이 {{boardName}} 보드를 옮겼습니다.` | `{{actorFirstName}} {{actorLastName}} moved the board {{boardName}}.` | `actorLastName`, `actorFirstName`, `boardName` |
| `BOARD_DELETE` | `{{actorLastName}}{{actorFirstName}}님이 {{boardName}} 보드와 {{listCount}}개의 리스트, {{cardCount}}개의 카드를 삭제했습니다.` | `{{actorFirstName}} {{actorLastName}} deleted the board {{boardName}} and {{listCount}} lists, {{cardCount}} cards.` | `actorLastName`, `actorFirstName`, `boardName`, `listCount`, `cardCount` |
| `BOARD_UPDATE_DESCRIPTION` | `{{actorLastName}}{{actorFirstName}}님이 {{boardName}} 보드의 설명을 수정했습니다.` | `{{actorFirstName}} {{actorLastName}} updated the description of the board {{boardName}}.` | `actorLastName`, `actorFirstName`, `boardName` |
| `BOARD_ARCHIVE` | `{{actorLastName}}{{actorFirstName}}님이 {{boardName}} 보드를 아카이브했습니다.` | `{{actorFirstName}} {{actorLastName}} archived the board {{boardName}}.` | `actorLastName`, `actorFirstName`, `boardName` |
| `BOARD_UNARCHIVE` | `{{actorLastName}}{{actorFirstName}}님이 {{boardName}} 보드를 아카이브에서 복원했습니다.` | `{{actorFirstName}} {{actorLastName}} unarchived the board {{boardName}}.` | `actorLastName`, `actorFirstName`, `boardName` |
| `BOARD_ADD_MEMBER` | `{{actorLastName}}{{actorFirstName}}님이 {{memberLastName}}{{memberFirstName}}님을 보드에 초대했습니다.` | `{{actorFirstName}} {{actorLastName}} invited {{memberFirstName}} {{memberLastName}} to the board.` | `actorLastName`, `actorFirstName`, `memberLastName`, `memberFirstName` |
| `BOARD_REMOVE_MEMBER` | `{{actorLastName}}{{actorFirstName}}님이 보드에서 {{memberLastName}}{{memberFirstName}}님을 제외했습니다.` | `{{actorFirstName}} {{actorLastName}} removed {{memberFirstName}} {{memberLastName}} from the board.` | `actorLastName`, `actorFirstName`, `memberLastName`, `memberFirstName` |

### 사용자 관련 활동

| 활동 타입 | 한국어 (ko) | 영어 (en) | 필요 변수 |
|-----------|-------------|-----------|-----------|
| `USER_UPDATE_PROFILE` | `{{actorLastName}}{{actorFirstName}}님이 프로필 정보를 수정했습니다.` | `{{actorFirstName}} {{actorLastName}} updated their profile.` | `actorLastName`, `actorFirstName` |
| `USER_CHANGE_LANGUAGE` | `{{actorLastName}}{{actorFirstName}}님이 언어 설정을 변경했습니다.` | `{{actorFirstName}} {{actorLastName}} changed their language setting.` | `actorLastName`, `actorFirstName` |
| `USER_CHANGE_PASSWORD` | `{{actorLastName}}{{actorFirstName}}님이 비밀번호를 변경했습니다.` | `{{actorFirstName}} {{actorLastName}} changed their password.` | `actorLastName`, `actorFirstName` |
| `USER_DELETE_ACCOUNT` | `{{actorLastName}}{{actorFirstName}}님이 계정을 삭제했습니다. ({{boardCount}}개 보드, {{listCount}}개 리스트, {{cardCount}}개 카드 삭제)` | `{{actorFirstName}} {{actorLastName}} deleted their account. ({{boardCount}} boards, {{listCount}} lists, {{cardCount}} cards deleted)` | `actorLastName`, `actorFirstName`, `boardCount`, `listCount`, `cardCount` |

### 사용 방법

1. **프론트엔드 i18n 라이브러리 설정**: 위의 메시지 템플릿을 각 언어별 JSON 파일에 저장
2. **변수 치환**: 백엔드에서 전달받은 `payload` 데이터를 템플릿의 변수에 치환
3. **언어별 렌더링**: 사용자의 언어 설정에 따라 적절한 메시지 템플릿 선택

**예시**:
```javascript
// 한국어 메시지 렌더링
const message = i18n.t('activity.CARD_CREATE', {
  actorLastName: '홍',
  actorFirstName: '길동',
  listName: 'To Do',
  cardTitle: '새 기능 기획'
});
// 결과: "홍길동님이 To Do 리스트에 새 기능 기획 카드를 추가했습니다."
```

