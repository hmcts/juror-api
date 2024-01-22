package uk.gov.hmcts.juror.api.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * E-Tag filter configuration.
 */
@Configuration
public class ETagConfiguration {

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> shallowEtagHeaderFilter() {

        FilterRegistrationBean<ShallowEtagHeaderFilter> filterRegistrationBean
            = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        filterRegistrationBean.addUrlPatterns("/api/v1/moj/*");
        filterRegistrationBean.setName("etagFilter");
        return filterRegistrationBean;
    }
}
