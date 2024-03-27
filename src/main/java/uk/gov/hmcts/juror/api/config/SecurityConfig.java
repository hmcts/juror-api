package uk.gov.hmcts.juror.api.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthenticationProvider;
import uk.gov.hmcts.juror.api.config.hmac.HmacJwtAuthenticationProvider;
import uk.gov.hmcts.juror.api.config.public1.PublicJwtAuthenticationProvider;

/**
 * Spring security configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityConfig {
    private final PublicJwtAuthenticationProvider publicJwtAuthenticationProvider;

    private final BureauJwtAuthenticationProvider bureauJwtAuthenticationProvider;


    private final HmacJwtAuthenticationProvider hmacJwtAuthenticationProvider;

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationEventPublisher(new NoopAuthenticationEventPublisher());
        authenticationManagerBuilder.authenticationProvider(publicJwtAuthenticationProvider);
        authenticationManagerBuilder.authenticationProvider(bureauJwtAuthenticationProvider);
        authenticationManagerBuilder.authenticationProvider(hmacJwtAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }
}
