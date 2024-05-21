package uk.gov.hmcts.juror.api.moj.client.interceptor;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import uk.gov.hmcts.juror.api.config.JwtSecurityConfig;
import uk.gov.hmcts.juror.api.moj.service.JwtService;

import java.io.IOException;
import java.security.Key;


public class JwtAuthenticationInterceptor implements ClientHttpRequestInterceptor {

    private final JwtSecurityConfig config;
    private final JwtService jwtService;

    public JwtAuthenticationInterceptor(JwtService jwtService, JwtSecurityConfig config) {
        this.jwtService = jwtService;
        this.config = config;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        HttpHeaders headers = request.getHeaders();
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            headers.setBearerAuth(generateJwt());
        }
        return execution.execute(request, body);
    }

    private String generateJwt() {
        return jwtService.generateJwtToken(null,this.config.getIssuer(),
            this.config.getSubject(),
            this.config.getTokenValidity(),
            getSigningKey(),
            this.config.getClaims());
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.config.getSecret()));
    }

}
