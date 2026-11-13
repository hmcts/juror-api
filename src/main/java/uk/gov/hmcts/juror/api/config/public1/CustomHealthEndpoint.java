package uk.gov.hmcts.juror.api.config.public1;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.boot.health.registry.HealthContributorRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j
@Component
@Endpoint(id = "health")
public class CustomHealthEndpoint extends HealthEndpoint {

    public CustomHealthEndpoint(HealthContributorRegistry registry, HealthEndpointGroups groups) {
        super(registry, null, groups, Duration.of(30, SECONDS));  // Todo confirm this, using 30 seconds for now
    }

    @Override
    @ReadOperation
    public HealthDescriptor health() {
        HealthDescriptor health = super.health();
        log.debug("Health status details: {}", health);
        log.info("Health status details: {}", health);
        return health;
    }
}
