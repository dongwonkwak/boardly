package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.UnassignCardMemberCommand;
import com.boardly.features.card.application.validation.CardMemberValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardMemberRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.application.dto.UserNameDto;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.model.UserProfile;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardMemberService unassignMember 테스트")
class CardMemberServiceUnassignMemberTest {

        @Mock
        private CardMemberValidator validator;

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
                                validator,
                                messageResolver,
                                cardRepository,
                                cardMemberRepository,
                                userFinder,
                                activityHelper,
                                boardListRepository,
                                boardRepository);

                // 공통으로 사용되는 메시지 설정
                lenient().when(messageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력값이 유효하지 않습니다.");
                lenient().when(messageResolver.getMessage("error.service.card.read.not_found"))
                                .thenReturn("카드를 찾을 수 없습니다.");
                lenient().when(messageResolver.getMessage("validation.user.not.found"))
                                .thenReturn("사용자를 찾을 수 없습니다.");
                lenient().when(messageResolver.getMessage("validation.card.member.not.assigned"))
                                .thenReturn("할당되지 않은 멤버입니다.");
                lenient().when(messageResolver.getMessage("error.service.card.read.list_not_found"))
                                .thenReturn("리스트를 찾을 수 없습니다.");
                lenient().when(messageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다.");
        }

        @Nested
        @DisplayName("unassignMember 메서드 테스트")
        class UnassignMemberTest {

                private UnassignCardMemberCommand validCommand;
                private Card card;
                private User memberUser;
                private User requesterUser;
                private BoardList boardList;
                private Board board;
                private UserNameDto memberName;

                @BeforeEach
                void setUp() {
                        // 테스트 데이터 설정
                        CardId cardId = new CardId("card-1");
                        UserId memberId = new UserId("member-1");
                        UserId requesterId = new UserId("requester-1");
                        ListId listId = new ListId("list-1");
                        BoardId boardId = new BoardId("board-1");

                        validCommand = new UnassignCardMemberCommand(cardId, memberId, requesterId);

                        card = Card.builder()
                                        .cardId(cardId)
                                        .title("테스트 카드")
                                        .listId(listId)
                                        .build();

                        memberUser = User.builder()
                                        .userId(memberId)
                                        .userProfile(new UserProfile("김", "멤버"))
                                        .build();

                        requesterUser = User.builder()
                                        .userId(requesterId)
                                        .userProfile(new UserProfile("김", "요청자"))
                                        .build();

                        boardList = BoardList.builder()
                                        .listId(listId)
                                        .boardId(boardId)
                                        .title("테스트 리스트")
                                        .build();

                        board = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .build();

                        memberName = new UserNameDto("김", "멤버");
                }

                @Test
                @DisplayName("유효한 커맨드로 멤버 해제 시 성공한다")
                @SuppressWarnings("unchecked")
                void shouldUnassignMemberSuccessfully() {
                        // given
                        ValidationResult<UnassignCardMemberCommand> validResult = ValidationResult.valid(validCommand);
                        when(validator.validateUnassign(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(card));
                        when(userFinder.findUserOrThrow(validCommand.memberId())).thenReturn(memberUser);
                        when(userFinder.findUserOrThrow(validCommand.requesterId())).thenReturn(requesterUser);
                        when(cardMemberRepository.existsByCardIdAndUserId(validCommand.cardId(),
                                        validCommand.memberId()))
                                        .thenReturn(true);
                        when(cardRepository.save(any(Card.class))).thenReturn(Either.right(card));
                        when(userFinder.findUserNameById(validCommand.memberId())).thenReturn(Optional.of(memberName));
                        when(boardListRepository.findById(card.getListId())).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardList.getBoardId())).thenReturn(Optional.of(board));

                        // when
                        Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository, times(1)).save(any(Card.class));
                        verify(activityHelper, times(1)).logCardActivity(
                                        eq(ActivityType.CARD_UNASSIGN_MEMBER),
                                        eq(validCommand.requesterId()),
                                        any(Map.class),
                                        eq(board.getTitle()),
                                        eq(board.getBoardId()),
                                        eq(card.getListId()),
                                        eq(validCommand.cardId()));
                }

                @Test
                @DisplayName("입력 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenValidationFails() {
                        // given
                        ValidationResult<UnassignCardMemberCommand> invalidResult = ValidationResult.invalid("title",
                                        "제목은 필수입니다.", null);
                        when(validator.validateUnassign(validCommand)).thenReturn(invalidResult);

                        // when
                        Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("입력값이 유효하지 않습니다.");
                        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("INVALID_INPUT");
                        verifyNoInteractions(cardRepository, userFinder, cardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenCardNotFound() {
                        // given
                        ValidationResult<UnassignCardMemberCommand> validResult = ValidationResult.valid(validCommand);
                        when(validator.validateUnassign(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("카드를 찾을 수 없습니다.");
                        verifyNoInteractions(userFinder, cardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("멤버 사용자가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenMemberUserNotFound() {
                        // given
                        ValidationResult<UnassignCardMemberCommand> validResult = ValidationResult.valid(validCommand);
                        when(validator.validateUnassign(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(card));
                        when(userFinder.findUserOrThrow(validCommand.memberId()))
                                        .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다"));

                        // when
                        Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
                        verifyNoInteractions(cardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("요청자 사용자가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenRequesterUserNotFound() {
                        // given
                        ValidationResult<UnassignCardMemberCommand> validResult = ValidationResult.valid(validCommand);
                        when(validator.validateUnassign(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(card));
                        when(userFinder.findUserOrThrow(validCommand.memberId())).thenReturn(memberUser);
                        when(userFinder.findUserOrThrow(validCommand.requesterId()))
                                        .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다"));

                        // when
                        Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
                        verifyNoInteractions(cardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("멤버가 할당되어 있지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenMemberNotAssigned() {
                        // given
                        ValidationResult<UnassignCardMemberCommand> validResult = ValidationResult.valid(validCommand);
                        when(validator.validateUnassign(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(card));
                        when(userFinder.findUserOrThrow(validCommand.memberId())).thenReturn(memberUser);
                        when(userFinder.findUserOrThrow(validCommand.requesterId())).thenReturn(requesterUser);
                        when(cardMemberRepository.existsByCardIdAndUserId(validCommand.cardId(),
                                        validCommand.memberId()))
                                        .thenReturn(false);

                        // when
                        Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("할당되지 않은 멤버입니다.");
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("카드 저장 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenCardSaveFails() {
                        // given
                        ValidationResult<UnassignCardMemberCommand> validResult = ValidationResult.valid(validCommand);
                        when(validator.validateUnassign(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(card));
                        when(userFinder.findUserOrThrow(validCommand.memberId())).thenReturn(memberUser);
                        when(userFinder.findUserOrThrow(validCommand.requesterId())).thenReturn(requesterUser);
                        when(cardMemberRepository.existsByCardIdAndUserId(validCommand.cardId(),
                                        validCommand.memberId()))
                                        .thenReturn(true);
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.left(
                                                        Failure.ofInternalError("저장 실패", "INTERNAL_ERROR", null)));

                        // when
                        Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("저장 실패");
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("활동 로그 기록 실패 시에도 성공을 반환한다")
                void shouldReturnSuccessEvenWhenActivityLogFails() {
                        // given
                        ValidationResult<UnassignCardMemberCommand> validResult = ValidationResult.valid(validCommand);
                        when(validator.validateUnassign(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(card));
                        when(userFinder.findUserOrThrow(validCommand.memberId())).thenReturn(memberUser);
                        when(userFinder.findUserOrThrow(validCommand.requesterId())).thenReturn(requesterUser);
                        when(cardMemberRepository.existsByCardIdAndUserId(validCommand.cardId(),
                                        validCommand.memberId()))
                                        .thenReturn(true);
                        when(cardRepository.save(any(Card.class))).thenReturn(Either.right(card));
                        when(userFinder.findUserNameById(validCommand.memberId())).thenReturn(Optional.of(memberName));
                        when(boardListRepository.findById(card.getListId())).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardList.getBoardId())).thenReturn(Optional.of(board));
                        // void 메서드는 doThrow를 사용해야 함
                        doThrow(new RuntimeException("활동 로그 실패"))
                                        .when(activityHelper)
                                        .logCardActivity(any(), any(), any(), any(), any(), any(), any());

                        // when
                        Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository, times(1)).save(any(Card.class));
                }
        }
}