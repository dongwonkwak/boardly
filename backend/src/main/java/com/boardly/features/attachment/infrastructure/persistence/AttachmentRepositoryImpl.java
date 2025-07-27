package com.boardly.features.attachment.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.boardly.features.attachment.domain.repository.AttachmentRepository;
import com.boardly.features.attachment.domain.model.Attachment;
import com.boardly.features.attachment.domain.model.AttachmentId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AttachmentRepositoryImpl implements AttachmentRepository {

    private final AttachmentJpaRepository attachmentJpaRepository;
    private final AttachmentMapper attachmentMapper;

    @Override
    public Either<Failure, Attachment> save(Attachment attachment) {
        log.debug("첨부파일 저장 시작: attachmentId={}, cardId={}, fileName={}",
                attachment.getAttachmentId(), attachment.getCardId(), attachment.getFileName());

        try {
            var entity = attachmentMapper.toEntity(attachment);
            var savedEntity = attachmentJpaRepository.save(entity);
            var savedAttachment = attachmentMapper.toDomain(savedEntity);
            log.debug("첨부파일 저장 성공: attachmentId={}, fileName={}",
                    savedAttachment.getAttachmentId(), savedAttachment.getFileName());

            return Either.right(savedAttachment);
        } catch (Exception e) {
            log.error("첨부파일 저장 실패: attachmentId={}, fileName={}, 예외={}",
                    attachment.getAttachmentId(), attachment.getFileName(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("첨부파일 저장 실패: " + e.getMessage()));
        }
    }

    @Override
    public Optional<Attachment> findById(AttachmentId attachmentId) {
        log.debug("첨부파일 조회 시작: attachmentId={}", attachmentId);

        var attachment = attachmentJpaRepository.findById(attachmentId.getId())
                .map(attachmentMapper::toDomain);

        if (attachment.isPresent()) {
            log.debug("첨부파일 조회 완료: attachmentId={}, fileName={}",
                    attachmentId, attachment.get().getFileName());
        }

        return attachment;
    }

    @Override
    public List<Attachment> findByCardIdOrderByCreatedAt(CardId cardId) {
        log.debug("카드별 첨부파일 조회 시작: cardId={}", cardId.getId());
        var entities = attachmentJpaRepository.findByCardIdOrderByCreatedAt(cardId.getId());
        var attachments = entities.stream()
                .map(attachmentMapper::toDomain)
                .toList();
        log.debug("카드별 첨부파일 조회 완료: cardId={}, 첨부파일 개수={}", cardId.getId(), attachments.size());
        return attachments;
    }

    @Override
    public List<Attachment> findByUploaderIdOrderByCreatedAtDesc(UserId uploaderId) {
        log.debug("업로더별 첨부파일 조회 시작: uploaderId={}", uploaderId.getId());
        var entities = attachmentJpaRepository.findByUploaderIdOrderByCreatedAtDesc(uploaderId.getId());
        var attachments = entities.stream()
                .map(attachmentMapper::toDomain)
                .toList();
        log.debug("업로더별 첨부파일 조회 완료: uploaderId={}, 첨부파일 개수={}", uploaderId.getId(), attachments.size());
        return attachments;
    }

    @Override
    public Either<Failure, Void> delete(AttachmentId attachmentId) {
        log.debug("첨부파일 삭제 시작: attachmentId={}", attachmentId);

        try {
            attachmentJpaRepository.deleteById(attachmentId.getId());
            log.debug("첨부파일 삭제 완료: attachmentId={}", attachmentId);
            return Either.right(null);
        } catch (Exception e) {
            log.error("첨부파일 삭제 실패: attachmentId={}, 예외={}", attachmentId, e.getMessage());
            return Either.left(Failure.ofInternalServerError("첨부파일 삭제 실패: " + e.getMessage()));
        }
    }

    public boolean existsById(AttachmentId attachmentId) {
        return attachmentJpaRepository.existsById(attachmentId.getId());
    }

    @Override
    public int countByCardId(CardId cardId) {
        return attachmentJpaRepository.countByCardId(cardId.getId());
    }

    @Override
    public long sumFileSizeByCardId(CardId cardId) {
        return attachmentJpaRepository.sumFileSizeByCardId(cardId.getId());
    }

    @Override
    public Either<Failure, Void> deleteByCardId(CardId cardId) {
        log.debug("카드별 첨부파일 삭제 시작: cardId={}", cardId.getId());

        try {
            attachmentJpaRepository.deleteByCardId(cardId.getId());
            log.debug("카드별 첨부파일 삭제 완료: cardId={}", cardId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("카드별 첨부파일 삭제 실패: cardId={}, 예외={}", cardId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("카드별 첨부파일 삭제 실패: " + e.getMessage()));
        }
    }
}
