package com.boardly.shared.application.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommonValidationRules 단위 테스트")
class CommonValidationRulesTest {

    @Mock
    private MessageSource messageSource;

    private ValidationMessageResolver messageResolver;

    private record TestObject(String email, String password, String name, String title, String description, Object id) {
    }

    @BeforeEach
    void setUp() {
        messageResolver = new ValidationMessageResolver(messageSource);
    }

    @Nested
    @DisplayName("이메일 검증은")
    class Describe_email_validation {

        @Test
        @DisplayName("올바른 이메일 형식을 통과한다")
        void should_pass_valid_email() {
            // given
            TestObject validObject = new TestObject("test@example.com", null, null, null, null, null);
            Validator<TestObject> validator = CommonValidationRules.emailComplete(
                TestObject::email, "email", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("잘못된 이메일 형식을 거부한다")
        void should_reject_invalid_email() {
            // given
            TestObject invalidObject = new TestObject("invalid-email", null, null, null, null, null);
            Validator<TestObject> validator = CommonValidationRules.emailComplete(
                TestObject::email, "email", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(invalidObject);

            // then
            assertThat(result.isInvalid()).isTrue();
        }

        @Test
        @DisplayName("null 이메일을 거부한다")
        void should_reject_null_email() {
            // given
            TestObject nullObject = new TestObject(null, null, null, null, null, null);
            Validator<TestObject> validator = CommonValidationRules.emailComplete(
                TestObject::email, "email", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(nullObject);

            // then
            assertThat(result.isInvalid()).isTrue();
        }
    }

    @Nested
    @DisplayName("비밀번호 검증은")
    class Describe_password_validation {

        @Test
        @DisplayName("올바른 비밀번호 형식을 통과한다")
        void should_pass_valid_password() {
            // given
            TestObject validObject = new TestObject(null, "ValidPass1!", null, null, null, null);
            Validator<TestObject> validator = CommonValidationRules.passwordComplete(
                TestObject::password, "password", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("너무 짧은 비밀번호를 거부한다")
        void should_reject_short_password() {
            // given
            TestObject shortObject = new TestObject(null, "Short1!", null, null, null, null);
            Validator<TestObject> validator = CommonValidationRules.passwordComplete(
                TestObject::password, "password", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(shortObject);

            // then
            assertThat(result.isInvalid()).isTrue();
        }

        @Test
        @DisplayName("특수문자가 없는 비밀번호를 거부한다")
        void should_reject_password_without_special_char() {
            // given
            TestObject noSpecialObject = new TestObject(null, "ValidPass1", null, null, null, null);
            Validator<TestObject> validator = CommonValidationRules.passwordComplete(
                TestObject::password, "password", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(noSpecialObject);

            // then
            assertThat(result.isInvalid()).isTrue();
        }
    }

    @Nested
    @DisplayName("이름 검증은")
    class Describe_name_validation {

        @Test
        @DisplayName("한글 이름을 통과한다")
        void should_pass_korean_name() {
            // given
            TestObject validObject = new TestObject(null, null, "홍길동", null, null, null);
            Validator<TestObject> validator = CommonValidationRules.nameComplete(
                TestObject::name, "name", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("영문 이름을 통과한다")
        void should_pass_english_name() {
            // given
            TestObject validObject = new TestObject(null, null, "John", null, null, null);
            Validator<TestObject> validator = CommonValidationRules.nameComplete(
                TestObject::name, "name", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("숫자가 포함된 이름을 거부한다")
        void should_reject_name_with_numbers() {
            // given
            TestObject invalidObject = new TestObject(null, null, "John123", null, null, null);
            Validator<TestObject> validator = CommonValidationRules.nameComplete(
                TestObject::name, "name", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(invalidObject);

            // then
            assertThat(result.isInvalid()).isTrue();
        }
    }

    @Nested
    @DisplayName("제목 검증은")
    class Describe_title_validation {

        @Test
        @DisplayName("올바른 제목을 통과한다")
        void should_pass_valid_title() {
            // given
            TestObject validObject = new TestObject(null, null, null, "Valid Title", null, null);
            Validator<TestObject> validator = CommonValidationRules.titleComplete(
                TestObject::title, "title", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("HTML 태그가 포함된 제목을 거부한다")
        void should_reject_title_with_html() {
            // given
            TestObject invalidObject = new TestObject(null, null, null, "<script>alert('xss')</script>", null, null);
            Validator<TestObject> validator = CommonValidationRules.titleComplete(
                TestObject::title, "title", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(invalidObject);

            // then
            assertThat(result.isInvalid()).isTrue();
        }

        @Test
        @DisplayName("빈 제목을 거부한다")
        void should_reject_empty_title() {
            // given
            TestObject emptyObject = new TestObject(null, null, null, "", null, null);
            Validator<TestObject> validator = CommonValidationRules.titleComplete(
                TestObject::title, "title", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(emptyObject);

            // then
            assertThat(result.isInvalid()).isTrue();
        }
    }

    @Nested
    @DisplayName("설명 검증은")
    class Describe_description_validation {

        @Test
        @DisplayName("올바른 설명을 통과한다")
        void should_pass_valid_description() {
            // given
            TestObject validObject = new TestObject(null, null, null, null, "Valid description", null);
            Validator<TestObject> validator = CommonValidationRules.descriptionComplete(
                TestObject::description, "description", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("null 설명을 통과한다")
        void should_pass_null_description() {
            // given
            TestObject nullObject = new TestObject(null, null, null, null, null, null);
            Validator<TestObject> validator = CommonValidationRules.descriptionComplete(
                TestObject::description, "description", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(nullObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("HTML 태그가 포함된 설명을 거부한다")
        void should_reject_description_with_html() {
            // given
            TestObject invalidObject = new TestObject(null, null, null, null, "Description <b>with</b> HTML", null);
            Validator<TestObject> validator = CommonValidationRules.descriptionComplete(
                TestObject::description, "description", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(invalidObject);

            // then
            assertThat(result.isInvalid()).isTrue();
        }
    }

    @Nested
    @DisplayName("ID 검증은")
    class Describe_id_validation {

        @Test
        @DisplayName("null이 아닌 ID를 통과한다")
        void should_pass_non_null_id() {
            // given
            TestObject validObject = new TestObject(null, null, null, null, null, "valid-id");
            Validator<TestObject> validator = CommonValidationRules.idRequired(
                TestObject::id, "id", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("null ID를 거부한다")
        void should_reject_null_id() {
            // given
            TestObject nullObject = new TestObject(null, null, null, null, null, null);
            Validator<TestObject> validator = CommonValidationRules.idRequired(
                TestObject::id, "id", messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(nullObject);

            // then
            assertThat(result.isInvalid()).isTrue();
        }
    }

    @Nested
    @DisplayName("길이 검증은")
    class Describe_length_validation {

        @Test
        @DisplayName("최소 길이 검증을 통과한다")
        void should_pass_min_length() {
            // given
            TestObject validObject = new TestObject(null, null, null, "12345", null, null);
            Validator<TestObject> validator = CommonValidationRules.minLength(
                TestObject::title, "title", 5, messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("최대 길이 검증을 통과한다")
        void should_pass_max_length() {
            // given
            TestObject validObject = new TestObject(null, null, null, "123", null, null);
            Validator<TestObject> validator = CommonValidationRules.maxLength(
                TestObject::title, "title", 5, messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("길이 범위 검증을 통과한다")
        void should_pass_length_range() {
            // given
            TestObject validObject = new TestObject(null, null, null, "12345", null, null);
            Validator<TestObject> validator = CommonValidationRules.lengthRange(
                TestObject::title, "title", 3, 10, messageResolver
            );

            // when
            ValidationResult<TestObject> result = validator.validate(validObject);

            // then
            assertThat(result.isValid()).isTrue();
        }
    }
} 