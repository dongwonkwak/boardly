package com.boardly.features.attachment.infrastructure.config;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 첨부파일 정책 프로퍼티
 * 
 * <p>
 * application.yml에서 첨부파일 관련 정책 값들을 주입받는 설정 클래스입니다.
 * Infrastructure 레이어에 위치하여 외부 설정을 도메인으로 전달하는 역할을 합니다.
 */
@Slf4j
@Getter
@ConfigurationProperties(prefix = "boardly.attachment.policy")
public class AttachmentPolicyProperties {

    /**
     * 최대 파일 크기 (MB)
     */
    private int maxFileSizeMB = 0; // 기본값은 0으로 설정 (미설정 상태)

    /**
     * 카드당 최대 첨부파일 개수
     */
    private int maxAttachmentsPerCard = 0;

    /**
     * 파일명 최대 길이
     */
    private int maxFileNameLength = 0;

    /**
     * 설정값 로딩 완료 로깅
     */
    @PostConstruct
    public void logLoadedConfiguration() {
        log.info("Attachment policy properties loaded:");
        log.info("  - Max file size: {}MB", maxFileSizeMB > 0 ? maxFileSizeMB : "Not set (using default)");
        log.info("  - Max attachments per card: {}",
                maxAttachmentsPerCard > 0 ? maxAttachmentsPerCard : "Not set (using default)");
        log.info("  - Max file name length: {}", maxFileNameLength > 0 ? maxFileNameLength : "Not set (using default)");
    }

    // Setter 메서드들 (Spring Boot가 사용)
    public void setMaxFileSizeMB(int maxFileSizeMB) {
        this.maxFileSizeMB = maxFileSizeMB;
    }

    public void setMaxAttachmentsPerCard(int maxAttachmentsPerCard) {
        this.maxAttachmentsPerCard = maxAttachmentsPerCard;
    }

    public void setMaxFileNameLength(int maxFileNameLength) {
        this.maxFileNameLength = maxFileNameLength;
    }
}