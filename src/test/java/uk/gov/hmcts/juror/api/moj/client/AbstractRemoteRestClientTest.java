package uk.gov.hmcts.juror.api.moj.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AbstractRemoteRestClientTest {


    @MockBean
    private RestTemplateBuilder restTemplateBuilder;
    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void beforeEach() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
    }

    @Test
    void positiveRestTemplateConstructor() {
        AbstractRemoteRestClient client
            = new AbstractRemoteRestClientTestClass(restTemplateBuilder);

        assertEquals(restTemplate, client.restTemplate, "Rest templates do not match");
        verify(restTemplateBuilder, times(1)).build();
        verifyNoMoreInteractions(restTemplateBuilder);
        verifyNoInteractions(restTemplate);
    }


    @Test
    void positiveWithBaseUrlRestTemplateConstructorAndBaseUrl() {
        final String baseUrl = "www.baseurl.com/";
        AbstractRemoteRestClient client
            = new AbstractRemoteRestClientTestClass(restTemplateBuilder, baseUrl);

        assertEquals(restTemplate, client.restTemplate, "Rest templates do not match");
        verify(restTemplateBuilder, times(1)).build();
        verifyNoMoreInteractions(restTemplateBuilder);
        ArgumentCaptor<DefaultUriBuilderFactory> uriBuilderFactoryArgumentCaptor =
            ArgumentCaptor.forClass(DefaultUriBuilderFactory.class);
        verify(restTemplate, times(1)).setUriTemplateHandler(uriBuilderFactoryArgumentCaptor.capture());

        DefaultUriBuilderFactory uriBuilderFactory = uriBuilderFactoryArgumentCaptor.getValue();
        URI uri = uriBuilderFactory.expand("/");
        assertEquals(baseUrl, uri.toString(), "URI builder factory and provided base URL do not align");
    }

    @Test
    void positiveWithoutBaseUrlRestTemplateConstructorAndBaseUrl() {
        AbstractRemoteRestClient client
            = new AbstractRemoteRestClientTestClass(restTemplateBuilder, null);

        assertEquals(restTemplate, client.restTemplate, "Rest templates do not match");
        verify(restTemplateBuilder, times(1)).build();
        verifyNoMoreInteractions(restTemplateBuilder);
        verifyNoInteractions(restTemplate);

    }


    private static class AbstractRemoteRestClientTestClass extends AbstractRemoteRestClient {

        AbstractRemoteRestClientTestClass(RestTemplateBuilder restTemplateBuilder) {
            super(restTemplateBuilder);
        }

        AbstractRemoteRestClientTestClass(RestTemplateBuilder restTemplateBuilder, String baseUrl) {
            super(restTemplateBuilder, baseUrl);
        }
    }
}
