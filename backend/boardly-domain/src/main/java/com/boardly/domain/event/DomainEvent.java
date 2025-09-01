package com.boardly.domain.event;

import lombok.Getter;

import java.time.Instant;

/**
 * 도메인 이벤트 기본 클래스
 */
@Getter
public abstract class DomainEvent {

    private final Instant occurredAt;

    protected DomainEvent() {
        this.occurredAt = Instant.now();
    }

    protected DomainEvent(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
