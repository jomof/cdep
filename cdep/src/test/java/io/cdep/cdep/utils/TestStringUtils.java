package io.cdep.cdep.utils;


import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TestStringUtils {
    private static class CoverConstructor extends StringUtils {

    }

    @Test
    public void coverConstructor() {
        // Call constructor of tested class to cover that code.
        new CoverConstructor();
    }

    @Test
    public void checkIsNumber() {
        assertThat(StringUtils.isNumeric("1")).isTrue();
    }

    @Test
    public void checkIsNotNumber() {
        assertThat(StringUtils.isNumeric("x")).isFalse();
    }
}
