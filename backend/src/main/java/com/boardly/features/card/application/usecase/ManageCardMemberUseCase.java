package com.boardly.features.card.application.usecase;

import java.util.List;

import com.boardly.features.card.application.port.input.AssignCardMemberCommand;
import com.boardly.features.card.application.port.input.UnassignCardMemberCommand;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface ManageCardMemberUseCase {

    Either<Failure, Void> assignMember(AssignCardMemberCommand command);

    Either<Failure, Void> unassignMember(UnassignCardMemberCommand command);

    List<CardMember> getCardMembers(CardId cardId, UserId requesterId);

}
