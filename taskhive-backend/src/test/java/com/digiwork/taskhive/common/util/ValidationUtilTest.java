package com.digiwork.taskhive.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilTest {

    @Test
    @DisplayName("isValidEmail: should correctly validate email formats")
    void isValidEmail() {
        assertThat(ValidationUtil.isValidEmail("test@example.com")).isTrue();
        assertThat(ValidationUtil.isValidEmail("user.name+tag@sub.domain.org")).isTrue();
    }

    @Test
    @DisplayName("isValidEmail: should return false for invalid formats")
    void isValidEmail_Invalid() {
        assertThat(ValidationUtil.isValidEmail("invalid-email")).isFalse();
        assertThat(StringUtil.isBlank(" ")).isTrue();
        assertThat(ValidationUtil.isValidEmail(null)).isFalse();
        assertThat(ValidationUtil.isValidEmail("")).isFalse();
    }

    @Test
    @DisplayName("isValidPassword: should correctly validate strong passwords")
    void isValidPassword() {
        // Password must be at least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special
        assertThat(ValidationUtil.isValidPassword("StrongP@ss123")).isTrue();
        assertThat(ValidationUtil.isValidPassword("S!e2nrtu")).isTrue();
    }

    @Test
    @DisplayName("isValidPassword: should return false for weak passwords")
    void isValidPassword_Weak() {
        assertThat(ValidationUtil.isValidPassword("weak")).isFalse();
        assertThat(ValidationUtil.isValidPassword("NoSpecial123")).isFalse();
        assertThat(ValidationUtil.isValidPassword("nonumber!")).isFalse();
        assertThat(ValidationUtil.isValidPassword("NO_LOWER_1!")).isFalse();
        assertThat(ValidationUtil.isValidPassword(null)).isFalse();
    }
}
