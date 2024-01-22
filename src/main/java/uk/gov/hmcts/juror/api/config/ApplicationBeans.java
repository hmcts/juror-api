package uk.gov.hmcts.juror.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Configuration
public class ApplicationBeans {

    @Bean
    @ConditionalOnProperty(
        prefix = "testing",
        name = {"fix-clock"},
        havingValue = "false",
        matchIfMissing = true
    )
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "testing",
        name = {"fix-clock"},
        havingValue = "true"
    )
    public Clock clockTest() {
        return Clock.fixed(
            Instant.now().truncatedTo(ChronoUnit.SECONDS),
            ZoneId.of("UCT")
        );
    }
}
