package com.boardly.features.card.domain.policy;

import com.boardly.features.boardlist.domain.model.ListId;
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
class CardCreationPolicyTest {

  private CardCreationPolicy cardCreationPolicy;

  @Mock
  private CardRepository cardRepository;

  @Mock
  private CardPolicyConfig policyConfig;

  private ListId testListId;

  @BeforeEach
  void setUp() {
    cardCreationPolicy = new CardCreationPolicy(cardRepository, policyConfig);
    testListId = new ListId();
  }

  // ==================== 파라미터화 테스트 데이터 제공 메서드들 ====================

  private static Stream<Arguments> cardCountLimitTestData() {
    return Stream.of(
        // (현재 카드 수, 최대 카드 수, 예상 결과)
        Arguments.of(0L, 100, true), // 빈 리스트, 생성 가능
        Arguments.of(50L, 100, true), // 절반, 생성 가능
        Arguments.of(99L, 100, true), // 최대-1, 생성 가능
        Arguments.of(100L, 100, false), // 최대, 생성 불가
        Arguments.of(101L, 100, false), // 최대+1, 생성 불가
        Arguments.of(150L, 100, false) // 많이 초과, 생성 불가
    );
  }

  private static Stream<Arguments> availableSlotsTestData() {
    return Stream.of(
        // (현재 카드 수, 최대 카드 수, 예상 사용 가능 슬롯)
        Arguments.of(0L, 100, 100L),
        Arguments.of(50L, 100, 50L),
        Arguments.of(99L, 100, 1L),
        Arguments.of(100L, 100, 0L),
        Arguments.of(101L, 100, 0L),
        Arguments.of(150L, 100, 0L));
  }

  // ==================== 기본 테스트 ====================

  @Test
  @DisplayName("카드 생성이 가능한 경우 성공을 반환해야 한다")
  void canCreateCard_whenPossible_shouldReturnSuccess() {
    // given
    long currentCount = 50L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    Either<Failure, Void> result = cardCreationPolicy.canCreateCard(testListId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  @Test
  @DisplayName("카드 개수 제한에 도달한 경우 실패를 반환해야 한다")
  void canCreateCard_whenLimitReached_shouldReturnFailure() {
    // given
    long currentCount = 100L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    Either<Failure, Void> result = cardCreationPolicy.canCreateCard(testListId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);

    Failure.ForbiddenFailure failure = (Failure.ForbiddenFailure) result.getLeft();
    assertThat(failure.message()).contains("리스트당 최대 100개의 카드만 생성할 수 있습니다");
    assertThat(failure.message()).contains("현재: 100개");

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  @Test
  @DisplayName("카드 개수 제한을 초과한 경우 실패를 반환해야 한다")
  void canCreateCard_whenLimitExceeded_shouldReturnFailure() {
    // given
    long currentCount = 150L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    Either<Failure, Void> result = cardCreationPolicy.canCreateCard(testListId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);

    Failure.ForbiddenFailure failure = (Failure.ForbiddenFailure) result.getLeft();
    assertThat(failure.message()).contains("리스트당 최대 100개의 카드만 생성할 수 있습니다");
    assertThat(failure.message()).contains("현재: 150개");

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  // ==================== 파라미터화 테스트 ====================

  @ParameterizedTest
  @DisplayName("다양한 카드 개수에 따른 생성 가능 여부를 검증해야 한다")
  @MethodSource("cardCountLimitTestData")
  void canCreateCard_withVariousCounts_shouldReturnExpectedResult(long currentCount, int maxCards,
      boolean expectedSuccess) {
    // given
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    Either<Failure, Void> result = cardCreationPolicy.canCreateCard(testListId);

    // then
    if (expectedSuccess) {
      assertThat(result.isRight()).isTrue();
    } else {
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
    }

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  @ParameterizedTest
  @DisplayName("다양한 카드 개수에 따른 사용 가능한 슬롯 수를 계산해야 한다")
  @MethodSource("availableSlotsTestData")
  void getAvailableCardSlots_withVariousCounts_shouldReturnExpectedSlots(long currentCount, int maxCards,
      long expectedSlots) {
    // given
    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    long availableSlots = cardCreationPolicy.getAvailableCardSlots(testListId);

    // then
    assertThat(availableSlots).isEqualTo(expectedSlots);

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  // ==================== 정책 설정 테스트 ====================

  @Test
  @DisplayName("최대 카드 개수를 반환해야 한다")
  void getMaxCardsPerList_shouldReturnConfiguredValue() {
    // given
    int expectedMaxCards = 150;
    when(policyConfig.getMaxCardsPerList()).thenReturn(expectedMaxCards);

    // when
    int maxCards = cardCreationPolicy.getMaxCardsPerList();

    // then
    assertThat(maxCards).isEqualTo(expectedMaxCards);
    verify(policyConfig).getMaxCardsPerList();
  }

  @Test
  @DisplayName("기본 최대 카드 개수를 반환해야 한다")
  void getDefaultMaxCardsPerList_shouldReturnDefaultValue() {
    // when
    int defaultMaxCards = CardCreationPolicy.getDefaultMaxCardsPerList();

    // then
    assertThat(defaultMaxCards).isEqualTo(CardPolicyConfig.Defaults.MAX_CARDS_PER_LIST);
    assertThat(defaultMaxCards).isEqualTo(100);
  }

  // ==================== 사용 가능한 슬롯 테스트 ====================

  @Test
  @DisplayName("빈 리스트의 사용 가능한 슬롯 수는 최대 개수와 같아야 한다")
  void getAvailableCardSlots_withEmptyList_shouldReturnMaxCount() {
    // given
    long currentCount = 0L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    long availableSlots = cardCreationPolicy.getAvailableCardSlots(testListId);

    // then
    assertThat(availableSlots).isEqualTo(maxCards);

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  @Test
  @DisplayName("최대 개수에 도달한 리스트의 사용 가능한 슬롯 수는 0이어야 한다")
  void getAvailableCardSlots_withFullList_shouldReturnZero() {
    // given
    long currentCount = 100L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    long availableSlots = cardCreationPolicy.getAvailableCardSlots(testListId);

    // then
    assertThat(availableSlots).isEqualTo(0L);

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  @Test
  @DisplayName("최대 개수를 초과한 리스트의 사용 가능한 슬롯 수는 0이어야 한다")
  void getAvailableCardSlots_withOverflowList_shouldReturnZero() {
    // given
    long currentCount = 150L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    long availableSlots = cardCreationPolicy.getAvailableCardSlots(testListId);

    // then
    assertThat(availableSlots).isEqualTo(0L);

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  // ==================== 경계값 테스트 ====================

  @Test
  @DisplayName("최대 개수-1개 카드가 있는 경우 생성이 가능해야 한다")
  void canCreateCard_withMaxMinusOne_shouldReturnSuccess() {
    // given
    long currentCount = 99L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    Either<Failure, Void> result = cardCreationPolicy.canCreateCard(testListId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  @Test
  @DisplayName("최대 개수 카드가 있는 경우 생성이 불가능해야 한다")
  void canCreateCard_withMaxCount_shouldReturnFailure() {
    // given
    long currentCount = 100L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    Either<Failure, Void> result = cardCreationPolicy.canCreateCard(testListId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  // ==================== 특수 케이스 테스트 ====================

  @Test
  @DisplayName("다른 리스트 ID로 검증 시 해당 리스트의 카드 개수를 확인해야 한다")
  void canCreateCard_withDifferentListId_shouldCheckCorrectList() {
    // given
    ListId differentListId = new ListId();
    long currentCount = 50L;
    int maxCards = 100;

    when(cardRepository.countByListId(differentListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    Either<Failure, Void> result = cardCreationPolicy.canCreateCard(differentListId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(differentListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  @Test
  @DisplayName("0개 카드가 있는 리스트에서 사용 가능한 슬롯 수는 최대 개수와 같아야 한다")
  void getAvailableCardSlots_withZeroCards_shouldReturnMaxCount() {
    // given
    long currentCount = 0L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    long availableSlots = cardCreationPolicy.getAvailableCardSlots(testListId);

    // then
    assertThat(availableSlots).isEqualTo(maxCards);

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  @Test
  @DisplayName("음수 카드 개수가 있는 경우에도 정상적으로 처리해야 한다")
  void canCreateCard_withNegativeCount_shouldHandleGracefully() {
    // given
    long currentCount = -5L; // 비정상적인 상황이지만 정책은 처리해야 함
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    Either<Failure, Void> result = cardCreationPolicy.canCreateCard(testListId);

    // then
    assertThat(result.isRight()).isTrue(); // 음수 < 최대값이므로 성공
    assertThat(result.get()).isNull();

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }

  @Test
  @DisplayName("음수 카드 개수가 있는 경우 사용 가능한 슬롯 수는 최대 개수보다 클 수 있다")
  void getAvailableCardSlots_withNegativeCount_shouldReturnIncreasedSlots() {
    // given
    long currentCount = -5L;
    int maxCards = 100;

    when(cardRepository.countByListId(testListId)).thenReturn(currentCount);
    when(policyConfig.getMaxCardsPerList()).thenReturn(maxCards);

    // when
    long availableSlots = cardCreationPolicy.getAvailableCardSlots(testListId);

    // then
    // Math.max(0, 100 - (-5)) = Math.max(0, 105) = 105
    assertThat(availableSlots).isEqualTo(105L);

    verify(cardRepository).countByListId(testListId);
    verify(policyConfig).getMaxCardsPerList();
  }
}