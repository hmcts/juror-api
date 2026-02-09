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
import uk.gov.hmcts.juror.api.moj.controller.jurorer.DeactiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the ER Dashboard controller.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ErAdministrationControllerITest extends AbstractIntegrationTest {

    private final TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initHeadersBureau();
    }


    @Nested
    @DisplayName("GET /api/v1/moj/er-dashboard/upload-stats")
    @Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardData.sql"})
    class DashboardStatsTests {

        @Test
        void testDeactiveLocalAuthority() {

            DeactiveLaRequestDto requestDto = new DeactiveLaRequestDto();
            requestDto.setLaCode("002");
            requestDto.setReason("This is a test reason for deactivating LA2");

            ResponseEntity<Void> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                         URI.create("/api/v1/moj/er-administration/deactivate-la")), Void.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

        }

        @Test
        void testGetErDashboardStatsExceptionForCourtUser() {
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
    }


    private void initHeadersCourt() {
        httpHeaders =
            initialiseHeaders("COURT_USER", UserType.COURT,null,"435");
    }

    private void initHeadersBureau() {
        httpHeaders =
            initialiseHeaders("BUREAU_USER", UserType.BUREAU,null,"400");
    }
}
