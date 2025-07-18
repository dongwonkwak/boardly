package com.boardly.shared.domain.common;

import lombok.Getter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 모든 도메인 엔티티의 기본 클래스
 * 공통 속성과 기능을 제공
 * 모든 시간은 UTC 기준으로 저장됩니다.
 */
@Getter
public abstract class BaseEntity {

    /**
     * 엔티티 생성 일시 (UTC)
     */
    private Instant createdAt;

    /**
     * 엔티티 최종 수정 일시 (UTC)
     */
    private Instant updatedAt;

    /**
     * 엔티티 버전 (낙관적 락킹용)
     */
    private Long version;

    /**
     * BaseEntity 생성자
     *
     * @param createdAt 생성 일시 (UTC)
     * @param updatedAt 수정 일시 (UTC)
     */
    protected BaseEntity(Instant createdAt, Instant updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = 0L;
    }

    /**
     * 현재 시간으로 생성되는 BaseEntity 생성자 (UTC 기준)
     */
    protected BaseEntity() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.version = 0L;
    }

    /**
     * 엔티티가 수정되었음을 표시 (UTC 기준)
     * 자식 클래스에서 상태 변경 시 호출해야 함
     */
    protected void markAsUpdated() {
        this.updatedAt = Instant.now();
    }

    /**
     * 엔티티 생성 시점 설정 (테스트 또는 데이터 마이그레이션용)
     */
    protected void setCreatedAt(Instant createdAt) {
        if (createdAt != null) {
            this.createdAt = createdAt;
        }
    }

    /**
     * 엔티티 수정 시점 설정 (테스트 또는 데이터 마이그레이션용)
     */
    protected void setUpdatedAt(Instant updatedAt) {
        if (updatedAt != null) {
            this.updatedAt = updatedAt;
        }
    }

    /**
     * 버전 설정 (Infrastructure Layer에서 사용)
     */
    protected void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 엔티티가 새로 생성된 것인지 확인
     * (ID가 없거나 버전이 0인 경우)
     */
    public boolean isNew() {
        return version == null || version == 0L;
    }

    /**
     * 엔티티가 최근에 생성되었는지 확인 (지정된 시간 이내)
     *
     * @param minutes 분 단위
     * @return 최근 생성 여부
     */
    public boolean isCreatedWithin(long minutes) {
        return createdAt.isAfter(Instant.now().minus(minutes, ChronoUnit.MINUTES));
    }

    /**
     * 엔티티가 최근에 수정되었는지 확인 (지정된 시간 이내)
     *
     * @param minutes 분 단위
     * @return 최근 수정 여부
     */
    public boolean isUpdatedWithin(long minutes) {
        return updatedAt.isAfter(Instant.now().minus(minutes, ChronoUnit.MINUTES));
    }

    /**
     * 엔티티가 수정된 적이 있는지 확인
     * (생성 시간과 수정 시간이 다른 경우)
     */
    public boolean hasBeenModified() {
        return !createdAt.equals(updatedAt);
    }

    /**
     * 엔티티의 수명 계산 (생성부터 현재까지의 시간)
     *
     * @return 생성 후 경과 시간 (분 단위)
     */
    public long getAgeInMinutes() {
        return ChronoUnit.MINUTES.between(createdAt, Instant.now());
    }

    /**
     * 마지막 수정 후 경과 시간 계산
     *
     * @return 마지막 수정 후 경과 시간 (분 단위)
     */
    public long getMinutesSinceLastUpdate() {
        return ChronoUnit.MINUTES.between(updatedAt, Instant.now());
    }

    /**
     * 감사 정보를 포맷된 문자열로 반환
     */
    public String getAuditInfo() {
        return String.format("Created: %s, Updated: %s, Version: %d",
                createdAt.toString(),
                updatedAt.toString(),
                version);
    }

    /**
     * BaseEntity의 equals는 서브클래스에서 구현해야 함
     * 일반적으로 ID 기반으로 비교
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * BaseEntity의 hashCode는 서브클래스에서 구현해야 함
     * 일반적으로 ID 기반으로 계산
     */
    @Override
    public abstract int hashCode();

    /**
     * 기본 toString 구현
     * 서브클래스에서 오버라이드 권장
     */
    @Override
    public String toString() {
        return String.format("%s{createdAt=%s, updatedAt=%s, version=%d}",
                getClass().getSimpleName(),
                createdAt,
                updatedAt,
                version);
    }
}
