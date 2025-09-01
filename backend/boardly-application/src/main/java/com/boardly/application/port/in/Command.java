package com.boardly.application.port.in;

/**
 * 모든 Command의 최상위 인터페이스
 * <p>
 * 이 인터페이스는 마커 인터페이스로, 모든 도메인의 Command들이 구현해야 합니다.
 * 필요에 따라 공통 메서드를 추가할 수 있습니다.
 * </p>
 */
public interface Command {
    // 현재는 마커 인터페이스
    // 미래에 공통 메서드 추가 가능 (예: 타임스탬프, 추적 정보 등)
}
