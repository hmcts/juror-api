package uk.gov.hmcts.juror.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureFlagConfigurationPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
        .withUserConfiguration(FeatureFlagConfigurationProperties.class);

    @Test
    void shouldBindConfiguredFeatureFlags() {
        contextRunner
            .withPropertyValues(
                "feature-flags.flags.digital-by-default=true",
                "feature-flags.flags.disabled-feature=false"
            )
            .run(context -> {
                FeatureFlagConfigurationProperties featureFlags =
                    context.getBean(FeatureFlagConfigurationProperties.class);

                assertThat(featureFlags.isEnabled("digital-by-default")).isTrue();
                assertThat(featureFlags.isEnabled("disabled-feature")).isFalse();
            });
    }

    @Test
    void shouldDefaultUnknownFeatureFlagsToDisabled() {
        contextRunner.run(context -> {
            FeatureFlagConfigurationProperties featureFlags =
                context.getBean(FeatureFlagConfigurationProperties.class);

            assertThat(featureFlags.isEnabled("unknown-feature")).isFalse();
        });
    }
}
