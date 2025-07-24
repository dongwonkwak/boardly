package com.boardly.shared.domain.util;

import java.time.Instant;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.context.i18n.LocaleContextHolder;

public final class TimeUtils {

    /**
     * 주어진 Instant 객체와 현재 시간의 차이를 자연스러운 문자열로 반환합니다.
     * (예: "방금 전", "5분 전")
     *
     * @param pastTime 비교할 과거의 시간 (Instant)
     * @return 시간 차이를 나타내는 문자열
     */
    public static String getRelativeTime(Instant pastTime) {
        if (pastTime == null) {
            return "알 수 없음";
        }

        var prettyTime = new PrettyTime(LocaleContextHolder.getLocale());
        // format 메서드를 사용하여 시간 차이 계산 및 문자열 변환
        return prettyTime.format(pastTime);
    }
}
