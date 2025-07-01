package com.boardly.shared.application.validation;

import com.boardly.shared.domain.common.Failure.FieldViolation;
import io.vavr.collection.List;
import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Validator 단위 테스트")
class ValidatorTest {

    private record TestObject(String name, int age) {
    }

    private Validator<TestObject> nameIsNotEmpty;
    private Validator<TestObject> ageIsPositive;

    @BeforeEach
    void setUp() {
        nameIsNotEmpty = Validator.field(
            TestObject::name,
            name -> name != null && !name.trim().isEmpty(),
            "name",
            "Name is required"
        );

        ageIsPositive = Validator.of(
            (TestObject obj) -> obj.age() > 0,
            "age",
            "Age must be positive"
        );
    }

    @Nested
    @DisplayName("and 메서드는")
    class Describe_and {

        @Test
        @DisplayName("두 검증이 모두 성공하면 성공 결과를 반환한다")
        void whenBothValid_shouldReturnValid() {
            Validator<TestObject> validator = nameIsNotEmpty.and(ageIsPositive);
            TestObject validObject = new TestObject("John", 30);
            ValidationResult<TestObject> result = validator.validate(validObject);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("하나의 검증만 실패하면 모든 에러를 포함한 실패 결과를 반환한다")
        void whenOneInvalid_shouldReturnInvalidWithAllErrors() {
            Validator<TestObject> validator = nameIsNotEmpty.and(ageIsPositive);
            TestObject invalidObject = new TestObject("", -5);
            ValidationResult<TestObject> result = validator.validate(invalidObject);
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(2);
            assertThat(result.getErrors().map(FieldViolation::field)).containsExactlyInAnyOrder("name", "age");
        }
    }

    @Nested
    @DisplayName("then 메서드는")
    class Describe_then {

        @Test
        @DisplayName("두 검증이 모두 성공하면 성공 결과를 반환한다")
        void whenBothValid_shouldReturnValid() {
            Validator<TestObject> validator = nameIsNotEmpty.then(ageIsPositive);
            TestObject validObject = new TestObject("John", 30);
            ValidationResult<TestObject> result = validator.validate(validObject);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("첫 검증이 실패하면 즉시 실패 결과를 반환한다")
        void whenFirstInvalid_shouldReturnImmediately() {
            Validator<TestObject> validator = nameIsNotEmpty.then(ageIsPositive);
            TestObject invalidObject = new TestObject("", -5);
            ValidationResult<TestObject> result = validator.validate(invalidObject);
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("when 메서드는")
    class Describe_when {

        @Test
        @DisplayName("조건이 참일 때만 검증을 수행한다")
        void whenConditionIsTrue_shouldValidate() {
            Validator<TestObject> validator = nameIsNotEmpty.when(obj -> obj.age > 18);

            ValidationResult<TestObject> resultForAdultWithNoName = validator.validate(new TestObject("", 20));
            assertThat(resultForAdultWithNoName.isInvalid()).isTrue();

            ValidationResult<TestObject> resultForChildWithNoName = validator.validate(new TestObject("", 15));
            assertThat(resultForChildWithNoName.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("contramap 메서드는")
    class Describe_contramap {

        private record Wrapper(TestObject testObject) {
        }

        @Test
        @DisplayName("검증 대상을 변환하여 검증을 수행한다")
        void shouldContramapValidator() {
            Validator<TestObject> ageValidator = Validator.field(TestObject::age, age -> age > 18, "age", "Must be adult");
            Validator<Wrapper> wrapperValidator = ageValidator.contramap(Wrapper::testObject);

            ValidationResult<Wrapper> invalidResult = wrapperValidator.validate(new Wrapper(new TestObject("kid", 10)));
            assertThat(invalidResult.isInvalid()).isTrue();

            ValidationResult<Wrapper> validResult = wrapperValidator.validate(new Wrapper(new TestObject("adult", 20)));
            assertThat(validResult.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("정적 팩토리 메서드는")
    class Describe_static_factories {

        @Mock
        private MessageSource messageSource;

        @Test
        @DisplayName("valid는 항상 성공 결과를 반환한다")
        void valid_shouldAlwaysReturnValid() {
            Validator<TestObject> validator = Validator.valid();
            assertThat(validator.validate(new TestObject("any", 1)).isValid()).isTrue();
        }

        @Test
        @DisplayName("invalid는 항상 실패 결과를 반환한다")
        void invalid_shouldAlwaysReturnInvalid() {
            Validator<TestObject> validator = Validator.invalid("field", "message");
            ValidationResult<TestObject> result = validator.validate(new TestObject("any", 1));
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors().get(0).field()).isEqualTo("field");
        }

        @Test
        @DisplayName("of는 PREDICATE에 따라 결과를 반환한다")
        void of_shouldValidateBasedOnPredicate() {
            Predicate<TestObject> predicate = obj -> obj.age() > 18;
            Validator<TestObject> validator = Validator.of(predicate, "age", "Too young");

            assertThat(validator.validate(new TestObject("John", 20)).isValid()).isTrue();
            assertThat(validator.validate(new TestObject("Jane", 15)).isInvalid()).isTrue();
        }

        @Test
        @DisplayName("field는 특정 필드를 검증한다")
        void field_shouldValidateField() {
            Validator<TestObject> validator = Validator.field(TestObject::name, n -> n.length() > 3, "name", "Too short");

            assertThat(validator.validate(new TestObject("John", 20)).isValid()).isTrue();
            assertThat(validator.validate(new TestObject("Jo", 20)).isInvalid()).isTrue();
        }

        @Test
        @DisplayName("fieldWithMessage는 국제화 메시지를 사용하여 필드를 검증한다")
        void fieldWithMessage_shouldUseMessageResolver() {
            ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
            when(messageSource.getMessage(eq("length.min"), any(), any(Locale.class)))
                .thenReturn("Error with code length.min");

            Validator<TestObject> validator = Validator.fieldWithMessage(
                TestObject::name,
                n -> n.length() > 3,
                "name",
                "length.min",
                messageResolver
            );

            ValidationResult<TestObject> result = validator.validate(new TestObject("Jo", 20));
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors().get(0).message()).isEqualTo("Error with code length.min");
        }

        @Test
        @DisplayName("combine은 여러 검증을 결합하여 모든 에러를 반환한다")
        void combine_shouldCombineValidators() {
            Validator<TestObject> validator = Validator.combine(nameIsNotEmpty, ageIsPositive);
            ValidationResult<TestObject> result = validator.validate(new TestObject("", -1));

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(2);
        }

        @Test
        @DisplayName("chain은 여러 검증을 순차 실행하여 첫 에러만 반환한다")
        void chain_shouldChainValidators() {
            Validator<TestObject> validator = Validator.chain(nameIsNotEmpty, ageIsPositive);
            ValidationResult<TestObject> result = validator.validate(new TestObject("", -1));

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("name");
        }

        @Test
        @DisplayName("fromValidation은 Vavr Validation으로부터 Validator를 생성한다")
        void fromValidation_shouldCreateFromVavrValidation() {
            Validator<TestObject> validator = Validator.fromValidation(obj -> {
                if (obj.name().isEmpty()) {
                    return Validation.invalid(List.of(
                        FieldViolation.builder().field("name").message("empty").rejectedValue("").build()
                    ));
                }
                return Validation.valid(obj);
            });

            assertThat(validator.validate(new TestObject("", 1)).isInvalid()).isTrue();
            assertThat(validator.validate(new TestObject("John", 1)).isValid()).isTrue();
        }
    }
} 