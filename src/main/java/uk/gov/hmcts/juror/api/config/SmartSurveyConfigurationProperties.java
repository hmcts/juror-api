package uk.gov.hmcts.juror.api.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.Proxy.Type;


/**
 * Smart Survey configuration options.
 */
@Component
@ConfigurationProperties(prefix = "smartsurvey")
@Validated
@Getter
@Setter
@ToString
public class SmartSurveyConfigurationProperties {
    private Boolean enabled;
    private String exportsUrl;
    private String token;
    private String secret;
    private Proxy proxy;

    /**
     * Proxy configuration options.
     */
    @Getter
    @Setter
    @ToString
    @ConfigurationProperties(prefix = "smartsurvey.proxy")
    public static class Proxy {
        private Boolean enabled = false;
        private String host;
        private String port;
        private Type type;
    }
}
