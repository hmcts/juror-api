package uk.gov.hmcts.juror.api.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public static final String DIGITAL_BY_DEFAULT_FEATURE_FLAG = "digital-by-default";

    private Map<String, Boolean> flags = new ConcurrentHashMap<>();

    public boolean isEnabled(String featureName) {
        return Boolean.TRUE.equals(flags.get(featureName));
    }
}
