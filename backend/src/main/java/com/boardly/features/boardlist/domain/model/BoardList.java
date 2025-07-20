package com.boardly.features.boardlist.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardList extends BaseEntity {

    private ListId listId;
    private String title;
    private String description;
    private int position;
    private ListColor color;
    private BoardId boardId;

    @Builder
    private BoardList(ListId listId, String title, String description,
            int position, ListColor color, BoardId boardId,
            Instant createdAt, Instant updatedAt) {

        super(createdAt, updatedAt);
        this.listId = listId;
        this.title = title;
        this.description = description;
        this.position = position;
        this.color = color;
        this.boardId = boardId;
    }

    /**
     * 새로운 보드 리스트를 생성합니다. (UTC 기준)
     */
    public static BoardList create(String title, String description,
            int position, ListColor color, BoardId boardId) {
        Instant now = Instant.now();
        return BoardList.builder()
                .listId(new ListId())
                .title(title)
                .description(description)
                .position(position)
                .color(color)
                .boardId(boardId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 기본 색상으로 새로운 보드 리스트를 생성합니다. (UTC 기준)
     */
    public static BoardList create(String title, int position, BoardId boardId) {
        Instant now = Instant.now();
        return BoardList.builder()
                .listId(new ListId())
                .title(title)
                .description("")
                .position(position)
                .color(ListColor.defaultColor())
                .boardId(boardId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 리스트 제목을 수정합니다.
     */
    public void updateTitle(String title) {
        this.title = title;
        markAsUpdated();
    }

    /**
     * 리스트 설명을 수정합니다.
     */
    public void updateDescription(String description) {
        this.description = description;
        markAsUpdated();
    }

    /**
     * 리스트 위치를 변경합니다.
     */
    public void updatePosition(int position) {
        this.position = position;
        markAsUpdated();
    }

    /**
     * 리스트 색상을 변경합니다.
     */
    public void updateColor(ListColor color) {
        this.color = color;
        markAsUpdated();
    }

    /**
     * 사용자가 이 리스트에 접근할 수 있는지 확인
     */
    public boolean canAccess(UserId userId, Board board) {
        return board.canAccess(userId);
    }

    /**
     * 사용자가 이 리스트를 수정할 수 있는지 확인
     */
    public boolean canModify(UserId userId, Board board) {
        return board.canModify(userId);
    }

    /**
     * 사용자가 이 리스트에 카드를 생성할 수 있는지 확인
     */
    public boolean canCreateCard(UserId userId, Board board) {
        return board.canModify(userId);
    }

    /**
     * 사용자가 이 리스트를 삭제할 수 있는지 확인
     */
    public boolean canDelete(UserId userId, Board board) {
        return board.canModify(userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        BoardList boardList = (BoardList) obj;
        return Objects.equals(listId, boardList.listId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listId);
    }

    @Override
    public String toString() {
        return String.format(
                "BoardList{listId=%s, title='%s', position=%d, color=%s, boardId=%s, createdAt=%s, updatedAt=%s}",
                listId, title, position, color, boardId, getCreatedAt(), getUpdatedAt());
    }
}
