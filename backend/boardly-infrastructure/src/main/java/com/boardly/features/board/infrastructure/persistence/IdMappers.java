package com.boardly.features.board.infrastructure.persistence;

import org.mapstruct.Named;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;

public interface IdMappers {
    @Named("boardIdToString")
    default String boardIdToString(BoardId id) { return id != null ? id.getId() : null; }

    @Named("stringToBoardId")
    default BoardId stringToBoardId(String id) { return id != null ? new BoardId(id) : null; }

    @Named("userIdToString")
    default String userIdToString(UserId id) { return id != null ? id.getId() : null; }

    @Named("stringToUserId")
    default UserId stringToUserId(String id) { return id != null ? new UserId(id) : null; }
}
