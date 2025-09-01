package com.boardly.features.board.infrastructure.persistence;

import org.mapstruct.Mapper;

import com.boardly.features.board.domain.Board;

@Mapper(componentModel = "spring")
public interface BoardMapper {
    default BoardEntity toEntity(Board board) {
        return BoardEntity.fromDomainEntity(board);
    }

    default Board toDomain(BoardEntity entity) {
        return entity.toDomainEntity();
    }
}
