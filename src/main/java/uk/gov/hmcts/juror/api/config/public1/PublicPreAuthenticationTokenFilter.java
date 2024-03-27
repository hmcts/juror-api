package uk.gov.hmcts.juror.api.config.public1;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;

import java.io.IOException;


@Slf4j
public class PublicPreAuthenticationTokenFilter extends OncePerRequestFilter {
    private final String headerName;
    private final AuthenticationManager authenticationManager;

    public PublicPreAuthenticationTokenFilter(final String headerName,
                                              final AuthenticationManager authenticationManager) {
        this.headerName = headerName;
        this.authenticationManager = authenticationManager;
        Assert.notNull(headerName, "The token header name was not set");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String jwt = request.getHeader(headerName);
        if (jwt == null || jwt.isEmpty()) {
            log.warn("JWT was null or empty: {}={}", headerName, jwt);
            throw new InvalidJwtAuthenticationException("Authentication header may not be empty!");
        }
        Authentication authentication = authenticationManager.authenticate(new PublicJwtAuthentication(jwt));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}