package uk.gov.hmcts.juror.api.config;

import com.google.common.io.Files;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Swagger 3 config.
 *
 * @implNote https://github.com/springdoc/springdoc-openapi
 */
@Profile("development")
@Configuration
@Slf4j
public class SwaggerConfig {
    @Value("${git.commit.id}")
    private String gitCommitId;

    @Value("${git.branch}")
    private String branchName;

    @Value("${build.version}")
    private String softwareVersion;

    @Value("${build.phase}")
    private String phase;

    @Value("classpath:/swagger/description.txt")
    private Resource descriptionFile;

    @Value("${docs.host}")
    private String hostname;

    @Value("${docs.licence}")
    private String licence;

    @Value("${docs.licence-url}")
    private String licenceUrl;


    @Bean
    public GroupedOpenApi jurorDigitalApi() throws IOException {
        final String versionInfo = phase + " " + softwareVersion + "\n(Git: [" + gitCommitId + "] " + branchName + ")";
        String description;
        try {
            description = Files.toString(descriptionFile.getFile(), Charset.defaultCharset());
        } catch (FileNotFoundException fnfe) {
            log.warn("Description file not found: {}", fnfe.getMessage());
            description = "RESTful API for Juror Response and administration operations.";
            log.info("Default API description: {}", description);
        }
        String finalDescription = description;

        return GroupedOpenApi.builder()
            .group("API - Juror Digital")
            .packagesToScan(JurorDigitalApplication.class.getPackage().getName())
            .addOperationCustomizer((operation, handlerMethod) -> operation.addParametersItem(
                    new HeaderParameter()
                        .name("Authorization")
                        .description("JWT Auth header (Base64 encoded)")
                        .required(true)
                )
            )
            .addOpenApiCustomizer(openApi ->
                openApi.info(
                        new Info()
                            .title("Juror Digital - BETA Public")
                            .description(finalDescription)
                            .version(versionInfo)
                            .license(new License().name(licence).url(licenceUrl)
                            ))
                    .servers(new ArrayList<>(List.of(new Server().url(hostname))))
            ).build();
    }

    @Bean
    public GroupedOpenApi springActuatorApi() {
        return GroupedOpenApi.builder()
            .group("Spring Boot Actuator")
            .packagesToScan(org.springframework.boot.actuate.endpoint.Operation.class.getPackage().getName())
            .addOperationCustomizer((operation, handlerMethod) -> operation.addParametersItem(
                    new HeaderParameter()
                        .name("Authorization")
                        .description("HMAC Auth header (Base64 encoded)")
                        .required(true)
                )
            )
            .addOpenApiCustomizer(openApi ->
                openApi.info(
                        new Info()
                            .title("Juror Digital - BETA Public - Actuator Endpoints")
                            .description("Spring Boot actuator endpoints.")
                            .version("Spring Boot 1.5.2.RELEASE")

                            .license(new License().name(licence).url(licenceUrl)
                            ))
                    .servers(new ArrayList<>(List.of(new Server().url(hostname))))
            ).build();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer configProperties() {
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocations(
            new ClassPathResource("git.properties"),
            new ClassPathResource("META-INF/build-info.properties")
        );
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }
}
