package com.boardly.features.attachment.infrastructure.config;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.boardly.features.attachment.domain.policy.AttachmentPolicyConfig;

/**
 * 첨부파일 정책 설정 구현체
 * 
 * <p>
 * Infrastructure 레이어에서 도메인의 AttachmentPolicyConfig 인터페이스를 구현합니다.
 * 설정값이 없거나 유효하지 않은 경우 기본값을 반환합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentPolicyConfigImpl implements AttachmentPolicyConfig {

    private final AttachmentPolicyProperties properties;

    @Override
    public int getMaxFileSizeMB() {
        int configValue = properties.getMaxFileSizeMB();

        if (configValue <= 0) {
            log.debug("최대 파일 크기 설정값이 유효하지 않음: {}. 기본값 {}MB 사용",
                    configValue, Defaults.MAX_FILE_SIZE_MB);
            return Defaults.MAX_FILE_SIZE_MB;
        }

        return configValue;
    }

    @Override
    public int getMaxAttachmentsPerCard() {
        int configValue = properties.getMaxAttachmentsPerCard();

        if (configValue <= 0) {
            log.debug("카드당 최대 첨부파일 개수 설정값이 유효하지 않음: {}. 기본값 {}개 사용",
                    configValue, Defaults.MAX_ATTACHMENTS_PER_CARD);
            return Defaults.MAX_ATTACHMENTS_PER_CARD;
        }

        return configValue;
    }

    @Override
    public int getMaxFileNameLength() {
        int configValue = properties.getMaxFileNameLength();

        if (configValue <= 0) {
            log.debug("파일명 최대 길이 설정값이 유효하지 않음: {}. 기본값 {}자 사용",
                    configValue, Defaults.MAX_FILE_NAME_LENGTH);
            return Defaults.MAX_FILE_NAME_LENGTH;
        }

        return configValue;
    }
}