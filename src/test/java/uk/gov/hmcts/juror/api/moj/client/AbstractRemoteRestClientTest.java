package uk.gov.hmcts.juror.api.moj.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        final String baseUrl = "www.baseurl.com";
        AbstractRemoteRestClient client
            = new AbstractRemoteRestClientTestClass(restTemplateBuilder, baseUrl);

        assertEquals(restTemplate, client.restTemplate, "Rest templates do not match");
        verify(restTemplateBuilder, times(1)).build();
        verifyNoMoreInteractions(restTemplateBuilder);
        ArgumentCaptor<RootUriTemplateHandler> rootUriTemplateHandlerArgumentCaptor =
            ArgumentCaptor.forClass(RootUriTemplateHandler.class);
        verify(restTemplate, times(1)).setUriTemplateHandler(rootUriTemplateHandlerArgumentCaptor.capture());


        RootUriTemplateHandler rootUriTemplateHandler = rootUriTemplateHandlerArgumentCaptor.getValue();
        assertNotNull(rootUriTemplateHandler, "Root uri template handler should not be null");
        assertEquals(baseUrl, rootUriTemplateHandler.getRootUri(), "Root uri template handler and provided base url do "
            + "not align");
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
