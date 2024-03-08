package uk.gov.hmcts.juror.api.moj.controller;

import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.TestUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractControllerTest<P, R> {


    @Setter
    private MockMvc mockMvc;
    private final HttpMethod method;
    private final String url;
    private final Authentication authentication;

    protected AbstractControllerTest(HttpMethod method,
                                     String url,
                                     Authentication authentication) {
        this.method = method;
        this.url = url;
        this.authentication = authentication;
    }

    protected ResultActions send(P payload, HttpStatus expectedStatus,
                                 String... pathParams) {
        return send(payload, expectedStatus, authentication, pathParams);
    }

    @SneakyThrows
    protected ResultActions send(P payload, HttpStatus expectedStatus, Authentication authentication,
                                 String... pathParams) {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .request(this.method, this.url, (Object[]) pathParams)
            .principal(authentication);

        if (payload != null) {
            requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(payload));
        }
        return mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is(expectedStatus.value()));
    }
}
