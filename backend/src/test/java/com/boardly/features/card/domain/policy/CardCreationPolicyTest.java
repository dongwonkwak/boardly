package com.boardly.features.card.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardCreationPolicy")
class CardCreationPolicyTest {

  @Mock
  private CardRepository cardRepository;

  @Mock
  private CardPolicyConfig policyConfig;

  private CardCreationPolicy cardCreationPolicy;

  @BeforeEach
  void setUp() {
    cardCreationPolicy = new CardCreationPolicy(cardRepository, policyConfig);
  }

  @Nested
  @DisplayName("canCreateCard")
  class CanCreateCard {

    @Test
    @DisplayName("카드 생성이 가능한 경우 성공을 반환한다")
    void shouldReturnSuccessWhenCreationIsPossible() {
      // given
      ListId listId = new ListId();
      when(cardRepository.countByListId(listId)).thenReturn(50L);
      when(policyConfig.getMaxCardsPerList()).thenReturn(100);

      // when
      Either<Failure, Void> result = cardCreationPolicy.canCreateCard(listId);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("리스트의 카드 개수가 최대 제한에 도달한 경우 실패를 반환한다")
    void shouldReturnFailureWhenCardLimitReached() {
      // given
      ListId listId = new ListId();
      when(cardRepository.countByListId(listId)).thenReturn(100L);
      when(policyConfig.getMaxCardsPerList()).thenReturn(100);

      // when
      Either<Failure, Void> result = cardCreationPolicy.canCreateCard(listId);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("접근이 거부되었습니다.");
    }

    @Test
    @DisplayName("리스트의 카드 개수가 최대 제한을 초과한 경우 실패를 반환한다")
    void shouldReturnFailureWhenCardLimitExceeded() {
      // given
      ListId listId = new ListId();
      when(cardRepository.countByListId(listId)).thenReturn(101L);
      when(policyConfig.getMaxCardsPerList()).thenReturn(100);

      // when
      Either<Failure, Void> result = cardCreationPolicy.canCreateCard(listId);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("접근이 거부되었습니다.");
    }

    @Test
    @DisplayName("설정된 최대 카드 개수가 다른 경우 해당 값으로 검증한다")
    void shouldUseConfiguredMaxCardCount() {
      // given
      ListId listId = new ListId();
      when(cardRepository.countByListId(listId)).thenReturn(50L);
      when(policyConfig.getMaxCardsPerList()).thenReturn(50);

      // when
      Either<Failure, Void> result = cardCreationPolicy.canCreateCard(listId);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("접근이 거부되었습니다.");
    }
  }

  @Nested
  @DisplayName("getMaxCardsPerList")
  class GetMaxCardsPerList {

    @Test
    @DisplayName("설정된 최대 카드 개수를 반환한다")
    void shouldReturnConfiguredMaxCardsPerList() {
      // given
      when(policyConfig.getMaxCardsPerList()).thenReturn(75);

      // when
      int maxCards = cardCreationPolicy.getMaxCardsPerList();

      // then
      assertThat(maxCards).isEqualTo(75);
    }
  }

  @Nested
  @DisplayName("getAvailableCardSlots")
  class GetAvailableCardSlots {

    @Test
    @DisplayName("사용 가능한 카드 슬롯 개수를 반환한다")
    void shouldReturnAvailableCardSlots() {
      // given
      ListId listId = new ListId();
      when(cardRepository.countByListId(listId)).thenReturn(30L);
      when(policyConfig.getMaxCardsPerList()).thenReturn(100);

      // when
      long availableSlots = cardCreationPolicy.getAvailableCardSlots(listId);

      // then
      assertThat(availableSlots).isEqualTo(70);
    }

    @Test
    @DisplayName("카드 개수가 최대 제한에 도달한 경우 0을 반환한다")
    void shouldReturnZeroWhenCardLimitReached() {
      // given
      ListId listId = new ListId();
      when(cardRepository.countByListId(listId)).thenReturn(100L);
      when(policyConfig.getMaxCardsPerList()).thenReturn(100);

      // when
      long availableSlots = cardCreationPolicy.getAvailableCardSlots(listId);

      // then
      assertThat(availableSlots).isEqualTo(0);
    }

    @Test
    @DisplayName("카드 개수가 최대 제한을 초과한 경우 0을 반환한다")
    void shouldReturnZeroWhenCardLimitExceeded() {
      // given
      ListId listId = new ListId();
      when(cardRepository.countByListId(listId)).thenReturn(101L);
      when(policyConfig.getMaxCardsPerList()).thenReturn(100);

      // when
      long availableSlots = cardCreationPolicy.getAvailableCardSlots(listId);

      // then
      assertThat(availableSlots).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("getDefaultMaxCardsPerList")
  class GetDefaultMaxCardsPerList {

    @Test
    @DisplayName("기본 최대 카드 개수를 반환한다")
    void shouldReturnDefaultMaxCardsPerList() {
      // when
      int defaultMaxCards = CardCreationPolicy.getDefaultMaxCardsPerList();

      // then
      assertThat(defaultMaxCards).isEqualTo(100);
    }
  }

  @Nested
  @DisplayName("경계값 테스트")
  class BoundaryTests {

    @Test
    @DisplayName("카드 개수가 99개일 때 생성이 가능하다")
    void shouldAllowCreationWhenCardCountIs99() {
      // given
      ListId listId = new ListId();
      when(cardRepository.countByListId(listId)).thenReturn(99L);
      when(policyConfig.getMaxCardsPerList()).thenReturn(100);

      // when
      Either<Failure, Void> result = cardCreationPolicy.canCreateCard(listId);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("카드 개수가 100개일 때 생성이 불가능하다")
    void shouldNotAllowCreationWhenCardCountIs100() {
      // given
      ListId listId = new ListId();
      when(cardRepository.countByListId(listId)).thenReturn(100L);
      when(policyConfig.getMaxCardsPerList()).thenReturn(100);

      // when
      Either<Failure, Void> result = cardCreationPolicy.canCreateCard(listId);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("접근이 거부되었습니다.");
    }
  }
}