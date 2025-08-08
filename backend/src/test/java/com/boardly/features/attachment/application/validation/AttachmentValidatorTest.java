package com.boardly.features.attachment.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import com.boardly.features.attachment.application.port.input.DeleteAttachmentCommand;
import com.boardly.features.attachment.application.port.input.UpdateAttachmentCommand;
import com.boardly.features.attachment.application.port.input.UploadAttachmentCommand;
import com.boardly.features.attachment.domain.model.AttachmentId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttachmentValidator 테스트")
class AttachmentValidatorTest {

    @Mock
    private MessageSource messageSource;

    private AttachmentValidator attachmentValidator;
    private CommonValidationRules commonValidationRules;
    private ValidationMessageResolver messageResolver;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);

        // 기본 메시지 설정 - lenient로 설정하여 불필요한 stubbing 허용
        lenient()
            .when(
                messageSource.getMessage(
                    anyString(),
                    any(Object[].class),
                    any(Locale.class)
                )
            )
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

        messageResolver = new ValidationMessageResolver(messageSource);
        commonValidationRules = new CommonValidationRules(messageResolver);
        attachmentValidator = new AttachmentValidator(
            commonValidationRules,
            messageResolver
        );
    }

    @Nested
    @DisplayName("UploadAttachmentCommand 검증 테스트")
    class UploadAttachmentValidationTest {

        @Test
        @DisplayName("유효한 업로드 커맨드 검증 성공")
        void validateUpload_ValidCommand_ShouldReturnSuccess() {
            // given
            CardId cardId = new CardId("card-123");
            UserId uploaderId = new UserId("user-123");
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes()
            );

            UploadAttachmentCommand command = new UploadAttachmentCommand(
                cardId,
                uploaderId,
                file
            );

            // when
            ValidationResult<UploadAttachmentCommand> result =
                attachmentValidator.validateUpload(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("파일이 null인 경우 검증 실패")
        void validateUpload_NullFile_ShouldReturnFailure() {
            // given
            CardId cardId = new CardId("card-123");
            UserId uploaderId = new UserId("user-123");
            UploadAttachmentCommand command = new UploadAttachmentCommand(
                cardId,
                uploaderId,
                null
            );

            // when
            ValidationResult<UploadAttachmentCommand> result =
                attachmentValidator.validateUpload(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("file");
        }

        @Test
        @DisplayName("파일이 비어있는 경우 검증 실패")
        void validateUpload_EmptyFile_ShouldReturnFailure() {
            // given
            CardId cardId = new CardId("card-123");
            UserId uploaderId = new UserId("user-123");
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[0]
            );
            UploadAttachmentCommand command = new UploadAttachmentCommand(
                cardId,
                uploaderId,
                file
            );

            // when
            ValidationResult<UploadAttachmentCommand> result =
                attachmentValidator.validateUpload(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("file");
        }

        @Test
        @DisplayName("허용되지 않는 파일 타입인 경우 검증 실패")
        void validateUpload_InvalidFileType_ShouldReturnFailure() {
            // given
            CardId cardId = new CardId("card-123");
            UserId uploaderId = new UserId("user-123");
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/x-executable",
                "executable content".getBytes()
            );
            UploadAttachmentCommand command = new UploadAttachmentCommand(
                cardId,
                uploaderId,
                file
            );

            // when
            ValidationResult<UploadAttachmentCommand> result =
                attachmentValidator.validateUpload(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("file");
        }

        @Test
        @DisplayName("허용되는 파일 타입들 검증 성공")
        void validateUpload_AllowedFileTypes_ShouldReturnSuccess() {
            // given
            CardId cardId = new CardId("card-123");
            UserId uploaderId = new UserId("user-123");

            // 허용되는 파일 타입들
            String[] allowedTypes = {
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/gif",
                "image/webp",
                "image/svg+xml",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "text/plain",
                "text/csv",
                "text/html",
                "text/css",
                "text/javascript",
                "application/zip",
                "application/x-rar-compressed",
                "application/x-7z-compressed",
                "application/json",
                "application/xml",
            };

            for (String contentType : allowedTypes) {
                MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test." + contentType.split("/")[1],
                    contentType,
                    "test content".getBytes()
                );
                UploadAttachmentCommand command = new UploadAttachmentCommand(
                    cardId,
                    uploaderId,
                    file
                );

                // when
                ValidationResult<UploadAttachmentCommand> result =
                    attachmentValidator.validateUpload(command);

                // then
                assertThat(result.isValid())
                    .withFailMessage(
                        "파일 타입 %s가 허용되어야 함",
                        contentType
                    )
                    .isTrue();
            }
        }

        @Test
        @DisplayName("기존 validate 메서드 호환성 테스트")
        void validate_UploadCommand_ShouldWorkWithLegacyMethod() {
            // given
            CardId cardId = new CardId("card-123");
            UserId uploaderId = new UserId("user-123");
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes()
            );

            UploadAttachmentCommand command = new UploadAttachmentCommand(
                cardId,
                uploaderId,
                file
            );

            // when
            ValidationResult<UploadAttachmentCommand> result =
                attachmentValidator.validate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("UpdateAttachmentCommand 검증 테스트")
    class UpdateAttachmentValidationTest {

        @Test
        @DisplayName("유효한 수정 커맨드 검증 성공")
        void validateUpdate_ValidCommand_ShouldReturnSuccess() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            String fileName = "updated-file.txt";
            UpdateAttachmentCommand command = new UpdateAttachmentCommand(
                attachmentId,
                fileName
            );

            // when
            ValidationResult<UpdateAttachmentCommand> result =
                attachmentValidator.validateUpdate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("첨부파일 ID가 null인 경우 검증 실패")
        void validateUpdate_NullAttachmentId_ShouldReturnFailure() {
            // given
            String fileName = "updated-file.txt";
            UpdateAttachmentCommand command = new UpdateAttachmentCommand(
                null,
                fileName
            );

            // when
            ValidationResult<UpdateAttachmentCommand> result =
                attachmentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "attachmentId"
            );
        }

        @Test
        @DisplayName("파일명이 null인 경우 검증 실패")
        void validateUpdate_NullFileName_ShouldReturnFailure() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            UpdateAttachmentCommand command = new UpdateAttachmentCommand(
                attachmentId,
                null
            );

            // when
            ValidationResult<UpdateAttachmentCommand> result =
                attachmentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "fileName"
            );
        }

        @Test
        @DisplayName("파일명이 비어있는 경우 검증 실패")
        void validateUpdate_EmptyFileName_ShouldReturnFailure() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            UpdateAttachmentCommand command = new UpdateAttachmentCommand(
                attachmentId,
                ""
            );

            // when
            ValidationResult<UpdateAttachmentCommand> result =
                attachmentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(2); // 필수 검증과 패턴 검증 모두 실패
            assertThat(
                result
                    .getErrors()
                    .map(v -> v.getField())
                    .forAll(field -> field.equals("fileName"))
            ).isTrue();
        }

        @Test
        @DisplayName("파일명이 공백만 있는 경우 검증 실패")
        void validateUpdate_WhitespaceOnlyFileName_ShouldReturnFailure() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            UpdateAttachmentCommand command = new UpdateAttachmentCommand(
                attachmentId,
                "   "
            );

            // when
            ValidationResult<UpdateAttachmentCommand> result =
                attachmentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(2); // 필수 검증과 패턴 검증 모두 실패
            assertThat(
                result
                    .getErrors()
                    .map(v -> v.getField())
                    .forAll(field -> field.equals("fileName"))
            ).isTrue();
        }

        @Test
        @DisplayName("파일명에 HTML 태그가 포함된 경우 검증 실패")
        void validateUpdate_FileNameWithHtmlTags_ShouldReturnFailure() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            String fileName = "<script>alert('test')</script>.txt";
            UpdateAttachmentCommand command = new UpdateAttachmentCommand(
                attachmentId,
                fileName
            );

            // when
            ValidationResult<UpdateAttachmentCommand> result =
                attachmentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "fileName"
            );
        }

        @Test
        @DisplayName("파일명에 특수문자가 포함된 경우 검증 실패")
        void validateUpdate_FileNameWithSpecialCharacters_ShouldReturnFailure() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            String fileName = "file@#$%^&*().txt";
            UpdateAttachmentCommand command = new UpdateAttachmentCommand(
                attachmentId,
                fileName
            );

            // when
            ValidationResult<UpdateAttachmentCommand> result =
                attachmentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "fileName"
            );
        }

        @Test
        @DisplayName("유효한 파일명 패턴 검증 성공")
        void validateUpdate_ValidFileNamePatterns_ShouldReturnSuccess() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");

            // 유효한 파일명들
            String[] validFileNames = {
                "document.txt",
                "image.jpg",
                "file-name.pdf",
                "한글파일.docx",
                "file with spaces.txt",
                "file_with_underscore.pdf",
                "file.with.dots.docx",
                "file!with!exclamation.pdf",
                "file(with)parentheses.txt",
                "file,with,commas.pdf",
            };

            for (String fileName : validFileNames) {
                UpdateAttachmentCommand command = new UpdateAttachmentCommand(
                    attachmentId,
                    fileName
                );

                // when
                ValidationResult<UpdateAttachmentCommand> result =
                    attachmentValidator.validateUpdate(command);

                // then
                assertThat(result.isValid())
                    .withFailMessage("파일명 '%s'가 유효해야 함", fileName)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("기존 validate 메서드 호환성 테스트")
        void validate_UpdateCommand_ShouldWorkWithLegacyMethod() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            String fileName = "updated-file.txt";
            UpdateAttachmentCommand command = new UpdateAttachmentCommand(
                attachmentId,
                fileName
            );

            // when
            ValidationResult<UpdateAttachmentCommand> result =
                attachmentValidator.validate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("DeleteAttachmentCommand 검증 테스트")
    class DeleteAttachmentValidationTest {

        @Test
        @DisplayName("유효한 삭제 커맨드 검증 성공")
        void validateDelete_ValidCommand_ShouldReturnSuccess() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            UserId requesterId = new UserId("user-123");
            DeleteAttachmentCommand command = new DeleteAttachmentCommand(
                attachmentId,
                requesterId
            );

            // when
            ValidationResult<DeleteAttachmentCommand> result =
                attachmentValidator.validateDelete(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("첨부파일 ID가 null인 경우 검증 실패")
        void validateDelete_NullAttachmentId_ShouldReturnFailure() {
            // given
            UserId requesterId = new UserId("user-123");
            DeleteAttachmentCommand command = new DeleteAttachmentCommand(
                null,
                requesterId
            );

            // when
            ValidationResult<DeleteAttachmentCommand> result =
                attachmentValidator.validateDelete(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "attachmentId"
            );
        }

        @Test
        @DisplayName("요청자 ID가 null인 경우 검증 실패")
        void validateDelete_NullRequesterId_ShouldReturnFailure() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            DeleteAttachmentCommand command = new DeleteAttachmentCommand(
                attachmentId,
                null
            );

            // when
            ValidationResult<DeleteAttachmentCommand> result =
                attachmentValidator.validateDelete(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "requesterId"
            );
        }

        @Test
        @DisplayName("기존 validate 메서드 호환성 테스트")
        void validate_DeleteCommand_ShouldWorkWithLegacyMethod() {
            // given
            AttachmentId attachmentId = new AttachmentId("attachment-123");
            UserId requesterId = new UserId("user-123");
            DeleteAttachmentCommand command = new DeleteAttachmentCommand(
                attachmentId,
                requesterId
            );

            // when
            ValidationResult<DeleteAttachmentCommand> result =
                attachmentValidator.validate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("헬퍼 메서드 테스트")
    class HelperMethodTest {

        @Test
        @DisplayName("파일 크기 읽기 쉬운 형태 변환 테스트")
        void getReadableFileSize_ShouldReturnFormattedSize() {
            // given & when & then
            assertThat(AttachmentValidator.getReadableFileSize(512)).isEqualTo(
                "512 B"
            );
            assertThat(AttachmentValidator.getReadableFileSize(1024)).isEqualTo(
                "1.0 KB"
            );
            assertThat(
                AttachmentValidator.getReadableFileSize(1024 * 1024)
            ).isEqualTo("1.0 MB");
            assertThat(
                AttachmentValidator.getReadableFileSize(1536 * 1024)
            ).isEqualTo("1.5 MB");
            assertThat(
                AttachmentValidator.getReadableFileSize(2048 * 1024)
            ).isEqualTo("2.0 MB");
        }

        @Test
        @DisplayName("파일 크기 0바이트 테스트")
        void getReadableFileSize_ZeroBytes_ShouldReturnZeroB() {
            // given & when & then
            assertThat(AttachmentValidator.getReadableFileSize(0)).isEqualTo(
                "0 B"
            );
        }

        @Test
        @DisplayName("파일 크기 큰 값 테스트")
        void getReadableFileSize_LargeFile_ShouldReturnCorrectFormat() {
            // given & when & then
            assertThat(
                AttachmentValidator.getReadableFileSize(1024 * 1024 * 1024)
            ).isEqualTo("1024.0 MB");
            assertThat(
                AttachmentValidator.getReadableFileSize(
                    1024 * 1024 * 1024 + 512 * 1024 * 1024
                )
            ).isEqualTo("1536.0 MB");
        }
    }
}
