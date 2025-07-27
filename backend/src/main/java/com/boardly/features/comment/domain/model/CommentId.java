package com.boardly.features.comment.domain.model;

import com.boardly.shared.domain.valueobject.EntityId;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CommentId extends EntityId {

    public CommentId(String commentId) {
        super(commentId);
    }

    public CommentId() {
        super();
    }
}
