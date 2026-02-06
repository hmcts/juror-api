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
import uk.gov.hmcts.juror.api.jurorer.domain.UploadStatus;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErDashboardStatsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthoritiesResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the ER Dashboard controller.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardData.sql"})
class ErDashboardControllerITest extends AbstractIntegrationTest {

    private final TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initHeadersBureau();
    }


    @Nested
    @DisplayName("GET /api/v1/moj/er-dashboard/local-authorities")
    @Sql({"/db/jurorer/createLocalAuthorities.sql"})
    class LocalAuthorityListingTests {

        @Test
        void testGetLocalAuthoritiesAll() {

            ResponseEntity<LocalAuthoritiesResponseDto> responseEntity =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/er-dashboard/local-authorities")), LocalAuthoritiesResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

            assertThat(responseEntity.getBody())
                .as("Expect the body to not be null.")
                .isNotNull();

            LocalAuthoritiesResponseDto body = responseEntity.getBody();

            assertThat(body.getLocalAuthorities())
                .as("Expect the local authorities list to not be empty.")
                .isNotEmpty();

            List<LocalAuthoritiesResponseDto.LocalAuthorityData> localAuthorities = body.getLocalAuthorities();

            assertThat(localAuthorities).hasSize(350); // Assuming there are 350 local authorities in total

            LocalAuthoritiesResponseDto.LocalAuthorityData firstLa = localAuthorities.get(0);
            assertThat(firstLa.getLocalAuthorityCode()).isEqualTo("001");
            assertThat(firstLa.getLocalAuthorityName()).isEqualTo("West Oxfordshire");

            LocalAuthoritiesResponseDto.LocalAuthorityData lastLa = localAuthorities.get(349);
            assertThat(lastLa.getLocalAuthorityCode()).isEqualTo("404");
            assertThat(lastLa.getLocalAuthorityName()).isEqualTo("Durham County Council");


        }

        @Test
        void testGetLocalAuthoritiesActiveOnly() {

            ResponseEntity<LocalAuthoritiesResponseDto> responseEntity =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                        URI.create("/api/v1/moj/er-dashboard/local-authorities?active_only=true")),
                    LocalAuthoritiesResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

            assertThat(responseEntity.getBody())
                .as("Expect the body to not be null.")
                .isNotNull();

            LocalAuthoritiesResponseDto body = responseEntity.getBody();

            assertThat(body.getLocalAuthorities())
                .as("Expect the local authorities list to not be empty.")
                .isNotEmpty();

            List<LocalAuthoritiesResponseDto.LocalAuthorityData> localAuthorities = body.getLocalAuthorities();

            assertThat(localAuthorities).hasSize(317); // Assuming there are 350 local authorities in total

            LocalAuthoritiesResponseDto.LocalAuthorityData firstLa = localAuthorities.get(0);
            assertThat(firstLa.getLocalAuthorityCode()).isEqualTo("001");
            assertThat(firstLa.getLocalAuthorityName()).isEqualTo("West Oxfordshire");

            // check to make sure inactive LAs are not present, LA codes 233, 222 are inactive in test data
            boolean containsInactiveLa = localAuthorities.stream()
                .anyMatch(la -> "233".equals(la.getLocalAuthorityCode()) || "222".equals(la.getLocalAuthorityCode()));
            assertThat(containsInactiveLa).isFalse();


        }
    }

    @Nested
    @DisplayName("GET /api/v1/moj/er-dashboard/upload-stats")
    class DashboardStatsTests {

        @Test
        void testGetDashboardStatsHappy() {

            ResponseEntity<ErDashboardStatsResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                            URI.create("/api/v1/moj/er-dashboard/upload-stats")), ErDashboardStatsResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK.")
                    .isEqualTo(HttpStatus.OK);

            assertThat(responseEntity.getBody())
                    .as("Expect the body to not be null.")
                    .isNotNull();

            ErDashboardStatsResponseDto statsResponseDto = responseEntity.getBody();
            assertThat(statsResponseDto.getDeadlineDate()).isEqualTo(LocalDate.now().plusWeeks(9));
            assertThat(statsResponseDto.getDaysRemaining()).isEqualTo(63);
            assertThat(statsResponseDto.getTotalNumberOfLocalAuthorities()).isEqualTo(6);
            assertThat(statsResponseDto.getUploadedCount()).isEqualTo(2);
            assertThat(statsResponseDto.getNotUploadedCount()).isEqualTo(4);

        }

        @Test
        void testGetErDashboardStatsExceptionForCourtUser() {
            initHeadersCourt();
            ResponseEntity<ErDashboardStatsResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                            URI.create("/api/v1/moj/er-dashboard/upload-stats")), ErDashboardStatsResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be forbidden.")
                    .isEqualTo(HttpStatus.FORBIDDEN);

        }
    }

    @Nested
    @DisplayName("GET with body /api/v1/moj/er-dashboard/local-authority-status")
    class UploadStatusTests {

        @Test
        void testGetUploadStatusNoFiltersHappy() {

            ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder().build();

            ResponseEntity<ErLocalAuthorityStatusResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                                    URI.create("/api/v1/moj/er-dashboard/local-authority-status")),
                            ErLocalAuthorityStatusResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK.")
                    .isEqualTo(HttpStatus.OK);

            assertThat(responseEntity.getBody())
                    .as("Expect the body to not be null.")
                    .isNotNull();
            ErLocalAuthorityStatusResponseDto statusResponseDto = responseEntity.getBody();

            assertThat(statusResponseDto.getLocalAuthorityStatuses())
                    .as("Expect the local authorities list to not be empty.")
                    .isNotEmpty();

            validateLocalAllLocalAuthorityStatuses(statusResponseDto);
        }


        @Test
        void testGetUploadStatusStatusFilterNotUploaded() {

            ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder()
                    .uploadStatus(List.of(UploadStatus.NOT_UPLOADED))
                    .build();

            ResponseEntity<ErLocalAuthorityStatusResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                                    URI.create("/api/v1/moj/er-dashboard/local-authority-status")),
                            ErLocalAuthorityStatusResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK.")
                    .isEqualTo(HttpStatus.OK);
            ErLocalAuthorityStatusResponseDto statusResponseDto = responseEntity.getBody();

            assertThat(statusResponseDto.getLocalAuthorityStatuses())
                    .as("Expect the local authorities list to not be empty.")
                    .isNotEmpty();

            List<ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus> localAuthorities =
                    statusResponseDto.getLocalAuthorityStatuses();

            assertThat(localAuthorities).hasSize(4); // there are 6 local authorities in test data
            ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus authority = localAuthorities.get(0);

            assertThat(authority.getLocalAuthorityCode()).isEqualTo("003");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Eastleigh");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(authority.getLastUploadDate()).isNull();

            authority = localAuthorities.get(1);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("005");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Harrogate");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(authority.getLastUploadDate()).isNull();

            authority = localAuthorities.get(2);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("006");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Folkestone & Hythe");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(authority.getLastUploadDate()).isNull();

            authority = localAuthorities.get(3);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("007");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Bradford");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(authority.getLastUploadDate()).isNull();

        }

        @Test
        void testGetUploadStatusStatusFilterUploaded() {

            ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder()
                    .uploadStatus(List.of(UploadStatus.UPLOADED))
                    .build();

            ResponseEntity<ErLocalAuthorityStatusResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                                    URI.create("/api/v1/moj/er-dashboard/local-authority-status")),
                            ErLocalAuthorityStatusResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK.")
                    .isEqualTo(HttpStatus.OK);

            ErLocalAuthorityStatusResponseDto statusResponseDto = responseEntity.getBody();

            assertThat(statusResponseDto.getLocalAuthorityStatuses())
                    .as("Expect the local authorities list to not be empty.")
                    .isNotEmpty();

            List<ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus> localAuthorities =
                    statusResponseDto.getLocalAuthorityStatuses();

            assertThat(localAuthorities).hasSize(2);
            ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus authority = localAuthorities.get(0);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("001");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("West Oxfordshire");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.UPLOADED);
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));

            authority = localAuthorities.get(1);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("002");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Broxtowe");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.UPLOADED);
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.of(2026, 2, 4, 0, 0));

        }

        @Test
        void testGetUploadStatusStatusFilterLocalAuthority() {

            ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder()
                    .localAuthorityCode("003")
                    .build();

            ResponseEntity<ErLocalAuthorityStatusResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                                    URI.create("/api/v1/moj/er-dashboard/local-authority-status")),
                            ErLocalAuthorityStatusResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK.")
                    .isEqualTo(HttpStatus.OK);

            ErLocalAuthorityStatusResponseDto statusResponseDto = responseEntity.getBody();

            assertThat(statusResponseDto.getLocalAuthorityStatuses())
                    .as("Expect the local authorities list to not be empty.")
                    .isNotEmpty();

            List<ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus> localAuthorities =
                    statusResponseDto.getLocalAuthorityStatuses();

            assertThat(localAuthorities).hasSize(1);
            ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus authority = localAuthorities.get(0);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("003");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Eastleigh");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(authority.getLastUploadDate()).isNull();

        }

        @Test
        void testGetUploadStatusStatusFilterAllUploadStatuses() {

            ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder()
                    .uploadStatus(List.of(UploadStatus.UPLOADED, UploadStatus.NOT_UPLOADED))
                    .build();

            ResponseEntity<ErLocalAuthorityStatusResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                                    URI.create("/api/v1/moj/er-dashboard/local-authority-status")),
                            ErLocalAuthorityStatusResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK.")
                    .isEqualTo(HttpStatus.OK);

            assertThat(responseEntity.getBody())
                    .as("Expect the body to not be null.")
                    .isNotNull();
            ErLocalAuthorityStatusResponseDto statusResponseDto = responseEntity.getBody();

            assertThat(statusResponseDto.getLocalAuthorityStatuses())
                    .as("Expect the local authorities list to not be empty.")
                    .isNotEmpty();

            validateLocalAllLocalAuthorityStatuses(statusResponseDto);

        }

        @Test
        void testGetUploadStatusStatusFilterAllFiltersAppliedOneResultExpected() {

            ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder()
                    .localAuthorityCode("001")
                    .uploadStatus(List.of(UploadStatus.UPLOADED, UploadStatus.NOT_UPLOADED))
                    .build();

            ResponseEntity<ErLocalAuthorityStatusResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                                    URI.create("/api/v1/moj/er-dashboard/local-authority-status")),
                            ErLocalAuthorityStatusResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK.")
                    .isEqualTo(HttpStatus.OK);

            ErLocalAuthorityStatusResponseDto statusResponseDto = responseEntity.getBody();

            assertThat(statusResponseDto.getLocalAuthorityStatuses())
                    .as("Expect the local authorities list to not be empty.")
                    .isNotEmpty();

            List<ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus> localAuthorities =
                    statusResponseDto.getLocalAuthorityStatuses();

            assertThat(localAuthorities).hasSize(1);
            ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus authority = localAuthorities.get(0);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("001");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("West Oxfordshire");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.UPLOADED);
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));

        }

        @Test
        void testGetUploadStatusStatusFilterAllFiltersAppliedNoResultExpected() {

            ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder()
                    .localAuthorityCode("001")
                    .uploadStatus(List.of(UploadStatus.NOT_UPLOADED))
                    .build();

            ResponseEntity<ErLocalAuthorityStatusResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                                    URI.create("/api/v1/moj/er-dashboard/local-authority-status")),
                            ErLocalAuthorityStatusResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK.")
                    .isEqualTo(HttpStatus.OK);

            ErLocalAuthorityStatusResponseDto statusResponseDto = responseEntity.getBody();

            assertThat(statusResponseDto.getLocalAuthorityStatuses())
                    .as("Expect the local authorities list to be empty.")
                    .isEmpty();

        }

        private void validateLocalAllLocalAuthorityStatuses(ErLocalAuthorityStatusResponseDto statusResponseDto) {
            List<ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus> localAuthorities =
                    statusResponseDto.getLocalAuthorityStatuses();

            assertThat(localAuthorities).hasSize(6); // there are 6 local authorities in test data
            ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus authority = localAuthorities.get(0);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("001");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("West Oxfordshire");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.UPLOADED);
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));

            authority = localAuthorities.get(1);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("002");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Broxtowe");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.UPLOADED);
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.of(2026, 2, 4, 0, 0));

            authority = localAuthorities.get(2);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("003");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Eastleigh");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(authority.getLastUploadDate()).isNull();

            authority = localAuthorities.get(3);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("005");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Harrogate");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(authority.getLastUploadDate()).isNull();

            authority = localAuthorities.get(4);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("006");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Folkestone & Hythe");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(authority.getLastUploadDate()).isNull();

            authority = localAuthorities.get(5);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("007");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Bradford");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(authority.getLastUploadDate()).isNull();
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
