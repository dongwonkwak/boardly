package com.boardly.features.attachment.application.command;

import com.boardly.shared.common.value.AttachmentId;
import com.boardly.shared.common.value.UserId;

public record DeleteAttachmentCommand(
        AttachmentId attachmentId,
        UserId requesterId) {

}
