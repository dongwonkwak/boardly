package com.boardly.features.attachment.application.port.input;

import com.boardly.features.attachment.domain.model.AttachmentId;
import com.boardly.shared.common.value.UserId;

public record DeleteAttachmentCommand(
        AttachmentId attachmentId,
        UserId requesterId) {

}
