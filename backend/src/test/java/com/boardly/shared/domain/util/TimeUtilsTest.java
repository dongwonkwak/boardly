package com.boardly.shared.domain.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class TimeUtilsTest {

    @BeforeEach
    void setUp() {
        // 기본적으로 한국어 로케일 설정
        LocaleContextHolder.setLocale(Locale.KOREAN);
    }

    @Test
    void getRelativeTime_null입력_알수없음_반환() {
        // given
        Instant nullTimestamp = null;

        // when
        String result = TimeUtils.getRelativeTime(nullTimestamp);

        // then
        assertThat(result).isEqualTo("알 수 없음");
    }

    @Test
    void getRelativeTime_현재시간_방금전_반환() {
        // given
        Instant now = Instant.now();

        // when
        String result = TimeUtils.getRelativeTime(now);

        // then
        assertThat(result).contains("방금");
    }

    @Test
    void getRelativeTime_1분전_1분전_반환() {
        // given
        Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);

        // when
        String result = TimeUtils.getRelativeTime(oneMinuteAgo);

        // then
        assertThat(result).contains("1분 전");
    }

    @Test
    void getRelativeTime_5분전_5분전_반환() {
        // given
        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);

        // when
        String result = TimeUtils.getRelativeTime(fiveMinutesAgo);

        // then
        assertThat(result).contains("5분 전");
    }

    @Test
    void getRelativeTime_1시간전_1시간전_반환() {
        // given
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        // when
        String result = TimeUtils.getRelativeTime(oneHourAgo);

        // then
        assertThat(result).contains("1시간 전");
    }

    @Test
    void getRelativeTime_3시간전_3시간전_반환() {
        // given
        Instant threeHoursAgo = Instant.now().minus(3, ChronoUnit.HOURS);

        // when
        String result = TimeUtils.getRelativeTime(threeHoursAgo);

        // then
        assertThat(result).contains("3시간 전");
    }

    @Test
    void getRelativeTime_1일전_1일전_반환() {
        // given
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

        // when
        String result = TimeUtils.getRelativeTime(oneDayAgo);

        // then
        assertThat(result).contains("1일 전");
    }

    @Test
    void getRelativeTime_1주일전_1주일전_반환() {
        // given
        Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        // when
        String result = TimeUtils.getRelativeTime(oneWeekAgo);

        // then
        assertThat(result).contains("1주 전");
    }

    @Test
    void getRelativeTime_1개월전_1개월전_반환() {
        // given
        Instant oneMonthAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        // when
        String result = TimeUtils.getRelativeTime(oneMonthAgo);

        // then
        assertThat(result).contains("1개월 전");
    }

    @Test
    void getRelativeTime_1년전_1년전_반환() {
        // given
        Instant oneYearAgo = Instant.now().minus(365, ChronoUnit.DAYS);

        // when
        String result = TimeUtils.getRelativeTime(oneYearAgo);

        // then
        assertThat(result).contains("12개월 전");
    }

    @Test
    void getRelativeTime_영어로케일_영어반환() {
        // given
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        // when
        String result = TimeUtils.getRelativeTime(oneHourAgo);

        // then
        assertThat(result).contains("1 hour ago");
    }

    @Test
    void getRelativeTime_일본어로케일_일본어반환() {
        // given
        LocaleContextHolder.setLocale(Locale.JAPANESE);
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        // when
        String result = TimeUtils.getRelativeTime(oneHourAgo);

        // then
        assertThat(result).contains("1時間前");
    }

    @Test
    void getRelativeTime_미래시간_앞으로_반환() {
        // given
        Instant futureTime = Instant.now().plus(1, ChronoUnit.HOURS);

        // when
        String result = TimeUtils.getRelativeTime(futureTime);

        // then
        assertThat(result).contains("60분 후");
    }

    @Test
    void getRelativeTime_과거시간_전_반환() {
        // given
        Instant pastTime = Instant.now().minus(1, ChronoUnit.HOURS);

        // when
        String result = TimeUtils.getRelativeTime(pastTime);

        // then
        assertThat(result).contains("전");
    }
}