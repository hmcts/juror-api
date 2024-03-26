package uk.gov.hmcts.juror.api.config.public1;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;


public class PublicJwtAuthentication extends AbstractAuthenticationToken {
    private final String token;
    private final PublicJwtPayload principal;

    /**
     * Pre-authenticated token. ({@link #isAuthenticated()} is <b>false</b>).
     *
     */
    public PublicJwtAuthentication(final String token) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.token = token;
        this.principal = PublicJwtPayload.builder().id(token).build();
        setAuthenticated(false);
    }

    /**
     * Authenticated token. ({@link #isAuthenticated()} is <b>true</b>).
     *
     
     */
    public PublicJwtAuthentication(Collection<? extends GrantedAuthority> authorities, PublicJwtPayload jwt) {
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
