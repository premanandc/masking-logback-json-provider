package com.premonition.logging.mask;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MaskRulesTest {

  @Test
  public void shouldValidateAllRules() {
    MaskRules rules = new MaskRules();
    rules.addRule(new MaskRule.Definition("Credit Card", "\\d{13,18}"));
    rules.addRule(new MaskRule.Definition("SSN", "\\d{3}-?\\d{3}-?\\d{4}"));

    String output = rules.apply("My credit card number is 4111111111111111 and my social security number is 123-123-1234");
    assertThat(output).isEqualTo("My credit card number is **************** and my social security number is ************");
  }
}