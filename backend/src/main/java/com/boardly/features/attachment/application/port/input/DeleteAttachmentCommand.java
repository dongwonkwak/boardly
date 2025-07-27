package com.boardly.features.attachment.application.port.input;

import com.boardly.features.attachment.domain.model.AttachmentId;
import com.boardly.features.user.domain.model.UserId;

public record DeleteAttachmentCommand(
        AttachmentId attachmentId,
        UserId requesterId) {

}
