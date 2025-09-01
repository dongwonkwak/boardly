package com.boardly.features.attachment.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UploadAttachmentCommandTest {

    @Test
    @DisplayName("필드가 정상적으로 세팅된다")
    void fields_are_set_correctly() {
        var cardId = new CardId("card-1");
        var uploaderId = new UserId("user-1");
        var file = new MockMultipartFile("file", "a.txt", "text/plain", "hi".getBytes());

        var cmd = new UploadAttachmentCommand(cardId, uploaderId, file);

        assertThat(cmd.cardId()).isEqualTo(cardId);
        assertThat(cmd.uploaderId()).isEqualTo(uploaderId);
        assertThat(cmd.file()).isEqualTo(file);
    }

    @Test
    @DisplayName("파일이 null이어도 레코드는 생성된다")
    void file_can_be_null() {
        var cmd = new UploadAttachmentCommand(new CardId("c"), new UserId("u"), null);
        assertThat(cmd.file()).isNull();
    }
}
