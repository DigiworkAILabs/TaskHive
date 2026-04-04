package com.digiwork.taskhive.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilTest {

    @Test
    @DisplayName("format: should correctly format to yyyy-MM-dd HH:mm:ss")
    void format() {
        LocalDateTime dt = LocalDateTime.of(2026, 3, 28, 12, 0, 0);
        assertThat(DateUtil.format(dt)).isEqualTo("2026-03-28 12:00:00");
    }

    @Test
    @DisplayName("format: should return null for null input")
    void format_Null() {
        assertThat(DateUtil.format(null)).isNull();
    }

    @Test
    @DisplayName("isExpired: should return true for past dates")
    void isExpired() {
        assertThat(DateUtil.isExpired(LocalDateTime.now().minusSeconds(1))).isTrue();
        assertThat(DateUtil.isExpired(LocalDateTime.now().minusDays(1))).isTrue();
    }

    @Test
    @DisplayName("isExpired: should return false for future and null dates")
    void isExpired_Future() {
        assertThat(DateUtil.isExpired(LocalDateTime.now().plusHours(1))).isFalse();
        assertThat(DateUtil.isExpired(null)).isFalse();
    }
}
