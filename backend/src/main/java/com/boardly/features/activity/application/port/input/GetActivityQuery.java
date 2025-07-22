package com.boardly.features.activity.application.port.input;

import java.time.Instant;
import java.util.Optional;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;

import lombok.Builder;

@Builder
public record GetActivityQuery(
        UserId userId,
        BoardId boardId,
        Instant since,
        Instant until,
        int page,
        int size) {

    public static GetActivityQuery forBoard(BoardId boardId) {
        return GetActivityQuery.builder()
                .boardId(boardId)
                .build();
    }

    public static GetActivityQuery forBoardWithPagination(BoardId boardId, int page, int size) {
        return GetActivityQuery.builder()
                .boardId(boardId)
                .page(page)
                .size(size)
                .build();
    }

    public static GetActivityQuery forUser(UserId userId) {
        return GetActivityQuery.builder()
                .userId(userId)
                .build();
    }

    public static GetActivityQuery forUserWithPagination(UserId userId, int page, int size) {
        return GetActivityQuery.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .build();
    }

    public static GetActivityQuery forBoardSince(BoardId boardId, Instant since) {
        return GetActivityQuery.builder()
                .boardId(boardId)
                .since(since)
                .build();
    }

    public int getPageOrDefault() {
        return Optional.ofNullable(page).orElse(0);
    }

    public int getSizeOrDefault() {
        return Optional.ofNullable(size).orElse(50);
    }
}
