package com.boardly.features.board.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.output.GetBoardDetailPort;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보드 상세 조회 어댑터
 * 
 * <p>
 * 보드 상세 정보 조회를 위한 포트 구현체입니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetBoardDetailAdapter implements GetBoardDetailPort {

    private final BoardRepository boardRepository;
    private final BoardListRepository boardListRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final LabelRepository labelRepository;
    private final CardRepository cardRepository;
    private final UserFinder userFinder;

    @Override
    public Either<Failure, BoardDetailData> getBoardDetail(BoardId boardId, UserId userId) {
        try {
            // 1. 보드 조회
            Board board = boardRepository.findById(boardId)
                    .orElse(null);

            if (board == null) {
                return Either.left(Failure.ofNotFound("보드를 찾을 수 없습니다."));
            }

            // 2. 보드 리스트 조회
            List<BoardList> boardLists = boardListRepository.findByBoardIdOrderByPosition(boardId);

            // 3. 보드 멤버 조회
            List<BoardMember> boardMembers = boardMemberRepository.findActiveByBoardId(boardId);

            // 4. 라벨 조회
            List<Label> labels = labelRepository.findByBoardIdOrderByName(boardId);

            // 5. 카드 조회 (리스트별로 그룹화)
            Map<ListId, List<Card>> cards = loadCardsByList(boardLists);

            // 6. 사용자 조회 (카드 담당자, 생성자 등)
            Map<UserId, User> users = loadUsers(boardMembers, cards);

            BoardDetailData data = new BoardDetailData(
                    board, boardLists, boardMembers, labels, cards, users);

            return Either.right(data);

        } catch (Exception e) {
            log.error("보드 상세 데이터 조회 중 예외 발생: boardId={}, error={}",
                    boardId.getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    /**
     * 리스트별 카드를 로드합니다.
     */
    private Map<ListId, List<Card>> loadCardsByList(
            List<BoardList> boardLists) {
        if (boardLists.isEmpty()) {
            return Map.of();
        }

        List<ListId> listIds = boardLists.stream()
                .map(BoardList::getListId)
                .collect(Collectors.toList());

        List<Card> allCards = cardRepository.findByListIdIn(listIds);

        return allCards.stream()
                .collect(Collectors.groupingBy(Card::getListId));
    }

    /**
     * 필요한 사용자들을 로드합니다.
     */
    private Map<UserId, User> loadUsers(List<BoardMember> boardMembers,
            Map<ListId, List<Card>> cards) {

        // 보드 멤버들의 사용자 ID 수집
        List<UserId> userIds = boardMembers.stream()
                .map(BoardMember::getUserId)
                .collect(Collectors.toList());

        // 카드 담당자들의 사용자 ID 수집 (실제로는 Card 도메인에 담당자 정보가 필요)
        // 현재는 임시로 보드 멤버만 사용

        // 중복 제거
        userIds = userIds.stream().distinct().collect(Collectors.toList());

        // 사용자 조회 (개별 조회)
        Map<UserId, User> users = userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> {
                            try {
                                return userFinder.findUserOrThrow(userId);
                            } catch (Exception e) {
                                log.warn("사용자를 찾을 수 없음: userId={}", userId.getId());
                                return null;
                            }
                        }));

        // null 값 제거
        users = users.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return users;
    }
}