package com.boardly.shared.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.shared.domain.common.Failure.FieldViolation;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommonValidationRules 테스트")
class CommonValidationRulesTest {

    @Mock
    private ValidationMessageResolver messageResolver;

    private CommonValidationRules validationRules;

    // 테스트용 데이터 클래스들
    private static class TestData {

        private final String email;
        private final String password;
        private final String firstName;
        private final String lastName;
        private final String title;
        private final String description;
        private final Object userId;
        private final Object boardId;
        private final Object listId;
        private final Object cardId;
        private final Object position;
        private final String color;
        private final ListColor listColor;

        public TestData(
            String email,
            String password,
            String firstName,
            String lastName,
            String title,
            String description,
            Object userId,
            Object boardId,
            Object listId,
            Object cardId,
            Object position,
            String color,
            ListColor listColor
        ) {
            this.email = email;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.title = title;
            this.description = description;
            this.userId = userId;
            this.boardId = boardId;
            this.listId = listId;
            this.cardId = cardId;
            this.position = position;
            this.color = color;
            this.listColor = listColor;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public Object getUserId() {
            return userId;
        }

        public Object getBoardId() {
            return boardId;
        }

        public Object getListId() {
            return listId;
        }

        public Object getCardId() {
            return cardId;
        }

        public Object getPosition() {
            return position;
        }

        public String getColor() {
            return color;
        }

        public ListColor getListColor() {
            return listColor;
        }
    }

    @BeforeEach
    void setUp() {
        validationRules = new CommonValidationRules(messageResolver);

        // 기본 메시지 설정 - lenient로 설정하여 불필요한 stubbing 허용
        lenient()
            .when(messageResolver.getMessage(anyString(), any()))
            .thenAnswer(invocation -> {
                String messageKey = invocation.getArgument(0);
                return messageKey.replace("validation.", "").replace(".", " ");
            });
    }

    @Nested
    @DisplayName("정적 검증 메서드 테스트")
    class StaticValidationMethodTests {

        @Test
        @DisplayName("required() - 필수 필드 검증 (성공)")
        void required_ShouldReturnSuccess_WhenFieldIsNotNullAndNotEmpty() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.required(
                fieldExtractor,
                "title",
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "Valid Title",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("required() - 필수 필드 검증 (실패 - null)")
        void required_ShouldReturnFailure_WhenFieldIsNull() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.required(
                fieldExtractor,
                "title",
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("title");
        }

        @Test
        @DisplayName("required() - 필수 필드 검증 (실패 - 빈 문자열)")
        void required_ShouldReturnFailure_WhenFieldIsEmpty() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.required(
                fieldExtractor,
                "title",
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("title");
        }

        @Test
        @DisplayName("maxLength() - 최대 길이 검증 (성공)")
        void maxLength_ShouldReturnSuccess_WhenFieldIsWithinLimit() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.maxLength(
                fieldExtractor,
                "title",
                10,
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "Short",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("maxLength() - 최대 길이 검증 (성공 - null)")
        void maxLength_ShouldReturnSuccess_WhenFieldIsNull() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.maxLength(
                fieldExtractor,
                "title",
                10,
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("maxLength() - 최대 길이 검증 (실패)")
        void maxLength_ShouldReturnFailure_WhenFieldExceedsLimit() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.maxLength(
                fieldExtractor,
                "title",
                5,
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "Too Long Title",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("title");
        }

        @Test
        @DisplayName("minLength() - 최소 길이 검증 (성공)")
        void minLength_ShouldReturnSuccess_WhenFieldMeetsMinimum() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.minLength(
                fieldExtractor,
                "title",
                3,
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "ABC",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("minLength() - 최소 길이 검증 (실패)")
        void minLength_ShouldReturnFailure_WhenFieldIsTooShort() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.minLength(
                fieldExtractor,
                "title",
                5,
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "ABC",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("title");
        }

        @Test
        @DisplayName("pattern() - 패턴 검증 (성공)")
        void pattern_ShouldReturnSuccess_WhenFieldMatchesPattern() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getEmail;
            Pattern emailPattern = Pattern.compile(
                "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
            );
            Validator<TestData> validator = CommonValidationRules.pattern(
                fieldExtractor,
                "email",
                emailPattern,
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    "test@example.com",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("pattern() - 패턴 검증 (실패)")
        void pattern_ShouldReturnFailure_WhenFieldDoesNotMatchPattern() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getEmail;
            Pattern emailPattern = Pattern.compile(
                "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
            );
            Validator<TestData> validator = CommonValidationRules.pattern(
                fieldExtractor,
                "email",
                emailPattern,
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    "invalid-email",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("email");
        }

        @Test
        @DisplayName("noHtmlTags() - HTML 태그 검증 (성공)")
        void noHtmlTags_ShouldReturnSuccess_WhenFieldHasNoHtmlTags() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.noHtmlTags(
                fieldExtractor,
                "title",
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "Clean Title",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("noHtmlTags() - HTML 태그 검증 (실패)")
        void noHtmlTags_ShouldReturnFailure_WhenFieldContainsHtmlTags() {
            // given
            Function<TestData, String> fieldExtractor = TestData::getTitle;
            Validator<TestData> validator = CommonValidationRules.noHtmlTags(
                fieldExtractor,
                "title",
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "<script>alert('xss')</script>",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("title");
        }

        @Test
        @DisplayName("idRequired() - ID 필수 검증 (성공)")
        void idRequired_ShouldReturnSuccess_WhenIdIsNotNull() {
            // given
            Function<TestData, Object> fieldExtractor = TestData::getUserId;
            Validator<TestData> validator = CommonValidationRules.idRequired(
                fieldExtractor,
                "userId",
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "user123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("idRequired() - ID 필수 검증 (실패)")
        void idRequired_ShouldReturnFailure_WhenIdIsNull() {
            // given
            Function<TestData, Object> fieldExtractor = TestData::getUserId;
            Validator<TestData> validator = CommonValidationRules.idRequired(
                fieldExtractor,
                "userId",
                messageResolver
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("userId");
        }
    }

    @Nested
    @DisplayName("완성된 검증 메서드 테스트")
    class CompleteValidationMethodTests {

        @Test
        @DisplayName("emailComplete() - 이메일 완전 검증 (성공)")
        void emailComplete_ShouldReturnSuccess_WhenEmailIsValid() {
            // given
            Function<TestData, String> emailExtractor = TestData::getEmail;
            Validator<TestData> validator = validationRules.emailComplete(
                emailExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    "test@example.com",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("emailComplete() - 이메일 완전 검증 (실패 - 빈 값)")
        void emailComplete_ShouldReturnFailure_WhenEmailIsEmpty() {
            // given
            Function<TestData, String> emailExtractor = TestData::getEmail;
            Validator<TestData> validator = validationRules.emailComplete(
                emailExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    "",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("emailComplete() - 이메일 완전 검증 (실패 - 잘못된 형식)")
        void emailComplete_ShouldReturnFailure_WhenEmailFormatIsInvalid() {
            // given
            Function<TestData, String> emailExtractor = TestData::getEmail;
            Validator<TestData> validator = validationRules.emailComplete(
                emailExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    "invalid-email",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("passwordComplete() - 비밀번호 완전 검증 (성공)")
        void passwordComplete_ShouldReturnSuccess_WhenPasswordIsValid() {
            // given
            Function<TestData, String> passwordExtractor =
                TestData::getPassword;
            Validator<TestData> validator = validationRules.passwordComplete(
                passwordExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    "ValidPass1!",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName(
            "passwordComplete() - 비밀번호 완전 검증 (실패 - 너무 짧음)"
        )
        void passwordComplete_ShouldReturnFailure_WhenPasswordIsTooShort() {
            // given
            Function<TestData, String> passwordExtractor =
                TestData::getPassword;
            Validator<TestData> validator = validationRules.passwordComplete(
                passwordExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    "Short1!",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("firstNameComplete() - 이름 완전 검증 (성공)")
        void firstNameComplete_ShouldReturnSuccess_WhenFirstNameIsValid() {
            // given
            Function<TestData, String> firstNameExtractor =
                TestData::getFirstName;
            Validator<TestData> validator = validationRules.firstNameComplete(
                firstNameExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    "홍길동",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName(
            "firstNameComplete() - 이름 완전 검증 (실패 - 특수문자 포함)"
        )
        void firstNameComplete_ShouldReturnFailure_WhenFirstNameContainsSpecialChars() {
            // given
            Function<TestData, String> firstNameExtractor =
                TestData::getFirstName;
            Validator<TestData> validator = validationRules.firstNameComplete(
                firstNameExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    "홍길동123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("lastNameComplete() - 성 완전 검증 (성공)")
        void lastNameComplete_ShouldReturnSuccess_WhenLastNameIsValid() {
            // given
            Function<TestData, String> lastNameExtractor = TestData::getLastName;
            Validator<TestData> validator = validationRules.lastNameComplete(
                lastNameExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    "김",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("lastNameComplete() - 성 완전 검증 (실패 - 특수문자/숫자 포함)")
        void lastNameComplete_ShouldReturnFailure_WhenLastNameContainsInvalidChars() {
            // given
            Function<TestData, String> lastNameExtractor = TestData::getLastName;
            Validator<TestData> validator = validationRules.lastNameComplete(
                lastNameExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    "Doe123!",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("titleComplete() - 제목 완전 검증 (성공)")
        void titleComplete_ShouldReturnSuccess_WhenTitleIsValid() {
            // given
            Function<TestData, String> titleExtractor = TestData::getTitle;
            Validator<TestData> validator = validationRules.titleComplete(
                titleExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "Valid Title",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("titleComplete() - 제목 완전 검증 (실패 - HTML 태그 포함)")
        void titleComplete_ShouldReturnFailure_WhenTitleContainsHtmlTags() {
            // given
            Function<TestData, String> titleExtractor = TestData::getTitle;
            Validator<TestData> validator = validationRules.titleComplete(
                titleExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "<script>alert('xss')</script>",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("titleOptional() - 제목 선택 검증 (성공 - null)")
        void titleOptional_ShouldReturnSuccess_WhenTitleIsNull() {
            // given
            Function<TestData, String> titleExtractor = TestData::getTitle;
            Validator<TestData> validator = validationRules.titleOptional(
                titleExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("descriptionComplete() - 설명 완전 검증 (성공)")
        void descriptionComplete_ShouldReturnSuccess_WhenDescriptionIsValid() {
            // given
            Function<TestData, String> descriptionExtractor =
                TestData::getDescription;
            Validator<TestData> validator = validationRules.descriptionComplete(
                descriptionExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Valid description",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("descriptionComplete() - 설명 완전 검증 (성공 - null)")
        void descriptionComplete_ShouldReturnSuccess_WhenDescriptionIsNull() {
            // given
            Function<TestData, String> descriptionExtractor =
                TestData::getDescription;
            Validator<TestData> validator = validationRules.descriptionComplete(
                descriptionExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("ID 검증 메서드 테스트")
    class IdValidationMethodTests {

        @Test
        @DisplayName("userIdRequired() - 사용자 ID 검증 (성공)")
        void userIdRequired_ShouldReturnSuccess_WhenUserIdIsNotNull() {
            // given
            Function<TestData, Object> userIdExtractor = TestData::getUserId;
            Validator<TestData> validator = validationRules.userIdRequired(
                userIdExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "user123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("boardIdRequired() - 보드 ID 검증 (성공)")
        void boardIdRequired_ShouldReturnSuccess_WhenBoardIdIsNotNull() {
            // given
            Function<TestData, Object> boardIdExtractor = TestData::getBoardId;
            Validator<TestData> validator = validationRules.boardIdRequired(
                boardIdExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "board123",
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("listIdRequired() - 리스트 ID 검증 (성공)")
        void listIdRequired_ShouldReturnSuccess_WhenListIdIsNotNull() {
            // given
            Function<TestData, Object> listIdExtractor = TestData::getListId;
            Validator<TestData> validator = validationRules.listIdRequired(
                listIdExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "list123",
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("cardIdRequired() - 카드 ID 검증 (성공)")
        void cardIdRequired_ShouldReturnSuccess_WhenCardIdIsNotNull() {
            // given
            Function<TestData, Object> cardIdExtractor = TestData::getCardId;
            Validator<TestData> validator = validationRules.cardIdRequired(
                cardIdExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "card123",
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("positionRequired() - 위치 검증 (성공)")
        void positionRequired_ShouldReturnSuccess_WhenPositionIsNotNull() {
            // given
            Function<TestData, Object> positionExtractor =
                TestData::getPosition;
            Validator<TestData> validator = validationRules.positionRequired(
                positionExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    1,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("색상 검증 메서드 테스트")
    class ColorValidationMethodTests {

        @Test
        @DisplayName("colorRequired() - 색상 검증 (성공)")
        void colorRequired_ShouldReturnSuccess_WhenColorIsValid() {
            // given
            Function<TestData, String> colorExtractor = TestData::getColor;
            Validator<TestData> validator = validationRules.colorRequired(
                colorExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "#FF0000",
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("colorRequired() - 색상 검증 (실패 - 빈 값)")
        void colorRequired_ShouldReturnFailure_WhenColorIsEmpty() {
            // given
            Function<TestData, String> colorExtractor = TestData::getColor;
            Validator<TestData> validator = validationRules.colorRequired(
                colorExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "",
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("listColorRequired() - 리스트 색상 검증 (성공)")
        void listColorRequired_ShouldReturnSuccess_WhenListColorIsValid() {
            // given
            Function<TestData, Object> colorExtractor = TestData::getListColor;
            Validator<TestData> validator = validationRules.listColorRequired(
                colorExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    new ListColor("#0079BF")
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("listColorRequired() - 리스트 색상 검증 (실패 - null)")
        void listColorRequired_ShouldReturnFailure_WhenListColorIsNull() {
            // given
            Function<TestData, Object> colorExtractor = TestData::getListColor;
            Validator<TestData> validator = validationRules.listColorRequired(
                colorExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("카드 검증 메서드 테스트")
    class CardValidationMethodTests {

        @Test
        @DisplayName("cardTitleComplete() - 카드 제목 완전 검증 (성공)")
        void cardTitleComplete_ShouldReturnSuccess_WhenCardTitleIsValid() {
            // given
            Function<TestData, String> titleExtractor = TestData::getTitle;
            Validator<TestData> validator = validationRules.cardTitleComplete(
                titleExtractor
            );

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    "Valid Card Title",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName(
            "cardTitleComplete() - 카드 제목 완전 검증 (실패 - 너무 김)"
        )
        void cardTitleComplete_ShouldReturnFailure_WhenCardTitleIsTooLong() {
            // given
            Function<TestData, String> titleExtractor = TestData::getTitle;
            Validator<TestData> validator = validationRules.cardTitleComplete(
                titleExtractor
            );

            // when
            String longTitle = "A".repeat(201); // 201자
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    longTitle,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("cardDescriptionComplete() - 카드 설명 완전 검증 (성공)")
        void cardDescriptionComplete_ShouldReturnSuccess_WhenCardDescriptionIsValid() {
            // given
            Function<TestData, String> descriptionExtractor =
                TestData::getDescription;
            Validator<TestData> validator =
                validationRules.cardDescriptionComplete(descriptionExtractor);

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Valid card description",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName(
            "cardDescriptionComplete() - 카드 설명 완전 검증 (성공 - null)"
        )
        void cardDescriptionComplete_ShouldReturnSuccess_WhenCardDescriptionIsNull() {
            // given
            Function<TestData, String> descriptionExtractor =
                TestData::getDescription;
            Validator<TestData> validator =
                validationRules.cardDescriptionComplete(descriptionExtractor);

            // when
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName(
            "cardDescriptionComplete() - 카드 설명 완전 검증 (실패 - 너무 김)"
        )
        void cardDescriptionComplete_ShouldReturnFailure_WhenCardDescriptionIsTooLong() {
            // given
            Function<TestData, String> descriptionExtractor =
                TestData::getDescription;
            Validator<TestData> validator =
                validationRules.cardDescriptionComplete(descriptionExtractor);

            // when
            String longDescription = "A".repeat(2001); // 2001자
            ValidationResult<TestData> result = validator.validate(
                new TestData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    longDescription,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("상수 테스트")
    class ConstantTests {

        @Test
        @DisplayName("패턴 상수들이 올바르게 정의되어 있는지 확인")
        void patterns_ShouldBeCorrectlyDefined() {
            // then
            assertThat(CommonValidationRules.EMAIL_PATTERN).isNotNull();
            assertThat(CommonValidationRules.PASSWORD_PATTERN).isNotNull();
            assertThat(CommonValidationRules.NAME_PATTERN).isNotNull();
            assertThat(CommonValidationRules.HTML_TAG_PATTERN).isNotNull();
        }

        @Test
        @DisplayName("길이 상수들이 올바르게 정의되어 있는지 확인")
        void lengthConstants_ShouldBeCorrectlyDefined() {
            // then
            assertThat(CommonValidationRules.EMAIL_MAX_LENGTH).isEqualTo(100);
            assertThat(CommonValidationRules.PASSWORD_MIN_LENGTH).isEqualTo(8);
            assertThat(CommonValidationRules.PASSWORD_MAX_LENGTH).isEqualTo(20);
            assertThat(CommonValidationRules.NAME_MAX_LENGTH).isEqualTo(50);
            assertThat(CommonValidationRules.TITLE_MAX_LENGTH).isEqualTo(100);
            assertThat(CommonValidationRules.DESCRIPTION_MAX_LENGTH).isEqualTo(
                500
            );
        }

        @Test
        @DisplayName("이메일 패턴이 올바른 이메일을 검증하는지 확인")
        void emailPattern_ShouldValidateCorrectEmails() {
            // given
            Pattern emailPattern = CommonValidationRules.EMAIL_PATTERN;

            // then
            assertThat(
                emailPattern.matcher("test@example.com").matches()
            ).isTrue();
            assertThat(
                emailPattern.matcher("user.name@domain.co.uk").matches()
            ).isTrue();
            assertThat(
                emailPattern.matcher("invalid-email").matches()
            ).isFalse();
            assertThat(emailPattern.matcher("@domain.com").matches()).isFalse();
        }

        @Test
        @DisplayName("비밀번호 패턴이 올바른 비밀번호를 검증하는지 확인")
        void passwordPattern_ShouldValidateCorrectPasswords() {
            // given
            Pattern passwordPattern = CommonValidationRules.PASSWORD_PATTERN;

            // then
            assertThat(
                passwordPattern.matcher("ValidPass1!").matches()
            ).isTrue();
            assertThat(
                passwordPattern.matcher("Password123@").matches()
            ).isTrue();
            assertThat(
                passwordPattern.matcher("nouppercase123!").matches()
            ).isTrue(); // 유효한 비밀번호 (알파벳+숫자+특수문자)
            assertThat(passwordPattern.matcher("short1!").matches()).isFalse(); // 너무 짧음 (7자)
            assertThat(
                passwordPattern.matcher("NOSPECIAL123").matches()
            ).isFalse(); // 특수문자 없음
            assertThat(
                passwordPattern.matcher("nouppercase123").matches()
            ).isFalse(); // 특수문자 없음
            assertThat(
                passwordPattern.matcher("nouppercase!@#").matches()
            ).isFalse(); // 숫자 없음
        }

        @Test
        @DisplayName("이름 패턴이 올바른 이름을 검증하는지 확인")
        void namePattern_ShouldValidateCorrectNames() {
            // given
            Pattern namePattern = CommonValidationRules.NAME_PATTERN;

            // then
            assertThat(namePattern.matcher("홍길동").matches()).isTrue();
            assertThat(namePattern.matcher("John Doe").matches()).isTrue();
            assertThat(namePattern.matcher("김철수").matches()).isTrue();
            assertThat(namePattern.matcher("John123").matches()).isFalse(); // 숫자 포함
            assertThat(namePattern.matcher("홍길동!").matches()).isFalse(); // 특수문자 포함
        }

        @Test
        @DisplayName("HTML 태그 패턴이 HTML 태그를 감지하는지 확인")
        void htmlTagPattern_ShouldDetectHtmlTags() {
            // given
            Pattern htmlTagPattern = CommonValidationRules.HTML_TAG_PATTERN;

            // then
            assertThat(
                htmlTagPattern.matcher("<script>alert('xss')</script>").find()
            ).isTrue();
            assertThat(
                htmlTagPattern.matcher("<div>content</div>").find()
            ).isTrue();
            assertThat(htmlTagPattern.matcher("<p>text</p>").find()).isTrue();
            assertThat(
                htmlTagPattern.matcher("Clean text without tags").find()
            ).isFalse();
            assertThat(
                htmlTagPattern.matcher("Text with > symbol").find()
            ).isFalse();
        }
    }
}
