package uk.gov.hmcts.juror.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientApi;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
@Slf4j
public class NotifyConfig {
    @Bean
    public NotificationClientApi notifyClient(final NotifyConfigurationProperties props) {
        final NotifyConfigurationProperties.Proxy proxyProps = props.getProxy();
        if (proxyProps != null && proxyProps.isEnabled()) {
            log.info("Configuring connection proxy!");
            final InetSocketAddress socketAddress = new InetSocketAddress(
                proxyProps.getHost(),
                Integer.valueOf(proxyProps.getPort())
            );
            final Proxy proxy = new Proxy(proxyProps.getType(), socketAddress);
            return new NotificationClient(props.getKey(), proxy);
        } else {
            log.warn("Skipped Notify client proxy configuration!");
            return new NotificationClient(props.getKey());
        }
    }
}
