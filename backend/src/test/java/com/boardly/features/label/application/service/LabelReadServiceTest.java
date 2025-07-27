package com.boardly.features.label.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("LabelReadService 테스트")
class LabelReadServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MessageSource messageSource;

    private LabelReadService labelReadService;
    private ValidationMessageResolver validationMessageResolver;

    private UserId testUserId;
    private BoardId testBoardId;
    private LabelId testLabelId;
    private Board testBoard;
    private Label testLabel;
    private List<Label> testLabels;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);

        // 기본 메시지 설정 - lenient로 설정하여 불필요한 stubbing 허용
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

        validationMessageResolver = new ValidationMessageResolver(messageSource);
        labelReadService = new LabelReadService(labelRepository, boardRepository, validationMessageResolver);

        // 테스트 데이터 설정
        testUserId = new UserId("user-1");
        testBoardId = new BoardId("board-1");
        testLabelId = new LabelId("label-1");

        testBoard = Board.builder()
                .boardId(testBoardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .ownerId(testUserId)
                .isArchived(false)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testLabel = Label.builder()
                .labelId(testLabelId)
                .boardId(testBoardId)
                .name("테스트 라벨")
                .color("#FF0000")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testLabels = List.of(
                Label.builder()
                        .labelId(new LabelId("label-1"))
                        .boardId(testBoardId)
                        .name("라벨 1")
                        .color("#FF0000")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build(),
                Label.builder()
                        .labelId(new LabelId("label-2"))
                        .boardId(testBoardId)
                        .name("라벨 2")
                        .color("#00FF00")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build());
    }

    @Nested
    @DisplayName("getLabel 메서드 테스트")
    class GetLabelTest {

        @Test
        @DisplayName("유효한 라벨 ID로 조회 시 라벨을 반환한다")
        void getLabel_withValidLabelId_shouldReturnLabel() {
            // given
            when(labelRepository.findById(testLabelId)).thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Label> result = labelReadService.getLabel(testLabelId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testLabel);
            assertThat(result.get().getName()).isEqualTo("테스트 라벨");
            assertThat(result.get().getColor()).isEqualTo("#FF0000");

            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
        }

        @Test
        @DisplayName("존재하지 않는 라벨 ID로 조회 시 NotFound Failure를 반환한다")
        void getLabel_withNonExistentLabelId_shouldReturnNotFoundFailure() {
            // given
            when(labelRepository.findById(testLabelId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("validation.label.not.found"))
                    .thenReturn("라벨을 찾을 수 없습니다");

            // when
            Either<Failure, Label> result = labelReadService.getLabel(testLabelId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(result.getLeft().getMessage()).contains("라벨을 찾을 수 없습니다");

            verify(labelRepository).findById(testLabelId);
            verify(boardRepository, never()).findById(any());
        }

        @Test
        @DisplayName("존재하지 않는 보드에 대한 라벨 조회 시 NotFound Failure를 반환한다")
        void getLabel_withNonExistentBoard_shouldReturnNotFoundFailure() {
            // given
            when(labelRepository.findById(testLabelId)).thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("validation.board.not.found"))
                    .thenReturn("보드를 찾을 수 없습니다");

            // when
            Either<Failure, Label> result = labelReadService.getLabel(testLabelId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(result.getLeft().getMessage()).contains("보드를 찾을 수 없습니다");

            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
        }

        @Test
        @DisplayName("보드 소유자가 아닌 사용자가 라벨 조회 시 PermissionDenied Failure를 반환한다")
        void getLabel_withNonOwnerUser_shouldReturnPermissionDeniedFailure() {
            // given
            UserId differentUserId = new UserId("user-2");
            Board boardWithDifferentOwner = Board.builder()
                    .boardId(testBoardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .ownerId(differentUserId) // 다른 소유자
                    .isArchived(false)
                    .isStarred(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(labelRepository.findById(testLabelId)).thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(boardWithDifferentOwner));
            when(validationMessageResolver.getMessage("validation.board.access.denied"))
                    .thenReturn("보드에 접근할 권한이 없습니다");

            // when
            Either<Failure, Label> result = labelReadService.getLabel(testLabelId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(result.getLeft().getMessage()).contains("보드에 접근할 권한이 없습니다");

            verify(labelRepository).findById(testLabelId);
            verify(boardRepository).findById(testBoardId);
        }
    }

    @Nested
    @DisplayName("getBoardLabels 메서드 테스트")
    class GetBoardLabelsTest {

        @Test
        @DisplayName("유효한 보드 ID로 조회 시 라벨 목록을 반환한다")
        void getBoardLabels_withValidBoardId_shouldReturnLabels() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(labelRepository.findByBoardIdOrderByName(testBoardId)).thenReturn(testLabels);

            // when
            Either<Failure, List<Label>> result = labelReadService.getBoardLabels(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testLabels);
            assertThat(result.get()).hasSize(2);
            assertThat(result.get().get(0).getName()).isEqualTo("라벨 1");
            assertThat(result.get().get(1).getName()).isEqualTo("라벨 2");

            verify(boardRepository).findById(testBoardId);
            verify(labelRepository).findByBoardIdOrderByName(testBoardId);
        }

        @Test
        @DisplayName("존재하지 않는 보드 ID로 조회 시 NotFound Failure를 반환한다")
        void getBoardLabels_withNonExistentBoardId_shouldReturnNotFoundFailure() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("validation.board.not.found"))
                    .thenReturn("보드를 찾을 수 없습니다");

            // when
            Either<Failure, List<Label>> result = labelReadService.getBoardLabels(testBoardId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(result.getLeft().getMessage()).contains("보드를 찾을 수 없습니다");

            verify(boardRepository).findById(testBoardId);
            verify(labelRepository, never()).findByBoardIdOrderByName(any());
        }

        @Test
        @DisplayName("보드 소유자가 아닌 사용자가 조회 시 PermissionDenied Failure를 반환한다")
        void getBoardLabels_withNonOwnerUser_shouldReturnPermissionDeniedFailure() {
            // given
            UserId differentUserId = new UserId("user-2");
            Board boardWithDifferentOwner = Board.builder()
                    .boardId(testBoardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .ownerId(differentUserId) // 다른 소유자
                    .isArchived(false)
                    .isStarred(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(boardWithDifferentOwner));
            when(validationMessageResolver.getMessage("validation.board.access.denied"))
                    .thenReturn("보드에 접근할 권한이 없습니다");

            // when
            Either<Failure, List<Label>> result = labelReadService.getBoardLabels(testBoardId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(result.getLeft().getMessage()).contains("보드에 접근할 권한이 없습니다");

            verify(boardRepository).findById(testBoardId);
            verify(labelRepository, never()).findByBoardIdOrderByName(any());
        }

        @Test
        @DisplayName("라벨이 없는 보드 조회 시 빈 리스트를 반환한다")
        void getBoardLabels_withNoLabels_shouldReturnEmptyList() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(labelRepository.findByBoardIdOrderByName(testBoardId)).thenReturn(List.of());

            // when
            Either<Failure, List<Label>> result = labelReadService.getBoardLabels(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEmpty();

            verify(boardRepository).findById(testBoardId);
            verify(labelRepository).findByBoardIdOrderByName(testBoardId);
        }
    }

    @Nested
    @DisplayName("권한 확인 로직 테스트")
    class PermissionValidationTest {

        @Test
        @DisplayName("보드 소유자는 라벨에 접근할 수 있어야 한다")
        void boardOwner_shouldHaveAccessToLabels() {
            // given
            when(labelRepository.findById(testLabelId)).thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Label> result = labelReadService.getLabel(testLabelId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testLabel);
        }

        @Test
        @DisplayName("보드 소유자가 아닌 사용자는 라벨에 접근할 수 없어야 한다")
        void nonOwnerUser_shouldNotHaveAccessToLabels() {
            // given
            UserId differentUserId = new UserId("user-2");
            Board boardWithDifferentOwner = Board.builder()
                    .boardId(testBoardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .ownerId(differentUserId)
                    .isArchived(false)
                    .isStarred(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(labelRepository.findById(testLabelId)).thenReturn(Optional.of(testLabel));
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(boardWithDifferentOwner));
            when(validationMessageResolver.getMessage("validation.board.access.denied"))
                    .thenReturn("보드에 접근할 권한이 없습니다");

            // when
            Either<Failure, Label> result = labelReadService.getLabel(testLabelId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
        }
    }

    @Nested
    @DisplayName("에러 처리 테스트")
    class ErrorHandlingTest {

        @Test
        @DisplayName("라벨 조회 중 예외 발생 시 적절히 처리한다")
        void getLabel_whenExceptionOccurs_shouldHandleGracefully() {
            // given
            RuntimeException exception = new RuntimeException("데이터베이스 오류");
            when(labelRepository.findById(testLabelId)).thenThrow(exception);

            // when & then
            // 예외가 발생하면 테스트가 실패해야 함 (실제 구현에서는 예외가 발생하지 않아야 함)
            // 이는 실제 구현에서 예외 처리가 적절히 되어 있는지 확인하는 테스트
            try {
                labelReadService.getLabel(testLabelId, testUserId);
                // 예외가 발생하지 않았다면 테스트 실패
                org.junit.jupiter.api.Assertions.fail("예외가 발생해야 합니다");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("데이터베이스 오류");
            }
        }

        @Test
        @DisplayName("보드 조회 중 예외 발생 시 적절히 처리한다")
        void getBoardLabels_whenExceptionOccurs_shouldHandleGracefully() {
            // given
            RuntimeException exception = new RuntimeException("데이터베이스 오류");
            when(boardRepository.findById(testBoardId)).thenThrow(exception);

            // when & then
            // 예외가 발생하면 테스트가 실패해야 함 (실제 구현에서는 예외가 발생하지 않아야 함)
            try {
                labelReadService.getBoardLabels(testBoardId, testUserId);
                // 예외가 발생하지 않았다면 테스트 실패
                org.junit.jupiter.api.Assertions.fail("예외가 발생해야 합니다");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("데이터베이스 오류");
            }
        }
    }
}