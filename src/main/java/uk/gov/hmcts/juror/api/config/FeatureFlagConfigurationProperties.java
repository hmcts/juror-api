package uk.gov.hmcts.juror.api.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * Application feature flags.
 */
@Component
@ConfigurationProperties(prefix = "feature-flags")
@Validated
@Getter
@Setter
@ToString
public class FeatureFlagConfigurationProperties {
    private Map<String, Boolean> flags = new HashMap<>();

    public boolean isEnabled(String featureName) {
        return Boolean.TRUE.equals(flags.get(featureName));
    }
}
