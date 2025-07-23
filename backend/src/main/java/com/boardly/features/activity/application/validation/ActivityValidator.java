package com.boardly.features.activity.application.validation;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.boardly.features.activity.application.port.input.CreateActivityCommand;
import com.boardly.features.activity.application.port.input.GetActivityQuery;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

/**
 * 활동 통합 검증기
 * 
 * <p>
 * 모든 활동 관련 Command와 Query들의 입력 검증을 담당합니다.
 * CreateActivityCommand와 GetActivityQuery의 검증 로직을 통합하여 관리합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ActivityValidator {

    private final CommonValidationRules commonValidationRules;
    private final ValidationMessageResolver messageResolver;

    // 상수 정의
    private static final int PAYLOAD_MAX_SIZE = 1000; // payload의 최대 크기
    private static final int MAX_PAGE_SIZE = 100; // 최대 페이지 크기
    private static final int MIN_PAGE_SIZE = 1; // 최소 페이지 크기

    /**
     * 활동 생성 커맨드 검증
     */
    public ValidationResult<CreateActivityCommand> validateCreate(CreateActivityCommand command) {
        return getCreateValidator().validate(command);
    }

    /**
     * 활동 조회 쿼리 검증
     */
    public ValidationResult<GetActivityQuery> validateGet(GetActivityQuery query) {
        return getGetValidator().validate(query);
    }

    /**
     * 활동 생성 검증 규칙
     */
    private Validator<CreateActivityCommand> getCreateValidator() {
        return Validator.combine(
                // 활동 유형 검증: 필수
                activityTypeRequired(),
                // 활동 생성자 ID 검증: 필수
                commonValidationRules.userIdRequired(CreateActivityCommand::actorId),
                // payload 검증: 필수, 크기 제한
                payloadRequired(),
                payloadSizeValid(),
                // 컨텍스트 ID 검증: 활동 유형에 따른 필수/선택 검증
                contextIdsValid());
    }

    /**
     * 활동 조회 검증 규칙
     */
    private Validator<GetActivityQuery> getGetValidator() {
        return Validator.combine(
                // 사용자 ID 또는 보드 ID 중 하나는 필수
                userOrBoardIdRequired(),
                // 페이지 번호 검증
                pageNumberValid(),
                // 페이지 크기 검증
                pageSizeValid(),
                // 날짜 범위 검증
                dateRangeValid());
    }

    /**
     * 활동 유형 필수 검증
     */
    private Validator<CreateActivityCommand> activityTypeRequired() {
        return Validator.fieldWithMessage(
                CreateActivityCommand::type,
                type -> type != null,
                "type",
                "validation.activity.type.required",
                messageResolver);
    }

    /**
     * payload 필수 검증
     */
    private Validator<CreateActivityCommand> payloadRequired() {
        return Validator.fieldWithMessage(
                CreateActivityCommand::payload,
                payload -> payload != null && !payload.isEmpty(),
                "payload",
                "validation.activity.payload.required",
                messageResolver);
    }

    /**
     * payload 크기 검증
     */
    private Validator<CreateActivityCommand> payloadSizeValid() {
        return Validator.fieldWithMessage(
                CreateActivityCommand::payload,
                payload -> payload == null || payload.size() <= PAYLOAD_MAX_SIZE,
                "payload",
                "validation.activity.payload.size.exceeded",
                messageResolver,
                PAYLOAD_MAX_SIZE);
    }

    /**
     * 컨텍스트 ID 검증 (활동 유형에 따른 필수/선택 검증)
     */
    private Validator<CreateActivityCommand> contextIdsValid() {
        return command -> {
            ActivityType type = command.type();

            // 카드 관련 활동
            if (isCardActivity(type)) {
                if (command.cardId() == null) {
                    return ValidationResult.invalid(
                            "cardId",
                            messageResolver.getDomainValidationMessage("activity", "cardId", "required"),
                            null);
                }
                if (command.listId() == null) {
                    return ValidationResult.invalid(
                            "listId",
                            messageResolver.getDomainValidationMessage("activity", "listId", "required"),
                            null);
                }
                if (command.boardId() == null) {
                    return ValidationResult.invalid(
                            "boardId",
                            messageResolver.getDomainValidationMessage("activity", "boardId", "required"),
                            null);
                }
            }
            // 리스트 관련 활동
            else if (isListActivity(type)) {
                if (command.listId() == null) {
                    return ValidationResult.invalid(
                            "listId",
                            messageResolver.getDomainValidationMessage("activity", "listId", "required"),
                            null);
                }
                if (command.boardId() == null) {
                    return ValidationResult.invalid(
                            "boardId",
                            messageResolver.getDomainValidationMessage("activity", "boardId", "required"),
                            null);
                }
            }
            // 보드 관련 활동
            else if (isBoardActivity(type)) {
                if (command.boardId() == null) {
                    return ValidationResult.invalid(
                            "boardId",
                            messageResolver.getDomainValidationMessage("activity", "boardId", "required"),
                            null);
                }
            }
            // 사용자 관련 활동은 추가 컨텍스트 ID가 필요하지 않음

            return ValidationResult.valid(command);
        };
    }

    /**
     * 사용자 ID 또는 보드 ID 중 하나는 필수
     */
    private Validator<GetActivityQuery> userOrBoardIdRequired() {
        return query -> {
            if (query.userId() != null || query.boardId() != null) {
                return ValidationResult.valid(query);
            }
            return ValidationResult.invalid(
                    "userId,boardId",
                    messageResolver.getDomainValidationMessage("activity", "query", "userOrBoardId.required"),
                    null);
        };
    }

    /**
     * 페이지 번호 검증
     */
    private Validator<GetActivityQuery> pageNumberValid() {
        return query -> {
            int page = query.getPageOrDefault();
            if (page >= 0) {
                return ValidationResult.valid(query);
            }
            return ValidationResult.invalid(
                    "page",
                    messageResolver.getDomainValidationMessage("activity", "query", "page.negative"),
                    page);
        };
    }

    /**
     * 페이지 크기 검증
     */
    private Validator<GetActivityQuery> pageSizeValid() {
        return query -> {
            int size = query.getSizeOrDefault();
            if (size >= MIN_PAGE_SIZE && size <= MAX_PAGE_SIZE) {
                return ValidationResult.valid(query);
            }
            return ValidationResult.invalid(
                    "size",
                    messageResolver.getDomainValidationMessage("activity", "query", "size.range", MIN_PAGE_SIZE,
                            MAX_PAGE_SIZE),
                    size);
        };
    }

    /**
     * 날짜 범위 검증
     */
    private Validator<GetActivityQuery> dateRangeValid() {
        return query -> {
            Instant since = query.since();
            Instant until = query.until();

            if (since != null && until != null && since.isAfter(until)) {
                return ValidationResult.invalid(
                        "since,until",
                        messageResolver.getDomainValidationMessage("activity", "dateRange", "invalid"),
                        Map.of("since", since, "until", until));
            }

            return ValidationResult.valid(query);
        };
    }

    /**
     * 카드 관련 활동인지 확인
     */
    private boolean isCardActivity(ActivityType type) {
        return type != null && (type == ActivityType.CARD_CREATE ||
                type == ActivityType.CARD_MOVE ||
                type == ActivityType.CARD_RENAME ||
                type == ActivityType.CARD_ARCHIVE ||
                type == ActivityType.CARD_DELETE ||
                type == ActivityType.CARD_ASSIGN_MEMBER ||
                type == ActivityType.CARD_UNASSIGN_MEMBER ||
                type == ActivityType.CARD_SET_DUE_DATE ||
                type == ActivityType.CARD_ADD_COMMENT ||
                type == ActivityType.CARD_ADD_ATTACHMENT ||
                type == ActivityType.CARD_ADD_CHECKLIST ||
                type == ActivityType.CARD_DUPLICATE ||
                type == ActivityType.CARD_UPDATE_DESCRIPTION);
    }

    /**
     * 리스트 관련 활동인지 확인
     */
    private boolean isListActivity(ActivityType type) {
        return type != null && (type == ActivityType.LIST_CREATE ||
                type == ActivityType.LIST_RENAME ||
                type == ActivityType.LIST_ARCHIVE ||
                type == ActivityType.LIST_MOVE ||
                type == ActivityType.LIST_CHANGE_COLOR ||
                type == ActivityType.LIST_DELETE);
    }

    /**
     * 보드 관련 활동인지 확인
     */
    private boolean isBoardActivity(ActivityType type) {
        return type != null && (type == ActivityType.BOARD_CREATE ||
                type == ActivityType.BOARD_RENAME ||
                type == ActivityType.BOARD_ARCHIVE ||
                type == ActivityType.BOARD_MOVE ||
                type == ActivityType.BOARD_DELETE ||
                type == ActivityType.BOARD_UPDATE_DESCRIPTION ||
                type == ActivityType.BOARD_ADD_MEMBER ||
                type == ActivityType.BOARD_REMOVE_MEMBER ||
                type == ActivityType.BOARD_UPDATE_MEMBER_ROLE ||
                type == ActivityType.BOARD_DUPLICATE);
    }
}