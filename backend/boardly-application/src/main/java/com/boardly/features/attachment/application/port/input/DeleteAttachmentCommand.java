package com.boardly.features.attachment.application.port.input;

import com.boardly.shared.common.value.AttachmentId;
import com.boardly.shared.common.value.UserId;

public record DeleteAttachmentCommand(
        AttachmentId attachmentId,
        UserId requesterId) {

}
