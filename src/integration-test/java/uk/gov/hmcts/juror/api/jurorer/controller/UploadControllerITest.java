package uk.gov.hmcts.juror.api.jurorer.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.jurorer.JurorErJwtPayload;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.DashboardInfoDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.DeadlineDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.FileUploadRequestDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.UploadHistoryDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.UploadStatusDto;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + UploadControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
@Sql({"/db/jurorer/teardownUsers.sql", "/db/jurorer/setupUploadControllerITest.sql"})
public class UploadControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/juror-er/upload";

    private final TestRestTemplate restTemplate;
    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initHeadersLaUsers("001");
    }

    @Nested
    @DisplayName("GET /deadline")
    class GetDeadlineDate {

        @Test
        @DisplayName("Should return deadline information with days remaining")
        void getDeadlineDateHappy() {
            final String URL = BASE_URL + "/deadline";

            ResponseEntity<DeadlineDto> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                      DeadlineDto.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be OK")
                .isEqualTo(HttpStatus.OK);

            DeadlineDto deadlineDto = response.getBody();
            assertThat(deadlineDto).isNotNull();

            // Verify deadline date is 6 weeks in the future (from setup SQL)
            LocalDate expectedDeadline = LocalDate.now().plusWeeks(6);
            assertThat(deadlineDto.getDeadlineDate())
                .as("Deadline date should be 6 weeks from now")
                .isEqualTo(expectedDeadline);

            // Verify days remaining calculation
            long expectedDaysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), expectedDeadline);
            assertThat(deadlineDto.getDaysRemaining())
                .as("Days remaining should match expected calculation")
                .isEqualTo(expectedDaysRemaining);

            assertThat(deadlineDto.isDeadlinePassed())
                .as("Deadline should not have passed")
                .isFalse();

            // Verify upload start date is 6 weeks in the past (from setup SQL)
            LocalDate expectedStartDate = LocalDate.now().minusWeeks(6);
            assertThat(deadlineDto.getUploadStartDate())
                .as("Upload start date should be 6 weeks ago")
                .isEqualTo(expectedStartDate);
        }
    }

    @Nested
    @DisplayName("GET /dashboard")
    class GetDashboardInfo {

        @Test
        @DisplayName("Should return dashboard information for LA 001")
        void getDashboardInfoHappy() {
            final String URL = BASE_URL + "/dashboard";

            ResponseEntity<DashboardInfoDto> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                      DashboardInfoDto.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be OK")
                .isEqualTo(HttpStatus.OK);

            DashboardInfoDto dashboardInfoDto = response.getBody();
            assertThat(dashboardInfoDto).isNotNull();

            // Verify deadline date
            LocalDate expectedDeadline = LocalDate.now().plusWeeks(6);
            assertThat(dashboardInfoDto.getDeadlineDate())
                .as("Deadline date should be 6 weeks from now")
                .isEqualTo(expectedDeadline);

            // Verify days remaining
            long expectedDaysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), expectedDeadline);
            assertThat(dashboardInfoDto.getDaysRemaining())
                .as("Days remaining should match calculation")
                .isEqualTo(expectedDaysRemaining);

            // Verify upload status for LA 001 (should be NOT_UPLOADED initially)
            assertThat(dashboardInfoDto.getUploadStatus())
                .as("Upload status should be NOT_UPLOADED")
                .isEqualTo("NOT_UPLOADED");

            // Verify last upload date (LA 001 has uploads in the past)
            assertThat(dashboardInfoDto.getLastUploadDate())
                .as("Last upload date should not be null for LA 001")
                .isNotNull();
        }

        @Test
        @DisplayName("Should return correct dashboard info for LA with no uploads")
        void getDashboardInfoForLaWithNoUploads() {
            // Switch to LA 003 which has no uploads
            initHeadersLaUsers("003");
            final String URL = BASE_URL + "/dashboard";

            ResponseEntity<DashboardInfoDto> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                      DashboardInfoDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            DashboardInfoDto dashboardInfoDto = response.getBody();
            assertThat(dashboardInfoDto).isNotNull();
            assertThat(dashboardInfoDto.getUploadStatus()).isEqualTo("NOT_UPLOADED");
            assertThat(dashboardInfoDto.getLastUploadDate()).isNull();
        }
    }

    @Nested
    @DisplayName("POST /file")
    class UploadFile {

        @Test
        @DisplayName("Should successfully upload file and update LA status to UPLOADED")
        void uploadFileHappy() {
            final String URL = BASE_URL + "/file";

            FileUploadRequestDto fileUploadRequestDto = FileUploadRequestDto.builder()
                .filename("testfile.csv")
                .fileFormat("CSV")
                .fileSizeBytes(1024L)
                .otherInformation("Test upload from integration test")
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(fileUploadRequestDto, httpHeaders, POST, URI.create(URL)),
                                      String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be CREATED")
                .isEqualTo(HttpStatus.CREATED);

            // Verify that LA status was updated to UPLOADED
            ResponseEntity<UploadStatusDto> statusResponse =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(BASE_URL + "/status")),
                                      UploadStatusDto.class);

            assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            UploadStatusDto statusDto = statusResponse.getBody();
            assertThat(statusDto).isNotNull();
            assertThat(statusDto.getUploadStatus())
                .as("Upload status should be UPLOADED after file upload")
                .isEqualTo("UPLOADED");
            assertThat(statusDto.getLaCode()).isEqualTo("001");
            assertThat(statusDto.getLaName()).isEqualTo("West Oxfordshire");
        }

        @Test
        @DisplayName("Should reject upload with invalid file format")
        void uploadFileWithInvalidFormat() {
            final String URL = BASE_URL + "/file";

            FileUploadRequestDto fileUploadRequestDto = FileUploadRequestDto.builder()
                .filename("testfile.exe")
                .fileFormat("") // Invalid format
                .fileSizeBytes(1024L)
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(fileUploadRequestDto, httpHeaders, POST, URI.create(URL)),
                                      String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be BAD_REQUEST for invalid format")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should reject upload with missing filename")
        void uploadFileWithMissingFilename() {
            final String URL = BASE_URL + "/file";

            FileUploadRequestDto fileUploadRequestDto = FileUploadRequestDto.builder()
                .fileFormat("CSV")
                .fileSizeBytes(1024L)
                .build();

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(fileUploadRequestDto, httpHeaders, POST, URI.create(URL)),
                                      String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be BAD_REQUEST for missing filename")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /upload-history")
    class GetUploadHistory {

        @Test
        @DisplayName("Should return upload history for LA 001 with recent uploads")
        void getUploadHistoryForLaWithUploads() {
            final String URL = BASE_URL + "/upload-history";

            ResponseEntity<UploadHistoryDto> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                      UploadHistoryDto.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be OK")
                .isEqualTo(HttpStatus.OK);

            UploadHistoryDto historyDto = response.getBody();
            assertThat(historyDto).isNotNull();

            // LA 001 has 5 uploads in the setup SQL
            assertThat(historyDto.getTotalUploads())
                .as("Total uploads for LA 001 should be 4")
                .isEqualTo(5L);

            assertThat(historyDto.getRecentUploads())
                .as("Recent uploads list should not be empty")
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(10);

            // Verify first upload is the most recent (sorted by upload_date DESC)
            assertThat(historyDto.getRecentUploads().get(0).getUploadDate())
                .as("First upload should be the most recent")
                .isAfterOrEqualTo(historyDto.getRecentUploads().get(1).getUploadDate());

            // Verify upload details
            assertThat(historyDto.getRecentUploads().get(0).getUploadedBy())
                .as("Uploaded by should match test user")
                .isEqualTo("test_user1@la1.uk");
        }

        @Test
        @DisplayName("Should return empty upload history for LA with no uploads")
        void getUploadHistoryForLaWithNoUploads() {
            // Switch to LA 003 which has no uploads
            initHeadersLaUsers("003");
            final String URL = BASE_URL + "/upload-history";

            ResponseEntity<UploadHistoryDto> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                      UploadHistoryDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            UploadHistoryDto historyDto = response.getBody();
            assertThat(historyDto).isNotNull();
            assertThat(historyDto.getTotalUploads())
                .as("Total uploads should be 0 for LA 003")
                .isEqualTo(0L);
            assertThat(historyDto.getRecentUploads())
                .as("Recent uploads list should be empty")
                .isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /status")
    class GetUploadStatus {

        @Test
        @DisplayName("Should return upload status for authenticated user's LA")
        void getUploadStatusForCurrentUser() {
            final String URL = BASE_URL + "/status";

            ResponseEntity<UploadStatusDto> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                      UploadStatusDto.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be OK")
                .isEqualTo(HttpStatus.OK);

            UploadStatusDto statusDto = response.getBody();
            assertThat(statusDto).isNotNull();
            assertThat(statusDto.getLaCode()).isEqualTo("001");
            assertThat(statusDto.getLaName()).isEqualTo("West Oxfordshire");
            assertThat(statusDto.getIsActive()).isTrue();
            assertThat(statusDto.getUploadStatus()).isEqualTo("NOT_UPLOADED");
        }
    }

    @Nested
    @DisplayName("GET /status/{la_code}")
    class GetUploadStatusByLaCode {

        @Test
        @DisplayName("Should return upload status for specified LA code")
        void getUploadStatusByLaCodeHappy() {
            final String URL = BASE_URL + "/status/001";

            ResponseEntity<UploadStatusDto> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                      UploadStatusDto.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be OK")
                .isEqualTo(HttpStatus.OK);

            UploadStatusDto statusDto = response.getBody();
            assertThat(statusDto).isNotNull();
            assertThat(statusDto.getLaCode()).isEqualTo("001");
            assertThat(statusDto.getLaName()).isEqualTo("West Oxfordshire");
            assertThat(statusDto.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should return FORBIDDEN when accessing different LA code")
        void getUploadStatusByLaCodeForbidden() {
            // User is authenticated for LA 001, trying to access LA 002
            final String URL = BASE_URL + "/status/002";

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                      String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be FORBIDDEN")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Should return BAD_REQUEST for invalid LA code format")
        void getUploadStatusByInvalidLaCodeFormat() {
            final String URL = BASE_URL + "/status/INVALID";

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                      String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be BAD_REQUEST for invalid format")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void initHeadersLaUsers(String laCode) {
        String username = "test_user1@la" + laCode.replaceFirst("^0+", "") + ".council.uk";

        JurorErJwtPayload payload = JurorErJwtPayload.builder()
            .username(username)
            .laCode(laCode)
            .laName("Local Authority " + laCode)
            .roles(Collections.singletonList("LA_USER"))
            .build();

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintJurorErJwt(payload));
        httpHeaders.setAccept(Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
    }
}
