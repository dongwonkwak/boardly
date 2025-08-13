# BoardList Policy Structure

보드 리스트 기능의 도메인 정책 구조입니다.

## 개요

이 패키지는 보드 리스트와 관련된 모든 비즈니스 규칙을 정의하고 관리합니다. 
Card feature의 정책 구조를 참고하여 일관성 있는 설계를 적용했습니다.

## 정책 구성

### 1. BoardListPolicyConfig (인터페이스)
- 정책 설정값을 정의하는 인터페이스
- Infrastructure 레이어에서 구현체를 제공
- 기본값들을 상수로 정의

### 2. BoardListCreationPolicy (생성 정책)
- 보드 리스트 생성과 관련된 비즈니스 규칙
- 보드당 리스트 개수 제한 검증
- 생성 가능 여부 확인
- 상태 정보 제공 (정상, 권장 초과, 경고, 제한 도달)

### 3. BoardListMovePolicy (이동 정책)
- 보드 리스트 이동과 관련된 비즈니스 규칙
- 위치 유효성 검증
- 이동 가능 여부 확인

## 설정

application.yml에서 정책 설정을 관리할 수 있습니다:

```yaml
boardly:
  boardlist:
    policy:
      max-lists-per-board: 20
      recommended-lists-per-board: 10
      warning-threshold: 15
      max-title-length: 100
```

## 사용법

### 새로운 리스트 생성 시
```java
@Autowired
private BoardListCreationPolicy creationPolicy;

public Either<Failure, Void> createBoardList(BoardId boardId) {
    return creationPolicy.canCreateBoardList(boardId);
}
```

### 리스트 이동 시
```java
@Autowired
private BoardListMovePolicy movePolicy;

public Either<Failure, Void> moveBoardList(BoardList boardList, int newPosition) {
    return movePolicy.canMoveWithinSameBoard(boardList, newPosition);
}
```

## 상태 관리

BoardListCreationPolicy.ListCountStatus를 통해 리스트 개수 상태를 관리합니다:

- **NORMAL**: 정상 범위 (권장 개수 이하)
- **ABOVE_RECOMMENDED**: 권장 개수 초과
- **WARNING**: 경고 임계값 초과
- **LIMIT_REACHED**: 최대 개수 도달

## 장점

1. **설정 기반**: 외부 설정으로 정책값 관리 가능
2. **타입 안전성**: Either 타입을 통한 명확한 성공/실패 처리
3. **확장성**: 새로운 정책 추가가 용이
4. **테스트 용이성**: 각 정책을 독립적으로 테스트 가능
5. **일관성**: Card feature와 동일한 패턴 적용 