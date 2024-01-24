package uk.gov.hmcts.juror.api.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.Proxy.Type;

/**
 * Notify client configuration options.
 */
@Component
@ConfigurationProperties(prefix = "notify")
@Validated
@Getter
@Setter
@ToString(exclude = "key")
public class NotifyConfigurationProperties {
    private boolean disabled = false;
    private String key;
    private Proxy proxy;

    /**
     * Notify client proxy configuration options.
     */
    @Getter
    @Setter
    @ToString
    @ConfigurationProperties(prefix = "notify.proxy")
    public static class Proxy {
        private boolean enabled = false;
        private String host;
        private String port;
        private Type type;
    }
}