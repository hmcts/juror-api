package uk.gov.hmcts.juror.api.moj.controller;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.DeactiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the ER Administration controller.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ErAdministrationControllerITest extends AbstractIntegrationTest {

    private final TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    private final LocalAuthorityRepository localAuthorityRepository;

    @BeforeEach
    public void setUp() throws Exception {
        initHeadersBureau();
    }


    @Nested
    @DisplayName("GET /api/v1/moj/er-administration/deactivate-la")
    @Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardData.sql"})
    class DeactivateLaTest {

        @Test
        void testDeactivateLocalAuthority() {

            DeactiveLaRequestDto requestDto = new DeactiveLaRequestDto();
            requestDto.setLaCode("002");
            requestDto.setReason("This is a test reason for deactivating LA2");

            ResponseEntity<Void> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                         URI.create("/api/v1/moj/er-administration/deactivate-la")), Void.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

            // the initial data has LA2 as active, so we can check that it has been deactivated
            // and the reason has been set correctly

            executeInTransaction(() -> {
                Optional<LocalAuthority> localAuthority = localAuthorityRepository.findByLaCode("002");
                assertThat(localAuthority)
                    .as("Expect the local authority to be present.")
                    .isPresent();
                assertThat(localAuthority.get().getActive())
                    .as("Expect the local authority to be deactivated.")
                    .isFalse();
                assertThat(localAuthority.get().getInactiveReason())
                    .as("Expect the local authority to have the correct inactive reason.")
                    .isEqualTo("This is a test reason for deactivating LA2");
            });

        }

        @Test
        void testDeactivateLaForCourtUserShouldBeForbidden() {
            initHeadersCourt();
            DeactiveLaRequestDto requestDto = new DeactiveLaRequestDto();
            requestDto.setLaCode("LA2");
            requestDto.setReason("This is a test reason for deactivating LA2");

            ResponseEntity<Void> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                              URI.create("/api/v1/moj/er-administration/deactivate-la")), Void.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be forbidden.")
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

        private void initHeadersCourt() {
            httpHeaders =
                initialiseHeaders("COURT_USER", UserType.COURT,null,"435");
        }
    }

    private void initHeadersBureau() {
        httpHeaders =
            initialiseHeaders("BUREAU_USER", UserType.BUREAU,null,"400");
    }
}
