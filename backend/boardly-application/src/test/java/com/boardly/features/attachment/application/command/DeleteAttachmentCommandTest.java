package com.boardly.features.attachment.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.boardly.shared.common.value.AttachmentId;
import com.boardly.shared.common.value.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteAttachmentCommandTest {

    @Test
    @DisplayName("필드가 정상적으로 세팅된다")
    void fields_are_set_correctly() {
        var id = new AttachmentId("att-1");
        var requester = new UserId("user-1");
        var cmd = new DeleteAttachmentCommand(id, requester);

        assertThat(cmd.attachmentId()).isEqualTo(id);
        assertThat(cmd.requesterId()).isEqualTo(requester);
    }
}
