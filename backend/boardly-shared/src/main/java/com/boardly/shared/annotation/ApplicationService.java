package com.boardly.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 애플리케이션 서비스 클래스를 표시하는 어노테이션
 * 
 * 이 어노테이션이 붙은 클래스는 애플리케이션 계층의 서비스임을 나타냅니다.
 * 애플리케이션 서비스는 여러 UseCase를 조합하여 복잡한 비즈니스 워크플로우를 처리합니다.
 * 
 * @see com.boardly.shared.annotation.UseCase
 * @see com.boardly.shared.annotation.DomainService
 * @see com.boardly.shared.annotation.Adapter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationService {

    /**
     * 애플리케이션 서비스의 설명 (선택사항)
     */
    String value() default "";

    /**
     * 애플리케이션 서비스의 도메인 (예: User, Board, Workspace)
     */
    String domain() default "";

    /**
     * 애플리케이션 서비스의 역할 (예: Orchestration, Coordination, Facade)
     */
    String role() default "";
}
