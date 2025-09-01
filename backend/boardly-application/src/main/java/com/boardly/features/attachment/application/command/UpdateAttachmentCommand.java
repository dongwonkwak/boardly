package com.boardly.features.attachment.application.command;

import com.boardly.shared.common.value.AttachmentId;

public record UpdateAttachmentCommand(
        AttachmentId attachmentId,
        String fileName) {

}
