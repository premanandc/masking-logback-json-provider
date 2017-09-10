package com.premonition.logging.mask;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class MaskRuleTest {

  @RunWith(Parameterized.class)
  public static class InvalidMasks {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String invalidPattern;

    public InvalidMasks(String invalidPattern) {
      this.invalidPattern = invalidPattern;
    }

    @Parameters
    public static Object[][] data() {
      return new Object[][] {
          {null},
          {""},
          {"   "},
          {"\t   \t"},
          {"\t   \t\n\n"},
      };
    }
    @Test
    public void shouldNotMask() throws Exception {
      thrown.expect(IllegalArgumentException.class);
      new MaskRule.Definition(invalidPattern).rule();
    }
  }

  @RunWith(Parameterized.class)
  public static class ValidMasks {
    private final String pattern;
    private final String input;
    private final String output;
    private final int unmasked;
    private final String prefix;
    private final String suffix;

    public ValidMasks(int unmasked, String prefix, String pattern, String suffix, String input, String output) {
      this.unmasked = unmasked;
      this.prefix = prefix;
      this.pattern = pattern;
      this.suffix = suffix;
      this.input = input;
      this.output = output;
    }

    @Parameters(name = "Pattern {2} - leave {0} unmasked character(s)")
    public static Object[][] data() {
      return new Object[][]{
          // @formatter:off
          {0, "<test>", "(\\S+)", "</test>", "<other>\n  <test>\nhello\n</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\nXXXXX\n</test>\n  <more>bye</more>\n</other>"},
          {0, "<test>", "(\\S+)", "</test>", "<other>\n  <test>hello</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>XXXXX</test>\n  <more>bye</more>\n</other>"},
          {0, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\nXXXXX</test>\n  <more>bye</more>\n</other>"},
          {2, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\nXXXlo</test>\n  <more>bye</more>\n</other>"},
          {5, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>"},
          {5, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello123</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\nXXXlo123</test>\n  <more>bye</more>\n</other>"},
          {0, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello123</test>\n  <more>bye</more>\n</other>", "<other>\n  <test>\n\nXXXXXXXX</test>\n  <more>bye</more>\n</other>"},
          {0, "", "\\d{3}-?\\d{3}-?\\d{4}", "", "123-123-1234", "XXXXXXXXXXXX"},
          {4, "", "\\d{3}-?\\d{3}-?\\d{4}", "", "1231231234", "XXXXXX1234"},
          {4, "", "\\d{3}-?\\d{3}-?\\d{4}", "", "123-123-1234", "XXXXXXXX1234"},
          {12, "", "\\d{3}-?\\d{3}-?\\d{4}", "", "123-123-1234", "123-123-1234"},
          {4, "", "\\d{13,18}", "", "4111111111111111", "XXXXXXXXXXXX1111"},
          // @formatter:on
      };
    }

    @Test
    public void shouldMask() throws Exception {
      MaskRule rule = new MaskRule.Definition(prefix, suffix, pattern, unmasked).rule();
      assertThat(rule.apply(input)).isEqualTo(output);
    }

  }
}
