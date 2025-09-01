package com.boardly.application.port.in.validation.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.boardly.application.port.in.traits.Identifiable;
import com.boardly.application.port.in.validation.ValidationResult;
import com.boardly.domain.user.UserId;
import com.boardly.domain.board.BoardId;
import com.boardly.domain.workspace.WorkspaceId;
import com.boardly.shared.DomainIdPrefixes;

import static org.assertj.core.api.Assertions.*;

/**
 * IdValidator 테스트
 * <p>
 * IdValidator의 모든 public 메서드에 대한 포괄적인 테스트를 제공합니다.
 * - validate(): Identifiable 객체의 ID 검증
 * - validateId(): EntityId 직접 검증
 * - validateIdString(): ID 문자열 검증
 * </p>
 */
@DisplayName("IdValidator 테스트")
class IdValidatorTest {

    /**
     * 테스트용 Identifiable 구현체 - UserId 사용
     */
    private record TestUserCommand(UserId userId) implements Identifiable<UserId> {
        @Override
        public UserId id() {
            return userId;
        }
    }

    /**
     * 테스트용 Identifiable 구현체 - BoardId 사용
     */
    private record TestBoardCommand(BoardId boardId) implements Identifiable<BoardId> {
        @Override
        public BoardId id() {
            return boardId;
        }
    }

    /**
     * 테스트용 Identifiable 구현체 - WorkspaceId 사용
     */
    private record TestWorkspaceCommand(WorkspaceId workspaceId) implements Identifiable<WorkspaceId> {
        @Override
        public WorkspaceId id() {
            return workspaceId;
        }
    }

    /**
     * 유효한 EntityId 문자열 생성 헬퍼
     */
    private String createValidEntityId(String prefix) {
        return prefix + "01ARZ3NDEKTSV4RRFFQ69G5FAV";
    }

    /**
     * 잘못된 형식의 EntityId 문자열 생성 헬퍼
     */
    private String createInvalidEntityId(String prefix) {
        return prefix + "INVALID_ULID_FORMAT";
    }

    @Nested
    @DisplayName("validate() 메서드 테스트")
    class ValidateMethodTest {

        @Test
        @DisplayName("유효한 UserId를 가진 Identifiable 객체 - 검증 성공")
        void givenValidUserId_whenValidate_thenReturnsValidResult() {
            // given
            UserId validUserId = new UserId(createValidEntityId(DomainIdPrefixes.USER));
            var command = new TestUserCommand(validUserId);

            // when
            ValidationResult<TestUserCommand> result = IdValidator.<UserId, TestUserCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("유효한 BoardId를 가진 Identifiable 객체 - 검증 성공")
        void givenValidBoardId_whenValidate_thenReturnsValidResult() {
            // given
            BoardId validBoardId = new BoardId(createValidEntityId(DomainIdPrefixes.BOARD));
            var command = new TestBoardCommand(validBoardId);

            // when
            ValidationResult<TestBoardCommand> result = IdValidator.<BoardId, TestBoardCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("유효한 WorkspaceId를 가진 Identifiable 객체 - 검증 성공")
        void givenValidWorkspaceId_whenValidate_thenReturnsValidResult() {
            // given
            WorkspaceId validWorkspaceId = new WorkspaceId(createValidEntityId(DomainIdPrefixes.WORKSPACE));
            var command = new TestWorkspaceCommand(validWorkspaceId);

            // when
            ValidationResult<TestWorkspaceCommand> result = IdValidator.<WorkspaceId, TestWorkspaceCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("null ID를 가진 Identifiable 객체 - NullPointerException 발생")
        void givenNullId_whenValidate_thenThrowsNullPointerException() {
            // given
            var command = new TestUserCommand(null);

            // when & then
            assertThatThrownBy(() -> IdValidator.<UserId, TestUserCommand>validate().validate(command))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Cannot invoke \"com.boardly.shared.EntityId.getValue()\" because \"id\" is null");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "usr01ARZ3NDEKTSV4RRFFQ69G5FAV",
                "usr01ARZ3NDEKTSV4RRFFQ69G5FAW",
                "usr01ARZ3NDEKTSV4RRFFQ69G5FAX",
                "usr01ARZ3NDEKTSV4RRFFQ69G5FAY",
                "usr01ARZ3NDEKTSV4RRFFQ69G5FAZ"
        })
        @DisplayName("다양한 유효한 UserId 형식들 - 검증 성공")
        void givenVariousValidUserIdFormats_whenValidate_thenReturnsValidResult(String validUserIdString) {
            // given
            UserId validUserId = new UserId(validUserIdString);
            var command = new TestUserCommand(validUserId);

            // when
            ValidationResult<TestUserCommand> result = IdValidator.<UserId, TestUserCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("다른 도메인의 유효한 EntityId 형식들 - 각각 해당 도메인 ID로 검증 성공")
        void givenValidOtherDomainEntityIds_whenValidate_thenReturnsValidResult() {
            // given - BoardId 테스트
            BoardId validBoardId = new BoardId("brd01ARZ3NDEKTSV4RRFFQ69G5FAV");
            var boardCommand = new TestBoardCommand(validBoardId);

            // when
            ValidationResult<TestBoardCommand> boardResult = IdValidator.<BoardId, TestBoardCommand>validate().validate(boardCommand);

            // then
            assertThat(boardResult.isValid()).isTrue();
            assertThat(boardResult.get()).isEqualTo(boardCommand);

            // given - WorkspaceId 테스트
            WorkspaceId validWorkspaceId = new WorkspaceId("wsp01ARZ3NDEKTSV4RRFFQ69G5FAV");
            var workspaceCommand = new TestWorkspaceCommand(validWorkspaceId);

            // when
            ValidationResult<TestWorkspaceCommand> workspaceResult = IdValidator.<WorkspaceId, TestWorkspaceCommand>validate().validate(workspaceCommand);

            // then
            assertThat(workspaceResult.isValid()).isTrue();
            assertThat(workspaceResult.get()).isEqualTo(workspaceCommand);
        }
    }

    @Nested
    @DisplayName("validateId() 메서드 테스트")
    class ValidateIdMethodTest {

        @Test
        @DisplayName("유효한 UserId 직접 검증 - 검증 성공")
        void givenValidUserId_whenValidateId_thenReturnsValidResult() {
            // given
            UserId validUserId = new UserId(createValidEntityId(DomainIdPrefixes.USER));

            // when
            ValidationResult<UserId> result = IdValidator.<UserId>validateId().validate(validUserId);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(validUserId);
        }

        @Test
        @DisplayName("유효한 BoardId 직접 검증 - 검증 성공")
        void givenValidBoardId_whenValidateId_thenReturnsValidResult() {
            // given
            BoardId validBoardId = new BoardId(createValidEntityId(DomainIdPrefixes.BOARD));

            // when
            ValidationResult<BoardId> result = IdValidator.<BoardId>validateId().validate(validBoardId);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(validBoardId);
        }

        @Test
        @DisplayName("유효한 WorkspaceId 직접 검증 - 검증 성공")
        void givenValidWorkspaceId_whenValidateId_thenReturnsValidResult() {
            // given
            WorkspaceId validWorkspaceId = new WorkspaceId(createValidEntityId(DomainIdPrefixes.WORKSPACE));

            // when
            ValidationResult<WorkspaceId> result = IdValidator.<WorkspaceId>validateId().validate(validWorkspaceId);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(validWorkspaceId);
        }

        @Test
        @DisplayName("null EntityId 직접 검증 - NullPointerException 발생")
        void givenNullEntityId_whenValidateId_thenThrowsNullPointerException() {
            // given
            UserId nullUserId = null;

            // when & then
            assertThatThrownBy(() -> IdValidator.<UserId>validateId().validate(nullUserId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Cannot invoke \"com.boardly.shared.EntityId.getValue()\" because \"target\" is null");
        }

        @Test
        @DisplayName("다양한 유효한 EntityId 형식들 직접 검증 - 검증 성공")
        void givenVariousValidEntityIdFormats_whenValidateId_thenReturnsValidResult() {
            // given - UserId 테스트
            UserId validUserId = new UserId("usr01ARZ3NDEKTSV4RRFFQ69G5FAV");

            // when
            ValidationResult<UserId> userResult = IdValidator.<UserId>validateId().validate(validUserId);

            // then
            assertThat(userResult.isValid()).isTrue();
            assertThat(userResult.get()).isEqualTo(validUserId);

            // given - BoardId 테스트
            BoardId validBoardId = new BoardId("brd01ARZ3NDEKTSV4RRFFQ69G5FAV");

            // when
            ValidationResult<BoardId> boardResult = IdValidator.<BoardId>validateId().validate(validBoardId);

            // then
            assertThat(boardResult.isValid()).isTrue();
            assertThat(boardResult.get()).isEqualTo(validBoardId);

            // given - WorkspaceId 테스트
            WorkspaceId validWorkspaceId = new WorkspaceId("wsp01ARZ3NDEKTSV4RRFFQ69G5FAV");

            // when
            ValidationResult<WorkspaceId> workspaceResult = IdValidator.<WorkspaceId>validateId().validate(validWorkspaceId);

            // then
            assertThat(workspaceResult.isValid()).isTrue();
            assertThat(workspaceResult.get()).isEqualTo(validWorkspaceId);
        }
    }

    @Nested
    @DisplayName("validateIdString() 메서드 테스트")
    class ValidateIdStringMethodTest {

        @Test
        @DisplayName("유효한 ID 문자열 - 검증 성공")
        void givenValidIdString_whenValidateIdString_thenReturnsValidResult() {
            // given
            String validIdString = createValidEntityId(DomainIdPrefixes.USER);

            // when
            ValidationResult<String> result = IdValidator.validateIdString("id").validate(validIdString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(validIdString);
        }

        @Test
        @DisplayName("null ID 문자열 - 검증 실패")
        void givenNullIdString_whenValidateIdString_thenReturnsInvalidResult() {
            // given
            String nullIdString = null;

            // when
            ValidationResult<String> result = IdValidator.validateIdString("id").validate(nullIdString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("id");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("빈 문자열 및 공백 문자열 - 검증 실패")
        void givenBlankIdString_whenValidateIdString_thenReturnsInvalidResult(String blankIdString) {
            // when
            ValidationResult<String> result = IdValidator.validateIdString("id").validate(blankIdString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("id");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("잘못된 형식의 ID 문자열 - 검증 성공 (notBlank만 검증)")
        void givenInvalidFormatIdString_whenValidateIdString_thenReturnsValidResult() {
            // given - validateIdString은 notBlank만 검증하므로 잘못된 형식도 통과
            String invalidFormatIdString = "invalid_id_format";

            // when
            ValidationResult<String> result = IdValidator.validateIdString("id").validate(invalidFormatIdString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(invalidFormatIdString);
        }

        @Test
        @DisplayName("특수문자가 포함된 ID 문자열 - 검증 성공 (notBlank만 검증)")
        void givenIdStringWithSpecialCharacters_whenValidateIdString_thenReturnsValidResult() {
            // given
            String specialCharIdString = "usr@#$%^&*()";

            // when
            ValidationResult<String> result = IdValidator.validateIdString("id").validate(specialCharIdString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(specialCharIdString);
        }

        @Test
        @DisplayName("숫자로만 구성된 ID 문자열 - 검증 성공 (notBlank만 검증)")
        void givenNumericIdString_whenValidateIdString_thenReturnsValidResult() {
            // given
            String numericIdString = "123456789";

            // when
            ValidationResult<String> result = IdValidator.validateIdString("id").validate(numericIdString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(numericIdString);
        }

        @Test
        @DisplayName("한글이 포함된 ID 문자열 - 검증 성공 (notBlank만 검증)")
        void givenKoreanIdString_whenValidateIdString_thenReturnsValidResult() {
            // given
            String koreanIdString = "사용자아이디";

            // when
            ValidationResult<String> result = IdValidator.validateIdString("id").validate(koreanIdString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(koreanIdString);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "usr01ARZ3NDEKTSV4RRFFQ69G5FAV",
                "brd01ARZ3NDEKTSV4RRFFQ69G5FAV",
                "wsp01ARZ3NDEKTSV4RRFFQ69G5FAV",
                "invalid_format",
                "special@#$%",
                "123456789",
                "한글아이디",
                "mixed123한글"
        })
        @DisplayName("다양한 ID 문자열 형식들 - 검증 성공 (notBlank만 검증)")
        void givenVariousIdStringFormats_whenValidateIdString_thenReturnsValidResult(String idString) {
            // when
            ValidationResult<String> result = IdValidator.validateIdString("id").validate(idString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(idString);
        }

        @Test
        @DisplayName("커스텀 필드명으로 ID 문자열 검증 - 검증 성공")
        void givenCustomFieldName_whenValidateIdString_thenReturnsValidResult() {
            // given
            String validIdString = "usr01ARZ3NDEKTSV4RRFFQ69G5FAV";
            String customFieldName = "userId";

            // when
            ValidationResult<String> result = IdValidator.validateIdString(customFieldName).validate(validIdString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(validIdString);
        }

        @Test
        @DisplayName("커스텀 필드명으로 null ID 문자열 검증 - 검증 실패")
        void givenCustomFieldNameAndNullIdString_whenValidateIdString_thenReturnsInvalidResult() {
            // given
            String nullIdString = null;
            String customFieldName = "userId";

            // when
            ValidationResult<String> result = IdValidator.validateIdString(customFieldName).validate(nullIdString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("userId");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }
    }

    @Nested
    @DisplayName("검증 체이닝 동작 테스트")
    class ValidationChainingTest {

        @Test
        @DisplayName("유효한 EntityId로 체이닝 검증 - 모든 검증 통과")
        void givenValidEntityId_whenValidateWithChaining_thenAllValidationsPass() {
            // given
            UserId validUserId = new UserId(createValidEntityId(DomainIdPrefixes.USER));
            var command = new TestUserCommand(validUserId);

            // when
            ValidationResult<TestUserCommand> result = IdValidator.<UserId, TestUserCommand>validate().validate(command);

            // then - notBlank -> EntityId 형식 검증 순서로 체이닝되어 모두 통과
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("null ID로 체이닝 검증 - 첫 번째 검증에서 NullPointerException 발생")
        void givenNullId_whenValidateWithChaining_thenThrowsNullPointerException() {
            // given
            var command = new TestUserCommand(null);

            // when & then - null로 인해 getValue() 호출 시 NullPointerException 발생
            assertThatThrownBy(() -> IdValidator.<UserId, TestUserCommand>validate().validate(command))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Cannot invoke \"com.boardly.shared.EntityId.getValue()\" because \"id\" is null");
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCasesTest {

        @Test
        @DisplayName("최소 길이 EntityId - 검증 성공")
        void givenMinLengthEntityId_whenValidate_thenReturnsValidResult() {
            // given - 29자 (prefix 3자 + ULID 26자)
            UserId minLengthUserId = new UserId("usr01ARZ3NDEKTSV4RRFFQ69G5FAV");
            var command = new TestUserCommand(minLengthUserId);

            // when
            ValidationResult<TestUserCommand> result = IdValidator.<UserId, TestUserCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("최대 길이 EntityId - 검증 성공")
        void givenMaxLengthEntityId_whenValidate_thenReturnsValidResult() {
            // given - 29자 (prefix 3자 + ULID 26자)
            UserId maxLengthUserId = new UserId("usr01ARZ3NDEKTSV4RRFFQ69G5FAV");
            var command = new TestUserCommand(maxLengthUserId);

            // when
            ValidationResult<TestUserCommand> result = IdValidator.<UserId, TestUserCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("잘못된 prefix로 EntityId 생성 시도 - IllegalArgumentException 발생")
        void givenInvalidPrefix_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given - 다른 도메인의 prefix로는 UserId를 생성할 수 없음
            String boardIdString = createValidEntityId(DomainIdPrefixes.BOARD);

            // when & then
            assertThatThrownBy(() -> new UserId(boardIdString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid EntityId format: must start with 'usr'");
        }

        @Test
        @DisplayName("잘못된 ULID 형식으로 EntityId 생성 시도 - IllegalArgumentException 발생")
        void givenInvalidUlidFormat_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given
            String invalidUlidString = "usrINVALID_ULID_FORMAT";

            // when & then
            assertThatThrownBy(() -> new UserId(invalidUlidString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EntityId ULID part must be 26 characters long");
        }

        @Test
        @DisplayName("짧은 ULID로 EntityId 생성 시도 - IllegalArgumentException 발생")
        void givenShortUlid_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given
            String shortUlidString = "usr01ARZ3NDEKTSV4RRFFQ69G5FA"; // 28자 (1자 부족)

            // when & then
            assertThatThrownBy(() -> new UserId(shortUlidString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EntityId ULID part must be 26 characters long");
        }

        @Test
        @DisplayName("긴 ULID로 EntityId 생성 시도 - IllegalArgumentException 발생")
        void givenLongUlid_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given
            String longUlidString = "usr01ARZ3NDEKTSV4RRFFQ69G5FAVA"; // 30자 (1자 초과)

            // when & then
            assertThatThrownBy(() -> new UserId(longUlidString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EntityId ULID part must be 26 characters long");
        }

        @Test
        @DisplayName("null 문자열로 EntityId 생성 시도 - IllegalArgumentException 발생")
        void givenNullString_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> new UserId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EntityId cannot be null or empty");
        }

        @Test
        @DisplayName("빈 문자열로 EntityId 생성 시도 - IllegalArgumentException 발생")
        void givenEmptyString_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> new UserId(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EntityId cannot be null or empty");
        }

        @Test
        @DisplayName("공백 문자열로 EntityId 생성 시도 - IllegalArgumentException 발생")
        void givenBlankString_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> new UserId("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EntityId cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("EntityId 형식 검증 테스트")
    class EntityIdFormatValidationTest {

        @Test
        @DisplayName("유효한 ULID 문자만 포함된 EntityId - 검증 성공")
        void givenValidUlidCharacters_whenValidate_thenReturnsValidResult() {
            // given - Crockford's Base32: 0-9, A-Z except I, L, O, U
            UserId validUserId = new UserId("usr01ARZ3NDEKTSV4RRFFQ69G5FAV");
            var command = new TestUserCommand(validUserId);

            // when
            ValidationResult<TestUserCommand> result = IdValidator.<UserId, TestUserCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("금지된 문자 I가 포함된 ULID - IllegalArgumentException 발생")
        void givenUlidWithForbiddenCharacterI_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given
            String invalidUlidString = "usr01ARZ3NDEKTSV4RRFFQ69G5FAI"; // I 포함

            // when & then
            assertThatThrownBy(() -> new UserId(invalidUlidString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid EntityId format: ULID part contains invalid characters");
        }

        @Test
        @DisplayName("금지된 문자 L이 포함된 ULID - IllegalArgumentException 발생")
        void givenUlidWithForbiddenCharacterL_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given
            String invalidUlidString = "usr01ARZ3NDEKTSV4RRFFQ69G5FAL"; // L 포함

            // when & then
            assertThatThrownBy(() -> new UserId(invalidUlidString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid EntityId format: ULID part contains invalid characters");
        }

        @Test
        @DisplayName("금지된 문자 O가 포함된 ULID - IllegalArgumentException 발생")
        void givenUlidWithForbiddenCharacterO_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given
            String invalidUlidString = "usr01ARZ3NDEKTSV4RRFFQ69G5FAO"; // O 포함

            // when & then
            assertThatThrownBy(() -> new UserId(invalidUlidString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid EntityId format: ULID part contains invalid characters");
        }

        @Test
        @DisplayName("금지된 문자 U가 포함된 ULID - IllegalArgumentException 발생")
        void givenUlidWithForbiddenCharacterU_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given
            String invalidUlidString = "usr01ARZ3NDEKTSV4RRFFQ69G5FAU"; // U 포함

            // when & then
            assertThatThrownBy(() -> new UserId(invalidUlidString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid EntityId format: ULID part contains invalid characters");
        }

        @Test
        @DisplayName("소문자가 포함된 ULID - IllegalArgumentException 발생")
        void givenUlidWithLowercase_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given
            String invalidUlidString = "usr01arz3ndektsv4rrffq69g5fav"; // 소문자 포함

            // when & then
            assertThatThrownBy(() -> new UserId(invalidUlidString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid EntityId format: ULID part contains invalid characters");
        }

        @Test
        @DisplayName("특수문자가 포함된 ULID - IllegalArgumentException 발생")
        void givenUlidWithSpecialCharacters_whenCreateEntityId_thenThrowsIllegalArgumentException() {
            // given
            String invalidUlidString = "usr01ARZ3NDEKTSV4RRFFQ69G5FA@"; // @ 포함

            // when & then
            assertThatThrownBy(() -> new UserId(invalidUlidString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid EntityId format: ULID part contains invalid characters");
        }
    }
}