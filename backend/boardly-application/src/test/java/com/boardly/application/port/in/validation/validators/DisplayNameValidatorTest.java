package com.boardly.application.port.in.validation.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.boardly.application.port.in.traits.HasDisplayName;
import com.boardly.application.port.in.validation.ValidationResult;

import static org.assertj.core.api.Assertions.*;

/**
 * DisplayNameValidator 테스트
 * <p>
 * validate() 메서드, validateDisplayNameString() 메서드, validateDisplayNameLength() 메서드 테스트를 포함합니다.
 * 각 메서드별로 중첩 클래스로 분리하여 테스트 코드를 구성합니다.
 * </p>
 */
@DisplayName("DisplayNameValidator 테스트")
class DisplayNameValidatorTest {

    /**
     * HasDisplayName trait를 구현한 테스트용 객체
     */
    private record TestCommand(String displayName) implements HasDisplayName {
    }

    @Nested
    @DisplayName("validate() 메서드 테스트")
    class ValidateMethodTest {

        @Test
        @DisplayName("유효한 표시 이름 - 성공")
        void givenValidDisplayName_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("홍길동");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("null 표시 이름 - 실패")
        void givenNullDisplayName_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand(null);

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("빈 문자열 표시 이름 - 실패")
        void givenEmptyDisplayName_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand("");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("공백만 있는 표시 이름 - 실패")
        void givenBlankDisplayName_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand("   ");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("너무 긴 표시 이름 - 실패")
        void givenTooLongDisplayName_whenValidate_thenReturnsInvalidResult() {
            // given - 51자 (최대 50자 초과)
            String longDisplayName = "가".repeat(51);
            var command = new TestCommand(longDisplayName);

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "홍길동!",
                "김철수@",
                "이영희#",
                "박민수$",
                "정수진%",
                "최동현^",
                "한지영&",
                "윤태호*",
                "강미영(",
                "임성호)",
                "조현우-",
                "신예진=",
                "오준석[",
                "서유진]",
                "권민수{",
                "황지영}",
                "송태호|",
                "노예진\\",
                "백준석:",
                "남유진;",
                "도민수\"",
                "구지영'",
                "문태호<",
                "유예진>",
                "홍준석,",
                "김유진.",
                "이민수?",
                "박지영/",
                "정태호`",
                "최예진~"
        })
        @DisplayName("특수문자가 포함된 표시 이름 - 실패")
        void givenDisplayNameWithSpecialCharacters_whenValidate_thenReturnsInvalidResult(String invalidDisplayName) {
            // given
            var command = new TestCommand(invalidDisplayName);

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "홍길동",
                "김철수",
                "이영희",
                "박민수",
                "정수진",
                "최동현",
                "한지영",
                "윤태호",
                "강미영",
                "임성호",
                "조현우",
                "신예진",
                "오준석",
                "서유진",
                "권민수",
                "황지영",
                "송태호",
                "노예진",
                "백준석",
                "남유진",
                "도민수",
                "구지영",
                "문태호",
                "유예진",
                "홍준석",
                "김유진",
                "이민수",
                "박지영",
                "정태호",
                "최예진",
                "John Doe",
                "Jane Smith",
                "Michael Johnson",
                "Sarah Wilson",
                "David Brown",
                "Lisa Davis",
                "Robert Miller",
                "Jennifer Garcia",
                "William Martinez",
                "Jessica Anderson",
                "홍길동123",
                "김철수456",
                "이영희789",
                "John123",
                "Jane456",
                "홍길동 John",
                "김철수 Smith",
                "이영희 Johnson",
                "John 홍길동",
                "Jane 김철수",
                "홍길동 123",
                "김철수 456",
                "이영희 789",
                "John 123",
                "Jane 456"
        })
        @DisplayName("유효한 표시 이름 형식들 - 성공")
        void givenValidDisplayNameFormats_whenValidate_thenReturnsValidResult(String validDisplayName) {
            // given
            var command = new TestCommand(validDisplayName);

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("경계값 테스트 - 최소 길이 표시 이름")
        void givenMinLengthDisplayName_whenValidate_thenReturnsValidResult() {
            // given - 1자리 최소 길이
            var command = new TestCommand("홍");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("경계값 테스트 - 최대 길이 표시 이름")
        void givenMaxLengthDisplayName_whenValidate_thenReturnsValidResult() {
            // given - 50자리 최대 길이
            String maxDisplayName = "가".repeat(50);
            var command = new TestCommand(maxDisplayName);

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }
    }

    @Nested
    @DisplayName("validateDisplayNameString() 메서드 테스트")
    class ValidateDisplayNameStringMethodTest {

        @Test
        @DisplayName("유효한 표시 이름 문자열 - 성공")
        void givenValidDisplayNameString_whenValidateDisplayNameString_thenReturnsValidResult() {
            // given
            String displayNameString = "홍길동";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(displayNameString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(displayNameString);
        }

        @Test
        @DisplayName("null 표시 이름 문자열 - 실패")
        void givenNullDisplayNameString_whenValidateDisplayNameString_thenReturnsInvalidResult() {
            // given
            String displayNameString = null;

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(displayNameString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("빈 표시 이름 문자열 - 실패")
        void givenEmptyDisplayNameString_whenValidateDisplayNameString_thenReturnsInvalidResult() {
            // given
            String displayNameString = "";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(displayNameString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("공백만 있는 표시 이름 문자열 - 실패")
        void givenBlankDisplayNameString_whenValidateDisplayNameString_thenReturnsInvalidResult() {
            // given
            String displayNameString = "   ";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(displayNameString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("너무 긴 표시 이름 문자열 - 실패")
        void givenTooLongDisplayNameString_whenValidateDisplayNameString_thenReturnsInvalidResult() {
            // given - 51자 (최대 50자 초과)
            String longDisplayName = "가".repeat(51);

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(longDisplayName);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "홍길동!",
                "김철수@",
                "이영희#",
                "박민수$",
                "정수진%",
                "최동현^",
                "한지영&",
                "윤태호*",
                "강미영(",
                "임성호)",
                "조현우-",
                "신예진=",
                "오준석[",
                "서유진]",
                "권민수{",
                "황지영}",
                "송태호|",
                "노예진\\",
                "백준석:",
                "남유진;",
                "도민수\"",
                "구지영'",
                "문태호<",
                "유예진>",
                "홍준석,",
                "김유진.",
                "이민수?",
                "박지영/",
                "정태호`",
                "최예진~"
        })
        @DisplayName("특수문자가 포함된 표시 이름 문자열 - 실패")
        void givenDisplayNameStringWithSpecialCharacters_whenValidateDisplayNameString_thenReturnsInvalidResult(String invalidDisplayName) {
            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(invalidDisplayName);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "홍길동",
                "김철수",
                "이영희",
                "박민수",
                "정수진",
                "최동현",
                "한지영",
                "윤태호",
                "강미영",
                "임성호",
                "조현우",
                "신예진",
                "오준석",
                "서유진",
                "권민수",
                "황지영",
                "송태호",
                "노예진",
                "백준석",
                "남유진",
                "도민수",
                "구지영",
                "문태호",
                "유예진",
                "홍준석",
                "김유진",
                "이민수",
                "박지영",
                "정태호",
                "최예진",
                "John Doe",
                "Jane Smith",
                "Michael Johnson",
                "Sarah Wilson",
                "David Brown",
                "Lisa Davis",
                "Robert Miller",
                "Jennifer Garcia",
                "William Martinez",
                "Jessica Anderson",
                "홍길동123",
                "김철수456",
                "이영희789",
                "John123",
                "Jane456",
                "홍길동 John",
                "김철수 Smith",
                "이영희 Johnson",
                "John 홍길동",
                "Jane 김철수",
                "홍길동 123",
                "김철수 456",
                "이영희 789",
                "John 123",
                "Jane 456"
        })
        @DisplayName("유효한 표시 이름 형식 문자열들 - 성공")
        void givenValidDisplayNameFormatStrings_whenValidateDisplayNameString_thenReturnsValidResult(String validDisplayName) {
            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(validDisplayName);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(validDisplayName);
        }

        @Test
        @DisplayName("경계값 테스트 - 최소 길이 표시 이름 문자열")
        void givenMinLengthDisplayNameString_whenValidateDisplayNameString_thenReturnsValidResult() {
            // given - 1자리 최소 길이
            String displayNameString = "홍";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(displayNameString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(displayNameString);
        }

        @Test
        @DisplayName("경계값 테스트 - 최대 길이 표시 이름 문자열")
        void givenMaxLengthDisplayNameString_whenValidateDisplayNameString_thenReturnsValidResult() {
            // given - 50자리 최대 길이
            String maxDisplayName = "가".repeat(50);

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(maxDisplayName);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(maxDisplayName);
        }
    }

    @Nested
    @DisplayName("validateDisplayNameLength() 메서드 테스트")
    class ValidateDisplayNameLengthMethodTest {

        @Test
        @DisplayName("유효한 표시 이름 길이 - 성공")
        void givenValidDisplayNameLength_whenValidateDisplayNameLength_thenReturnsValidResult() {
            // given
            String displayNameString = "홍길동";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameLength().validate(displayNameString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(displayNameString);
        }

        @Test
        @DisplayName("null 표시 이름 - 실패")
        void givenNullDisplayName_whenValidateDisplayNameLength_thenReturnsInvalidResult() {
            // given
            String displayNameString = null;

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameLength().validate(displayNameString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("빈 표시 이름 - 실패")
        void givenEmptyDisplayName_whenValidateDisplayNameLength_thenReturnsInvalidResult() {
            // given
            String displayNameString = "";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameLength().validate(displayNameString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("공백만 있는 표시 이름 - 실패")
        void givenBlankDisplayName_whenValidateDisplayNameLength_thenReturnsInvalidResult() {
            // given
            String displayNameString = "   ";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameLength().validate(displayNameString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("너무 긴 표시 이름 - 실패")
        void givenTooLongDisplayName_whenValidateDisplayNameLength_thenReturnsInvalidResult() {
            // given - 51자 (최대 50자 초과)
            String longDisplayName = "가".repeat(51);

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameLength().validate(longDisplayName);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
        }

        @Test
        @DisplayName("특수문자가 포함된 표시 이름 - 성공 (길이 검증만 수행)")
        void givenDisplayNameWithSpecialCharacters_whenValidateDisplayNameLength_thenReturnsValidResult() {
            // given - 특수문자가 포함되어 있지만 길이는 유효함
            String displayNameWithSpecialChars = "홍길동!@#";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameLength().validate(displayNameWithSpecialChars);

            // then - 길이 검증만 수행하므로 성공
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(displayNameWithSpecialChars);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "홍길동!@#",
                "김철수$%^",
                "이영희&*()",
                "박민수-=",
                "정수진[]",
                "최동현{}",
                "한지영|\\",
                "윤태호:;",
                "강미영\"'",
                "임성호<>",
                "조현우,.",
                "신예진?/",
                "오준석`~",
                "서유진123!@#",
                "권민수456$%^",
                "황지영789&*()",
                "John Doe!@#",
                "Jane Smith$%^",
                "홍길동 John!@#",
                "김철수 Smith$%^"
        })
        @DisplayName("특수문자가 포함된 표시 이름들 - 성공 (길이 검증만 수행)")
        void givenDisplayNamesWithSpecialCharacters_whenValidateDisplayNameLength_thenReturnsValidResult(String displayNameWithSpecialChars) {
            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameLength().validate(displayNameWithSpecialChars);

            // then - 길이 검증만 수행하므로 성공
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(displayNameWithSpecialChars);
        }

        @Test
        @DisplayName("경계값 테스트 - 최소 길이 표시 이름")
        void givenMinLengthDisplayName_whenValidateDisplayNameLength_thenReturnsValidResult() {
            // given - 1자리 최소 길이
            String displayNameString = "홍";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameLength().validate(displayNameString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(displayNameString);
        }

        @Test
        @DisplayName("경계값 테스트 - 최대 길이 표시 이름")
        void givenMaxLengthDisplayName_whenValidateDisplayNameLength_thenReturnsValidResult() {
            // given - 50자리 최대 길이
            String maxDisplayName = "가".repeat(50);

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameLength().validate(maxDisplayName);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(maxDisplayName);
        }
    }

    @Nested
    @DisplayName("추가 엣지 케이스 테스트")
    class AdditionalEdgeCasesTest {

        @Test
        @DisplayName("탭 문자가 포함된 표시 이름 - 성공 (\\s 패턴이 탭을 허용)")
        void givenDisplayNameWithTabCharacter_whenValidate_thenReturnsValidResult() {
            // given - \\s 패턴이 탭 문자를 허용하므로 성공
            var command = new TestCommand("홍길동\t");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("개행 문자가 포함된 표시 이름 - 성공 (\\s 패턴이 개행을 허용)")
        void givenDisplayNameWithNewlineCharacter_whenValidate_thenReturnsValidResult() {
            // given - \\s 패턴이 개행 문자를 허용하므로 성공
            var command = new TestCommand("홍길동\n");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("캐리지 리턴 문자가 포함된 표시 이름 - 성공 (\\s 패턴이 캐리지 리턴을 허용)")
        void givenDisplayNameWithCarriageReturnCharacter_whenValidate_thenReturnsValidResult() {
            // given - \\s 패턴이 캐리지 리턴 문자를 허용하므로 성공
            var command = new TestCommand("홍길동\r");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("유니코드 이모지가 포함된 표시 이름 - 실패")
        void givenDisplayNameWithEmoji_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand("홍길동😀");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @Test
        @DisplayName("유니코드 특수 기호가 포함된 표시 이름 - 실패")
        void givenDisplayNameWithUnicodeSymbols_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand("홍길동★");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @Test
        @DisplayName("중국어 문자가 포함된 표시 이름 - 실패")
        void givenDisplayNameWithChineseCharacters_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand("홍길동王");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @Test
        @DisplayName("일본어 문자가 포함된 표시 이름 - 실패")
        void givenDisplayNameWithJapaneseCharacters_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand("홍길동田中");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @Test
        @DisplayName("아랍어 문자가 포함된 표시 이름 - 실패")
        void givenDisplayNameWithArabicCharacters_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand("홍길동أحمد");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @Test
        @DisplayName("키릴 문자가 포함된 표시 이름 - 실패")
        void givenDisplayNameWithCyrillicCharacters_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand("홍길동Иван");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @Test
        @DisplayName("숫자로만 구성된 표시 이름 - 성공")
        void givenDisplayNameWithOnlyNumbers_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("123456");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("영문자로만 구성된 표시 이름 - 성공")
        void givenDisplayNameWithOnlyEnglishLetters_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("JohnDoe");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("한글로만 구성된 표시 이름 - 성공")
        void givenDisplayNameWithOnlyKoreanLetters_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("홍길동");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("공백으로만 구성된 표시 이름 - 실패")
        void givenDisplayNameWithOnlySpaces_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand("     ");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("앞뒤 공백이 있는 유효한 표시 이름 - 성공")
        void givenValidDisplayNameWithLeadingTrailingSpaces_whenValidate_thenReturnsValidResult() {
            // given - 앞뒤 공백이 있어도 패턴은 통과 (trim은 하지 않음)
            var command = new TestCommand(" 홍길동 ");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("중간에 여러 공백이 있는 표시 이름 - 성공")
        void givenDisplayNameWithMultipleSpaces_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("홍  길  동");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("대소문자 혼합 영문 표시 이름 - 성공")
        void givenDisplayNameWithMixedCaseEnglish_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("JohnDoe");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("한글과 영문이 혼합된 표시 이름 - 성공")
        void givenDisplayNameWithKoreanAndEnglish_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("홍길동John");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("한글과 숫자가 혼합된 표시 이름 - 성공")
        void givenDisplayNameWithKoreanAndNumbers_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("홍길동123");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("영문과 숫자가 혼합된 표시 이름 - 성공")
        void givenDisplayNameWithEnglishAndNumbers_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("John123");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("모든 허용 문자 타입이 혼합된 표시 이름 - 성공")
        void givenDisplayNameWithAllAllowedCharacters_whenValidate_thenReturnsValidResult() {
            // given
            var command = new TestCommand("홍길동 John 123");

            // when
            ValidationResult<TestCommand> result = DisplayNameValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }
    }

    @Nested
    @DisplayName("체이닝 검증 순서 테스트")
    class ChainingValidationOrderTest {

        @Test
        @DisplayName("체이닝 검증 순서 테스트 - 첫 번째 실패에서 중단")
        void givenInvalidDisplayName_whenValidateDisplayNameString_thenStopsAtFirstFailure() {
            // given - 빈 문자열 (notBlank에서 실패)
            String invalidDisplayName = "";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(invalidDisplayName);

            // then - notBlank 검증에서 실패하고 minLength, maxLength, pattern 검증은 실행되지 않음
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("체이닝 검증 순서 테스트 - 두 번째 실패에서 중단")
        void givenTooLongDisplayName_whenValidateDisplayNameString_thenStopsAtMaxLengthFailure() {
            // given - 너무 긴 문자열 (notBlank 통과, minLength 통과, maxLength에서 실패)
            String tooLongDisplayName = "가".repeat(51);

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(tooLongDisplayName);

            // then - maxLength 검증에서 실패하고 pattern 검증은 실행되지 않음
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
        }

        @Test
        @DisplayName("체이닝 검증 순서 테스트 - 마지막 검증에서 실패")
        void givenInvalidPatternDisplayName_whenValidateDisplayNameString_thenFailsAtPatternValidation() {
            // given - 길이는 유효하지만 패턴이 맞지 않는 문자열
            String invalidPatternDisplayName = "홍길동!@#";

            // when
            ValidationResult<String> result = DisplayNameValidator.validateDisplayNameString().validate(invalidPatternDisplayName);

            // then - pattern 검증에서 실패
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }
    }
}