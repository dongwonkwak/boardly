package com.boardly.features.attachment.application.port.input;

import com.boardly.features.attachment.domain.model.AttachmentId;

public record UpdateAttachmentCommand(
        AttachmentId attachmentId,
        String fileName) {

}
