package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AttachmentId extends EntityId {

    public AttachmentId(String attachmentId) {
        super(attachmentId);
    }

    public AttachmentId() {
        super("att_" + UlidCreator.getUlid().toString());
    }
}
