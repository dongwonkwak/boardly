package com.boardly.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 어댑터 클래스를 표시하는 어노테이션
 * 
 * 이 어노테이션이 붙은 클래스는 포트와 어댑터 패턴의 어댑터 구현체임을 나타냅니다.
 * Infrastructure 계층에서 포트 인터페이스를 구현하는 클래스에 사용됩니다.
 * 
 * @see com.boardly.shared.annotation.UseCase
 * @see com.boardly.shared.annotation.ApplicationService
 * @see com.boardly.shared.annotation.DomainService
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Adapter {

    /**
     * 어댑터의 설명 (선택사항)
     */
    String value() default "";

    /**
     * 어댑터의 타입 (예: Inbound, Outbound)
     */
    AdapterType type() default AdapterType.OUTBOUND;

    /**
     * 어댑터의 종류 (예: Persistence, Messaging, External)
     */
    String category() default "";

    /**
     * 어댑터 타입 열거형
     */
    enum AdapterType {
        /**
         * 인바운드 어댑터 (예: REST Controller, GraphQL Resolver)
         */
        INBOUND,

        /**
         * 아웃바운드 어댑터 (예: Repository, External Service)
         */
        OUTBOUND
    }
}
