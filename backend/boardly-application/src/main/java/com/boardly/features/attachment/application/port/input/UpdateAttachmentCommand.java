package com.boardly.features.attachment.application.port.input;

import com.boardly.shared.common.value.AttachmentId;

public record UpdateAttachmentCommand(
        AttachmentId attachmentId,
        String fileName) {

}
