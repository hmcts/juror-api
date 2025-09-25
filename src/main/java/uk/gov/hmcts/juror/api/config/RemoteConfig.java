package uk.gov.hmcts.juror.api.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.util.DefaultUriBuilderFactory;
import uk.gov.hmcts.juror.api.moj.client.interceptor.JwtAuthenticationInterceptor;
import uk.gov.hmcts.juror.api.moj.service.JwtService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Configuration
@Slf4j
@ConfigurationProperties("uk.gov.hmcts.juror.remote")
@Data
public class RemoteConfig {

    @NestedConfigurationProperty
    private WebConfig schedulerService;

    @NestedConfigurationProperty
    private WebConfig pncCheckService;

    public RestTemplateBuilder schedulerServiceRestTemplateBuilder(
        final JwtService jwtService
    ) {
        return restTemplateBuilder(this.getSchedulerService(), jwtService);
    }

    public RestTemplateBuilder pncCheckServiceRestTemplateBuilder(
        final JwtService jwtService
    ) {
        return restTemplateBuilder(this.getPncCheckService(), jwtService);
    }

    private RestTemplateBuilder restTemplateBuilder(final WebConfig webConfig,
                                                    final JwtService jwtService) {

        final List<ClientHttpRequestInterceptor> interceptors =
            List.of(new JwtAuthenticationInterceptor(jwtService, webConfig.getSecurity()));

        String baseUrl;
        try {
            int port = Integer.parseInt(webConfig.getPort());
            URI uri = new URI(webConfig.getScheme(), null, webConfig.getHost(), port, null, null, null);
            baseUrl = uri.toString();
        } catch (URISyntaxException | NumberFormatException | NullPointerException e) {
            log.error("Invalid URI for RestTemplate: scheme={}, host={}, port={}",
                      webConfig.getScheme(), webConfig.getHost(), webConfig.getPort(), e);
            throw new IllegalStateException("Cannot build RestTemplate: invalid URI components", e);
        }

        log.info("Creating RestTemplate for base URL: {}", baseUrl);

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);

        return new RestTemplateBuilder()
            .requestFactory(webConfig::getRequestFactory)
            .uriTemplateHandler(uriBuilderFactory)
            .additionalInterceptors(interceptors);
    }
}
