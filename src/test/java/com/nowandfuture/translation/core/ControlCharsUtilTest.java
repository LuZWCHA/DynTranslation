package com.nowandfuture.translation.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ControlCharsUtilTest {

    @Test
    void removeControlChars() {
        String testString = "{x=2, y=1}你好";
        String controlChars = ControlCharsUtil.removeControlChars(testString);
        assertThat(controlChars).isEqualTo("你好");

        testString = "{x=2, y=1你好";
        controlChars = ControlCharsUtil.removeControlChars(testString);
        assertThat(controlChars).isEqualTo("{x=2, y=1你好");

        testString = "{}{x=2, y=1}你好";
        controlChars = ControlCharsUtil.removeControlChars(testString);
        assertThat(controlChars).isEqualTo("{x=2, y=1}你好");

        testString = "{{x=2, y=1}你好";
        controlChars = ControlCharsUtil.removeControlChars(testString);
        assertThat(controlChars).isEqualTo("你好");
    }

    @Test
    void getControlChars() {

        String testString = "{x=2, y=1}你好";
        ControlChars controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(false);
        assertThat(controlChars)
                .extracting(ControlChars::getOffsetY)
                .as("check double")
                .isEqualTo(1);
        assertThat(controlChars)
                .extracting(ControlChars::getOffsetX)
                .as("check double")
                .isEqualTo(2);

        testString = "{x=0.4, y=1}你好";
        controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(true);

        testString = "{x=2,        y=1}你好";
        controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(false);

        testString = "{x=2,     ,   y=1}你好";
        controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(true);

        testString = "{y=1, x=2}你好";
        controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(false);
        assertThat(controlChars).extracting(ControlChars::getOffsetX).isEqualTo(2);

        testString = "{y=1, x=2, scale=2.}你好";
        controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(false);
        assertThat(controlChars).extracting(ControlChars::getScale).isEqualTo(2.0f);

        testString = "{y=1, x=2, scale=2.2}你好";
        controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(false);
        assertThat(controlChars).extracting(ControlChars::getScale).isEqualTo(2.2f);

        testString = "{y=1, x=2, scale=.2}你好";
        controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(false);
        assertThat(controlChars).extracting(ControlChars::getScale).isEqualTo(0.2f);

        testString = "{y=1, x=2, scale=2}你好";
        controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(false);
        assertThat(controlChars).extracting(ControlChars::getScale).isEqualTo(2f);

        testString = "{y=1, x=2, scale=2, scale=3}你好";
        controlChars = ControlCharsUtil.getControlChars(testString);
        assertThat(controlChars).extracting(ControlChars::isEMPTY).isEqualTo(false);
        assertThat(controlChars).extracting(ControlChars::getScale).isEqualTo(3f);
    }
}