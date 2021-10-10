package com.premonition.logging.logback;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.OutputCaptureRule;

import static org.assertj.core.api.Assertions.assertThat;

public class MaskingMessageProviderTest {

  @Rule
  public OutputCaptureRule capture = new OutputCaptureRule();

  private static final Logger logger = LoggerFactory.getLogger(MaskingMessageProviderTest.class);
  @Test
  public void shouldMask() {
    logger.info("This is a test with credit card number {}", "4111111111111111");
    assertThat(capture.toString()).contains("************1111").doesNotContain("4111111111111111");
  }

  @Test
  public void shouldContainStackTrace() {
    logger.error("This is an error", new RuntimeException("Error!!"));
    DocumentContext out = JsonPath.parse(capture.toString());
    assertThat(out.read("$.message", String.class)).isEqualTo("This is an error");
    assertThat(out.read("$.stack_trace", String.class)).contains("RuntimeException");
  }
}