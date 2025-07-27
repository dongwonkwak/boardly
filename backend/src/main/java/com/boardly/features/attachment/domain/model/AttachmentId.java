package com.boardly.features.attachment.domain.model;

import com.boardly.shared.domain.valueobject.EntityId;

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
