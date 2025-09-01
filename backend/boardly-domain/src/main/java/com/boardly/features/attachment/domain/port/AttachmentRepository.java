package com.boardly.features.attachment.domain.port;

import com.boardly.features.attachment.domain.Attachment;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.AttachmentId;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;
import io.vavr.control.Either;
import java.util.List;
import java.util.Optional;

public interface AttachmentRepository {
    /**
     * 첨부파일을 저장합니다.
     */
    Either<Failure, Attachment> save(Attachment attachment);

    /**
     * 첨부파일 ID로 첨부파일을 조회합니다.
     */
    Optional<Attachment> findById(AttachmentId attachmentId);

    /**
     * 카드별 첨부파일 조회 (업로드일시 순)
     */
    List<Attachment> findByCardIdOrderByCreatedAt(CardId cardId);

    /**
     * 업로더별 첨부파일 조회
     */
    List<Attachment> findByUploaderIdOrderByCreatedAtDesc(UserId uploaderId);

    /**
     * 첨부파일을 삭제합니다.
     */
    Either<Failure, Void> delete(AttachmentId attachmentId);

    /**
     * 첨부파일이 존재하는지 확인합니다.
     */
    boolean existsById(AttachmentId attachmentId);

    /**
     * 카드별 첨부파일 수 조회
     */
    int countByCardId(CardId cardId);

    /**
     * 카드별 총 파일 크기 조회
     */
    long sumFileSizeByCardId(CardId cardId);

    /**
     * 카드 삭제 시 관련 첨부파일 모두 삭제
     */
    Either<Failure, Void> deleteByCardId(CardId cardId);
}
