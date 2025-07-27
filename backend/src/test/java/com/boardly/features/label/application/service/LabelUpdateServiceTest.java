package com.boardly.features.label.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.label.application.port.input.UpdateLabelCommand;
import com.boardly.features.label.application.validation.LabelValidator;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.domain.common.Failure.FieldViolation;
import com.boardly.features.user.domain.model.UserId;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("LabelUpdateService 테스트")
class LabelUpdateServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private LabelValidator labelValidator;

    @Mock
    private ValidationMessageResolver messageResolver;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private LabelUpdateService labelUpdateService;

    private UpdateLabelCommand validCommand;
    private Board validBoard;
    private Label validLabel;
    private UserId userId;
    private BoardId boardId;
    private LabelId labelId;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);

        // 기본 메시지 설정
        lenient().when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenAnswer(invocation -> {
                    String code = invocation.getArgument(0);
                    Object[] args = invocation.getArgument(1);
                    StringBuilder message = new StringBuilder(code);
                    if (args != null) {
                        for (Object arg : args) {
                            message.append(" ").append(arg);
                        }
                    }
                    return message.toString();
                });

        // 테스트 데이터 설정
        userId = new UserId("user-1");
        boardId = new BoardId("board-1");
        labelId = new LabelId("label-1");
        validCommand = UpdateLabelCommand.of(labelId, userId, "수정된 라벨", "#00FF00");

        validBoard = Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .ownerId(userId)
                .isArchived(false)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        validLabel = Label.builder()
                .labelId(labelId)
                .boardId(boardId)
                .name("기존 라벨")
                .color("#FF0000")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("updateLabel 성공 테스트")
    class UpdateLabelSuccessTest {

        @Test
        @DisplayName("유효한 라벨 수정 요청이 성공해야 한다")
        void updateLabel_withValidCommand_shouldSucceed() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(true));
            when(labelRepository.save(any(Label.class)))
                    .thenReturn(Either.right(validLabel));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            Label updatedLabel = result.get();
            assertThat(updatedLabel.getLabelId()).isEqualTo(labelId);
            assertThat(updatedLabel.getBoardId()).isEqualTo(boardId);

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository).findById(boardId);
            verify(boardPermissionService).canWriteBoard(boardId, userId);
            verify(labelRepository).save(any(Label.class));
        }

        @Test
        @DisplayName("이름만 수정하는 경우 성공해야 한다")
        void updateLabel_withNameOnly_shouldSucceed() {
            // given
            UpdateLabelCommand nameOnlyCommand = UpdateLabelCommand.of(labelId, userId, "새 이름", null);
            when(labelValidator.validateUpdateLabel(nameOnlyCommand))
                    .thenReturn(ValidationResult.valid(nameOnlyCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(true));
            when(labelRepository.save(any(Label.class)))
                    .thenReturn(Either.right(validLabel));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(nameOnlyCommand);

            // then
            assertThat(result.isRight()).isTrue();
        }

        @Test
        @DisplayName("색상만 수정하는 경우 성공해야 한다")
        void updateLabel_withColorOnly_shouldSucceed() {
            // given
            UpdateLabelCommand colorOnlyCommand = UpdateLabelCommand.of(labelId, userId, null, "#0000FF");
            when(labelValidator.validateUpdateLabel(colorOnlyCommand))
                    .thenReturn(ValidationResult.valid(colorOnlyCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(true));
            when(labelRepository.save(any(Label.class)))
                    .thenReturn(Either.right(validLabel));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(colorOnlyCommand);

            // then
            assertThat(result.isRight()).isTrue();
        }
    }

    @Nested
    @DisplayName("검증 실패 테스트")
    class ValidationFailureTest {

        @Test
        @DisplayName("라벨 ID가 없으면 실패해야 한다")
        void updateLabel_withNullLabelId_shouldFail() {
            // given
            UpdateLabelCommand invalidCommand = UpdateLabelCommand.of(null, userId, "이름", "#FF0000");
            FieldViolation violation = FieldViolation.builder()
                    .field("labelId")
                    .message("라벨 ID는 필수입니다")
                    .rejectedValue(null)
                    .build();
            ValidationResult<UpdateLabelCommand> validationResult = ValidationResult.invalid(violation);

            when(labelValidator.validateUpdateLabel(invalidCommand))
                    .thenReturn(validationResult);

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(invalidCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);

            verify(labelValidator).validateUpdateLabel(invalidCommand);
            verify(labelRepository, never()).findById(any());
        }

        @Test
        @DisplayName("잘못된 색상 형식이면 실패해야 한다")
        void updateLabel_withInvalidColor_shouldFail() {
            // given
            UpdateLabelCommand invalidCommand = UpdateLabelCommand.of(labelId, userId, "이름", "INVALID_COLOR");
            FieldViolation violation = FieldViolation.builder()
                    .field("color")
                    .message("잘못된 색상 형식입니다")
                    .rejectedValue("INVALID_COLOR")
                    .build();
            ValidationResult<UpdateLabelCommand> validationResult = ValidationResult.invalid(violation);

            when(labelValidator.validateUpdateLabel(invalidCommand))
                    .thenReturn(validationResult);

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(invalidCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);

            verify(labelValidator).validateUpdateLabel(invalidCommand);
            verify(labelRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("라벨 조회 실패 테스트")
    class LabelNotFoundTest {

        @Test
        @DisplayName("존재하지 않는 라벨이면 실패해야 한다")
        void updateLabel_withNonExistentLabel_shouldFail() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.empty());

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("보드 존재 확인 테스트")
    class BoardExistsTest {

        @Test
        @DisplayName("라벨이 속한 보드가 존재하지 않으면 실패해야 한다")
        void updateLabel_withNonExistentBoard_shouldFail() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.empty());

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository).findById(boardId);
            verify(boardPermissionService, never()).canWriteBoard(any(), any());
        }
    }

    @Nested
    @DisplayName("권한 검증 테스트")
    class PermissionTest {

        @Test
        @DisplayName("라벨 수정 권한이 없으면 실패해야 한다")
        void updateLabel_withoutPermission_shouldFail() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(false));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository).findById(boardId);
            verify(boardPermissionService).canWriteBoard(boardId, userId);
            verify(labelRepository, never()).save(any());
        }

        @Test
        @DisplayName("보드 권한 조회 중 오류가 발생하면 실패해야 한다")
        void updateLabel_whenPermissionCheckFails_shouldFail() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.left(Failure.ofNotFound("보드를 찾을 수 없습니다")));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository).findById(boardId);
            verify(boardPermissionService).canWriteBoard(boardId, userId);
            verify(labelRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("라벨 필드 업데이트 테스트")
    class LabelFieldUpdateTest {

        @Test
        @DisplayName("라벨 필드 업데이트가 정상적으로 수행되어야 한다")
        void updateLabel_fieldUpdate_shouldSucceed() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(true));
            when(labelRepository.save(any(Label.class)))
                    .thenReturn(Either.right(validLabel));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isRight()).isTrue();

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository).findById(boardId);
            verify(boardPermissionService).canWriteBoard(boardId, userId);
            verify(labelRepository).save(any(Label.class));
        }
    }

    @Nested
    @DisplayName("라벨 저장 테스트")
    class LabelSaveTest {

        @Test
        @DisplayName("라벨 저장 중 예외가 발생하면 실패해야 한다")
        void updateLabel_whenSaveThrowsException_shouldFail() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(true));
            when(labelRepository.save(any(Label.class)))
                    .thenReturn(Either.left(Failure.ofInternalError("저장 중 오류 발생", "SAVE_ERROR", null)));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InternalError.class);

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository).findById(boardId);
            verify(boardPermissionService).canWriteBoard(boardId, userId);
            verify(labelRepository).save(any(Label.class));
        }
    }

    @Nested
    @DisplayName("체이닝 동작 테스트")
    class ChainingBehaviorTest {

        @Test
        @DisplayName("첫 번째 스텝에서 실패하면 나머지 스텝이 실행되지 않아야 한다")
        void updateLabel_whenFirstStepFails_shouldNotExecuteRemainingSteps() {
            // given
            FieldViolation violation = FieldViolation.builder()
                    .field("labelId")
                    .message("라벨 ID는 필수입니다")
                    .rejectedValue(null)
                    .build();
            ValidationResult<UpdateLabelCommand> validationResult = ValidationResult.invalid(violation);

            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(validationResult);

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository, never()).findById(any());
            verify(boardRepository, never()).findById(any());
            verify(boardPermissionService, never()).canWriteBoard(any(), any());
            verify(labelRepository, never()).save(any());
        }

        @Test
        @DisplayName("두 번째 스텝에서 실패하면 나머지 스텝이 실행되지 않아야 한다")
        void updateLabel_whenSecondStepFails_shouldNotExecuteRemainingSteps() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.empty());

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository, never()).findById(any());
            verify(boardPermissionService, never()).canWriteBoard(any(), any());
            verify(labelRepository, never()).save(any());
        }

        @Test
        @DisplayName("세 번째 스텝에서 실패하면 나머지 스텝이 실행되지 않아야 한다")
        void updateLabel_whenThirdStepFails_shouldNotExecuteRemainingSteps() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.empty());

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository).findById(boardId);
            verify(boardPermissionService, never()).canWriteBoard(any(), any());
            verify(labelRepository, never()).save(any());
        }

        @Test
        @DisplayName("네 번째 스텝에서 실패하면 나머지 스텝이 실행되지 않아야 한다")
        void updateLabel_whenFourthStepFails_shouldNotExecuteRemainingSteps() {
            // given
            when(labelValidator.validateUpdateLabel(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(false));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();

            verify(labelValidator).validateUpdateLabel(validCommand);
            verify(labelRepository).findById(labelId);
            verify(boardRepository).findById(boardId);
            verify(boardPermissionService).canWriteBoard(boardId, userId);
            verify(labelRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTest {

        @Test
        @DisplayName("빈 문자열로 이름을 수정하는 경우 성공해야 한다")
        void updateLabel_withEmptyName_shouldSucceed() {
            // given
            UpdateLabelCommand emptyNameCommand = UpdateLabelCommand.of(labelId, userId, "", "#FF0000");
            when(labelValidator.validateUpdateLabel(emptyNameCommand))
                    .thenReturn(ValidationResult.valid(emptyNameCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(true));
            when(labelRepository.save(any(Label.class)))
                    .thenReturn(Either.right(validLabel));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(emptyNameCommand);

            // then
            assertThat(result.isRight()).isTrue();
        }

        @Test
        @DisplayName("빈 문자열로 색상을 수정하는 경우 성공해야 한다")
        void updateLabel_withEmptyColor_shouldSucceed() {
            // given
            UpdateLabelCommand emptyColorCommand = UpdateLabelCommand.of(labelId, userId, "이름", "");
            when(labelValidator.validateUpdateLabel(emptyColorCommand))
                    .thenReturn(ValidationResult.valid(emptyColorCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(true));
            when(labelRepository.save(any(Label.class)))
                    .thenReturn(Either.right(validLabel));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(emptyColorCommand);

            // then
            assertThat(result.isRight()).isTrue();
        }

        @Test
        @DisplayName("3자리 hex 색상으로 수정이 성공해야 한다")
        void updateLabel_with3DigitHexColor_shouldSucceed() {
            // given
            UpdateLabelCommand threeDigitColorCommand = UpdateLabelCommand.of(labelId, userId, "이름", "#F00");
            when(labelValidator.validateUpdateLabel(threeDigitColorCommand))
                    .thenReturn(ValidationResult.valid(threeDigitColorCommand));
            when(labelRepository.findById(labelId))
                    .thenReturn(java.util.Optional.of(validLabel));
            when(boardRepository.findById(boardId))
                    .thenReturn(java.util.Optional.of(validBoard));
            when(boardPermissionService.canWriteBoard(boardId, userId))
                    .thenReturn(Either.right(true));
            when(labelRepository.save(any(Label.class)))
                    .thenReturn(Either.right(validLabel));

            // when
            Either<Failure, Label> result = labelUpdateService.updateLabel(threeDigitColorCommand);

            // then
            assertThat(result.isRight()).isTrue();
        }
    }
}