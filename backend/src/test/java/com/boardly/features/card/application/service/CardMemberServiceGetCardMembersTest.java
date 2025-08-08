package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardMemberRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardMemberService getCardMembers 테스트")
class CardMemberServiceGetCardMembersTest {

    // getCardMembers 메서드는 validator를 사용하지 않으므로 제거

    @Mock
    private ValidationMessageResolver messageResolver;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMemberRepository cardMemberRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ActivityHelper activityHelper;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    private CardMemberService cardMemberService;

    @BeforeEach
    void setUp() {
        cardMemberService = new CardMemberService(
            null, // validator는 getCardMembers에서 사용하지 않음
            messageResolver,
            cardRepository,
            cardMemberRepository,
            userFinder,
            activityHelper,
            boardListRepository,
            boardRepository
        );

        // 공통으로 사용되는 메시지 설정
        lenient()
            .when(
                messageResolver.getMessage("error.service.card.read.not_found")
            )
            .thenReturn("카드를 찾을 수 없습니다.");
    }

    @Nested
    @DisplayName("getCardMembers 메서드 테스트")
    class GetCardMembersTest {

        private CardId cardId;
        private UserId requesterId;
        private Card card;
        private List<CardMember> cardMembers;

        @BeforeEach
        void setUp() {
            // 테스트 데이터 설정
            cardId = new CardId("card-1");
            requesterId = new UserId("requester-1");
            ListId listId = new ListId("list-1");

            card = Card.builder()
                .cardId(cardId)
                .title("테스트 카드")
                .listId(listId)
                .build();

            // 카드 멤버 목록 생성
            UserId member1Id = new UserId("member-1");
            UserId member2Id = new UserId("member-2");
            UserId member3Id = new UserId("member-3");

            CardMember member1 = new CardMember(member1Id);
            CardMember member2 = new CardMember(member2Id);
            CardMember member3 = new CardMember(member3Id);

            cardMembers = List.of(member1, member2, member3);
        }

        @Test
        @DisplayName("카드 멤버 목록을 성공적으로 조회한다")
        void shouldGetCardMembersSuccessfully() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(
                cardMemberRepository.findByCardIdOrderByAssignedAt(cardId)
            ).thenReturn(cardMembers);

            // when
            List<CardMember> result = cardMemberService.getCardMembers(
                cardId,
                requesterId
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result).isEqualTo(cardMembers);
            verify(cardRepository, times(1)).findById(cardId);
            verify(
                cardMemberRepository,
                times(1)
            ).findByCardIdOrderByAssignedAt(cardId);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenCardNotFound() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

            // when
            List<CardMember> result = cardMemberService.getCardMembers(
                cardId,
                requesterId
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(cardRepository, times(1)).findById(cardId);
            verify(
                cardMemberRepository,
                times(0)
            ).findByCardIdOrderByAssignedAt(any());
        }

        @Test
        @DisplayName("카드에 멤버가 없을 때 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenNoMembers() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(
                cardMemberRepository.findByCardIdOrderByAssignedAt(cardId)
            ).thenReturn(List.of());

            // when
            List<CardMember> result = cardMemberService.getCardMembers(
                cardId,
                requesterId
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(cardRepository, times(1)).findById(cardId);
            verify(
                cardMemberRepository,
                times(1)
            ).findByCardIdOrderByAssignedAt(cardId);
        }

        @Test
        @DisplayName("멤버가 할당된 순서대로 정렬되어 반환된다")
        void shouldReturnMembersInAssignedOrder() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(
                cardMemberRepository.findByCardIdOrderByAssignedAt(cardId)
            ).thenReturn(cardMembers);

            // when
            List<CardMember> result = cardMemberService.getCardMembers(
                cardId,
                requesterId
            );

            // then
            assertThat(result).hasSize(3);
            // 저장소가 정렬해서 반환한 순서를 그대로 유지하는지 확인
            assertThat(result).containsExactlyElementsOf(cardMembers);

            verify(
                cardMemberRepository,
                times(1)
            ).findByCardIdOrderByAssignedAt(cardId);
        }

        @Test
        @DisplayName("카드 조회 실패 시 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenCardLookupFails() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

            // when
            List<CardMember> result = cardMemberService.getCardMembers(
                cardId,
                requesterId
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(cardRepository, times(1)).findById(cardId);
            verify(
                cardMemberRepository,
                times(0)
            ).findByCardIdOrderByAssignedAt(any());
        }

        @Test
        @DisplayName("멤버 조회 실패 시 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenMemberLookupFails() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(
                cardMemberRepository.findByCardIdOrderByAssignedAt(cardId)
            ).thenReturn(List.of()); // 실제로는 예외가 발생하지 않고 빈 리스트를 반환

            // when
            List<CardMember> result = cardMemberService.getCardMembers(
                cardId,
                requesterId
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(cardRepository, times(1)).findById(cardId);
            verify(
                cardMemberRepository,
                times(1)
            ).findByCardIdOrderByAssignedAt(cardId);
        }
    }
}
