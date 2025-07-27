package com.boardly.features.label.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.card.domain.repository.CardLabelRepository;
import com.boardly.features.label.application.port.input.DeleteLabelCommand;
import com.boardly.features.label.application.validation.LabelValidator;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("LabelDeleteService 테스트")
class LabelDeleteServiceTest {

    @Mock
    private LabelValidator labelValidator;

    @Mock
    private ValidationMessageResolver messageResolver;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CardLabelRepository cardLabelRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @InjectMocks
    private LabelDeleteService labelDeleteService;

    private LabelId testLabelId;
    private UserId testUserId;
    private BoardId testBoardId;
    private DeleteLabelCommand testCommand;
    private Label testLabel;
    private Board testBoard;

    @BeforeEach
    void setUp() {
        testLabelId = new LabelId("label-1");
        testUserId = new UserId("user-1");
        testBoardId = new BoardId("board-1");

        testCommand = new DeleteLabelCommand(testLabelId, testUserId);

        testLabel = createTestLabel();
        testBoard = createTestBoard();
    }

    // ==================== HELPER METHODS ====================

    private Label createTestLabel() {
        return Label.restore(
                testLabelId,
                testBoardId,
                "테스트 라벨",
                "#FF0000",
                Instant.now(),
                Instant.now());
    }

    private Board createTestBoard() {
        return Board.builder()
                .boardId(testBoardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .isArchived(false)
                .ownerId(testUserId)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private ValidationResult<DeleteLabelCommand> createValidValidationResult() {
        return ValidationResult.valid(testCommand);
    }

    private ValidationResult<DeleteLabelCommand> createInvalidValidationResult() {
        return ValidationResult.invalid(
                Failure.FieldViolation.builder()
                        .field("labelId")
                        .message("라벨 ID는 필수입니다")
                        .rejectedValue(null)
                        .build());
    }

    // ==================== DELETE LABEL TESTS ====================

    @Nested
    @DisplayName("deleteLabel 메서드 테스트")
    class DeleteLabelTests {

        @Test
        @DisplayName("유효한 라벨 삭제 요청은 성공해야 한다")
        void deleteLabel_withValidRequest_shouldSucceed() {
            // given
            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                    .thenReturn(Either.right(true));
            when(cardLabelRepository.deleteByLabelId(testLabelId))
                    .thenReturn(Either.right(null));
            when(labelRepository.delete(testLabelId))
                    .thenReturn(Either.right(null));

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isNull();

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canWriteBoard(testBoardId, testUserId);
            verify(cardLabelRepository).deleteByLabelId(testLabelId);
            verify(labelRepository).delete(testLabelId);
        }

        @Test
        @DisplayName("명령어 검증 실패 시 Validation 오류를 반환해야 한다")
        void deleteLabel_withInvalidCommand_shouldReturnValidationError() {
            // given
            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createInvalidValidationResult());
            when(messageResolver.getMessage("error.service.label.delete.validation"))
                    .thenReturn("라벨 삭제 입력 검증에 실패했습니다");

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("라벨 삭제 입력 검증에 실패했습니다");

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(messageResolver).getMessage("error.service.label.delete.validation");
            verifyNoInteractions(labelRepository, boardRepository, boardPermissionService,
                    cardLabelRepository);
        }

        @Test
        @DisplayName("라벨이 존재하지 않는 경우 NotFound 오류를 반환해야 한다")
        void deleteLabel_withNonExistentLabel_shouldReturnNotFoundError() {
            // given
            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.empty());
            when(messageResolver.getMessage("error.service.label.delete.not.found"))
                    .thenReturn("삭제할 라벨을 찾을 수 없습니다");

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("삭제할 라벨을 찾을 수 없습니다");

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(messageResolver).getMessage("error.service.label.delete.not.found");
            verifyNoInteractions(boardRepository, boardPermissionService, cardLabelRepository);
        }

        @Test
        @DisplayName("보드가 존재하지 않는 경우 NotFound 오류를 반환해야 한다")
        void deleteLabel_withNonExistentBoard_shouldReturnNotFoundError() {
            // given
            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.empty());
            when(messageResolver.getMessage("error.service.label.delete.board.not.found"))
                    .thenReturn("라벨이 속한 보드를 찾을 수 없습니다");

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("라벨이 속한 보드를 찾을 수 없습니다");

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
            verify(messageResolver).getMessage("error.service.label.delete.board.not.found");
            verifyNoInteractions(boardPermissionService, cardLabelRepository);
        }

        @Test
        @DisplayName("권한이 없는 경우 PermissionDenied 오류를 반환해야 한다")
        void deleteLabel_withInsufficientPermission_shouldReturnPermissionDeniedError() {
            // given
            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                    .thenReturn(Either.right(false));
            when(messageResolver.getMessage("error.service.label.delete.permission.denied"))
                    .thenReturn("라벨을 삭제할 권한이 없습니다");

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("라벨을 삭제할 권한이 없습니다");

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canWriteBoard(testBoardId, testUserId);
            verify(messageResolver).getMessage("error.service.label.delete.permission.denied");
            verifyNoInteractions(cardLabelRepository);
        }

        @Test
        @DisplayName("카드-라벨 연결 삭제 실패 시 InternalServerError를 반환해야 한다")
        void deleteLabel_withCardConnectionDeletionFailure_shouldReturnInternalServerError() {
            // given
            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                    .thenReturn(Either.right(true));
            when(cardLabelRepository.deleteByLabelId(testLabelId))
                    .thenReturn(Either.left(Failure.ofInternalServerError("카드 연결 삭제 실패")));
            when(messageResolver.getMessage("error.service.label.delete.card.connections"))
                    .thenReturn("라벨-카드 연결 삭제 중 오류가 발생했습니다");

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("라벨-카드 연결 삭제 중 오류가 발생했습니다");

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canWriteBoard(testBoardId, testUserId);
            verify(cardLabelRepository).deleteByLabelId(testLabelId);
            verify(messageResolver).getMessage("error.service.label.delete.card.connections");
            verify(labelRepository, never()).delete(any());
        }

        @Test
        @DisplayName("라벨 엔티티 삭제 실패 시 InternalServerError를 반환해야 한다")
        void deleteLabel_withLabelDeletionFailure_shouldReturnInternalServerError() {
            // given
            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                    .thenReturn(Either.right(true));
            when(cardLabelRepository.deleteByLabelId(testLabelId))
                    .thenReturn(Either.right(null));
            when(labelRepository.delete(testLabelId))
                    .thenReturn(Either.left(Failure.ofInternalServerError("라벨 삭제 실패")));
            when(messageResolver.getMessage("error.service.label.delete.internal"))
                    .thenReturn("라벨 삭제 중 오류가 발생했습니다");

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("라벨 삭제 중 오류가 발생했습니다");

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canWriteBoard(testBoardId, testUserId);
            verify(cardLabelRepository).deleteByLabelId(testLabelId);
            verify(labelRepository).delete(testLabelId);
            verify(messageResolver).getMessage("error.service.label.delete.internal");
        }

        @Test
        @DisplayName("권한 검증에서 오류가 발생한 경우 해당 오류를 반환해야 한다")
        void deleteLabel_withPermissionCheckFailure_shouldReturnPermissionError() {
            // given
            Failure permissionFailure = Failure.ofPermissionDenied("권한 확인 실패");

            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                    .thenReturn(Either.left(permissionFailure));

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(permissionFailure);

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canWriteBoard(testBoardId, testUserId);
            verifyNoInteractions(cardLabelRepository);
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("다른 사용자가 소유한 보드의 라벨 삭제 시도 시 권한 오류를 반환해야 한다")
        void deleteLabel_withDifferentBoardOwner_shouldReturnPermissionError() {
            // given
            UserId differentUserId = new UserId("user-2");
            Board differentBoard = Board.builder()
                    .boardId(testBoardId)
                    .title("다른 사용자의 보드")
                    .description("다른 사용자가 소유한 보드")
                    .isArchived(false)
                    .ownerId(differentUserId)
                    .isStarred(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(differentBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                    .thenReturn(Either.right(false));
            when(messageResolver.getMessage("error.service.label.delete.permission.denied"))
                    .thenReturn("라벨을 삭제할 권한이 없습니다");

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("라벨을 삭제할 권한이 없습니다");

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canWriteBoard(testBoardId, testUserId);
            verifyNoInteractions(cardLabelRepository);
        }

        @Test
        @DisplayName("아카이브된 보드의 라벨 삭제도 성공해야 한다")
        void deleteLabel_withArchivedBoard_shouldSucceed() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(testBoardId)
                    .title("아카이브된 보드")
                    .description("아카이브된 보드")
                    .isArchived(true)
                    .ownerId(testUserId)
                    .isStarred(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(archivedBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                    .thenReturn(Either.right(true));
            when(cardLabelRepository.deleteByLabelId(testLabelId))
                    .thenReturn(Either.right(null));
            when(labelRepository.delete(testLabelId))
                    .thenReturn(Either.right(null));

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isNull();

            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canWriteBoard(testBoardId, testUserId);
            verify(cardLabelRepository).deleteByLabelId(testLabelId);
            verify(labelRepository).delete(testLabelId);
        }
    }

    // ==================== INTEGRATION SCENARIOS ====================

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("라벨 삭제 시 모든 관련 데이터가 올바른 순서로 삭제되어야 한다")
        void deleteLabel_shouldDeleteRelatedDataInCorrectOrder() {
            // given
            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                    .thenReturn(Either.right(true));
            when(cardLabelRepository.deleteByLabelId(testLabelId))
                    .thenReturn(Either.right(null));
            when(labelRepository.delete(testLabelId))
                    .thenReturn(Either.right(null));

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isRight()).isTrue();

            // 순서 검증
            verify(labelValidator).validateDeleteLabel(testCommand);
            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canWriteBoard(testBoardId, testUserId);
            verify(cardLabelRepository).deleteByLabelId(testLabelId);
            verify(labelRepository).delete(testLabelId);

            // 순서 확인을 위한 순서 검증
            verifyNoMoreInteractions(labelValidator, labelRepository, boardRepository,
                    boardPermissionService, cardLabelRepository);
        }

        @Test
        @DisplayName("라벨 삭제 실패 시 롤백이 올바르게 처리되어야 한다")
        void deleteLabel_withFailure_shouldHandleRollbackCorrectly() {
            // given
            when(labelValidator.validateDeleteLabel(testCommand))
                    .thenReturn(createValidValidationResult());
            when(labelRepository.findById(testLabelId))
                    .thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                    .thenReturn(Either.right(true));
            when(cardLabelRepository.deleteByLabelId(testLabelId))
                    .thenReturn(Either.right(null));
            when(labelRepository.delete(testLabelId))
                    .thenReturn(Either.left(Failure.ofInternalServerError("데이터베이스 오류")));
            when(messageResolver.getMessage("error.service.label.delete.internal"))
                    .thenReturn("라벨 삭제 중 오류가 발생했습니다");

            // when
            Either<Failure, Void> result = labelDeleteService.deleteLabel(testCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);

            // 카드 연결은 삭제되었지만 라벨 삭제는 실패했으므로 롤백이 필요
            verify(cardLabelRepository).deleteByLabelId(testLabelId);
            verify(labelRepository).delete(testLabelId);
        }
    }
}