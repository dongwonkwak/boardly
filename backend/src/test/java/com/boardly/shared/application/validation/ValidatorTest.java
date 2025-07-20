package com.boardly.shared.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.shared.domain.common.Failure.FieldViolation;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

@ExtendWith(MockitoExtension.class)
@DisplayName("Validator 테스트")
class ValidatorTest {

  @Mock
  private ValidationMessageResolver messageResolver;

  // 테스트용 데이터 클래스
  private static class TestData {
    private final String name;
    private final int age;
    private final String email;

    public TestData(String name, int age, String email) {
      this.name = name;
      this.age = age;
      this.email = email;
    }

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }

    public String getEmail() {
      return email;
    }
  }

  @Nested
  @DisplayName("기본 검증 메서드 테스트")
  class BasicValidationTests {

    @Test
    @DisplayName("valid() - 항상 성공하는 검증기")
    void valid_ShouldAlwaysReturnSuccess() {
      // given
      Validator<String> validator = Validator.valid();

      // when
      ValidationResult<String> result = validator.validate("test");

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get()).isEqualTo("test");
    }

    @Test
    @DisplayName("invalid() - 항상 실패하는 검증기")
    void invalid_ShouldAlwaysReturnFailure() {
      // given
      Validator<String> validator = Validator.invalid("field", "error message");

      // when
      ValidationResult<String> result = validator.validate("test");

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("field");
      assertThat(violation.message()).isEqualTo("error message");
      assertThat(violation.rejectedValue()).isEqualTo("test");
    }

    @Test
    @DisplayName("of() - 조건부 검증기 (성공 케이스)")
    void of_ShouldReturnSuccess_WhenConditionIsTrue() {
      // given
      Validator<String> validator = Validator.of(
          str -> str.length() > 0,
          "name",
          "이름은 필수입니다");

      // when
      ValidationResult<String> result = validator.validate("test");

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get()).isEqualTo("test");
    }

    @Test
    @DisplayName("of() - 조건부 검증기 (실패 케이스)")
    void of_ShouldReturnFailure_WhenConditionIsFalse() {
      // given
      Validator<String> validator = Validator.of(
          str -> str.length() > 0,
          "name",
          "이름은 필수입니다");

      // when
      ValidationResult<String> result = validator.validate("");

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("name");
      assertThat(violation.message()).isEqualTo("이름은 필수입니다");
      assertThat(violation.rejectedValue()).isEqualTo("");
    }
  }

  @Nested
  @DisplayName("필드 검증 메서드 테스트")
  class FieldValidationTests {

    @Test
    @DisplayName("field() - 필드 검증기 (성공 케이스)")
    void field_ShouldReturnSuccess_WhenFieldValidationPasses() {
      // given
      Validator<TestData> validator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      // when
      ValidationResult<TestData> result = validator.validate(new TestData("John", 25, "john@test.com"));

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("field() - 필드 검증기 (실패 케이스)")
    void field_ShouldReturnFailure_WhenFieldValidationFails() {
      // given
      Validator<TestData> validator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      // when
      ValidationResult<TestData> result = validator.validate(new TestData("", 25, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("name");
      assertThat(violation.message()).isEqualTo("이름은 필수입니다");
      assertThat(violation.rejectedValue()).isEqualTo("");
    }

    @Test
    @DisplayName("fieldWithMessage() - 국제화 메시지 필드 검증기")
    void fieldWithMessage_ShouldUseMessageResolver() {
      // given
      when(messageResolver.getMessage("validation.name.required")).thenReturn("이름은 필수입니다");

      Validator<TestData> validator = Validator.fieldWithMessage(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "validation.name.required",
          messageResolver);

      // when
      ValidationResult<TestData> result = validator.validate(new TestData("", 25, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("name");
      assertThat(violation.message()).isEqualTo("이름은 필수입니다");
      assertThat(violation.rejectedValue()).isEqualTo("");
    }
  }

  @Nested
  @DisplayName("조합 메서드 테스트")
  class CombinationTests {

    @Test
    @DisplayName("and() - 두 검증기 결합 (모두 성공)")
    void and_ShouldReturnSuccess_WhenBothValidatorsPass() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> combinedValidator = nameValidator.and(ageValidator);

      // when
      ValidationResult<TestData> result = combinedValidator.validate(new TestData("John", 25, "john@test.com"));

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("and() - 두 검증기 결합 (하나 실패)")
    void and_ShouldReturnFailure_WhenOneValidatorFails() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> combinedValidator = nameValidator.and(ageValidator);

      // when
      ValidationResult<TestData> result = combinedValidator.validate(new TestData("", 25, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("name");
      assertThat(violation.message()).isEqualTo("이름은 필수입니다");
    }

    @Test
    @DisplayName("and() - 두 검증기 결합 (모두 실패)")
    void and_ShouldReturnAllFailures_WhenBothValidatorsFail() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> combinedValidator = nameValidator.and(ageValidator);

      // when
      ValidationResult<TestData> result = combinedValidator.validate(new TestData("", -5, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(2);

      Seq<FieldViolation> violations = result.getErrors();
      assertThat(violations.map(FieldViolation::field)).contains("name", "age");
      assertThat(violations.map(FieldViolation::message)).contains("이름은 필수입니다", "나이는 0 이상이어야 합니다");
    }

    @Test
    @DisplayName("then() - 순차 검증기 (첫 번째 성공, 두 번째 성공)")
    void then_ShouldReturnSuccess_WhenBothValidatorsPass() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> chainedValidator = nameValidator.then(ageValidator);

      // when
      ValidationResult<TestData> result = chainedValidator.validate(new TestData("John", 25, "john@test.com"));

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("then() - 순차 검증기 (첫 번째 실패)")
    void then_ShouldReturnFirstFailure_WhenFirstValidatorFails() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> chainedValidator = nameValidator.then(ageValidator);

      // when
      ValidationResult<TestData> result = chainedValidator.validate(new TestData("", -5, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("name");
      assertThat(violation.message()).isEqualTo("이름은 필수입니다");
    }

    @Test
    @DisplayName("then() - 순차 검증기 (첫 번째 성공, 두 번째 실패)")
    void then_ShouldReturnSecondFailure_WhenFirstPassesAndSecondFails() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> chainedValidator = nameValidator.then(ageValidator);

      // when
      ValidationResult<TestData> result = chainedValidator.validate(new TestData("John", -5, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("age");
      assertThat(violation.message()).isEqualTo("나이는 0 이상이어야 합니다");
    }
  }

  @Nested
  @DisplayName("조건부 검증 테스트")
  class ConditionalValidationTests {

    @Test
    @DisplayName("when() - 조건부 검증 (조건 참)")
    void when_ShouldValidate_WhenConditionIsTrue() {
      // given
      Validator<TestData> validator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다").when(data -> data.getAge() >= 18);

      // when
      ValidationResult<TestData> result = validator.validate(new TestData("", 25, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("name");
    }

    @Test
    @DisplayName("when() - 조건부 검증 (조건 거짓)")
    void when_ShouldSkipValidation_WhenConditionIsFalse() {
      // given
      Validator<TestData> validator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다").when(data -> data.getAge() >= 18);

      // when
      ValidationResult<TestData> result = validator.validate(new TestData("", 15, "john@test.com"));

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get().getName()).isEqualTo("");
    }
  }

  @Nested
  @DisplayName("변환 메서드 테스트")
  class TransformationTests {

    @Test
    @DisplayName("contramap() - 검증 대상 변환")
    void contramap_ShouldTransformTarget() {
      // given
      Validator<String> stringValidator = Validator.field(
          Function.identity(),
          str -> str != null && !str.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> dataValidator = stringValidator.contramap(TestData::getName);

      // when
      ValidationResult<TestData> result = dataValidator.validate(new TestData("", 25, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("name");
      assertThat(violation.message()).isEqualTo("이름은 필수입니다");
      assertThat(violation.rejectedValue()).isEqualTo("");
    }
  }

  @Nested
  @DisplayName("정적 조합 메서드 테스트")
  class StaticCombinationTests {

    @Test
    @DisplayName("combine() - 여러 검증기 결합 (모두 성공)")
    void combine_ShouldReturnSuccess_WhenAllValidatorsPass() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> emailValidator = Validator.field(
          TestData::getEmail,
          email -> email != null && email.contains("@"),
          "email",
          "이메일 형식이 올바르지 않습니다");

      Validator<TestData> combinedValidator = Validator.combine(nameValidator, ageValidator, emailValidator);

      // when
      ValidationResult<TestData> result = combinedValidator.validate(new TestData("John", 25, "john@test.com"));

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("combine() - 여러 검증기 결합 (일부 실패)")
    void combine_ShouldReturnAllFailures_WhenSomeValidatorsFail() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> emailValidator = Validator.field(
          TestData::getEmail,
          email -> email != null && email.contains("@"),
          "email",
          "이메일 형식이 올바르지 않습니다");

      Validator<TestData> combinedValidator = Validator.combine(nameValidator, ageValidator, emailValidator);

      // when
      ValidationResult<TestData> result = combinedValidator.validate(new TestData("", -5, "invalid-email"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(3);

      Seq<FieldViolation> violations = result.getErrors();
      assertThat(violations.map(FieldViolation::field)).contains("name", "age", "email");
    }

    @Test
    @DisplayName("chain() - 순차 검증기 (첫 번째 실패에서 중단)")
    void chain_ShouldStopAtFirstFailure() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> emailValidator = Validator.field(
          TestData::getEmail,
          email -> email != null && email.contains("@"),
          "email",
          "이메일 형식이 올바르지 않습니다");

      Validator<TestData> chainedValidator = Validator.chain(nameValidator, ageValidator, emailValidator);

      // when
      ValidationResult<TestData> result = chainedValidator.validate(new TestData("", -5, "invalid-email"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("name");
      assertThat(violation.message()).isEqualTo("이름은 필수입니다");
    }

    @Test
    @DisplayName("chain() - 순차 검증기 (모두 성공)")
    void chain_ShouldReturnSuccess_WhenAllValidatorsPass() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 0,
          "age",
          "나이는 0 이상이어야 합니다");

      Validator<TestData> emailValidator = Validator.field(
          TestData::getEmail,
          email -> email != null && email.contains("@"),
          "email",
          "이메일 형식이 올바르지 않습니다");

      Validator<TestData> chainedValidator = Validator.chain(nameValidator, ageValidator, emailValidator);

      // when
      ValidationResult<TestData> result = chainedValidator.validate(new TestData("John", 25, "john@test.com"));

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get().getName()).isEqualTo("John");
    }
  }

  @Nested
  @DisplayName("Vavr Validation 변환 테스트")
  class VavrValidationTests {

    @Test
    @DisplayName("fromValidation() - Vavr Validation에서 Validator 생성 (성공)")
    void fromValidation_ShouldReturnSuccess_WhenValidationSucceeds() {
      // given
      Function<TestData, Validation<Seq<FieldViolation>, TestData>> validationFunc = data -> {
        if (data.getName() != null && !data.getName().trim().isEmpty()) {
          return Validation.valid(data);
        } else {
          FieldViolation violation = FieldViolation.builder()
              .field("name")
              .message("이름은 필수입니다")
              .rejectedValue(data.getName())
              .build();
          return Validation.invalid(io.vavr.collection.List.of(violation));
        }
      };

      Validator<TestData> validator = Validator.fromValidation(validationFunc);

      // when
      ValidationResult<TestData> result = validator.validate(new TestData("John", 25, "john@test.com"));

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("fromValidation() - Vavr Validation에서 Validator 생성 (실패)")
    void fromValidation_ShouldReturnFailure_WhenValidationFails() {
      // given
      Function<TestData, Validation<Seq<FieldViolation>, TestData>> validationFunc = data -> {
        if (data.getName() != null && !data.getName().trim().isEmpty()) {
          return Validation.valid(data);
        } else {
          FieldViolation violation = FieldViolation.builder()
              .field("name")
              .message("이름은 필수입니다")
              .rejectedValue(data.getName())
              .build();
          return Validation.invalid(io.vavr.collection.List.of(violation));
        }
      };

      Validator<TestData> validator = Validator.fromValidation(validationFunc);

      // when
      ValidationResult<TestData> result = validator.validate(new TestData("", 25, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("name");
      assertThat(violation.message()).isEqualTo("이름은 필수입니다");
      assertThat(violation.rejectedValue()).isEqualTo("");
    }
  }

  @Nested
  @DisplayName("복합 시나리오 테스트")
  class ComplexScenarioTests {

    @Test
    @DisplayName("복합 검증 시나리오 - 성인 사용자 검증")
    void complexScenario_AdultUserValidation() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 18,
          "age",
          "성인만 가입할 수 있습니다").when(data -> data.getName() != null && !data.getName().trim().isEmpty());

      Validator<TestData> emailValidator = Validator.field(
          TestData::getEmail,
          email -> email != null && email.contains("@"),
          "email",
          "이메일 형식이 올바르지 않습니다").when(data -> data.getAge() >= 18);

      Validator<TestData> combinedValidator = nameValidator.and(ageValidator).and(emailValidator);

      // when
      ValidationResult<TestData> result = combinedValidator.validate(new TestData("John", 25, "john@test.com"));

      // then
      assertThat(result.isValid()).isTrue();
      assertThat(result.get().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("복합 검증 시나리오 - 미성년자 검증")
    void complexScenario_MinorUserValidation() {
      // given
      Validator<TestData> nameValidator = Validator.field(
          TestData::getName,
          name -> name != null && !name.trim().isEmpty(),
          "name",
          "이름은 필수입니다");

      Validator<TestData> ageValidator = Validator.field(
          TestData::getAge,
          age -> age >= 18,
          "age",
          "성인만 가입할 수 있습니다").when(data -> data.getName() != null && !data.getName().trim().isEmpty());

      Validator<TestData> emailValidator = Validator.field(
          TestData::getEmail,
          email -> email != null && email.contains("@"),
          "email",
          "이메일 형식이 올바르지 않습니다").when(data -> data.getAge() >= 18);

      Validator<TestData> combinedValidator = nameValidator.and(ageValidator).and(emailValidator);

      // when
      ValidationResult<TestData> result = combinedValidator.validate(new TestData("John", 15, "john@test.com"));

      // then
      assertThat(result.isInvalid()).isTrue();
      assertThat(result.getErrors()).hasSize(1);
      FieldViolation violation = result.getErrors().get(0);
      assertThat(violation.field()).isEqualTo("age");
      assertThat(violation.message()).isEqualTo("성인만 가입할 수 있습니다");
    }
  }
}