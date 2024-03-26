package uk.gov.hmcts.juror.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableAsync
@SuppressWarnings("HideUtilityClassConstructor")
public class JurorDigitalApplication {
    /**
     * Constant for the <code>owner</code> field in the Juror database where required.
     */
    public static final String JUROR_OWNER = "400";

    /**
     * Username for tagging auto completed response audits.
     */
    public static final String AUTO_USER = "AUTO";

    /**
     * Default page size when returning paginated result sets to the front-end.
     */
    public static final int PAGE_SIZE = 25;

    public static void main(String[] args) {
        SpringApplication.run(JurorDigitalApplication.class, args);
    }

    public JurorDigitalApplication() {

    }
}
