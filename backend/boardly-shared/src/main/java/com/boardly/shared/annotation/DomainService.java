package com.boardly.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 도메인 서비스 클래스를 표시하는 어노테이션
 * 
 * 이 어노테이션이 붙은 클래스는 도메인 계층의 서비스임을 나타냅니다.
 * 도메인 서비스는 엔티티나 값 객체에 속하지 않는 도메인 로직을 담당합니다.
 * 
 * @see com.boardly.shared.annotation.UseCase
 * @see com.boardly.shared.annotation.ApplicationService
 * @see com.boardly.shared.annotation.Adapter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainService {

    /**
     * 도메인 서비스의 설명 (선택사항)
     */
    String value() default "";

    /**
     * 도메인 서비스의 도메인 (예: User, Board, Workspace)
     */
    String domain() default "";

    /**
     * 도메인 서비스의 책임 영역 (예: Validation, Calculation, Policy)
     */
    String responsibility() default "";
}
