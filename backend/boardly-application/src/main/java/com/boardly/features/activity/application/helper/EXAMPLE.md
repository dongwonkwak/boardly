# 사용 예제


 ## 사용 예제 1: MVP 핵심 기능 (편의 메서드)
 
```java
class CardCreateServiceExample {
    private final ActivityHelper activityHelper;
    
    public void createCard() {
        // 간단하고 명확한 API
        activityHelper.logCardCreate(userId, listName, cardTitle, boardId, listId, cardId);
    }
}
```


 ## 사용 예제 2: 확장 기능 (범용 메서드)
 
 ```java
 class CardUpdateServiceExample {
    private final ActivityHelper activityHelper;
    
    public void updateCardDescription() {
        // 범용 메서드로 새로운 활동 타입 쉽게 추가
        var payload = Map.<String, Object>of(
                "cardTitle", cardTitle,
                "cardId", cardId.getId(),
                "oldDescription", oldDescription,
                "newDescription", newDescription
        );
        
        activityHelper.logCardActivity(
                ActivityType.CARD_UPDATE_DESCRIPTION,
                userId, payload, boardId, listId, cardId
        );
    }
}
 ```


## 사용 예제 3: 복잡한 활동 (범용 메서드)
 
```java
class BoardDeleteServiceExample {
    private final ActivityHelper activityHelper;
    
    public void deleteBoard() {
        // 복잡한 payload도 유연하게 처리
        var payload = Map.<String, Object>of(
                "boardName", boardName,
                "boardId", boardId.getId(),
                "listCount", listCount,
                "cardCount", cardCount
        );
        
        // 중요한 활동이므로 동기 처리
        activityHelper.logActivitySync(
                ActivityType.BOARD_DELETE,
                userId, payload, boardId, null, null
        );
    }
}
```