package com.premonition.logging.mask;

import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MaskRuleTest {

  static Stream<Arguments> provideDataForTest() {
    return Stream.of(
            Arguments.of(0, "<test>", "(\\S+)", "</test>", "<other>\n  <test>\nhello\n</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n*****\n</test>\n  <more>bye</more>\n</other>"),
            Arguments.of(0, "<test>", "(\\S+)", "</test>", "<other>\n  <test>hello</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>*****</test>\n  <more>bye</more>\n</other>"),
            Arguments.of(0, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\n*****</test>\n  <more>bye</more>\n</other>"),
            Arguments.of(2, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\n***lo</test>\n  <more>bye</more>\n</other>"),
            Arguments.of(5, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>"),
            Arguments.of(5, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello123</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\n***lo123</test>\n  <more>bye</more>\n</other>"),
            Arguments.of(0, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello123</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\n********</test>\n  <more>bye</more>\n</other>"),
            Arguments.of(0, "", "\\d{3}-?\\d{3}-?\\d{4}", "", "123-123-1234", "************"),
            Arguments.of(4, "", "\\d{3}-?\\d{3}-?\\d{4}", "", "1231231234", "******1234"),
            Arguments.of(4, "", "\\d{3}-?\\d{3}-?\\d{4}", "", "123-123-1234", "********1234"),
            Arguments.of(12, "", "\\d{3}-?\\d{3}-?\\d{4}", "", "123-123-1234", "123-123-1234"),
            Arguments.of(4, "", "\\d{13,18}", "", "4111111111111111", "************1111")
    );
  }

  @Nested
  class WithInvalidMasks {
    @ParameterizedTest(name = "[{index}] should not create with invalid pattern: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t   \t", "\t   \t\n\n"})
    void shouldNotCreateWithAnInvalidPattern(String invalid) {
      MaskRule.Definition definition = new MaskRule.Definition("Test", invalid);
      assertThatExceptionOfType(IllegalArgumentException.class)
              .isThrownBy(definition::rule);
    }

    @ParameterizedTest(name = "[{index}] should not create with an invalid name: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t   \t", "\t   \t\n\n"})
    void shouldNotCreateWithAnInvalidName(String invalid) {
      MaskRule.Definition definition = new MaskRule.Definition(invalid, "\\d{13,18}");
      assertThatExceptionOfType(IllegalArgumentException.class)
              .isThrownBy(definition::rule);
    }
  }

  @ParameterizedTest(name = "[{index}] should mask \"{4}\" to \"{5}\"")
  @MethodSource("provideDataForTest")
  void shouldMask(int unmasked, String prefix, String pattern, String suffix, String input, String output) {
    MaskRule rule = new MaskRule.Definition("Test", prefix, suffix, pattern, unmasked, MaskRule.Position.BEGIN).rule();
    assertThat(rule.apply(input)).isEqualTo(output);
  }

  @ParameterizedTest(name = "[{index}] should position mask to the {0}")
  @CsvSource(value = {
          "BEGIN,********1234",
          "END,123-********"
  })
  void shouldPositionMaks(MaskRule.Position position, String output) {
    MaskRule rule = new MaskRule.Definition("Test", StringUtils.EMPTY, StringUtils.EMPTY, "\\d{3}-?\\d{3}-?\\d{4}", 4, position).rule();
    assertThat(rule.apply("123-123-1234")).isEqualTo(output);
  }
}
