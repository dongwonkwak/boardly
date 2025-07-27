package com.boardly.features.attachment.application.port.input;

import org.springframework.web.multipart.MultipartFile;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;

public record UploadAttachmentCommand(
        CardId cardId,
        UserId uploaderId,
        MultipartFile file) {

}
