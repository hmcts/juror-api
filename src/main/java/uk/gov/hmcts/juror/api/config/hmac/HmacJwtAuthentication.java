package uk.gov.hmcts.juror.api.config.hmac;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;


public class HmacJwtAuthentication extends AbstractAuthenticationToken {
    private final String token;

    /**
     * Pre-authenticated token. ({@link #isAuthenticated()} is <b>false</b>).
     *
     */
    public HmacJwtAuthentication(final String token) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.token = token;
        setAuthenticated(false);
    }

    /**
     * Authenticated token. ({@link #isAuthenticated()} is <b>true</b>).
     *
     
     */
    public HmacJwtAuthentication(Collection<? extends GrantedAuthority> authorities, String token) {
        super(authorities);
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "N/A";// MUST be a non-null value
    }

    @Override
    public Object getPrincipal() {
        return "HMAC";
    }

    public String getToken() {
        return token;
    }
}
