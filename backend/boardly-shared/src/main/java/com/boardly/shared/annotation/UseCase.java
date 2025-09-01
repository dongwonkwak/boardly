package com.boardly.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * UseCase 구현체를 표시하는 어노테이션
 * 
 * 이 어노테이션이 붙은 클래스는 인바운드 포트(UseCase 인터페이스)의 구현체임을 나타냅니다.
 * Application 계층에서 비즈니스 로직을 구현하는 서비스 클래스에 사용됩니다.
 * 
 * @see com.boardly.shared.annotation.ApplicationService
 * @see com.boardly.shared.annotation.DomainService
 * @see com.boardly.shared.annotation.Adapter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseCase {

    /**
     * UseCase의 설명 (선택사항)
     */
    String value() default "";

    /**
     * UseCase의 카테고리 (예: CRUD, Analytics, Workflow, Search)
     */
    String category() default "";
}
