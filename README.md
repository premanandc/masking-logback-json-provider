# Log Masking for JSON formatted events
[![Java CI](https://github.com/joabetc/masking-logback-json-provider/actions/workflows/master.yml/badge.svg)](https://github.com/joabetc/masking-logback-json-provider/actions/workflows/main.yml)

Provides an extension to [Logstash JSON Encoder](https://github.com/logstash/logstash-logback-encoder) to mask sensitive contents of log events using rules like below:

```xml
<encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
  <providers>
    <timestamp>
      <timeZone>UTC</timeZone>
    </timestamp>
    <provider class="com.premonition.logging.logback.MaskingMessageProvider">
      <rules>
        <rule>
          <name>credit card</name>
          <pattern>\d{13,18}</pattern>
          <unmasked>4</unmasked>
          <position>END</position>
        </rule>
        <rule>
          <name>SSN</name>
          <pattern>\d{3}-?\d{3}-?\d{4}</pattern>
        </rule>
      </rules>
    </provider>
    <stackTrace/>
    <pattern>
      <pattern>
        {
        "severity": "%level",
        "thread": "%thread",
        "class": "%logger{40}"
        }
      </pattern>
    </pattern>
  </providers>
</encoder>
```

## Rule tags

| tag | description |
| --- | --- |
|`name`| an optional friendly name for the rule |
| `prefix` | an optional literal prefix preceding the actual search pattern |
| `suffix` | an optional literal suffix following the actual search pattern |
| `pattern` | a regular expression pattern to identify the personally identifiable information |
| `unmasked` | the number of characters to leave unmasked |
| `position` | the position of the mask |

## Also see

* [Logstash JSON Encoder](https://github.com/logstash/logstash-logback-encoder)
* [Logback](https://logback.qos.ch)
