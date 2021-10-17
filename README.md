# Log Masking for JSON formatted events

Provides an extension to [Logstash JSON Encoder](https://github.com/logstash/logstash-logback-encoder) to mask sensitive contents of log events using rules like below:

```$xml
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
Also see

* [Logstash JSON Encoder](https://github.com/logstash/logstash-logback-encoder)
* [Logback](https://logback.qos.ch)
