package com.boardly.features.attachment.domain.policy;

/**
 * 첨부파일 정책 설정 인터페이스
 * 
 * <p>
 * 도메인 레이어에서 정책 설정값을 읽기 위한 인터페이스입니다.
 * Infrastructure 레이어의 구현체로부터 설정값을 받아옵니다.
 */
public interface AttachmentPolicyConfig {
    /**
     * 최대 파일 크기를 MB 단위로 반환합니다.
     * 설정되지 않은 경우 기본값을 반환합니다.
     */
    int getMaxFileSizeMB();

    /**
     * 카드당 최대 첨부파일 개수를 반환합니다.
     * 설정되지 않은 경우 기본값을 반환합니다.
     */
    int getMaxAttachmentsPerCard();

    /**
     * 파일명 최대 길이를 반환합니다.
     * 설정되지 않은 경우 기본값을 반환합니다.
     */
    int getMaxFileNameLength();

    /**
     * 기본값들을 반환하는 상수 클래스
     */
    final class Defaults {
        public static final int MAX_FILE_SIZE_MB = 10;
        public static final int MAX_ATTACHMENTS_PER_CARD = 10;
        public static final int MAX_FILE_NAME_LENGTH = 255;

        private Defaults() {
            // 상수 클래스
        }
    }
}