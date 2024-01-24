package uk.gov.hmcts.juror.api.config;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Data
public class WebConfig {
    @NotBlank
    private String scheme;
    @NotBlank
    private String host;
    @NotBlank
    private String port;
    @NotBlank
    private String url;
    @NotNull
    @NestedConfigurationProperty
    private JwtSecurityConfig security;

    @Min(0)
    private Integer maxRetries;
    @Min(0)
    private long retryDelay;

    @NestedConfigurationProperty
    private Proxy proxy;

    public String getUri() {
        return this.scheme + "://" + this.host + ":" + this.port + this.url;
    }

    @Data
    public static class Proxy {
        private String schema;
        @NotBlank
        private String host;
        @NotNull
        private Integer port;
    }

    public ClientHttpRequestFactory getRequestFactory() {
        return getRequestFactory(this);
    }

    public static ClientHttpRequestFactory getRequestFactory(
        WebConfig webConfig
    ) {
        final RequestConfig config =
            RequestConfig.custom()
                .build();


        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
            .setDefaultRequestConfig(config);

        if (webConfig.getProxy() != null) {
            Proxy proxy = webConfig.getProxy();
            httpClientBuilder.setProxy(
                new HttpHost(proxy.getSchema(),
                    proxy.getHost(),
                    proxy.getPort())
            );
        }

        if (webConfig.getMaxRetries() != null) {
            httpClientBuilder.setRetryStrategy(
                new DefaultHttpRequestRetryStrategy(webConfig.getMaxRetries(),
                    TimeValue.ofMicroseconds(webConfig.getRetryDelay()))
            );
        }
        return new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
    }
}
