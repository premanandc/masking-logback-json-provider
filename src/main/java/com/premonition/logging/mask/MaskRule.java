package com.premonition.logging.mask;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

/**
 * Rule to mask sensitive information in logs.
 */
public class MaskRule {
  private static final String DEFAULT_MASK = "*";
  
  private final String name;
  private final Pattern pattern;
  private final int unmasked;
  private final Position position;

  /**
   * Class constructor
   * @param name  a friendly name for the rule.
   * @param prefix  a literal prefix preceding the actual search pattern.
   * @param suffix  a literal suffix following the actual search pattern.
   * @param pattern  a regular expression pattern to identify the personally identifiable information.
   * @param unmasked  the number of characters to leave unmasked.
   * @param position  the position of the mask
   */
  MaskRule(String name, String prefix, String suffix, String pattern, int unmasked, Position position) {
    this.name = parse(name);
    this.pattern = parse(prefix, suffix, pattern);
    this.unmasked = unmasked;
    this.position = position;
  }

  private String parse(String name) {
    if (nullOrBlank(name)) {
      throw new IllegalArgumentException("Name cannot be null blank!");
    }
    return name.trim();
  }

  private static String repeat(String input, int times) {
    if (times <= 0) return StringUtils.EMPTY;
    else if (times % 2 == 0) return repeat(input + input, times / 2);
    else return input + repeat(input + input, times / 2);
  }

  private static Pattern parse(String prefix, String suffix, String pattern) {
    String parsedPrefix = nullOrBlank(prefix) ? StringUtils.EMPTY : "(?<=" + prefix + ")(?:\\s*)";
    String parsedSuffix = nullOrBlank(suffix) ? StringUtils.EMPTY : "(?:\\s*)(?=" + suffix + ")";
    return compile(parsedPrefix + validated(pattern) + parsedSuffix, DOTALL | MULTILINE);
  }

  private static String validated(String pattern) {
    if (nullOrBlank(pattern)) {
      throw new IllegalArgumentException("Need a non-blank pattern value!");
    }
    return pattern.matches("\\(.*\\)") ? pattern : "(" + pattern + ")";
  }

  private static boolean nullOrBlank(String input) {
    return Objects.isNull(input) || StringUtils.isBlank(input) || StringUtils.isEmpty(input);
  }

  /**
   * Applies the masking rule to the input.
   * @param input the PII that needs to be masked.
   * @return the masked version of the input.
   */
  public String apply(String input) {
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
      String match = matcher.group(1);
      String mask = repeat(DEFAULT_MASK, Math.min(match.length(), match.length() - unmasked));
      String replacement = position.getReplacement(match, mask);
      return input.replace(match, replacement);
    }
    return input;
  }

  /**
   * Helper to create a new rule instance.
   * @see MaskRule
   * @see ch.qos.logback.classic.joran.JoranConfigurator
   */
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Definition {
    private String name;
    private String prefix = StringUtils.EMPTY;
    private String suffix = StringUtils.EMPTY;
    private String pattern;
    private int unmasked = 0;
    private Position position;

    public Definition(String name, String pattern) {
      this(name, StringUtils.EMPTY, StringUtils.EMPTY, pattern, 0, Position.BEGIN);
    }

    public MaskRule rule() {
      return new MaskRule(name, prefix, suffix, pattern, unmasked, position);
    }
  }

  public enum Position {
    BEGIN {
      @Override
      String getReplacement(String match, String mask) {
        return mask + match.substring(mask.length());
      }
    },
    END {
      @Override
      String getReplacement(String match, String mask) {
        return match.substring(0, match.length() - mask.length()) + mask;
      }
    };

    abstract String getReplacement(String match, String mask);
  }
}
