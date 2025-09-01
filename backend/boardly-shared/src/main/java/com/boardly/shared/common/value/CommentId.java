package com.boardly.shared.common.value;

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
