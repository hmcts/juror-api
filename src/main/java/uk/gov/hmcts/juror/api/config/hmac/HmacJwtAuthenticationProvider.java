package uk.gov.hmcts.juror.api.config.hmac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.JwtService;


@Slf4j
@Component
public class HmacJwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtService jwtService;
    private final String secret;

    @Autowired
    public HmacJwtAuthenticationProvider(final JwtService jwtService,
                                         @Value("${jwt.secret.hmac}") final String secret) {
        this.jwtService = jwtService;
        this.secret = secret;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return HmacJwtAuthentication.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            if (authentication instanceof HmacJwtAuthentication hmacJwtAuthentication) {
                if (jwtService.isJwtExpired(hmacJwtAuthentication.getToken(), secret)) {
                    log.error("Failed to authenticate token! Jwt has expired");
                    throw new InvalidJwtAuthenticationException("Jwt has expired");
                }
                return new HmacJwtAuthentication(
                    AuthorityUtils.NO_AUTHORITIES,
                    authentication.getPrincipal().toString()
                );
            }
            if (log.isErrorEnabled()) {
                log.error(
                    "Token type expected {}, got {}",
                    HmacJwtAuthentication.class.getSimpleName(),
                    authentication.getClass().getSimpleName()
                );
            }
        } catch (InvalidJwtAuthenticationException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new MojException.InternalServerError(
                "An unexpected exception has occurred when trying to authenticate using HmacJwtAuthentication",
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
