package com.boardly.application.port.out;

import java.util.Locale;

/**
 * 메시지 국제화 및 중첩 메시지 키 해석을 담당하는 아웃바운드 포트
 * 
 * 이 포트는 외부 메시지 시스템(Spring MessageSource 등)에 대한 의존성을 추상화합니다.
 */
public interface MessageResolver {

    /**
     * 메시지 키와 인자들을 사용하여 국제화된 메시지를 반환합니다.
     * 인자에 포함된 중첩 메시지 키들도 자동으로 해석합니다.
     * 
     * @param messageKey 메시지 키
     * @param args       메시지 인자들 (중첩 메시지 키 포함 가능)
     * @return 국제화된 메시지
     */
    String resolveMessage(String messageKey, Object... args);

    /**
     * 특정 로케일을 사용하여 메시지를 해석합니다.
     * 
     * @param messageKey 메시지 키
     * @param locale     로케일
     * @param args       메시지 인자들
     * @return 국제화된 메시지
     */
    String resolveMessage(String messageKey, Locale locale, Object... args);

    /**
     * 현재 요청 컨텍스트의 로케일을 반환합니다.
     * 
     * @return 현재 로케일
     */
    Locale getCurrentLocale();
}
