package uk.gov.hmcts.juror.api.moj.client.interceptor;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.config.JwtSecurityConfig;
import uk.gov.hmcts.juror.api.moj.service.JwtService;
import uk.gov.hmcts.juror.api.utils.TestConstants;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class JwtAuthenticationInterceptorTest {

    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @MockBean
    private ClientHttpRequestExecution clientHttpRequestExecution;
    @MockBean
    private HttpRequest request;
    @MockBean
    private HttpHeaders httpHeaders;
    @MockBean
    private ClientHttpResponse clientHttpResponse;

    @MockBean
    private JwtService jwtService;

    private JwtSecurityConfig securityConfig;

    private byte[] body;

    @BeforeEach
    void beforeEach() throws IOException {
        body = new byte[]{'A', 'B', 'C', 'D'};
        this.securityConfig = new JwtSecurityConfig();
        this.securityConfig.setIssuer("MyIssuer");
        this.securityConfig.setSubject("MySubject");
        this.securityConfig.setTokenValidity(160_000);
        this.securityConfig.setSecret(TestConstants.JWT_SECRET);
        this.securityConfig.setClaims(new HashMap<>());

        when(this.request.getHeaders()).thenReturn(this.httpHeaders);
        when(this.clientHttpRequestExecution.execute(this.request, this.body)).thenReturn(this.clientHttpResponse);
        this.jwtAuthenticationInterceptor = new JwtAuthenticationInterceptor(this.jwtService, this.securityConfig);

        when(jwtService.generateJwtToken(null,
            this.securityConfig.getIssuer(),
            this.securityConfig.getSubject(),
            this.securityConfig.getTokenValidity(),
            Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.securityConfig.getSecret())),
            this.securityConfig.getClaims()
        )).thenReturn(TestConstants.JWT);
    }

    @Test
    void positiveInterceptValid() throws IOException {
        when(httpHeaders.containsKey("Authorization")).thenReturn(false);

        assertEquals(clientHttpResponse,
            jwtAuthenticationInterceptor.intercept(request, body, clientHttpRequestExecution),
            "Client response not returned correctly");


        verify(request, times(1)).getHeaders();
        verify(httpHeaders, times(1)).setBearerAuth(TestConstants.JWT);
        verify(jwtService, times(1))
            .generateJwtToken(null,
                this.securityConfig.getIssuer(),
                this.securityConfig.getSubject(),
                this.securityConfig.getTokenValidity(),
                Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.securityConfig.getSecret())),
                this.securityConfig.getClaims()
            );
        verifyNoMoreInteractions(jwtService);
        verify(clientHttpRequestExecution, times(1)).execute(request, body);
    }

    @Test
    void negativeInterceptAlreadyHasHeader() throws IOException {
        when(httpHeaders.containsKey("Authorization")).thenReturn(true);

        assertEquals(clientHttpResponse,
            jwtAuthenticationInterceptor.intercept(request, body, clientHttpRequestExecution),
            "Client response not returned correctly");


        verify(request, times(1)).getHeaders();
        verify(httpHeaders, never()).setBearerAuth(any());
        verify(jwtService, never())
            .generateJwtToken(any(), any(), any(), any(Long.class), any(), any());
        verifyNoMoreInteractions(jwtService);
        verify(clientHttpRequestExecution, times(1)).execute(request, body);
    }
}
