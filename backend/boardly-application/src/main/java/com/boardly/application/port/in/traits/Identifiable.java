package com.boardly.application.port.in.traits;

import com.boardly.shared.EntityId;

/**
 * ID를 가지는 명령을 나타내는 제네릭 인터페이스
 * 
 * @param <T> EntityId를 상속받은 ID 타입
 */
public interface Identifiable<T extends EntityId> {
    T id();
}
