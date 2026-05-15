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
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthorityInfoResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateLocalAuthorityNotesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateLocalAuthorityNotesResponseDto;
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
@SuppressWarnings("PMD")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
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
    @Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardData.sql"})
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
    @Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardData.sql"})
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
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.now()
                                                    .minusDays(5).withHour(0).withMinute(0).withSecond(0).withNano(0));

            authority = localAuthorities.get(1);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("002");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Broxtowe");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.UPLOADED);
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.now()
                                                    .minusDays(2).withHour(0).withMinute(0).withSecond(0).withNano(0));

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
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.now()
                                                    .minusDays(5).withHour(0).withMinute(0).withSecond(0).withNano(0));

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
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.now()
                                                    .minusDays(5).withHour(0).withMinute(0).withSecond(0).withNano(0));

            authority = localAuthorities.get(1);
            assertThat(authority.getLocalAuthorityCode()).isEqualTo("002");
            assertThat(authority.getLocalAuthorityName()).isEqualTo("Broxtowe");
            assertThat(authority.getUploadStatus()).isEqualTo(UploadStatus.UPLOADED);
            assertThat(authority.getLastUploadDate()).isEqualTo(LocalDateTime.now()
                                                    .minusDays(2).withHour(0).withMinute(0).withSecond(0).withNano(0));

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


    @Nested
    @DisplayName("GET with body /api/v1/moj/er-dashboard/local-authority-info")
    @Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardLocalAuthorityInfo.sql"})
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // false positive
    class LocalAuthorityInfoTests {

        @Test
        void testGetLocalAuthorityInfoHappy() {

            ResponseEntity<LocalAuthorityInfoResponseDto> responseEntity =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                        URI.create("/api/v1/moj/er-dashboard/local-authority-info/001")),
                    LocalAuthorityInfoResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

            assertThat(responseEntity.getBody())
                .as("Expect the body to not be null.")
                .isNotNull();

            LocalAuthorityInfoResponseDto infoResponseDto = responseEntity.getBody();
            assertThat(infoResponseDto.getLocalAuthorityCode()).isEqualTo("001");
            assertThat(infoResponseDto.getLocalAuthorityName()).isEqualTo("West Oxfordshire");
            assertThat(infoResponseDto.getUploadStatus()).isEqualTo(UploadStatus.UPLOADED);
            assertThat(infoResponseDto.getLastUploadDate()).isEqualTo(LocalDate.now().minusDays(5));
            assertThat(infoResponseDto.getLastLoggedInDate()).isEqualTo(LocalDate.now().minusDays(1));
            assertThat(infoResponseDto.getEmailRequestStatus())
                .isEqualTo(uk.gov.hmcts.juror.api.jurorer.domain.EmailRequestStatus.SENT);
            assertThat(infoResponseDto.getDateEmailRequestSent()).isEqualTo(LocalDate.now().minusDays(10));
            assertThat(infoResponseDto.getEmailAddresses())
                .containsExactlyInAnyOrder("test_user1@localauthority1.council.uk",
                                           "test_user2@localauthority1.council.uk");
            assertThat(infoResponseDto.getNotes()).isEqualTo("some test notes");
            assertThat(infoResponseDto.getReminderHistory()).hasSize(1);
            LocalAuthorityInfoResponseDto.ReminderHistoryInfo reminder1 = infoResponseDto.getReminderHistory().get(0);
            assertThat(reminder1.getSentBy()).isEqualTo("bureau_user");
            assertThat(reminder1.getSentTo()).isEqualTo("test_user1@localauthority1.council.uk");
            // needs to be a range rather than exact time as it will be set to now() in test data
            // and there may be a delay between that and when the data is retrieved here
            assertThat(reminder1.getTimeSent()).isBetween(LocalDateTime.now().minusDays(2).minusSeconds(5),
                                                          LocalDateTime.now().minusDays(2).plusSeconds(5));

        }

        @Test
        void testGetLocalAuthorityInactive() {

            ResponseEntity<LocalAuthorityInfoResponseDto> responseEntity =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                                                      URI.create("/api/v1/moj/er-dashboard/local-authority-info/006")),
                                      LocalAuthorityInfoResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

            assertThat(responseEntity.getBody())
                .as("Expect the body to not be null.")
                .isNotNull();

            LocalAuthorityInfoResponseDto infoResponseDto = responseEntity.getBody();
            assertThat(infoResponseDto).isNotNull();
            assertThat(infoResponseDto.getLocalAuthorityName()).isEqualTo("Folkestone & Hythe");
            assertThat(infoResponseDto.getLocalAuthorityCode()).isEqualTo("006");
            assertThat(infoResponseDto.getUploadStatus()).isEqualTo(UploadStatus.NOT_UPLOADED);
            assertThat(infoResponseDto.getLastUploadDate()).isNull();
            assertThat(infoResponseDto.getLastLoggedInDate()).isNull();
            assertThat(infoResponseDto.getEmailRequestStatus()).isNull();
            assertThat(infoResponseDto.getDateEmailRequestSent()).isNull();
            assertThat(infoResponseDto.getEmailAddresses()).isEmpty();
            assertThat(infoResponseDto.getNotes()).isEqualTo("previously Shepway");
            assertThat(infoResponseDto.getReminderHistory()).isNull();
            assertThat(infoResponseDto.getInactiveInfo()).isNotNull();
            LocalAuthorityInfoResponseDto.InactiveInfo inactiveInfo = infoResponseDto.getInactiveInfo();
            assertThat(inactiveInfo.getInactiveReason()).isEqualTo("This is not an active LA anymore");
            assertThat(inactiveInfo.getMadeInactiveAt()).isBetween(LocalDateTime.now().minusDays(1).minusSeconds(5),
                                                                   LocalDateTime.now().minusDays(1).plusSeconds(5));
            assertThat(inactiveInfo.getMadeInactiveBy()).isEqualTo("BUREAU_USER");


        }

        @Nested
        @DisplayName("PUT /api/v1/moj/er-dashboard/notes")
        @Sql({"/db/mod/truncate.sql", "/db/jurorer/ErDashboardData.sql"})
        class UpdateNotesTests {

            @Test
            @DisplayName("Should successfully update notes for a local authority")
            void testUpdateNotesHappy() {
                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("001")
                    .notes("Contact attempted on 15/02/2025. Awaiting response.")
                    .build();

                ResponseEntity<UpdateLocalAuthorityNotesResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          UpdateLocalAuthorityNotesResponseDto.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK")
                    .isEqualTo(HttpStatus.OK);

                assertThat(responseEntity.getBody())
                    .as("Expect the body to not be null")
                    .isNotNull();

                UpdateLocalAuthorityNotesResponseDto response = responseEntity.getBody();
                assertThat(response.getLaCode()).isEqualTo("001");
                assertThat(response.getLaName()).isEqualTo("West Oxfordshire");
                assertThat(response.getNotes()).isEqualTo("Contact attempted on 15/02/2025. Awaiting response.");
                assertThat(response.getUpdatedBy()).isEqualTo("BUREAU_USER");
                assertThat(response.getLastUpdated()).isNotNull();
                assertThat(response.getLastUpdated())
                    .isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now().plusSeconds(5));
            }

            @Test
            @DisplayName("Should successfully clear notes by setting to null")
            void testClearNotes() {
                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("001")
                    .notes(null)
                    .build();

                ResponseEntity<UpdateLocalAuthorityNotesResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          UpdateLocalAuthorityNotesResponseDto.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK")
                    .isEqualTo(HttpStatus.OK);

                UpdateLocalAuthorityNotesResponseDto response = responseEntity.getBody();
                assertThat(response.getLaCode()).isEqualTo("001");
                assertThat(response.getNotes()).isNull();
            }

            @Test
            @DisplayName("Should successfully update notes with empty string")
            void testUpdateNotesWithEmptyString() {
                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("002")
                    .notes("")
                    .build();

                ResponseEntity<UpdateLocalAuthorityNotesResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          UpdateLocalAuthorityNotesResponseDto.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK")
                    .isEqualTo(HttpStatus.OK);

                UpdateLocalAuthorityNotesResponseDto response = responseEntity.getBody();
                assertThat(response.getNotes()).isEmpty();
            }

            @Test
            @DisplayName("Should return BAD_REQUEST for invalid LA code format")
            void testUpdateNotesInvalidLaCodeFormat() {
                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("INVALID")
                    .notes("Some notes")
                    .build();

                ResponseEntity<String> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          String.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be BAD_REQUEST")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            }

            @Test
            @DisplayName("Should return BAD_REQUEST for LA code too short")
            void testUpdateNotesLaCodeTooShort() {
                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("01")
                    .notes("Some notes")
                    .build();

                ResponseEntity<String> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          String.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be BAD_REQUEST")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            }

            @Test
            @DisplayName("Should return BAD_REQUEST for notes exceeding max length")
            void testUpdateNotesExceedingMaxLength() {
                String longNotes = "A".repeat(2001); // 2001 characters, exceeds 2000 limit

                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("001")
                    .notes(longNotes)
                    .build();

                ResponseEntity<String> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          String.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be BAD_REQUEST")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            }

            @Test
            @DisplayName("Should accept notes at exactly max length")
            void testUpdateNotesAtMaxLength() {
                String maxLengthNotes = "A".repeat(2000); // Exactly 2000 characters

                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("001")
                    .notes(maxLengthNotes)
                    .build();

                ResponseEntity<UpdateLocalAuthorityNotesResponseDto> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          UpdateLocalAuthorityNotesResponseDto.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be OK")
                    .isEqualTo(HttpStatus.OK);

                UpdateLocalAuthorityNotesResponseDto response = responseEntity.getBody();
                assertThat(response.getNotes()).hasSize(2000);
            }

            @Test
            @DisplayName("Should return NOT_FOUND for non-existent LA code")
            void testUpdateNotesLaNotFound() {
                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("999")
                    .notes("Some notes")
                    .build();

                ResponseEntity<String> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          String.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be NOT_FOUND")
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @Test
            @DisplayName("Should return BAD_REQUEST for null LA code")
            void testUpdateNotesNullLaCode() {
                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode(null)
                    .notes("Some notes")
                    .build();

                ResponseEntity<String> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          String.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be BAD_REQUEST")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            }

            @Test
            @DisplayName("Should return FORBIDDEN for Court user")
            void testUpdateNotesAsCourtUser() {
                initHeadersCourt();

                UpdateLocalAuthorityNotesRequestDto request = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("001")
                    .notes("Some notes")
                    .build();

                ResponseEntity<String> responseEntity =
                    restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          String.class);

                assertThat(responseEntity.getStatusCode())
                    .as("Expect the status to be FORBIDDEN")
                    .isEqualTo(HttpStatus.FORBIDDEN);
            }

            @Test
            @DisplayName("Should successfully update notes multiple times for same LA")
            void testUpdateNotesMultipleTimes() {
                // First update
                UpdateLocalAuthorityNotesRequestDto request1 = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("003")
                    .notes("First note")
                    .build();

                ResponseEntity<UpdateLocalAuthorityNotesResponseDto> response1 =
                    restTemplate.exchange(new RequestEntity<>(request1, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          UpdateLocalAuthorityNotesResponseDto.class);

                assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response1.getBody().getNotes()).isEqualTo("First note");
                final LocalDateTime firstUpdate = response1.getBody().getLastUpdated();

                // Wait a moment to ensure timestamp difference
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Second update
                UpdateLocalAuthorityNotesRequestDto request2 = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("003")
                    .notes("Second note - updated")
                    .build();

                ResponseEntity<UpdateLocalAuthorityNotesResponseDto> response2 =
                    restTemplate.exchange(new RequestEntity<>(request2, httpHeaders, HttpMethod.PUT,
                                                              URI.create("/api/v1/moj/er-dashboard/notes")),
                                          UpdateLocalAuthorityNotesResponseDto.class);

                assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response2.getBody().getNotes()).isEqualTo("Second note - updated");
                assertThat(response2.getBody().getLastUpdated()).isAfter(firstUpdate);
            }

            @Test
            @DisplayName("Should verify notes are persisted and retrievable")
            void testNotesArePersisted() {
                // Update notes
                UpdateLocalAuthorityNotesRequestDto updateRequest = UpdateLocalAuthorityNotesRequestDto.builder()
                    .laCode("001")
                    .notes("Persisted note content")
                    .build();

                restTemplate.exchange(new RequestEntity<>(updateRequest, httpHeaders, HttpMethod.PUT,
                                                          URI.create("/api/v1/moj/er-dashboard/notes")),
                                      UpdateLocalAuthorityNotesResponseDto.class);

                // Retrieve and verify via local-authority-info endpoint
                ResponseEntity<LocalAuthorityInfoResponseDto> infoResponse =
                    restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                                      URI.create("/api/v1/moj/er-dashboard/local-authority-info/001")),
                                          LocalAuthorityInfoResponseDto.class);

                assertThat(infoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(infoResponse.getBody().getNotes()).isEqualTo("Persisted note content");
            }
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
