package com.boardly.features.board.domain.port;

import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.BoardMember;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.card.domain.Card;
import com.boardly.features.card.domain.CardMember;
import com.boardly.features.label.domain.Label;
import com.boardly.features.user.domain.User;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.LabelId;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;
import io.vavr.control.Either;
import java.util.List;
import java.util.Map;

/**
 * 보드 상세 조회 포트
 *
 * <p>
 * 보드 상세 정보 조회를 위한 포트 인터페이스입니다.
 *
 * @since 1.0.0
 */
public interface GetBoardDetailPort {
    /**
     * 보드 상세 정보를 조회합니다.
     *
     * @param boardId 보드 ID
     * @param userId  사용자 ID
     * @return 보드 상세 정보 또는 실패
     */
    Either<Failure, BoardDetailData> getBoardDetail(
        BoardId boardId,
        UserId userId
    );

    /**
     * 보드 상세 데이터
     *
     * @param board        보드 정보
     * @param boardLists   보드 리스트 목록
     * @param boardMembers 보드 멤버 목록
     * @param labels       라벨 목록
     * @param cards        카드 목록 (리스트별로 그룹화)
     * @param cardMembers  카드 멤버 목록 (카드별로 그룹화)
     * @param cardLabels   카드 라벨 목록 (카드별로 그룹화)
     * @param users        사용자 목록 (카드 담당자, 생성자 등)
     */
    record BoardDetailData(
        Board board,
        List<BoardList> boardLists,
        List<BoardMember> boardMembers,
        List<Label> labels,
        Map<ListId, List<Card>> cards,
        Map<CardId, List<CardMember>> cardMembers,
        Map<CardId, List<LabelId>> cardLabels,
        Map<UserId, User> users
    ) {}
}
