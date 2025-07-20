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
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardClonePolicy")
class CardClonePolicyTest {

  @Mock
  private CardRepository cardRepository;

  private CardClonePolicy cardClonePolicy;

  @BeforeEach
  void setUp() {
    cardClonePolicy = new CardClonePolicy(cardRepository);
  }

  @Nested
  @DisplayName("canCloneWithinSameList")
  class CanCloneWithinSameList {

    @Test
    @DisplayName("같은 리스트 내 카드 복제가 가능한 경우 성공을 반환한다")
    void shouldReturnSuccessWhenCloneIsPossible() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId);
      when(cardRepository.countByListId(listId)).thenReturn(50L);

      // when
      Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(card);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("리스트의 카드 개수가 최대 제한에 도달한 경우 실패를 반환한다")
    void shouldReturnFailureWhenCardLimitReached() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId);
      when(cardRepository.countByListId(listId)).thenReturn(100L);

      // when
      Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(card);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");
    }

    @Test
    @DisplayName("리스트의 카드 개수가 최대 제한을 초과한 경우 실패를 반환한다")
    void shouldReturnFailureWhenCardLimitExceeded() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId);
      when(cardRepository.countByListId(listId)).thenReturn(101L);

      // when
      Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(card);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");
    }
  }

  @Nested
  @DisplayName("canCloneToAnotherList")
  class CanCloneToAnotherList {

    @Test
    @DisplayName("다른 리스트로 카드 복제가 가능한 경우 성공을 반환한다")
    void shouldReturnSuccessWhenCloneToAnotherListIsPossible() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId);
      when(cardRepository.countByListId(targetListId)).thenReturn(50L);

      // when
      Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(card, targetListId);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("대상 리스트의 카드 개수가 최대 제한에 도달한 경우 실패를 반환한다")
    void shouldReturnFailureWhenTargetListCardLimitReached() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId);
      when(cardRepository.countByListId(targetListId)).thenReturn(100L);

      // when
      Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(card, targetListId);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");
    }

    @Test
    @DisplayName("대상 리스트의 카드 개수가 최대 제한을 초과한 경우 실패를 반환한다")
    void shouldReturnFailureWhenTargetListCardLimitExceeded() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId);
      when(cardRepository.countByListId(targetListId)).thenReturn(101L);

      // when
      Either<Failure, Void> result = cardClonePolicy.canCloneToAnotherList(card, targetListId);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");
    }
  }

  @Nested
  @DisplayName("getMaxCardsPerList")
  class GetMaxCardsPerList {

    @Test
    @DisplayName("최대 카드 개수를 반환한다")
    void shouldReturnMaxCardsPerList() {
      // when
      int maxCards = cardClonePolicy.getMaxCardsPerList();

      // then
      assertThat(maxCards).isEqualTo(100);
    }
  }

  @Nested
  @DisplayName("경계값 테스트")
  class BoundaryTests {

    @Test
    @DisplayName("카드 개수가 99개일 때 복제가 가능하다")
    void shouldAllowCloneWhenCardCountIs99() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId);
      when(cardRepository.countByListId(listId)).thenReturn(99L);

      // when
      Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(card);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("카드 개수가 100개일 때 복제가 불가능하다")
    void shouldNotAllowCloneWhenCardCountIs100() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId);
      when(cardRepository.countByListId(listId)).thenReturn(100L);

      // when
      Either<Failure, Void> result = cardClonePolicy.canCloneWithinSameList(card);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");
    }
  }

  private Card createCard(String title, ListId listId) {
    return Card.builder()
        .cardId(new CardId())
        .title(title)
        .description("테스트 설명")
        .position(0)
        .listId(listId)
        .build();
  }
}