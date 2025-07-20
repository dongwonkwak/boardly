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
class CardClonePolicyTest {

  private CardClonePolicy cardClonePolicy;

  @Mock
  private CardRepository cardRepository;

  private Card testCard;
  private ListId testListId;
  private ListId targetListId;

  @BeforeEach
  void setUp() {
    cardClonePolicy = new CardClonePolicy(cardRepository);
    testListId = new ListId();
    targetListId = new ListId();
    testCard = Card.create("테스트 카드", "테스트 설명", 0, testListId);
  }

  // ==================== 파라미터화 테스트 데이터 제공 메서드들 ====================

  private static Stream<Arguments> cardCountLimitTestData() {
    return Stream.of(
        // (현재 카드 수, 예상 결과)
        Arguments.of(0L, true), // 빈 리스트, 복제 가능
        Arguments.of(50L, true), // 절반, 복제 가능
        Arguments.of(99L, true), // 최대-1, 복제 가능
        Arguments.of(100L, false), // 최대, 복제 불가
        Arguments.of(101L, false), // 최대+1, 복제 불가
        Arguments.of(150L, false) // 많이 초과, 복제 불가
    );
  }

  // ==================== 같은 리스트 내 복제 테스트 ====================

  @Test
  @DisplayName("같은 리스트 내 카드 복제가 가능한 경우 성공을 반환해야 한다")
  void canCloneWithinSameList_whenPossible_shouldReturnSuccess() {
    // given
    long currentCount = 50L;
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(testCard);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("같은 리스트 내 카드 복제 시 카드 개수 제한에 도달한 경우 실패를 반환해야 한다")
  void canCloneWithinSameList_whenLimitReached_shouldReturnFailure() {
    // given
    long currentCount = 100L;
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(testCard);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("같은 리스트 내 카드 복제 시 카드 개수 제한을 초과한 경우 실패를 반환해야 한다")
  void canCloneWithinSameList_whenLimitExceeded_shouldReturnFailure() {
    // given
    long currentCount = 150L;
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(testCard);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");

    verify(cardRepository).countByListId(testListId);
  }

  // ==================== 다른 리스트로 복제 테스트 ====================

  @Test
  @DisplayName("다른 리스트로 카드 복제가 가능한 경우 성공을 반환해야 한다")
  void canCloneToAnotherList_whenPossible_shouldReturnSuccess() {
    // given
    long currentCount = 50L;
    when(cardRepository.countByListId(targetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(testCard, targetListId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(targetListId);
  }

  @Test
  @DisplayName("다른 리스트로 카드 복제 시 카드 개수 제한에 도달한 경우 실패를 반환해야 한다")
  void canCloneToAnotherList_whenLimitReached_shouldReturnFailure() {
    // given
    long currentCount = 100L;
    when(cardRepository.countByListId(targetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(testCard, targetListId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");

    verify(cardRepository).countByListId(targetListId);
  }

  @Test
  @DisplayName("다른 리스트로 카드 복제 시 카드 개수 제한을 초과한 경우 실패를 반환해야 한다")
  void canCloneToAnotherList_whenLimitExceeded_shouldReturnFailure() {
    // given
    long currentCount = 150L;
    when(cardRepository.countByListId(targetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(testCard, targetListId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");

    verify(cardRepository).countByListId(targetListId);
  }

  // ==================== 파라미터화 테스트 ====================

  @ParameterizedTest
  @DisplayName("같은 리스트 내 복제 시 다양한 카드 개수에 따른 복제 가능 여부를 검증해야 한다")
  @MethodSource("cardCountLimitTestData")
  void canCloneWithinSameList_withVariousCounts_shouldReturnExpectedResult(long currentCount, boolean expectedSuccess) {
    // given
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(testCard);

    // then
    if (expectedSuccess) {
      assertThat(result.isRight()).isTrue();
    } else {
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
    }

    verify(cardRepository).countByListId(testListId);
  }

  @ParameterizedTest
  @DisplayName("다른 리스트로 복제 시 다양한 카드 개수에 따른 복제 가능 여부를 검증해야 한다")
  @MethodSource("cardCountLimitTestData")
  void canCloneToAnotherList_withVariousCounts_shouldReturnExpectedResult(long currentCount, boolean expectedSuccess) {
    // given
    when(cardRepository.countByListId(targetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(testCard, targetListId);

    // then
    if (expectedSuccess) {
      assertThat(result.isRight()).isTrue();
    } else {
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
    }

    verify(cardRepository).countByListId(targetListId);
  }

  // ==================== 정책 설정 테스트 ====================

  @Test
  @DisplayName("최대 카드 개수를 반환해야 한다")
  void getMaxCardsPerList_shouldReturnConfiguredValue() {
    // when
    int maxCards = cardClonePolicy.getMaxCardsPerList();

    // then
    assertThat(maxCards).isEqualTo(100);
  }

  // ==================== 경계값 테스트 ====================

  @Test
  @DisplayName("같은 리스트 내 복제 시 최대 개수-1개 카드가 있는 경우 복제가 가능해야 한다")
  void canCloneWithinSameList_withMaxMinusOne_shouldReturnSuccess() {
    // given
    long currentCount = 99L;
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(testCard);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("같은 리스트 내 복제 시 최대 개수 카드가 있는 경우 복제가 불가능해야 한다")
  void canCloneWithinSameList_withMaxCount_shouldReturnFailure() {
    // given
    long currentCount = 100L;
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(testCard);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("다른 리스트로 복제 시 최대 개수-1개 카드가 있는 경우 복제가 가능해야 한다")
  void canCloneToAnotherList_withMaxMinusOne_shouldReturnSuccess() {
    // given
    long currentCount = 99L;
    when(cardRepository.countByListId(targetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(testCard, targetListId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(targetListId);
  }

  @Test
  @DisplayName("다른 리스트로 복제 시 최대 개수 카드가 있는 경우 복제가 불가능해야 한다")
  void canCloneToAnotherList_withMaxCount_shouldReturnFailure() {
    // given
    long currentCount = 100L;
    when(cardRepository.countByListId(targetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(testCard, targetListId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    verify(cardRepository).countByListId(targetListId);
  }

  // ==================== 특수 케이스 테스트 ====================

  @Test
  @DisplayName("다른 카드로 같은 리스트 내 복제 시 해당 리스트의 카드 개수를 확인해야 한다")
  void canCloneWithinSameList_withDifferentCard_shouldCheckCorrectList() {
    // given
    ListId differentListId = new ListId();
    Card differentCard = Card.create("다른 카드", "설명", 0, differentListId);
    long currentCount = 50L;

    when(cardRepository.countByListId(differentListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(differentCard);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(differentListId);
  }

  @Test
  @DisplayName("다른 대상 리스트로 복제 시 해당 리스트의 카드 개수를 확인해야 한다")
  void canCloneToAnotherList_withDifferentTargetList_shouldCheckCorrectList() {
    // given
    ListId differentTargetListId = new ListId();
    long currentCount = 50L;

    when(cardRepository.countByListId(differentTargetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(testCard, differentTargetListId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(differentTargetListId);
  }

  @Test
  @DisplayName("0개 카드가 있는 리스트에서 복제가 가능해야 한다")
  void canCloneWithinSameList_withZeroCards_shouldReturnSuccess() {
    // given
    long currentCount = 0L;
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(testCard);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("0개 카드가 있는 대상 리스트로 복제가 가능해야 한다")
  void canCloneToAnotherList_withZeroCards_shouldReturnSuccess() {
    // given
    long currentCount = 0L;
    when(cardRepository.countByListId(targetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(testCard, targetListId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(targetListId);
  }

  @Test
  @DisplayName("음수 카드 개수가 있는 경우에도 정상적으로 처리해야 한다")
  void canCloneWithinSameList_withNegativeCount_shouldHandleGracefully() {
    // given
    long currentCount = -5L; // 비정상적인 상황이지만 정책은 처리해야 함
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(testCard);

    // then
    assertThat(result.isRight()).isTrue(); // 음수 < 최대값이므로 성공
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
  }

  @Test
  @DisplayName("음수 카드 개수가 있는 대상 리스트로도 복제가 가능해야 한다")
  void canCloneToAnotherList_withNegativeCount_shouldHandleGracefully() {
    // given
    long currentCount = -5L;
    when(cardRepository.countByListId(targetListId)).thenReturn(currentCount);

    // when
    Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(testCard, targetListId);

    // then
    assertThat(result.isRight()).isTrue(); // 음수 < 최대값이므로 성공
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(targetListId);
  }
}