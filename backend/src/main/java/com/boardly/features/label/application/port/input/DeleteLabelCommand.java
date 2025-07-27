package com.boardly.features.label.application.port.input;

import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.user.domain.model.UserId;

public record DeleteLabelCommand(
        LabelId labelId,
        UserId userId) {

}
