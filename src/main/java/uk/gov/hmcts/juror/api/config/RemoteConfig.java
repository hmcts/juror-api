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
        final List<ClientHttpRequestInterceptor> clientHttpRequestInterceptorList =
            List.of(new JwtAuthenticationInterceptor(jwtService, webConfig.getSecurity()));

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(
            webConfig.getScheme() + "://" + webConfig.getHost() + ":" + webConfig.getPort());
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);

        return new RestTemplateBuilder()
            .requestFactory(webConfig::getRequestFactory)
            .uriTemplateHandler(uriBuilderFactory)
            .additionalInterceptors(clientHttpRequestInterceptorList);
    }
}
