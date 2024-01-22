package uk.gov.hmcts.juror.api.config.public_;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.JwtService;

import java.util.List;


@Slf4j
@Component
public class PublicJwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtService jwtService;
    private final String secret;

    @Autowired
    public PublicJwtAuthenticationProvider(final JwtService jwtService,
                                           @Value("${jwt.secret.public}") final String secret) {
        this.jwtService = jwtService;
        this.secret = secret;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PublicJwtAuthentication.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            if (authentication instanceof PublicJwtAuthentication publicJwtAuthentication) {
                final Claims jwt = jwtService
                    .extractClaims(publicJwtAuthentication.getToken(), secret);

                ObjectMapper mapper = new ObjectMapper();
                Object data = jwt.get("data");
                final PublicJWTPayload payload = mapper.convertValue(data, PublicJWTPayload.class);
                final String[] roles = payload.getRoles();
                final List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                    String.join(",", roles));

                return new PublicJwtAuthentication(grantedAuthorities, payload);
            }
            if (log.isErrorEnabled()) {
                log.error(
                    "Token type expected {}, got {}",
                    PublicJwtAuthentication.class.getSimpleName(),
                    authentication.getClass().getSimpleName()
                );
            }
        } catch (InvalidJwtAuthenticationException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new MojException.InternalServerError(
                "An unexpected exception has occurred when trying to authenticate using PublicJwtAuthentication",
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
