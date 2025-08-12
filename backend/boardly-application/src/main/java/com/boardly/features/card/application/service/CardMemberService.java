package com.boardly.features.card.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.ActivityType;
import com.boardly.features.board.domain.Board;
import com.boardly.shared.common.value.BoardId;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.card.application.command.AssignCardMemberCommand;
import com.boardly.features.card.application.command.UnassignCardMemberCommand;
import com.boardly.features.card.application.usecase.ManageCardMemberUseCase;
import com.boardly.features.card.application.validation.CardMemberValidator;
import com.boardly.features.card.domain.Card;
import com.boardly.shared.common.value.CardId;
import com.boardly.features.card.domain.port.CardRepository;
import com.boardly.features.card.domain.CardMember;
import com.boardly.features.card.domain.port.CardMemberRepository;
import com.boardly.features.user.domain.port.UserFinder.UserNameDto;
import com.boardly.features.user.domain.port.UserFinder;
import com.boardly.features.user.domain.User;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CardMemberService implements ManageCardMemberUseCase {

    private final CardMemberValidator validator;
    private final MessageResolver messageResolver;
    private final CardRepository cardRepository;
    private final CardMemberRepository cardMemberRepository;
    private final UserFinder userFinder;
    private final ActivityHelper activityHelper;
    private final BoardListRepository boardListRepository;
    private final BoardRepository boardRepository;

    @Override
    public Either<Failure, Void> assignMember(AssignCardMemberCommand command) {
        log.info("카드 멤버 할당 시작: cardId={}, memberId={}, requesterId={}",
                command.cardId().getId(), command.memberId().getId(), command.requesterId().getId());

        return validateAssignCommand(command)
                .flatMap(this::findCard)
                .flatMap(card -> findMemberUser(command))
                .flatMap(member -> findRequesterUser(command))
                .flatMap(requester -> checkMemberNotAlreadyAssigned(command))
                .flatMap(validatedCommand -> assignMemberToCard(validatedCommand))
                .flatMap(this::saveCard)
                .flatMap(savedCard -> logAssignActivity(command));
    }

    @Override
    public Either<Failure, Void> unassignMember(UnassignCardMemberCommand command) {
        log.info("카드 멤버 해제 시작: cardId={}, memberId={}, requesterId={}",
                command.cardId().getId(), command.memberId().getId(), command.requesterId().getId());

        return validateUnassignCommand(command)
                .flatMap(this::findCard)
                .flatMap(card -> findMemberUser(command))
                .flatMap(member -> findRequesterUser(command))
                .flatMap(requester -> checkMemberIsAssigned(command))
                .flatMap(validatedCommand -> unassignMemberFromCard(validatedCommand))
                .flatMap(this::saveCard)
                .flatMap(savedCard -> logUnassignActivity(command));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardMember> getCardMembers(CardId cardId, UserId requesterId) {
        log.info("카드 멤버 조회 시작: cardId={}, requesterId={}",
                cardId.getId(), requesterId.getId());

        return findCardForRead(cardId, requesterId)
                .map(card -> cardMemberRepository.findByCardIdOrderByAssignedAt(cardId))
                .getOrElse(List.of());
    }

    // =================================================================
    // 🛡️ 검증 메서드들
    // =================================================================

    /**
     * 카드 멤버 할당 명령 검증
     */
    private Either<Failure, AssignCardMemberCommand> validateAssignCommand(AssignCardMemberCommand command) {
        var validationResult = validator.validateAssign(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 멤버 할당 검증 실패: {}", validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    /**
     * 카드 멤버 해제 명령 검증
     */
    private Either<Failure, UnassignCardMemberCommand> validateUnassignCommand(UnassignCardMemberCommand command) {
        var validationResult = validator.validateUnassign(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 멤버 해제 검증 실패: {}", validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    // =================================================================
    // 🔍 조회 메서드들
    // =================================================================

    /**
     * 카드 조회 (할당/해제용)
     */
    private Either<Failure, Card> findCard(AssignCardMemberCommand command) {
        return findCard(command.cardId());
    }

    private Either<Failure, Card> findCard(UnassignCardMemberCommand command) {
        return findCard(command.cardId());
    }

    private Either<Failure, Card> findCard(CardId cardId) {
        var cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            log.warn("카드를 찾을 수 없음: cardId={}", cardId.getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("error.service.card.read.not_found")));
        }
        return Either.right(cardOpt.get());
    }

    /**
     * 카드 조회 (읽기용)
     */
    private Either<Failure, Card> findCardForRead(CardId cardId, UserId requesterId) {
        var cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            log.warn("카드를 찾을 수 없음: cardId={}", cardId.getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("error.service.card.read.not_found")));
        }
        return Either.right(cardOpt.get());
    }

    /**
     * 멤버 사용자 조회
     */
    private Either<Failure, User> findMemberUser(AssignCardMemberCommand command) {
        return findUser(command.memberId(), "멤버");
    }

    private Either<Failure, User> findMemberUser(UnassignCardMemberCommand command) {
        return findUser(command.memberId(), "멤버");
    }

    private Either<Failure, User> findUser(UserId userId, String userType) {
        try {
            User user = userFinder.findUserOrThrow(userId);
            return Either.right(user);
        } catch (Exception e) {
            log.warn("{} 사용자를 찾을 수 없음: userId={}", userType, userId.getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.user.not.found")));
        }
    }

    /**
     * 요청자 사용자 조회
     */
    private Either<Failure, User> findRequesterUser(AssignCardMemberCommand command) {
        return findUser(command.requesterId(), "요청자");
    }

    private Either<Failure, User> findRequesterUser(UnassignCardMemberCommand command) {
        return findUser(command.requesterId(), "요청자");
    }

    // =================================================================
    // 🔒 비즈니스 로직 검증 메서드들
    // =================================================================

    /**
     * 멤버가 이미 할당되어 있지 않은지 확인
     */
    private Either<Failure, AssignCardMemberCommand> checkMemberNotAlreadyAssigned(AssignCardMemberCommand command) {
        if (cardMemberRepository.existsByCardIdAndUserId(command.cardId(), command.memberId())) {
            log.warn("멤버가 이미 할당되어 있음: cardId={}, memberId={}",
                    command.cardId().getId(), command.memberId().getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.card.member.already.assigned")));
        }
        return Either.right(command);
    }

    /**
     * 멤버가 할당되어 있는지 확인
     */
    private Either<Failure, UnassignCardMemberCommand> checkMemberIsAssigned(UnassignCardMemberCommand command) {
        if (!cardMemberRepository.existsByCardIdAndUserId(command.cardId(), command.memberId())) {
            log.warn("멤버가 할당되어 있지 않음: cardId={}, memberId={}",
                    command.cardId().getId(), command.memberId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.card.member.not.assigned")));
        }
        return Either.right(command);
    }

    // =================================================================
    // 💾 저장 메서드들
    // =================================================================

    /**
     * 카드에 멤버 할당
     */
    private Either<Failure, Card> assignMemberToCard(AssignCardMemberCommand command) {
        var cardOpt = cardRepository.findById(command.cardId());
        if (cardOpt.isEmpty()) {
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("error.service.card.read.not_found")));
        }

        Card card = cardOpt.get();
        card.assignMember(command.memberId());
        return Either.right(card);
    }

    /**
     * 카드에서 멤버 해제
     */
    private Either<Failure, Card> unassignMemberFromCard(UnassignCardMemberCommand command) {
        var cardOpt = cardRepository.findById(command.cardId());
        if (cardOpt.isEmpty()) {
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("error.service.card.read.not_found")));
        }

        Card card = cardOpt.get();
        card.unassignMember(command.memberId());
        return Either.right(card);
    }

    /**
     * 카드 저장
     */
    private Either<Failure, Card> saveCard(Card card) {
        return cardRepository.save(card);
    }

    // =================================================================
    // 📝 활동 로그 메서드들
    // =================================================================

    /**
     * 보드 정보 조회
     */
    private Either<Failure, Board> findBoardByCard(Card card) {
        var boardListOpt = boardListRepository.findById(card.getListId());
        if (boardListOpt.isEmpty()) {
            log.warn("카드의 리스트를 찾을 수 없음: listId={}", card.getListId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("error.service.card.read.list_not_found")));
        }

        BoardList boardList = boardListOpt.get();
        var boardOpt = boardRepository.findById(boardList.getBoardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", boardList.getBoardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.not.found")));
        }

        return Either.right(boardOpt.get());
    }

    /**
     * 멤버 할당 활동 로그
     */
    private Either<Failure, Void> logAssignActivity(AssignCardMemberCommand command) {
        try {
            var memberNameOpt = userFinder.findUserNameById(command.memberId());
            if (memberNameOpt.isPresent()) {
                UserNameDto memberName = memberNameOpt.get();
                var cardOpt = cardRepository.findById(command.cardId());
                if (cardOpt.isPresent()) {
                    Card card = cardOpt.get();

                    // 보드 정보 조회
                    var boardResult = findBoardByCard(card);
                    String boardName = boardResult.map(Board::getTitle).getOrElse("알 수 없는 보드");
                    BoardId boardId = boardResult.map(Board::getBoardId).getOrElse((BoardId) null);

                    var payload = Map.<String, Object>of(
                            "cardTitle", card.getTitle(),
                            "cardId", card.getCardId().getId(),
                            "memberId", command.memberId().getId(),
                            "memberFirstName", memberName.firstName(),
                            "memberLastName", memberName.lastName(),
                            "boardName", boardName);

                    activityHelper.logCardActivity(
                            ActivityType.CARD_ASSIGN_MEMBER,
                            command.requesterId(),
                            payload,
                            boardName,
                            boardId,
                            card.getListId(),
                            command.cardId());
                }
            }
            return Either.right(null);
        } catch (Exception e) {
            log.error("멤버 할당 활동 로그 기록 실패: {}", e.getMessage(), e);
            return Either.right(null); // 활동 로그 실패는 전체 작업 실패로 처리하지 않음
        }
    }

    /**
     * 멤버 해제 활동 로그
     */
    private Either<Failure, Void> logUnassignActivity(UnassignCardMemberCommand command) {
        try {
            var memberNameOpt = userFinder.findUserNameById(command.memberId());
            if (memberNameOpt.isPresent()) {
                UserNameDto memberName = memberNameOpt.get();
                var cardOpt = cardRepository.findById(command.cardId());
                if (cardOpt.isPresent()) {
                    Card card = cardOpt.get();

                    // 보드 정보 조회
                    var boardResult = findBoardByCard(card);
                    String boardName = boardResult.map(Board::getTitle).getOrElse("알 수 없는 보드");
                    BoardId boardId = boardResult.map(Board::getBoardId).getOrElse((BoardId) null);

                    var payload = Map.<String, Object>of(
                            "cardTitle", card.getTitle(),
                            "cardId", card.getCardId().getId(),
                            "memberId", command.memberId().getId(),
                            "memberFirstName", memberName.firstName(),
                            "memberLastName", memberName.lastName(),
                            "boardName", boardName);

                    activityHelper.logCardActivity(
                            ActivityType.CARD_UNASSIGN_MEMBER,
                            command.requesterId(),
                            payload,
                            boardName,
                            boardId,
                            card.getListId(),
                            command.cardId());
                }
            }
            return Either.right(null);
        } catch (Exception e) {
            log.error("멤버 해제 활동 로그 기록 실패: {}", e.getMessage(), e);
            return Either.right(null); // 활동 로그 실패는 전체 작업 실패로 처리하지 않음
        }
    }
}
