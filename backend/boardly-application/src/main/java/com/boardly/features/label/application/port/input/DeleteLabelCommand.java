package com.boardly.features.label.application.port.input;

import com.boardly.shared.common.value.LabelId;
import com.boardly.shared.common.value.UserId;

public record DeleteLabelCommand(
        LabelId labelId,
        UserId userId) {

}
