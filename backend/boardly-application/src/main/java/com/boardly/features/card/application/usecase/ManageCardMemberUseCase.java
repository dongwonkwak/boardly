package com.boardly.features.card.application.usecase;

import java.util.List;

import com.boardly.features.card.application.command.AssignCardMemberCommand;
import com.boardly.features.card.application.command.UnassignCardMemberCommand;
import com.boardly.shared.common.value.CardId;
import com.boardly.features.card.domain.CardMember;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

public interface ManageCardMemberUseCase {

    Either<Failure, Void> assignMember(AssignCardMemberCommand command);

    Either<Failure, Void> unassignMember(UnassignCardMemberCommand command);

    List<CardMember> getCardMembers(CardId cardId, UserId requesterId);

}
