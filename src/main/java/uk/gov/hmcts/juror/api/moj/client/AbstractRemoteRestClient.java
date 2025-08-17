package uk.gov.hmcts.juror.api.moj.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractRemoteRestClient {

    protected final RestTemplate restTemplate;

    protected AbstractRemoteRestClient(RestTemplateBuilder restTemplateBuilder) {
        this(restTemplateBuilder, null);
    }

    protected AbstractRemoteRestClient(RestTemplateBuilder restTemplateBuilder, String baseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        if (baseUrl != null) {
            this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
        }
    }
}
