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
@DisplayName("CardMovePolicy")
class CardMovePolicyTest {

  @Mock
  private CardRepository cardRepository;

  private CardMovePolicy cardMovePolicy;

  @BeforeEach
  void setUp() {
    cardMovePolicy = new CardMovePolicy(cardRepository);
  }

  @Nested
  @DisplayName("canMoveWithinSameList")
  class CanMoveWithinSameList {

    @Test
    @DisplayName("같은 리스트 내에서 카드 이동이 가능한 경우 성공을 반환한다")
    void shouldReturnSuccessWhenMoveIsPossible() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId, 0);
      when(cardRepository.countByListId(listId)).thenReturn(5L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(card, 3);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("위치가 음수인 경우 실패를 반환한다")
    void shouldReturnFailureWhenPositionIsNegative() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId, 0);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(card, -1);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("POSITION_INVALID");
    }

    @Test
    @DisplayName("위치가 카드 개수를 초과하는 경우 실패를 반환한다")
    void shouldReturnFailureWhenPositionExceedsCardCount() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId, 0);
      when(cardRepository.countByListId(listId)).thenReturn(5L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(card, 6);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("POSITION_OUT_OF_RANGE");
    }

    @Test
    @DisplayName("위치가 카드 개수와 같은 경우 성공을 반환한다")
    void shouldReturnSuccessWhenPositionEqualsCardCount() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId, 0);
      when(cardRepository.countByListId(listId)).thenReturn(5L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(card, 5);

      // then
      assertThat(result.isRight()).isTrue();
    }
  }

  @Nested
  @DisplayName("canMoveToAnotherList")
  class CanMoveToAnotherList {

    @Test
    @DisplayName("다른 리스트로 카드 이동이 가능한 경우 성공을 반환한다")
    void shouldReturnSuccessWhenMoveToAnotherListIsPossible() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId, 0);
      when(cardRepository.countByListId(targetListId)).thenReturn(50L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(card, targetListId, 3);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("위치가 음수인 경우 실패를 반환한다")
    void shouldReturnFailureWhenPositionIsNegativeForAnotherList() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId, 0);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(card, targetListId, -1);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("POSITION_INVALID");
    }

    @Test
    @DisplayName("대상 리스트의 카드 개수가 최대 제한에 도달한 경우 실패를 반환한다")
    void shouldReturnFailureWhenTargetListCardLimitReached() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId, 0);
      when(cardRepository.countByListId(targetListId)).thenReturn(100L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(card, targetListId, 0);

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
      Card card = createCard("테스트 카드", sourceListId, 0);
      when(cardRepository.countByListId(targetListId)).thenReturn(101L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(card, targetListId, 0);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");
    }

    @Test
    @DisplayName("위치가 대상 리스트의 카드 개수를 초과하는 경우 실패를 반환한다")
    void shouldReturnFailureWhenPositionExceedsTargetListCardCount() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId, 0);
      when(cardRepository.countByListId(targetListId)).thenReturn(5L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(card, targetListId, 6);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("POSITION_OUT_OF_RANGE");
    }

    @Test
    @DisplayName("위치가 대상 리스트의 카드 개수와 같은 경우 성공을 반환한다")
    void shouldReturnSuccessWhenPositionEqualsTargetListCardCount() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId, 0);
      when(cardRepository.countByListId(targetListId)).thenReturn(5L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(card, targetListId, 5);

      // then
      assertThat(result.isRight()).isTrue();
    }
  }

  @Nested
  @DisplayName("경계값 테스트")
  class BoundaryTests {

    @Test
    @DisplayName("위치가 0인 경우 성공을 반환한다")
    void shouldAllowPositionZero() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId, 0);
      when(cardRepository.countByListId(listId)).thenReturn(5L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(card, 0);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("위치가 1인 경우 성공을 반환한다")
    void shouldAllowPositionOne() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId, 0);
      when(cardRepository.countByListId(listId)).thenReturn(5L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(card, 1);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("대상 리스트의 카드 개수가 99개일 때 이동이 가능하다")
    void shouldAllowMoveWhenTargetListCardCountIs99() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId, 0);
      when(cardRepository.countByListId(targetListId)).thenReturn(99L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(card, targetListId, 0);

      // then
      assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("대상 리스트의 카드 개수가 100개일 때 이동이 불가능하다")
    void shouldNotAllowMoveWhenTargetListCardCountIs100() {
      // given
      ListId sourceListId = new ListId();
      ListId targetListId = new ListId();
      Card card = createCard("테스트 카드", sourceListId, 0);
      when(cardRepository.countByListId(targetListId)).thenReturn(100L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveToAnotherList(card, targetListId, 0);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");
    }
  }

  @Nested
  @DisplayName("에러 케이스 테스트")
  class ErrorCaseTests {

    @Test
    @DisplayName("위치가 매우 큰 음수인 경우 실패를 반환한다")
    void shouldReturnFailureWhenPositionIsVeryLargeNegative() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId, 0);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(card, -1000);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("POSITION_INVALID");
    }

    @Test
    @DisplayName("위치가 매우 큰 양수인 경우 실패를 반환한다")
    void shouldReturnFailureWhenPositionIsVeryLargePositive() {
      // given
      ListId listId = new ListId();
      Card card = createCard("테스트 카드", listId, 0);
      when(cardRepository.countByListId(listId)).thenReturn(5L);

      // when
      Either<Failure, Void> result = cardMovePolicy.canMoveWithinSameList(card, 1000);

      // then
      assertThat(result.isLeft()).isTrue();
      assertThat(result.getLeft().getMessage()).isEqualTo("POSITION_OUT_OF_RANGE");
    }
  }

  private Card createCard(String title, ListId listId, int position) {
    return Card.builder()
        .cardId(new CardId())
        .title(title)
        .description("테스트 설명")
        .position(position)
        .listId(listId)
        .build();
  }
}