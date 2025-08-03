package com.boardly.features.board.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.AddBoardMemberCommand;
import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.application.port.input.DeleteBoardCommand;
import com.boardly.features.board.application.port.input.GetBoardDetailCommand;
import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.port.input.UpdateBoardMemberRoleCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

/**
 * 보드 관련 모든 검증을 담당하는 통합 Validator
 * 
 * <p>
 * 보드 생성, 수정, 삭제, 아카이브, 즐겨찾기 토글 등 모든 보드 관련 명령의 검증을 처리합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class BoardValidator {

    private final CommonValidationRules commonValidationRules;

    public ValidationResult<CreateBoardCommand> validateCreate(CreateBoardCommand command) {
        return getCreateValidator().validate(command);
    }

    public ValidationResult<UpdateBoardCommand> validateUpdate(UpdateBoardCommand command) {
        return getUpdateValidator().validate(command);
    }

    public ValidationResult<DeleteBoardCommand> validateDelete(DeleteBoardCommand command) {
        return getDeleteValidator().validate(command);
    }

    public ValidationResult<ArchiveBoardCommand> validateArchive(ArchiveBoardCommand command) {
        return getArchiveValidator().validate(command);
    }

    public ValidationResult<GetBoardDetailCommand> validateGetDetail(GetBoardDetailCommand command) {
        return getGetDetailValidator().validate(command);
    }

    // ==================== TOGGLE STAR BOARD ====================

    public ValidationResult<ToggleStarBoardCommand> validateToggleStar(ToggleStarBoardCommand command) {
        return getToggleStarValidator().validate(command);
    }

    // ==================== BOARD MEMBER MANAGEMENT ====================

    public ValidationResult<AddBoardMemberCommand> validateAddMember(AddBoardMemberCommand command) {
        return getAddMemberValidator().validate(command);
    }

    public ValidationResult<RemoveBoardMemberCommand> validateRemoveMember(RemoveBoardMemberCommand command) {
        return getRemoveMemberValidator().validate(command);
    }

    public ValidationResult<UpdateBoardMemberRoleCommand> validateUpdateMemberRole(
            UpdateBoardMemberRoleCommand command) {
        return getUpdateMemberRoleValidator().validate(command);
    }

    private Validator<CreateBoardCommand> getCreateValidator() {
        return Validator.combine(
                commonValidationRules.titleComplete(CreateBoardCommand::title),
                commonValidationRules.descriptionComplete(CreateBoardCommand::description),
                commonValidationRules.userIdRequired(CreateBoardCommand::ownerId));
    }

    private Validator<UpdateBoardCommand> getUpdateValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(UpdateBoardCommand::boardId),
                commonValidationRules.userIdRequired(UpdateBoardCommand::requestedBy),
                commonValidationRules.titleOptional(UpdateBoardCommand::title),
                commonValidationRules.descriptionComplete(UpdateBoardCommand::description));
    }

    private Validator<DeleteBoardCommand> getDeleteValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(DeleteBoardCommand::boardId),
                commonValidationRules.userIdRequired(DeleteBoardCommand::requestedBy));
    }

    private Validator<ArchiveBoardCommand> getArchiveValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(ArchiveBoardCommand::boardId),
                commonValidationRules.userIdRequired(ArchiveBoardCommand::requestedBy));
    }

    private Validator<GetBoardDetailCommand> getGetDetailValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(GetBoardDetailCommand::boardId),
                commonValidationRules.userIdRequired(GetBoardDetailCommand::userId));
    }

    private Validator<ToggleStarBoardCommand> getToggleStarValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(ToggleStarBoardCommand::boardId),
                commonValidationRules.userIdRequired(ToggleStarBoardCommand::requestedBy));
    }

    private Validator<AddBoardMemberCommand> getAddMemberValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(AddBoardMemberCommand::boardId),
                commonValidationRules.userIdRequired(AddBoardMemberCommand::userId),
                commonValidationRules.boardMemberRoleRequired(AddBoardMemberCommand::role),
                commonValidationRules.userIdRequired(AddBoardMemberCommand::requestedBy));
    }

    private Validator<RemoveBoardMemberCommand> getRemoveMemberValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(RemoveBoardMemberCommand::boardId),
                commonValidationRules.userIdRequired(RemoveBoardMemberCommand::targetUserId),
                commonValidationRules.userIdRequired(RemoveBoardMemberCommand::requestedBy));
    }

    private Validator<UpdateBoardMemberRoleCommand> getUpdateMemberRoleValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(UpdateBoardMemberRoleCommand::boardId),
                commonValidationRules.userIdRequired(UpdateBoardMemberRoleCommand::targetUserId),
                commonValidationRules.boardMemberRoleRequired(UpdateBoardMemberRoleCommand::newRole),
                commonValidationRules.userIdRequired(UpdateBoardMemberRoleCommand::requestedBy));
    }
}