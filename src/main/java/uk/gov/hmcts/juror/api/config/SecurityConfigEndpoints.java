package uk.gov.hmcts.juror.api.config;

import jakarta.servlet.Filter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import uk.gov.hmcts.juror.api.config.bureau.BureauPreAuthenticationTokenFilter;
import uk.gov.hmcts.juror.api.config.hmac.HmacPreAuthenticationTokenFilter;
import uk.gov.hmcts.juror.api.config.public_.PublicPreAuthenticationTokenFilter;

@Configuration
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityConfigEndpoints {
    private static final String ERROR = "/error";

    private final RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    private final AuthenticationManager authenticationManager;

    /**
     * public (juror) security.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicApiJwtSecurityConfigurationFilterChain(HttpSecurity http) throws Exception {
        return buildSecurityFilterChainBase(
            http,
            new PublicPreAuthenticationTokenFilter(HttpHeaders.AUTHORIZATION, authenticationManager))
            .securityMatcher("/api/v1/public/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .build();
    }

    /**
     * Bureau (juror) security.
     */
    @Bean
    @Order(1)
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain bureauApiJwtSecurityConfigurationFilterChain(HttpSecurity http) throws Exception {
        return buildSecurityFilterChainBase(
            http,
            new BureauPreAuthenticationTokenFilter(HttpHeaders.AUTHORIZATION, authenticationManager))
            .securityMatcher(
                "/api/v1/bureau/**",
                "/api/v1/moj/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated()
            ).build();
    }


    /**
     * hmac security.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authenticationEndpointHmacSecurityConfigurationFilterChain(
        HttpSecurity http) throws Exception {
        return buildSecurityFilterChainBase(
            http,
            new HmacPreAuthenticationTokenFilter(HttpHeaders.AUTHORIZATION, authenticationManager))
            .securityMatcher("/api/v1/auth/**")
            .authorizeHttpRequests(auth -> auth.requestMatchers(
                "/api/v1/auth/juror/**",
                "/api/v1/auth/bureau/**",
                "/api/v1/auth/moj/**",
                "/api/v1/auth/public/**",
                "/api/v1/auth/settings/**"
            ).authenticated())
            .build();
    }

    public HttpSecurity buildSecurityFilterChainBase(HttpSecurity http, Filter filter)
        throws Exception {
        return http
            .addFilterBefore(filter, AbstractPreAuthenticatedProcessingFilter.class)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exc -> exc.authenticationEntryPoint(restfulAuthenticationEntryPoint));
    }

    /**
     * Error endpoint security.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain permitErrorConfigurationFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher(ERROR)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(auths -> auths.authenticationEntryPoint(restfulAuthenticationEntryPoint))
            .build();
    }

    @Autowired
    protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        //no-op
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auths -> auths.anyRequest().denyAll())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exc -> exc.authenticationEntryPoint(restfulAuthenticationEntryPoint))
            .build();
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return web -> web
            .ignoring()
            .requestMatchers(
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, ERROR),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, ERROR),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/webjars/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/swagger-resources/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/v3/api-docs/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/swagger-ui/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/login"),
                //TODO: remove me once the auto-config redirect mvc login is disabled
                //FIXME: remove the actuator ignores later, move them to be secured under
                // AuthenticationEndpointHmacSecurityConfiguration
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/health"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/info")
            );
    }
}
