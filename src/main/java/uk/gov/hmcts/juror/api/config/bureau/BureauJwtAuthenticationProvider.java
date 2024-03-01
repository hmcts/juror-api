package uk.gov.hmcts.juror.api.config.bureau;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.JwtService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Performs a Spring Security {@link Authentication} for a {@link BureauJwtAuthentication}.
 */
@Slf4j
@Component
public class BureauJwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtService jwtService;
    private final String secret;

    @Autowired
    public BureauJwtAuthenticationProvider(final JwtService jwtService,
                                           @Value("${jwt.secret.bureau}") final String secret) {
        this.jwtService = jwtService;
        this.secret = secret;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BureauJwtAuthentication.class.isAssignableFrom(authentication);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            if (authentication instanceof BureauJwtAuthentication bureauJwtAuthentication) {
                final Claims body = jwtService
                    .extractClaims(bureauJwtAuthentication.getToken(), secret);

                // parse the staff object
                String name = null;
                Integer rank = null;
                Integer active = null;
                List<String> courts = null;
                final Map<String, Object> staffMap = body.get("staff", Map.class);
                if (!ObjectUtils.isEmpty(staffMap)) {
                    name = String.valueOf(staffMap.getOrDefault("name", null));
                    rank = (Integer) staffMap.getOrDefault("rank", Integer.MIN_VALUE);
                    active = (Integer) staffMap.getOrDefault("active", 0);
                    courts = (List<String>) staffMap.getOrDefault("courts", Collections.emptyList());
                }

                final BureauJWTPayload.Staff staff = BureauJWTPayload.Staff.builder()
                    .name(name)
                    .rank(rank)
                    .active(active)
                    .courts(courts)
                    .build();


                final List<String> roleString = body.containsKey("roles")
                    ? body.get("roles", List.class) : Collections.emptyList();

                final Set<Role> roles = roleString
                    .stream()
                    .map(o -> Role.valueOf(String.valueOf(o)))
                    .collect(Collectors.toSet());

                UserType userType = body.containsKey("userType")
                    ? UserType.valueOf(body.get("userType", String.class))
                    : null;

                final BureauJWTPayload payload = BureauJWTPayload.builder()
                    .daysToExpire(body.get("daysToExpire", Integer.class))
                    .login(body.get("login", String.class))
                    .owner(body.get("owner", String.class))
                    .passwordWarning(body.get("passwordWarning", Boolean.class))
                    .userLevel(body.get("userLevel", String.class))
                    .staff(staff)
                    .roles(roles)
                    .userType(userType)
                    .build();

                return new BureauJwtAuthentication(payload.getGrantedAuthority(), payload);
            }
            if (log.isErrorEnabled()) {
                log.error(
                    "Token type expected {}, got {}",
                    BureauJwtAuthentication.class.getSimpleName(),
                    authentication.getClass().getSimpleName()
                );
            }
        } catch (InvalidJwtAuthenticationException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new MojException.InternalServerError(
                "An unexpected exception has occurred when trying to authenticate using BureauJwtAuthentication",
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
