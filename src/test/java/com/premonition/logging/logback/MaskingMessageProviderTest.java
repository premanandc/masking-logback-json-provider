package com.premonition.logging.logback;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class MaskingMessageProviderTest {

  private static final Logger logger = LoggerFactory.getLogger(MaskingMessageProviderTest.class);

  @Test
  public void shouldMask(CapturedOutput output) {
    logger.info("This is a test with credit card number {}", "4111111111111111");
    assertThat(output.toString()).contains("************1111").doesNotContain("4111111111111111");
  }

  @Test
  public void shouldContainStackTrace(CapturedOutput output) {
    logger.error("This is an error", new RuntimeException("Error!!"));
    DocumentContext out = JsonPath.parse(output.toString());
    assertThat(out.read("$.message", String.class)).isEqualTo("This is an error");
    assertThat(out.read("$.stack_trace", String.class)).contains("RuntimeException");
  }
}