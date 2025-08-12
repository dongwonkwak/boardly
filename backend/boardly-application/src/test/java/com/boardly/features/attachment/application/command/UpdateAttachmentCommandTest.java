package com.boardly.features.attachment.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.boardly.shared.common.value.AttachmentId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateAttachmentCommandTest {

    @Test
    @DisplayName("필드가 정상적으로 세팅된다")
    void fields_are_set_correctly() {
        var id = new AttachmentId("att-1");
        var cmd = new UpdateAttachmentCommand(id, "new-name.txt");

        assertThat(cmd.attachmentId()).isEqualTo(id);
        assertThat(cmd.fileName()).isEqualTo("new-name.txt");
    }

    @Test
    @DisplayName("파일명은 null 가능하다 (검증은 Validator에서)")
    void fileName_can_be_null() {
        var id = new AttachmentId("att-1");
        var cmd = new UpdateAttachmentCommand(id, null);
        assertThat(cmd.fileName()).isNull();
    }
}
