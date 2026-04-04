package com.digiwork.taskhive.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilTest {

    @Test
    @DisplayName("isBlank: should correctly evaluate null, empty, and whitespace strings")
    void isBlank() {
        assertThat(StringUtil.isBlank(null)).isTrue();
        assertThat(StringUtil.isBlank("")).isTrue();
        assertThat(StringUtil.isBlank("  ")).isTrue();
        assertThat(StringUtil.isBlank("text")).isFalse();
    }

    @Test
    @DisplayName("isNotBlank: should be the logical inverse of isBlank")
    void isNotBlank() {
        assertThat(StringUtil.isNotBlank("text")).isTrue();
        assertThat(StringUtil.isNotBlank("  ")).isFalse();
        assertThat(StringUtil.isNotBlank(null)).isFalse();
    }

    @Test
    @DisplayName("capitalize: should normalize the string to First-uppercase, rest-lowercase")
    void capitalize() {
        assertThat(StringUtil.capitalize("hello")).isEqualTo("Hello");
        assertThat(StringUtil.capitalize("HELLO")).isEqualTo("Hello");
        assertThat(StringUtil.capitalize("h")).isEqualTo("H");
    }

    @Test
    @DisplayName("capitalize: should handle null and empty strings gracefully")
    void capitalize_EdgeCases() {
        assertThat(StringUtil.capitalize(null)).isNull();
        assertThat(StringUtil.capitalize("")).isEmpty();
        assertThat(StringUtil.capitalize("   ")).isEqualTo("   ");
    }
}
