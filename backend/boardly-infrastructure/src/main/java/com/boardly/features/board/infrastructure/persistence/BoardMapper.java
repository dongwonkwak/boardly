package com.boardly.features.board.infrastructure.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.boardly.features.board.domain.Board;

@Mapper(componentModel = "spring", uses = IdMappers.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BoardMapper {
    @Mapping(target = "boardId", source = "boardId", qualifiedByName = "boardIdToString")
    @Mapping(target = "ownerId", source = "ownerId", qualifiedByName = "userIdToString")
    @Mapping(target = "isArchived", source = "archived")
    @Mapping(target = "isStarred", source = "starred")
    BoardEntity toEntity(Board board);

    @Mapping(target = "boardId", source = "boardId", qualifiedByName = "stringToBoardId")
    @Mapping(target = "ownerId", source = "ownerId", qualifiedByName = "stringToUserId")
    @Mapping(target = "isArchived", source = "archived")
    @Mapping(target = "isStarred", source = "starred")
    Board toDomain(BoardEntity entity);
}
