package com.premonition.logging.mask;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

public class MaskRule {
  private final Pattern pattern;
  private final int unmasked;

  MaskRule(String prefix, String suffix, String pattern, int unmasked) {
    this.pattern = parsed(prefix, suffix, pattern);
    this.unmasked = unmasked;
  }

  public static String repeat(String input, int times) {
    if (times <= 0) return "";
    else if (times % 2 == 0) return repeat(input + input, times / 2);
    else return input + repeat(input + input, times / 2);
  }

  private Pattern parsed(String prefix, String suffix, String pattern) {
    String parsedPrefix = nullOrBlank(prefix) ? "" : "(?<=" + prefix + ")(?:\\s*)";
    String parsedSuffix = nullOrBlank(suffix) ? "" : "(?:\\s*)(?=" + suffix + ")";
    return compile(parsedPrefix + validated(pattern) + parsedSuffix, DOTALL | MULTILINE);
  }

  private String validated(String pattern) {
    if (nullOrBlank(pattern)) {
      throw new IllegalArgumentException("Need a non-blank pattern value!");
    }
    return pattern.startsWith("(") ? pattern : "(" + pattern + ")";
  }

  private static boolean nullOrBlank(String input) {
    return input == null || "".equals(input.trim());
  }

  public String apply(String input) {
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
      String match = matcher.group(1);
      String mask = repeat("X", Math.min(match.length(), match.length() - unmasked));
      String replacement = mask + match.substring(mask.length());
      return input.replace(match, replacement);
    }
    return input;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Definition {
    private String prefix = "";
    private String suffix = "";
    private String pattern;
    private int unmasked = 0;

    public Definition(String pattern) {
      this("", "", pattern, 0);
    }

    public MaskRule rule() {
      return new MaskRule(prefix, suffix, pattern, unmasked);
    }
  }
}
