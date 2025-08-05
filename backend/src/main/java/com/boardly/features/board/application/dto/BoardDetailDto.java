package com.boardly.features.board.application.dto;

import java.util.List;
import java.util.Map;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.user.domain.model.User;

/**
 * 보드 상세 정보 DTO
 * 
 * <p>
 * 보드의 상세 정보를 담는 Application Layer DTO입니다.
 * 컬럼, 카드, 멤버, 라벨 등의 모든 정보를 포함합니다.
 * 
 * @since 1.0.0
 */
public record BoardDetailDto(
        Board board,
        List<BoardList> columns,
        List<BoardMember> boardMembers,
        List<Label> labels,
        List<Card> cards,
        Map<CardId, List<CardMember>> cardMembers,
        Map<CardId, Integer> cardCommentCounts,
        Map<CardId, Integer> cardAttachmentCounts,
        List<User> users) {

    /**
     * BoardDetailDto를 생성합니다.
     * 
     * @param board        보드 정보
     * @param columns      컬럼 목록
     * @param boardMembers 보드 멤버 목록
     * @param labels       라벨 목록
     * @param cards        카드 목록
     * @param cardMembers  카드 멤버 목록 (카드별로 그룹화)
     * @param cardCommentCounts 카드별 댓글 수
     * @param cardAttachmentCounts 카드별 첨부파일 수
     * @param users        사용자 목록
     * @return BoardDetailDto 객체
     */
    public static BoardDetailDto of(Board board, List<BoardList> columns,
            List<BoardMember> boardMembers, List<Label> labels,
            List<Card> cards, Map<CardId, List<CardMember>> cardMembers, 
            Map<CardId, Integer> cardCommentCounts, Map<CardId, Integer> cardAttachmentCounts,
            List<User> users) {
        return new BoardDetailDto(board, columns, boardMembers, labels, cards, cardMembers, 
                cardCommentCounts, cardAttachmentCounts, users);
    }
}