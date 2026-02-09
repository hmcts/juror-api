package uk.gov.hmcts.juror.api.config.jurorer;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.JwtService;

/**
 * Performs a Spring Security {@link Authentication} for a {@link JurorErJwtAuthentication}.
 */
@Slf4j
@Component
public class JurorErJwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtService jwtService;
    private final String secret;

    @Autowired
    public JurorErJwtAuthenticationProvider(final JwtService jwtService,
                                            @Value("${jwt.secret.er-portal}") final String secret) {
        this.jwtService = jwtService;
        this.secret = secret;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JurorErJwtAuthentication.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            if (authentication instanceof JurorErJwtAuthentication jurorErJwtAuthentication) {
                final Claims body = jwtService
                    .extractClaims(jurorErJwtAuthentication.getToken(), secret);

                final JurorErJwtPayload payload = JurorErJwtPayload.fromClaims(body);
                return new JurorErJwtAuthentication(payload.getGrantedAuthority(), payload);
            }
            if (log.isErrorEnabled()) {
                log.error(
                    "Token type expected {}, got {}",
                    JurorErJwtAuthentication.class.getSimpleName(),
                    authentication.getClass().getSimpleName()
                );
            }
        } catch (InvalidJwtAuthenticationException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new MojException.InternalServerError(
                "An unexpected exception has occurred when trying to authenticate using JurorErJwtAuthentication",
                throwable
            );
        }

        /*
         * this check is relevant since the only requests that should be mapped to the provider's chain are for the
         * correct type
         */
        log.warn("Authentication type for this provider! This provider should be the only one for this chain");
        throw new IllegalStateException("Provider should have handled this request.  Check filter chain mappings "
            + "config");
    }
}
