package uk.gov.hmcts.juror.api.config.public_;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;


public class PublicJwtAuthentication extends AbstractAuthenticationToken {
    private final String token;
    private final PublicJWTPayload principal;

    /**
     * Pre-authenticated token. ({@link #isAuthenticated()} is <b>false</b>).
     *
     * @param token
     */
    public PublicJwtAuthentication(final String token) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.token = token;
        this.principal = PublicJWTPayload.builder().id(token).build();
        setAuthenticated(false);
    }

    /**
     * Authenticated token. ({@link #isAuthenticated()} is <b>true</b>).
     *
     * @param authorities
     * @param jwt
     */
    public PublicJwtAuthentication(Collection<? extends GrantedAuthority> authorities, PublicJWTPayload jwt) {
        super(authorities);
        this.token = null;
        setAuthenticated(true);

        //decode the token and expose relevant fields (principal = payload)
        this.principal = jwt;
    }

    @Override
    public Object getCredentials() {
        return "N/A";// MUST be a non-null value
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getToken() {
        return token;
    }
}
