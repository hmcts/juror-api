package uk.gov.hmcts.juror.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfig {

}
