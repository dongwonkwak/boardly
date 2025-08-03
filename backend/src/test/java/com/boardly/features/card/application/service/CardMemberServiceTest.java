package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.AssignCardMemberCommand;
import com.boardly.features.card.application.port.input.UnassignCardMemberCommand;
import com.boardly.features.card.application.validation.CardMemberValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardMemberRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.card.domain.valueobject.CardMember;
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
@DisplayName("CardMemberService 테스트")
class CardMemberServiceTest {

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
        lenient().when(messageResolver.getMessage("validation.card.member.already.assigned"))
                .thenReturn("멤버가 이미 할당되어 있습니다.");
        lenient().when(messageResolver.getMessage("validation.card.member.not.assigned"))
                .thenReturn("멤버가 할당되어 있지 않습니다.");
        lenient().when(messageResolver.getMessage("error.service.card.read.list_not_found"))
                .thenReturn("리스트를 찾을 수 없습니다.");
        lenient().when(messageResolver.getMessage("validation.board.not.found"))
                .thenReturn("보드를 찾을 수 없습니다.");
    }

    @Nested
    @DisplayName("assignMember 메서드 테스트")
    class AssignMemberTest {

        private AssignCardMemberCommand validCommand;
        private Card card;
        private User memberUser;
        private User requesterUser;
        private UserNameDto memberName;
        private BoardList boardList;
        private Board board;
        private CardId cardId;
        private UserId memberId;
        private UserId requesterId;
        private ListId listId;
        private BoardId boardId;

        @BeforeEach
        void setUp() {
            cardId = new CardId("card-123");
            memberId = new UserId("member-123");
            requesterId = new UserId("requester-456");
            listId = new ListId("list-123");
            boardId = new BoardId("board-123");

            validCommand = new AssignCardMemberCommand(cardId, memberId, requesterId);

            card = Card.restore(
                    cardId,
                    "테스트 카드",
                    "테스트 설명",
                    0,
                    null,
                    false,
                    listId,
                    new HashSet<>(),
                    0,
                    0,
                    0,
                    Instant.now(),
                    Instant.now());

            memberUser = User.builder()
                    .userId(memberId)
                    .userProfile(new UserProfile("김", "멤버"))
                    .email("member@test.com")
                    .build();

            requesterUser = User.builder()
                    .userId(requesterId)
                    .userProfile(new UserProfile("이", "요청자"))
                    .email("requester@test.com")
                    .build();

            memberName = new UserNameDto("김", "멤버");

            boardList = BoardList.builder()
                    .listId(listId)
                    .title("테스트 리스트")
                    .boardId(boardId)
                    .position(0)
                    .build();

            board = Board.builder()
                    .boardId(boardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .build();
        }

        @Test
        @DisplayName("유효한 커맨드로 멤버 할당 시 성공한다")
        void shouldAssignMemberSuccessfully() {
            // given
            when(validator.validateAssign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser);
            when(userFinder.findUserOrThrow(requesterId))
                    .thenReturn(requesterUser);
            when(cardMemberRepository.existsByCardIdAndUserId(cardId, memberId))
                    .thenReturn(false);
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.right(card));
            when(userFinder.findUserNameById(memberId))
                    .thenReturn(Optional.of(memberName));
            when(boardListRepository.findById(listId))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId))
                    .thenReturn(Optional.of(board));
            lenient().doNothing().when(activityHelper).logCardActivity(any(), any(), any(), any(), any(),
                    any(), any());

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(cardRepository).save(any(Card.class));
            verify(activityHelper).logCardActivity(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("입력 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenValidationFails() {
            // given
            ValidationResult<AssignCardMemberCommand> validationResult = ValidationResult.invalid(
                    io.vavr.collection.List.of(Failure.FieldViolation.builder()
                            .field("memberId")
                            .message("멤버 ID는 필수입니다.")
                            .rejectedValue(null)
                            .build()));
            when(validator.validateAssign(validCommand))
                    .thenReturn(validationResult);

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("입력값이 유효하지 않습니다.");
            verifyNoInteractions(cardRepository, userFinder, cardMemberRepository, activityHelper);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            when(validator.validateAssign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드를 찾을 수 없습니다.");
            verifyNoInteractions(userFinder, cardMemberRepository, activityHelper);
        }

        @Test
        @DisplayName("멤버 사용자가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenMemberUserNotFound() {
            // given
            when(validator.validateAssign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenThrow(new UsernameNotFoundException(memberId.getId()));

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
            verifyNoInteractions(cardMemberRepository, activityHelper);
        }

        @Test
        @DisplayName("요청자 사용자가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenRequesterUserNotFound() {
            // given
            when(validator.validateAssign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser);
            when(userFinder.findUserOrThrow(requesterId))
                    .thenThrow(new UsernameNotFoundException(requesterId.getId()));

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
            verifyNoInteractions(cardMemberRepository, activityHelper);
        }

        @Test
        @DisplayName("멤버가 이미 할당되어 있을 때 실패를 반환한다")
        void shouldReturnFailureWhenMemberAlreadyAssigned() {
            // given
            when(validator.validateAssign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser);
            when(userFinder.findUserOrThrow(requesterId))
                    .thenReturn(requesterUser);
            when(cardMemberRepository.existsByCardIdAndUserId(cardId, memberId))
                    .thenReturn(true);

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("멤버가 이미 할당되어 있습니다.");
            verifyNoInteractions(activityHelper);
        }

        @Test
        @DisplayName("카드 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCardSaveFails() {
            // given
            when(validator.validateAssign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser);
            when(userFinder.findUserOrThrow(requesterId))
                    .thenReturn(requesterUser);
            when(cardMemberRepository.existsByCardIdAndUserId(cardId, memberId))
                    .thenReturn(false);
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("저장 실패");
            verifyNoInteractions(activityHelper);
        }

        @Test
        @DisplayName("활동 로그 기록 실패 시에도 성공을 반환한다")
        void shouldReturnSuccessEvenWhenActivityLogFails() {
            // given
            when(validator.validateAssign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser);
            when(userFinder.findUserOrThrow(requesterId))
                    .thenReturn(requesterUser);
            when(cardMemberRepository.existsByCardIdAndUserId(cardId, memberId))
                    .thenReturn(false);
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.right(card));
            when(userFinder.findUserNameById(memberId))
                    .thenReturn(Optional.empty()); // 활동 로그 실패 시뮬레이션

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(cardRepository).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("unassignMember 메서드 테스트")
    class UnassignMemberTest {

        private UnassignCardMemberCommand validCommand;
        private Card card;
        private User memberUser;
        private User requesterUser;
        private UserNameDto memberName;
        private BoardList boardList;
        private Board board;
        private CardId cardId;
        private UserId memberId;
        private UserId requesterId;
        private ListId listId;
        private BoardId boardId;

        @BeforeEach
        void setUp() {
            cardId = new CardId("card-123");
            memberId = new UserId("member-123");
            requesterId = new UserId("requester-456");
            listId = new ListId("list-123");
            boardId = new BoardId("board-123");

            validCommand = new UnassignCardMemberCommand(cardId, memberId, requesterId);

            // 이미 멤버가 할당된 카드
            Set<CardMember> assignedMembers = new HashSet<>();
            assignedMembers.add(new CardMember(memberId, Instant.now()));

            card = Card.restore(
                    cardId,
                    "테스트 카드",
                    "테스트 설명",
                    0,
                    null,
                    false,
                    listId,
                    assignedMembers,
                    0,
                    0,
                    0,
                    Instant.now(),
                    Instant.now());

            memberUser = User.builder()
                    .userId(memberId)
                    .userProfile(new UserProfile("김", "멤버"))
                    .email("member@test.com")
                    .build();

            requesterUser = User.builder()
                    .userId(requesterId)
                    .userProfile(new UserProfile("이", "요청자"))
                    .email("requester@test.com")
                    .build();

            memberName = new UserNameDto("김", "멤버");

            boardList = BoardList.builder()
                    .listId(listId)
                    .title("테스트 리스트")
                    .boardId(boardId)
                    .position(0)
                    .build();

            board = Board.builder()
                    .boardId(boardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .build();
        }

        @Test
        @DisplayName("유효한 커맨드로 멤버 해제 시 성공한다")
        @SuppressWarnings("unchecked")
        void shouldUnassignMemberSuccessfully() {
            // given
            when(validator.validateUnassign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser);
            when(userFinder.findUserOrThrow(requesterId))
                    .thenReturn(requesterUser);
            when(cardMemberRepository.existsByCardIdAndUserId(cardId, memberId))
                    .thenReturn(true);
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.right(card));
            when(userFinder.findUserNameById(memberId))
                    .thenReturn(Optional.of(memberName));
            when(boardListRepository.findById(listId))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId))
                    .thenReturn(Optional.of(board));
            lenient().doNothing().when(activityHelper).logCardActivity(any(), any(), any(), any(), any(),
                    any(), any());

            // when
            Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(cardRepository).save(any(Card.class));
            verify(activityHelper).logCardActivity(
                    eq(ActivityType.CARD_UNASSIGN_MEMBER),
                    eq(requesterId),
                    any(Map.class),
                    eq("테스트 보드"),
                    eq(boardId),
                    eq(listId),
                    eq(cardId));
        }

        @Test
        @DisplayName("입력 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenValidationFails() {
            // given
            ValidationResult<UnassignCardMemberCommand> invalidResult = ValidationResult.invalid(
                    io.vavr.collection.List.of(Failure.FieldViolation.builder()
                            .field("cardId")
                            .message("카드 ID는 필수입니다")
                            .rejectedValue(null)
                            .build()));
            when(validator.validateUnassign(validCommand))
                    .thenReturn(invalidResult);

            // when
            Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("입력값이 유효하지 않습니다.");
            verifyNoInteractions(cardRepository, userFinder, cardMemberRepository, activityHelper);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            when(validator.validateUnassign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.empty());

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
            when(validator.validateUnassign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenThrow(new UsernameNotFoundException(memberId.getId()));

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
            when(validator.validateUnassign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser);
            when(userFinder.findUserOrThrow(requesterId))
                    .thenThrow(new UsernameNotFoundException(requesterId.getId()));

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
            when(validator.validateUnassign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser);
            when(userFinder.findUserOrThrow(requesterId))
                    .thenReturn(requesterUser);
            when(cardMemberRepository.existsByCardIdAndUserId(cardId, memberId))
                    .thenReturn(false);

            // when
            Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("멤버가 할당되어 있지 않습니다.");
            verifyNoInteractions(activityHelper);
        }

        @Test
        @DisplayName("카드 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCardSaveFails() {
            // given
            when(validator.validateUnassign(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser);
            when(userFinder.findUserOrThrow(requesterId))
                    .thenReturn(requesterUser);
            when(cardMemberRepository.existsByCardIdAndUserId(cardId, memberId))
                    .thenReturn(true);
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

            // when
            Either<Failure, Void> result = cardMemberService.unassignMember(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("저장 실패");
            verifyNoInteractions(activityHelper);
        }
    }

    @Nested
    @DisplayName("getCardMembers 메서드 테스트")
    class GetCardMembersTest {

        private CardId cardId;
        private UserId requesterId;
        private Card card;
        private List<CardMember> expectedMembers;

        @BeforeEach
        void setUp() {
            cardId = new CardId("card-123");
            requesterId = new UserId("requester-456");

            card = Card.restore(
                    cardId,
                    "테스트 카드",
                    "테스트 설명",
                    0,
                    null,
                    false,
                    new ListId("list-123"),
                    new HashSet<>(),
                    0,
                    0,
                    0,
                    Instant.now(),
                    Instant.now());

            expectedMembers = List.of(
                    new CardMember(new UserId("member-1"), Instant.now()),
                    new CardMember(new UserId("member-2"), Instant.now().plusSeconds(60)));
        }

        @Test
        @DisplayName("카드 멤버 조회 시 성공한다")
        void shouldGetCardMembersSuccessfully() {
            // given
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(cardMemberRepository.findByCardIdOrderByAssignedAt(cardId))
                    .thenReturn(expectedMembers);

            // when
            List<CardMember> result = cardMemberService.getCardMembers(cardId, requesterId);

            // then
            assertThat(result).isEqualTo(expectedMembers);
            verify(cardMemberRepository).findByCardIdOrderByAssignedAt(cardId);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenCardNotFound() {
            // given
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.empty());

            // when
            List<CardMember> result = cardMemberService.getCardMembers(cardId, requesterId);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(cardMemberRepository);
        }

        @Test
        @DisplayName("멤버가 없을 때 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoMembers() {
            // given
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(cardMemberRepository.findByCardIdOrderByAssignedAt(cardId))
                    .thenReturn(List.of());

            // when
            List<CardMember> result = cardMemberService.getCardMembers(cardId, requesterId);

            // then
            assertThat(result).isEmpty();
            verify(cardMemberRepository).findByCardIdOrderByAssignedAt(cardId);
        }
    }

    @Nested
    @DisplayName("활동 로그 관련 테스트")
    class ActivityLogTest {

        private AssignCardMemberCommand assignCommand;
        private Card card;
        private UserNameDto memberName;
        private BoardList boardList;
        private CardId cardId;
        private UserId memberId;
        private UserId requesterId;
        private ListId listId;
        private BoardId boardId;

        @BeforeEach
        void setUp() {
            cardId = new CardId("card-123");
            memberId = new UserId("member-123");
            requesterId = new UserId("requester-456");
            listId = new ListId("list-123");
            boardId = new BoardId("board-123");

            assignCommand = new AssignCardMemberCommand(cardId, memberId, requesterId);

            card = Card.restore(
                    cardId,
                    "테스트 카드",
                    "테스트 설명",
                    0,
                    null,
                    false,
                    listId,
                    new HashSet<>(),
                    0,
                    0,
                    0,
                    Instant.now(),
                    Instant.now());

            memberName = new UserNameDto("김", "멤버");

            boardList = BoardList.builder()
                    .listId(listId)
                    .title("테스트 리스트")
                    .boardId(boardId)
                    .position(0)
                    .build();
        }

        @Test
        @DisplayName("보드 리스트가 존재하지 않을 때 활동 로그는 성공한다")
        @SuppressWarnings("unchecked")
        void shouldLogActivitySuccessfullyWhenBoardListNotFound() {
            // given
            when(validator.validateAssign(assignCommand))
                    .thenReturn(ValidationResult.valid(assignCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser());
            when(userFinder.findUserOrThrow(requesterId))
                    .thenReturn(requesterUser());
            when(cardMemberRepository.existsByCardIdAndUserId(cardId, memberId))
                    .thenReturn(false);
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.right(card));
            when(userFinder.findUserNameById(memberId))
                    .thenReturn(Optional.of(memberName));
            lenient().doNothing().when(activityHelper).logCardActivity(any(), any(), any(), any(), any(),
                    any(), any());

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(assignCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(activityHelper).logCardActivity(
                    eq(ActivityType.CARD_ASSIGN_MEMBER),
                    eq(requesterId),
                    any(Map.class),
                    eq("알 수 없는 보드"),
                    eq((BoardId) null),
                    eq(listId),
                    eq(cardId));
        }

        @Test
        @DisplayName("보드가 존재하지 않을 때 활동 로그는 성공한다")
        @SuppressWarnings("unchecked")
        void shouldLogActivitySuccessfullyWhenBoardNotFound() {
            // given
            when(validator.validateAssign(assignCommand))
                    .thenReturn(ValidationResult.valid(assignCommand));
            when(cardRepository.findById(cardId))
                    .thenReturn(Optional.of(card));
            when(userFinder.findUserOrThrow(memberId))
                    .thenReturn(memberUser());
            when(userFinder.findUserOrThrow(requesterId))
                    .thenReturn(requesterUser());
            when(cardMemberRepository.existsByCardIdAndUserId(cardId, memberId))
                    .thenReturn(false);
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.right(card));
            when(userFinder.findUserNameById(memberId))
                    .thenReturn(Optional.of(memberName));
            when(boardListRepository.findById(listId))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId))
                    .thenReturn(Optional.empty());
            lenient().doNothing().when(activityHelper).logCardActivity(any(), any(), any(), any(), any(),
                    any(), any());

            // when
            Either<Failure, Void> result = cardMemberService.assignMember(assignCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(activityHelper).logCardActivity(
                    eq(ActivityType.CARD_ASSIGN_MEMBER),
                    eq(requesterId),
                    any(Map.class),
                    eq("알 수 없는 보드"),
                    eq((BoardId) null),
                    eq(listId),
                    eq(cardId));
        }

        private User memberUser() {
            return User.builder()
                    .userId(memberId)
                    .userProfile(new UserProfile("김", "멤버"))
                    .email("member@test.com")
                    .build();
        }

        private User requesterUser() {
            return User.builder()
                    .userId(requesterId)
                    .userProfile(new UserProfile("이", "요청자"))
                    .email("requester@test.com")
                    .build();
        }
    }
}