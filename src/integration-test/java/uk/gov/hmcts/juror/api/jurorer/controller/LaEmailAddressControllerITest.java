package uk.gov.hmcts.juror.api.jurorer.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.ExportLaEmailAddressResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + LaEmailAddressControllerITest.BASE_URL)
@SuppressWarnings("PMD")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Sql(
    scripts = {"/db/jurorer/teardownLaEmailAddressControllerITest.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = {"/db/jurorer/setupLaEmailAddressControllerITest.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = {"/db/jurorer/teardownLaEmailAddressControllerITest.sql"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LaEmailAddressControllerITest extends AbstractIntegrationTest {

    public static final String BASE_URL = "/api/v1/moj/LaExport";

    private final TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initBureauHeaders();
    }

    @Nested
    @DisplayName("GET /email-addresses")
    class GetEmailAddresses {

        private static final String URL = BASE_URL + "/email-addresses";

        @Test
        @DisplayName("Should successfully export all LA email addresses")
        void exportEmailAddressesHappy() {
            ResponseEntity<ExportLaEmailAddressResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                ExportLaEmailAddressResponseDto.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be OK")
                .isEqualTo(HttpStatus.OK);

            ExportLaEmailAddressResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            List<ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto> testLas =
                body.getLocalAuthorities().stream()
                    .filter(la -> List.of("001", "002", "003", "004", "005").contains(la.getLaCode()))
                    .collect(Collectors.toList());

            assertThat(testLas)
                .as("Should return our 5 test Local Authorities")
                .hasSize(5);
        }

        @Test
        @DisplayName("Should return LAs sorted alphabetically by name")
        void exportEmailAddressesSortedByLaName() {
            ResponseEntity<ExportLaEmailAddressResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                ExportLaEmailAddressResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Filter to only our test LAs and check they're sorted
            List<String> testLaNames = response.getBody().getLocalAuthorities().stream()
                .filter(la -> List.of("001", "002", "003", "004", "005").contains(la.getLaCode()))
                .map(ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto::getLaName)
                .collect(Collectors.toList());

            assertThat(testLaNames)
                .as("Our test LAs should be sorted alphabetically by name")
                .containsExactly("Blackburn", "Broxtowe", "Eastleigh", "Harrogate", "West Oxfordshire");
        }

        @Test
        @DisplayName("Should include both active and inactive LAs")
        void exportEmailAddressesIncludesInactiveLas() {
            ResponseEntity<ExportLaEmailAddressResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                ExportLaEmailAddressResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            List<ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto> las =
                response.getBody().getLocalAuthorities();

            // LA 005 (Harrogate) is inactive
            ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto harrogate = las.stream()
                .filter(la -> "005".equals(la.getLaCode()))
                .findFirst()
                .orElseThrow();

            assertThat(harrogate.getIsActive())
                .as("Harrogate should be marked as inactive")
                .isFalse();
        }

        @Test
        @DisplayName("Should return LA with multiple email addresses")
        void exportEmailAddressesWithMultipleUsers() {
            ResponseEntity<ExportLaEmailAddressResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                ExportLaEmailAddressResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // LA 001 has 2 users
            ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto la001 =
                response.getBody().getLocalAuthorities().stream()
                    .filter(la -> "001".equals(la.getLaCode()))
                    .findFirst()
                    .orElseThrow();

            assertThat(la001.getLaCode()).isEqualTo("001");
            assertThat(la001.getLaName()).isEqualTo("West Oxfordshire");
            assertThat(la001.getEmailAddresses())
                .as("LA 001 should have 2 email addresses")
                .hasSize(2);

            assertThat(la001.getEmailAddresses())
                .extracting(ExportLaEmailAddressResponseDto.EmailAddressDto::getUsername)
                .containsExactlyInAnyOrder("user1@la001.gov.uk", "user2@la001.gov.uk");
        }

        @Test
        @DisplayName("Should return LA with single email address")
        void exportEmailAddressesWithSingleUser() {
            ResponseEntity<ExportLaEmailAddressResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                ExportLaEmailAddressResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // LA 002 has 1 user
            ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto la002 =
                response.getBody().getLocalAuthorities().stream()
                    .filter(la -> "002".equals(la.getLaCode()))
                    .findFirst()
                    .orElseThrow();

            assertThat(la002.getLaCode()).isEqualTo("002");
            assertThat(la002.getLaName()).isEqualTo("Broxtowe");
            assertThat(la002.getEmailAddresses())
                .as("LA 002 should have 1 email address")
                .hasSize(1);

            assertThat(la002.getEmailAddresses().get(0).getUsername())
                .isEqualTo("user1@la002.gov.uk");
        }

        @Test
        @DisplayName("Should return LA with no users")
        void exportEmailAddressesWithNoUsers() {
            ResponseEntity<ExportLaEmailAddressResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                ExportLaEmailAddressResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // LA 003 has no users
            ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto la003 =
                response.getBody().getLocalAuthorities().stream()
                    .filter(la -> "003".equals(la.getLaCode()))
                    .findFirst()
                    .orElseThrow();

            assertThat(la003.getLaCode()).isEqualTo("003");
            assertThat(la003.getLaName()).isEqualTo("Eastleigh");
            assertThat(la003.getEmailAddresses())
                .as("LA 003 should have no email addresses")
                .isEmpty();
        }

        @Test
        @DisplayName("Should include both active and inactive users")
        void exportEmailAddressesIncludesInactiveUsers() {
            ResponseEntity<ExportLaEmailAddressResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                ExportLaEmailAddressResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // LA 004 has 1 inactive user
            ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto la004 =
                response.getBody().getLocalAuthorities().stream()
                    .filter(la -> "004".equals(la.getLaCode()))
                    .findFirst()
                    .orElseThrow();

            assertThat(la004.getEmailAddresses())
                .as("LA 004 should have 1 email address")
                .hasSize(1);

            ExportLaEmailAddressResponseDto.EmailAddressDto email = la004.getEmailAddresses().get(0);
            assertThat(email.getUsername()).isEqualTo("inactive@la004.gov.uk");
            assertThat(email.getActive())
                .as("User should be marked as inactive")
                .isFalse();
        }

        @Test
        @DisplayName("Should return email addresses sorted alphabetically within each LA")
        void exportEmailAddressesSortedWithinLa() {
            ResponseEntity<ExportLaEmailAddressResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                ExportLaEmailAddressResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // LA 001 has 2 users - check they're sorted
            ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto la001 =
                response.getBody().getLocalAuthorities().stream()
                    .filter(la -> "001".equals(la.getLaCode()))
                    .findFirst()
                    .orElseThrow();

            assertThat(la001.getEmailAddresses())
                .extracting(ExportLaEmailAddressResponseDto.EmailAddressDto::getUsername)
                .as("Email addresses should be sorted alphabetically")
                .containsExactly("user1@la001.gov.uk", "user2@la001.gov.uk");
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user is not Bureau user")
        void exportEmailAddressesNotBureauUser() {
            // Create court user JWT instead of bureau
            String courtJwt = createJwt("COURT_USER", "415", UserType.COURT, Set.of(), "415");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be FORBIDDEN")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Should return INTERNAL_SERVER_ERROR when no JWT provided")
        void exportEmailAddressesNoJwt() {
            httpHeaders.remove(HttpHeaders.AUTHORIZATION);

            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be INTERNAL_SERVER_ERROR")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void initBureauHeaders() {
        BureauJwtPayload payload = BureauJwtPayload.builder()
            .login(BUREAU_USER)
            .staff(BureauJwtPayload.Staff.builder()
                       .name("Bureau User")
                       .active(1)
                       .build())
            .userType(UserType.BUREAU)
            .owner("400")
            .build();

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(payload));
        httpHeaders.setAccept(Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
    }
}
