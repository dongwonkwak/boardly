package com.boardly.features.card.domain.policy;

import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardMovePolicyTest {

  private CardMovePolicy cardMovePolicy;

  @Mock
  private CardRepository cardRepository;

  private Card testCard;
  private ListId testListId;
  private ListId targetListId;

  @BeforeEach
  void setUp() {
    cardMovePolicy = new CardMovePolicy(cardRepository);
    testListId = new ListId();
    targetListId = new ListId();
    testCard = Card.create("테스트 카드", "테스트 설명", 0, testListId);
  }

  // ==================== 파라미터화 테스트 데이터 제공 메서드들 ====================

  private static Stream<Arguments> positionValidationTestData() {
    return Stream.of(
        // (위치, 예상 결과)
        Arguments.of(0, true), // 첫 번째 위치, 이동 가능
        Arguments.of(5, true), // 중간 위치, 이동 가능
        Arguments.of(10, true), // 마지막 위치, 이동 가능
        Arguments.of(-1, false), // 음수 위치, 이동 불가
        Arguments.of(-5, false), // 큰 음수 위치, 이동 불가
        Arguments.of(-100, false) // 매우 큰 음수 위치, 이동 불가
    );
  }

  private static Stream<Arguments> cardCountLimitTestData() {
    return Stream.of(
        // (현재 카드 수, 예상 결과)
        Arguments.of(0L, true), // 빈 리스트, 이동 가능
        Arguments.of(50L, true), // 절반, 이동 가능
        Arguments.of(99L, true), // 최대-1, 이동 가능
        Arguments.of(100L, false), // 최대, 이동 불가
        Arguments.of(101L, false), // 최대+1, 이동 불가
        Arguments.of(150L, false) // 많이 초과, 이동 불가
    );
  }

  private static Stream<Arguments> positionRangeTestData() {
    return Stream.of(
        // (현재 카드 수, 새로운 위치, 예상 결과)
        Arguments.of(0L, 0, true), // 빈 리스트, 첫 번째 위치
        Arguments.of(5L, 0, true), // 5개 카드, 첫 번째 위치
        Arguments.of(5L, 3, true), // 5개 카드, 중간 위치
        Arguments.of(5L, 5, true), // 5개 카드, 마지막 위치
        Arguments.of(5L, 6, false), // 5개 카드, 범위 초과 위치
        Arguments.of(10L, 10, true), // 10개 카드, 마지막 위치
        Arguments.of(10L, 11, false) // 10개 카드, 범위 초과 위치
    );
  }

  // ==================== 같은 리스트 내 이동 테스트 ====================

  @Test
  @DisplayName("같은 리스트 내 카드 이동이 가능한 경우 성공을 반환해야 한다")
  void canMoveWithinSameList_whenPossible_shouldReturnSuccess() {
    // given
    int newPosition = 5;
    long cardCount = 10L;
    when(cardRepository.countByListId(testListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(testCard, newPosition);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("같은 리스트 내 카드 이동 시 음수 위치인 경우 실패를 반환해야 한다")
  void canMoveWithinSameList_withNegativePosition_shouldReturnFailure() {
    // given
    int newPosition = -1;

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(testCard, newPosition);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("POSITION_INVALID");

    verifyNoInteractions(cardRepository);
  }

  @Test
  @DisplayName("같은 리스트 내 카드 이동 시 위치 범위를 초과한 경우 실패를 반환해야 한다")
  void canMoveWithinSameList_withOutOfRangePosition_shouldReturnFailure() {
    // given
    int newPosition = 11;
    long cardCount = 10L;
    when(cardRepository.countByListId(testListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(testCard, newPosition);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("POSITION_OUT_OF_RANGE");

    verify(cardRepository).countByListId(testListId);
  }

  // ==================== 다른 리스트로 이동 테스트 ====================

  @Test
  @DisplayName("다른 리스트로 카드 이동이 가능한 경우 성공을 반환해야 한다")
  void canMoveToAnotherList_whenPossible_shouldReturnSuccess() {
    // given
    int newPosition = 5;
    long cardCount = 10L;
    when(cardRepository.countByListId(targetListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, newPosition);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    // 다른 리스트로 이동 시 checkTargetListCardLimit과 validatePositionRange에서 각각 호출
    verify(cardRepository, times(2)).countByListId(targetListId);
  }

  @Test
  @DisplayName("다른 리스트로 카드 이동 시 음수 위치인 경우 실패를 반환해야 한다")
  void canMoveToAnotherList_withNegativePosition_shouldReturnFailure() {
    // given
    int newPosition = -1;

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, newPosition);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("POSITION_INVALID");

    verifyNoInteractions(cardRepository);
  }

  @Test
  @DisplayName("다른 리스트로 카드 이동 시 카드 개수 제한에 도달한 경우 실패를 반환해야 한다")
  void canMoveToAnotherList_whenLimitReached_shouldReturnFailure() {
    // given
    int newPosition = 5;
    long cardCount = 100L;
    when(cardRepository.countByListId(targetListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, newPosition);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");

    // checkTargetListCardLimit에서만 호출 (validatePositionRange까지 도달하지 않음)
    verify(cardRepository, times(1)).countByListId(targetListId);
  }

  @Test
  @DisplayName("다른 리스트로 카드 이동 시 위치 범위를 초과한 경우 실패를 반환해야 한다")
  void canMoveToAnotherList_withOutOfRangePosition_shouldReturnFailure() {
    // given
    int newPosition = 11;
    long cardCount = 10L;
    when(cardRepository.countByListId(targetListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, newPosition);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("POSITION_OUT_OF_RANGE");

    // checkTargetListCardLimit과 validatePositionRange에서 각각 호출
    verify(cardRepository, times(2)).countByListId(targetListId);
  }

  // ==================== 파라미터화 테스트 ====================

  @ParameterizedTest
  @DisplayName("같은 리스트 내 이동 시 다양한 위치에 따른 이동 가능 여부를 검증해야 한다")
  @MethodSource("positionValidationTestData")
  void canMoveWithinSameList_withVariousPositions_shouldReturnExpectedResult(int position, boolean expectedSuccess) {
    // given
    long cardCount = 10L;
    if (position >= 0) {
      when(cardRepository.countByListId(testListId)).thenReturn(cardCount);
    }

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(testCard, position);

    // then
    if (expectedSuccess && position <= cardCount) {
      assertThat(result.isRight()).isTrue();
    } else if (!expectedSuccess) {
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
    } else {
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
    }

    if (position >= 0) {
      verify(cardRepository).countByListId(testListId);
    } else {
      verifyNoInteractions(cardRepository);
    }
  }

  @ParameterizedTest
  @DisplayName("다른 리스트로 이동 시 다양한 위치에 따른 이동 가능 여부를 검증해야 한다")
  @MethodSource("positionValidationTestData")
  void canMoveToAnotherList_withVariousPositions_shouldReturnExpectedResult(int position, boolean expectedSuccess) {
    // given
    long cardCount = 10L;
    if (position >= 0) {
      when(cardRepository.countByListId(targetListId)).thenReturn(cardCount);
    }

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, position);

    // then
    if (expectedSuccess && position <= cardCount) {
      assertThat(result.isRight()).isTrue();
    } else if (!expectedSuccess) {
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
    } else {
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
    }

    if (position >= 0) {
      // 다른 리스트로 이동 시 checkTargetListCardLimit과 validatePositionRange에서 각각 호출
      verify(cardRepository, times(2)).countByListId(targetListId);
    } else {
      verifyNoInteractions(cardRepository);
    }
  }

  @ParameterizedTest
  @DisplayName("다른 리스트로 이동 시 다양한 카드 개수에 따른 이동 가능 여부를 검증해야 한다")
  @MethodSource("cardCountLimitTestData")
  void canMoveToAnotherList_withVariousCounts_shouldReturnExpectedResult(long currentCount, boolean expectedSuccess) {
    // given
    int newPosition = 5;
    when(cardRepository.countByListId(targetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, newPosition);

    // then
    if (expectedSuccess && newPosition <= currentCount) {
      assertThat(result.isRight()).isTrue();
    } else {
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
    }

    // 카드 개수 제한에 도달한 경우 checkTargetListCardLimit에서만 호출
    // 그렇지 않은 경우 checkTargetListCardLimit과 validatePositionRange에서 각각 호출
    if (currentCount >= 100) {
      verify(cardRepository, times(1)).countByListId(targetListId);
    } else {
      verify(cardRepository, times(2)).countByListId(targetListId);
    }
  }

  @ParameterizedTest
  @DisplayName("다양한 카드 개수와 위치에 따른 위치 범위 검증을 해야 한다")
  @MethodSource("positionRangeTestData")
  void canMoveWithinSameList_withVariousRanges_shouldReturnExpectedResult(long currentCount, int newPosition,
      boolean expectedSuccess) {
    // given
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(testCard, newPosition);

    // then
    if (expectedSuccess) {
      assertThat(result.isRight()).isTrue();
    } else {
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
    }

    verify(cardRepository).countByListId(testListId);
  }

  // ==================== 경계값 테스트 ====================

  @Test
  @DisplayName("같은 리스트 내 이동 시 위치 0으로 이동이 가능해야 한다")
  void canMoveWithinSameList_withPositionZero_shouldReturnSuccess() {
    // given
    int newPosition = 0;
    long cardCount = 10L;
    when(cardRepository.countByListId(testListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(testCard, newPosition);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("같은 리스트 내 이동 시 마지막 위치로 이동이 가능해야 한다")
  void canMoveWithinSameList_withLastPosition_shouldReturnSuccess() {
    // given
    long cardCount = 10L;
    int newPosition = (int) cardCount;
    when(cardRepository.countByListId(testListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(testCard, newPosition);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("다른 리스트로 이동 시 위치 0으로 이동이 가능해야 한다")
  void canMoveToAnotherList_withPositionZero_shouldReturnSuccess() {
    // given
    int newPosition = 0;
    long cardCount = 10L;
    when(cardRepository.countByListId(targetListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, newPosition);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    // 다른 리스트로 이동 시 checkTargetListCardLimit과 validatePositionRange에서 각각 호출
    verify(cardRepository, times(2)).countByListId(targetListId);
  }

  @Test
  @DisplayName("다른 리스트로 이동 시 마지막 위치로 이동이 가능해야 한다")
  void canMoveToAnotherList_withLastPosition_shouldReturnSuccess() {
    // given
    long cardCount = 10L;
    int newPosition = (int) cardCount;
    when(cardRepository.countByListId(targetListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, newPosition);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    // 다른 리스트로 이동 시 checkTargetListCardLimit과 validatePositionRange에서 각각 호출
    verify(cardRepository, times(2)).countByListId(targetListId);
  }

  // ==================== 특수 케이스 테스트 ====================

  @Test
  @DisplayName("빈 리스트로 이동 시 위치 0으로만 이동이 가능해야 한다")
  void canMoveToAnotherList_withEmptyList_shouldOnlyAllowPositionZero() {
    // given
    long cardCount = 0L;
    when(cardRepository.countByListId(targetListId)).thenReturn(cardCount);

    // when & then - 위치 0은 성공
    Either<Failure, Void> resultZero = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, 0);
    assertThat(resultZero.isRight()).isTrue();

    // 위치 1은 실패
    Either<Failure, Void> resultOne = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, 1);
    assertThat(resultOne.isLeft()).isTrue();
    assertThat(resultOne.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    // 각각 checkTargetListCardLimit과 validatePositionRange에서 호출
    verify(cardRepository, times(4)).countByListId(targetListId);
  }

  @Test
  @DisplayName("빈 리스트 내에서 이동 시 위치 0으로만 이동이 가능해야 한다")
  void canMoveWithinSameList_withEmptyList_shouldOnlyAllowPositionZero() {
    // given
    Card emptyListCard = Card.create("빈 리스트 카드", "설명", 0, testListId);
    long cardCount = 0L;
    when(cardRepository.countByListId(testListId)).thenReturn(cardCount);

    // when & then - 위치 0은 성공
    Either<Failure, Void> resultZero = cardMovePolicy.canMoveWithinSameList(emptyListCard, 0);
    assertThat(resultZero.isRight()).isTrue();

    // 위치 1은 실패
    Either<Failure, Void> resultOne = cardMovePolicy.canMoveWithinSameList(emptyListCard, 1);
    assertThat(resultOne.isLeft()).isTrue();
    assertThat(resultOne.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    verify(cardRepository, times(2)).countByListId(testListId);
  }

  @Test
  @DisplayName("다른 카드로 같은 리스트 내 이동 시 해당 리스트의 카드 개수를 확인해야 한다")
  void canMoveWithinSameList_withDifferentCard_shouldCheckCorrectList() {
    // given
    ListId differentListId = new ListId();
    Card differentCard = Card.create("다른 카드", "설명", 0, differentListId);
    long cardCount = 5L;
    int newPosition = 3;

    when(cardRepository.countByListId(differentListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(differentCard, newPosition);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(differentListId);
  }

  @Test
  @DisplayName("다른 대상 리스트로 이동 시 해당 리스트의 카드 개수를 확인해야 한다")
  void canMoveToAnotherList_withDifferentTargetList_shouldCheckCorrectList() {
    // given
    ListId differentTargetListId = new ListId();
    long cardCount = 5L;
    int newPosition = 3;

    when(cardRepository.countByListId(differentTargetListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, differentTargetListId, newPosition);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    // 다른 리스트로 이동 시 checkTargetListCardLimit과 validatePositionRange에서 각각 호출
    verify(cardRepository, times(2)).countByListId(differentTargetListId);
  }

  @Test
  @DisplayName("음수 카드 개수가 있는 경우에도 정상적으로 처리해야 한다")
  void canMoveWithinSameList_withNegativeCount_shouldHandleGracefully() {
    // given
    long cardCount = -5L; // 비정상적인 상황이지만 정책은 처리해야 함
    int newPosition = 0;
    when(cardRepository.countByListId(testListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(testCard, newPosition);

    // then
    // position(0) > cardCount(-5)는 true이므로 실패해야 함
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("음수 카드 개수가 있는 대상 리스트로도 이동이 가능해야 한다")
  void canMoveToAnotherList_withNegativeCount_shouldHandleGracefully() {
    // given
    long cardCount = -5L;
    int newPosition = 0;
    when(cardRepository.countByListId(targetListId)).thenReturn(cardCount);

    // when
    Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(testCard, targetListId, newPosition);

    // then
    // position(0) > cardCount(-5)는 true이므로 실패해야 함
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    // checkTargetListCardLimit과 validatePositionRange에서 각각 호출
    verify(cardRepository, times(2)).countByListId(targetListId);
  }
}