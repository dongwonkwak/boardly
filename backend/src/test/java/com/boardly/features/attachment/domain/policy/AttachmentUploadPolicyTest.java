package com.boardly.features.attachment.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.boardly.features.attachment.domain.repository.AttachmentRepository;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttachmentUploadPolicy")
class AttachmentUploadPolicyTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private AttachmentPolicyConfig policyConfig;

    private AttachmentUploadPolicy attachmentUploadPolicy;

    @BeforeEach
    void setUp() {
        attachmentUploadPolicy = new AttachmentUploadPolicy(attachmentRepository, policyConfig);
    }

    @Nested
    @DisplayName("canUploadAttachment")
    class CanUploadAttachment {

        @Test
        @DisplayName("모든 조건이 만족되면 성공을 반환한다")
        void shouldReturnSuccessWhenAllConditionsAreMet() {
            // given
            CardId cardId = new CardId("card-123");
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "test content".getBytes());

            when(policyConfig.getMaxFileSizeMB()).thenReturn(10);
            when(policyConfig.getMaxAttachmentsPerCard()).thenReturn(10);
            when(policyConfig.getMaxFileNameLength()).thenReturn(255);
            when(attachmentRepository.countByCardId(cardId)).thenReturn(5);

            // when
            Either<Failure, Void> result = attachmentUploadPolicy.canUploadAttachment(cardId, file);

            // then
            assertThat(result.isRight()).isTrue();
        }

        @Test
        @DisplayName("파일 크기가 제한을 초과하면 실패를 반환한다")
        void shouldReturnFailureWhenFileSizeExceedsLimit() {
            // given
            CardId cardId = new CardId("card-123");
            MockMultipartFile file = new MockMultipartFile(
                    "file", "large.jpg", "image/jpeg", new byte[11 * 1024 * 1024]); // 11MB

            when(policyConfig.getMaxFileSizeMB()).thenReturn(10);
            when(policyConfig.getMaxFileNameLength()).thenReturn(255);

            // when
            Either<Failure, Void> result = attachmentUploadPolicy.canUploadAttachment(cardId, file);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("파일 크기는 최대 10MB까지 업로드할 수 있습니다");
        }

        @Test
        @DisplayName("카드당 첨부파일 개수가 제한에 도달하면 실패를 반환한다")
        void shouldReturnFailureWhenAttachmentCountReachesLimit() {
            // given
            CardId cardId = new CardId("card-123");
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "test content".getBytes());

            when(policyConfig.getMaxFileSizeMB()).thenReturn(10);
            when(policyConfig.getMaxAttachmentsPerCard()).thenReturn(10);
            when(policyConfig.getMaxFileNameLength()).thenReturn(255);
            when(attachmentRepository.countByCardId(cardId)).thenReturn(10);

            // when
            Either<Failure, Void> result = attachmentUploadPolicy.canUploadAttachment(cardId, file);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("카드당 최대 10개의 첨부파일만 업로드할 수 있습니다");
        }

        @Test
        @DisplayName("파일명이 null이면 실패를 반환한다")
        void shouldReturnFailureWhenFileNameIsNull() {
            // given
            CardId cardId = new CardId("card-123");
            MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn(null);

            // when
            Either<Failure, Void> result = attachmentUploadPolicy.canUploadAttachment(cardId, file);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("파일명이 유효하지 않습니다");
        }

        @Test
        @DisplayName("파일명 길이가 제한을 초과하면 실패를 반환한다")
        void shouldReturnFailureWhenFileNameLengthExceedsLimit() {
            // given
            CardId cardId = new CardId("card-123");
            String longFileName = "a".repeat(256); // 256자
            MockMultipartFile file = new MockMultipartFile(
                    "file", longFileName, "image/jpeg", "test content".getBytes());

            when(policyConfig.getMaxFileNameLength()).thenReturn(255);

            // when
            Either<Failure, Void> result = attachmentUploadPolicy.canUploadAttachment(cardId, file);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("파일명은 최대 255자까지 입력할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("정책 값 조회")
    class PolicyValueGetters {

        @Test
        @DisplayName("최대 파일 크기를 반환한다")
        void shouldReturnMaxFileSizeMB() {
            // given
            when(policyConfig.getMaxFileSizeMB()).thenReturn(15);

            // when
            int result = attachmentUploadPolicy.getMaxFileSizeMB();

            // then
            assertThat(result).isEqualTo(15);
        }

        @Test
        @DisplayName("카드당 최대 첨부파일 개수를 반환한다")
        void shouldReturnMaxAttachmentsPerCard() {
            // given
            when(policyConfig.getMaxAttachmentsPerCard()).thenReturn(20);

            // when
            int result = attachmentUploadPolicy.getMaxAttachmentsPerCard();

            // then
            assertThat(result).isEqualTo(20);
        }

        @Test
        @DisplayName("파일명 최대 길이를 반환한다")
        void shouldReturnMaxFileNameLength() {
            // given
            when(policyConfig.getMaxFileNameLength()).thenReturn(300);

            // when
            int result = attachmentUploadPolicy.getMaxFileNameLength();

            // then
            assertThat(result).isEqualTo(300);
        }

        @Test
        @DisplayName("사용 가능한 첨부파일 슬롯 개수를 반환한다")
        void shouldReturnAvailableAttachmentSlots() {
            // given
            CardId cardId = new CardId("card-123");
            when(policyConfig.getMaxAttachmentsPerCard()).thenReturn(10);
            when(attachmentRepository.countByCardId(cardId)).thenReturn(3);

            // when
            int result = attachmentUploadPolicy.getAvailableAttachmentSlots(cardId);

            // then
            assertThat(result).isEqualTo(7);
        }

        @Test
        @DisplayName("첨부파일 개수가 최대에 도달하면 사용 가능한 슬롯은 0개이다")
        void shouldReturnZeroAvailableSlotsWhenLimitReached() {
            // given
            CardId cardId = new CardId("card-123");
            when(policyConfig.getMaxAttachmentsPerCard()).thenReturn(10);
            when(attachmentRepository.countByCardId(cardId)).thenReturn(10);

            // when
            int result = attachmentUploadPolicy.getAvailableAttachmentSlots(cardId);

            // then
            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("기본값 조회")
    class DefaultValueGetters {

        @Test
        @DisplayName("기본 최대 파일 크기를 반환한다")
        void shouldReturnDefaultMaxFileSizeMB() {
            // when
            int result = AttachmentUploadPolicy.getDefaultMaxFileSizeMB();

            // then
            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("기본 카드당 최대 첨부파일 개수를 반환한다")
        void shouldReturnDefaultMaxAttachmentsPerCard() {
            // when
            int result = AttachmentUploadPolicy.getDefaultMaxAttachmentsPerCard();

            // then
            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("기본 파일명 최대 길이를 반환한다")
        void shouldReturnDefaultMaxFileNameLength() {
            // when
            int result = AttachmentUploadPolicy.getDefaultMaxFileNameLength();

            // then
            assertThat(result).isEqualTo(255);
        }
    }
}