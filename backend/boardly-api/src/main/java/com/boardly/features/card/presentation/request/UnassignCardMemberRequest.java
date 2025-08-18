package com.boardly.api.adapters.in.rest.card.request;

import jakarta.validation.constraints.NotBlank;

public record UnassignCardMemberRequest(
        @NotBlank(message = "멤버 ID는 필수입니다") String memberId) {
}