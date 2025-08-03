package com.boardly.features.board.presentation.response;

import com.boardly.features.board.application.dto.BoardDetailDto;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 보드 상세 조회 응답 DTO
 * 
 * @param boardId          보드 ID
 * @param boardName        보드 이름
 * @param boardDescription 보드 설명
 * @param isStarred        즐겨찾기 여부
 * @param boardColor       보드 색상
 * @param columns          컬럼 목록
 * @param boardMembers     보드 멤버 목록
 * @param labels           라벨 목록
 * @param createdAt        생성 시간
 * @param updatedAt        수정 시간
 * 
 * @since 1.0.0
 */
public record BoardDetailResponse(
        String boardId,
        String boardName,
        String boardDescription,
        boolean isStarred,
        String boardColor,
        List<BoardColumnResponse> columns,
        List<BoardMemberResponse> boardMembers,
        List<BoardLabelResponse> labels,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant updatedAt) {

    /**
     * BoardDetailDto를 BoardDetailResponse로 변환합니다.
     * 
     * @param boardDetailDto 변환할 BoardDetailDto
     * @return BoardDetailResponse 객체
     */
    public static BoardDetailResponse from(BoardDetailDto boardDetailDto) {
        // 사용자 맵 생성 (ID로 빠른 조회를 위해)
        Map<String, com.boardly.features.user.domain.model.User> userMap = boardDetailDto.users().stream()
                .collect(Collectors.toMap(
                        user -> user.getUserId().getId(),
                        user -> user));

        // 컬럼 응답 변환
        List<BoardColumnResponse> columns = boardDetailDto.columns().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(boardList -> {
                    // 해당 리스트의 카드들 필터링
                    List<com.boardly.features.card.domain.model.Card> listCards = boardDetailDto.cards().stream()
                            .filter(card -> card.getListId().equals(boardList.getListId()))
                            .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                            .collect(Collectors.toList());

                    List<BoardCardResponse> cardResponses = listCards.stream()
                            .map(card -> convertToBoardCardResponse(card, userMap))
                            .collect(Collectors.toList());

                    return BoardColumnResponse.from(boardList, cardResponses);
                })
                .collect(Collectors.toList());

        // 멤버 응답 변환
        List<BoardMemberResponse> boardMembers = boardDetailDto.boardMembers().stream()
                .map(boardMember -> {
                    com.boardly.features.user.domain.model.User user = userMap.get(boardMember.getUserId().getId());
                    return BoardMemberResponse.from(boardMember, user);
                })
                .collect(Collectors.toList());

        // 라벨 응답 변환
        List<BoardLabelResponse> labels = boardDetailDto.labels().stream()
                .map(BoardLabelResponse::from)
                .collect(Collectors.toList());

        return new BoardDetailResponse(
                boardDetailDto.board().getBoardId().getId(),
                boardDetailDto.board().getTitle(),
                boardDetailDto.board().getDescription(),
                boardDetailDto.board().isStarred(),
                generateBoardColor(boardDetailDto.board().getBoardId().getId()),
                columns,
                boardMembers,
                labels,
                boardDetailDto.board().getCreatedAt(),
                boardDetailDto.board().getUpdatedAt());
    }

    /**
     * Card 도메인 모델을 BoardCardResponse로 변환합니다.
     */
    private static BoardCardResponse convertToBoardCardResponse(
            com.boardly.features.card.domain.model.Card card,
            Map<String, com.boardly.features.user.domain.model.User> userMap) {

        // 실제 구현에서는 카드별 라벨, 담당자, 댓글 정보 등을 조회해야 함
        List<CardLabelResponse> labels = List.of(); // 임시 빈 리스트
        List<CardAssigneeResponse> assignees = List.of(); // 임시 빈 리스트

        // 생성자 정보 (실제로는 Card 도메인에 createdBy 필드가 필요)
        CardUserResponse createdBy = CardUserResponse.of("unknown", "Unknown", "User");
        CardUserResponse completedBy = null; // 완료되지 않은 카드

        return BoardCardResponse.from(card, labels, assignees, createdBy, completedBy, null, null);
    }

    /**
     * 보드 ID를 기반으로 색상을 생성합니다.
     */
    private static String generateBoardColor(String boardId) {
        String[] colors = {
                "bg-blue-600", "bg-green-600", "bg-purple-600",
                "bg-red-600", "bg-yellow-600", "bg-indigo-600"
        };
        int index = Math.abs(boardId.hashCode()) % colors.length;
        return colors[index];
    }
}