package com.boardly.application.port.in.validation.validators;

import com.boardly.application.port.in.traits.Identifiable;
import com.boardly.application.port.in.validation.CommonValidator;
import com.boardly.application.port.in.validation.Validator;
import com.boardly.shared.EntityId;

/**
 * EntityId 검증기
 * <p>
 * EntityId의 자체 validate 메서드를 활용하여 검증합니다.
 * notBlank -> EntityId 형식 검증 순서로 체이닝합니다.
 * 체이닝 중 하나라도 실패하면 즉시 반환합니다.
 * </p>
 */
public final class IdValidator {

    private IdValidator() {
        // 유틸리티 클래스
    }

    /**
     * Identifiable trait를 구현한 객체의 ID를 검증합니다.
     * 
     * @param <T> EntityId를 상속받은 ID 타입
     * @param <U> Identifiable<T>를 구현한 객체 타입
     * @return 검증기
     */
    public static <T extends EntityId, U extends Identifiable<T>> Validator<U> validate() {
        return target -> {
            T id = target.id();
            String idValue = id.getValue();

            return CommonValidator.notBlank("id")
                    .then(validateEntityIdFormat(id))
                    .validate(idValue)
                    .map(result -> target); // 원본 객체 반환
        };
    }

    /**
     * EntityId를 직접 검증합니다.
     * 
     * @param <T> EntityId를 상속받은 ID 타입
     * @return 검증기
     */
    public static <T extends EntityId> Validator<T> validateId() {
        return target -> {
            String idValue = target.getValue();

            return CommonValidator.notBlank("id")
                    .then(validateEntityIdFormat(target))
                    .validate(idValue)
                    .map(result -> target); // 원본 객체 반환
        };
    }

    /**
     * ID 문자열을 직접 검증합니다.
     * 
     * @param fieldName 필드명 (에러 메시지용)
     * @return 검증기
     */
    public static Validator<String> validateIdString(String fieldName) {
        return CommonValidator.notBlank(fieldName);
        // 문자열만으로는 EntityId 형식 검증이 불가능하므로 notBlank만 수행
    }

    /**
     * EntityId 형식을 검증합니다.
     * EntityId의 자체 validate 메서드를 활용합니다.
     * 
     * @param <T>      EntityId를 상속받은 ID 타입
     * @param entityId 검증할 EntityId 인스턴스
     * @return 검증기
     */
    private static <T extends EntityId> Validator<String> validateEntityIdFormat(T entityId) {
        return Validator.of(
                value -> {
                    if (value == null || value.trim().isEmpty()) {
                        return false;
                    }
                    try {
                        // EntityId의 validate 메서드를 사용하여 형식 검증
                        // 임시 인스턴스 생성을 통해 검증 수행
                        entityId.getClass().getDeclaredConstructor(String.class).newInstance(value);
                        return true;
                    } catch (Exception e) {
                        // 검증 실패 시 false 반환
                        return false;
                    }
                },
                "id",
                "common.validation.field.pattern");
    }
}
