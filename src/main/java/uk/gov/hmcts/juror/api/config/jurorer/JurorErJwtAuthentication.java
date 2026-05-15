package uk.gov.hmcts.juror.api.config.jurorer;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

@Hidden
public class JurorErJwtAuthentication extends AbstractAuthenticationToken {
    @Getter
    private final String token;
    private final JurorErJwtPayload principal;

    /**
     * Pre-authenticated token. ({@link #isAuthenticated()} is <b>false</b>).
     *
     */
    public JurorErJwtAuthentication(final String token) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.token = token;
        this.principal = JurorErJwtPayload.builder().username(token).build();
        setAuthenticated(false);
    }

    /**
     * Authenticated token. ({@link #isAuthenticated()} is <b>true</b>).
     *

     */
    public JurorErJwtAuthentication(Collection<? extends GrantedAuthority> authorities, JurorErJwtPayload jwt) {
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

}
