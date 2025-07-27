package com.boardly.features.label.application.port.input;

import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.user.domain.model.UserId;

public record UpdateLabelCommand(
        LabelId labelId,
        UserId requesterId,
        String name,
        String color) {
    public static UpdateLabelCommand of(LabelId labelId, UserId requesterId, String name, String color) {
        return new UpdateLabelCommand(
                labelId,
                requesterId,
                name != null ? name.trim() : "",
                color != null ? color.trim() : "");
    }
}
