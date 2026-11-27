package uk.gov.hmcts.juror.api.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SwaggerConfigITest.DocumentedController.class,
    properties = "springdoc.show-actuator=false")
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {SwaggerConfig.class, SwaggerConfigITest.DocumentedController.class})
@EnableConfigurationProperties(SpringDocConfigProperties.class)
@ImportAutoConfiguration({SpringDocConfiguration.class, SpringDocWebMvcConfiguration.class,
    MultipleOpenApiSupportConfiguration.class})
@ActiveProfiles({"development", "test"})
class SwaggerConfigITest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {"/v3/api-docs", "/v3/api-docs/API - Juror Digital"})
    void generatedSpecificationDeclaresAuthentication(String docsUrl) throws Exception {
        String response = mockMvc.perform(get(docsUrl)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode specification = objectMapper.readTree(response);
        JsonNode schemes = specification.path("components").path("securitySchemes");
        assertThat(schemes.size()).isEqualTo(4);
        schemes.forEach(scheme -> {
            assertThat(scheme.path("type").asText()).isEqualTo("apiKey");
            assertThat(scheme.path("in").asText()).isEqualTo("header");
            assertThat(scheme.path("name").asText()).isEqualTo("Authorization");
        });
        assertThat(specification.path("security").get(0).has("jwtAuth")).isTrue();
        JsonNode paths = specification.path("paths");
        assertSecurity(paths, "/api/v1/auth/juror/test", "hmacJwtAuth");
        assertSecurity(paths, "/api/v1/public/test", "publicJwtAuth");
        assertSecurity(paths, "/api/v1/juror-er/test", "electoralRegisterJwtAuth");
        for (String path : new String[]{"/api/v1/bureau/test", "/api/v1/moj/test"}) {
            assertThat(paths.path(path).path("get").has("security")).isFalse();
        }
        for (String path : new String[]{"/", "/login", "/health", "/info", "/error"}) {
            assertThat(paths.path(path).path("get").path("security").isArray()).isTrue();
            assertThat(paths.path(path).path("get").path("security").size()).isZero();
        }
        assertThat(paths.path("/error").path("post").path("security").isArray()).isTrue();
        assertThat(paths.path("/error").path("post").path("security").size()).isZero();
        assertThat(paths.path("/health").path("post").has("security")).isFalse();
        paths.forEach(path -> path.forEach(operation -> operation.path("parameters").forEach(parameter ->
            assertThat("header".equals(parameter.path("in").asText())
                && "Authorization".equalsIgnoreCase(parameter.path("name").asText())).isFalse())));
    }

    private void assertSecurity(JsonNode paths, String path, String scheme) {
        JsonNode security = paths.path(path).path("get").path("security");
        assertThat(security.size()).isEqualTo(1);
        assertThat(security.get(0).has(scheme)).isTrue();
        assertThat(security.get(0).path(scheme).isArray()).isTrue();
        assertThat(security.get(0).path(scheme).size()).isZero();
    }

    @RestController
    static class DocumentedController {
        @GetMapping({"/api/v1/public/test", "/api/v1/bureau/test", "/api/v1/moj/test",
            "/api/v1/juror-er/test", "/api/v1/auth/juror/test", "/", "/login", "/health", "/info", "/error"})
        String getResponse() {
            return "ok";
        }

        @PostMapping({"/health", "/error"})
        String postResponse() {
            return "ok";
        }
    }
}
