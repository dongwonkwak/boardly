package com.boardly.features.attachment.application.port.input;

import org.springframework.web.multipart.MultipartFile;

import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;

public record UploadAttachmentCommand(
        CardId cardId,
        UserId uploaderId,
        MultipartFile file) {

}
