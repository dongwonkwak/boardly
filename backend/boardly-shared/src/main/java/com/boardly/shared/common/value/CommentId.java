package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CommentId extends EntityId {

    public CommentId(String commentId) {
        super(commentId);
    }

    public CommentId() {
        super("cmt_" + UlidCreator.getUlid().toString());
    }
}
