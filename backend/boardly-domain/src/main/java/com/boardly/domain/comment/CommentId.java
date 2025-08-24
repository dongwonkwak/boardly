package com.boardly.domain.comment;

import com.boardly.shared.EntityId;

/**
 * 댓글 ID 도메인 객체
 */
public class CommentId extends EntityId {

    public CommentId() {
        super();
    }

    public CommentId(String value) {
        super(value);
    }

    public static CommentId generate() {
        return new CommentId(generateWithPrefix("cmt_"));
    }

    public static CommentId of(String value) {
        return new CommentId(value);
    }

    @Override
    protected String getPrefix() {
        return "cmt_";
    }
}
