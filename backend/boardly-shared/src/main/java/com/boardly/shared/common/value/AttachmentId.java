package com.boardly.shared.common.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AttachmentId extends EntityId {

    public AttachmentId(String attachmentId) {
        super(attachmentId);
    }

    public AttachmentId() {
        super();
    }
}
