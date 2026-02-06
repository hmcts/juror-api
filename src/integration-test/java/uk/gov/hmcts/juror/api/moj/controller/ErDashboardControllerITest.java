package uk.gov.hmcts.juror.api.moj.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the ER Dashboard controller.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Sql({"/db/mod/truncate.sql"})
class ErDashboardControllerITest extends AbstractIntegrationTest {

    private final TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initHeadersBureau();
    }

    @Test
    @Sql({"/db/jurorer/createLocalAuthorities.sql"})
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
    @Sql({"/db/jurorer/createLocalAuthorities.sql"})
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

    @Test
    @Sql({"/db/jurorer/ErDashboardData.sql"})
    void testGetDashboardStatsHappy() {

        ResponseEntity<ErDashboardStatsResponseDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/er-dashboard/upload-stats")), ErDashboardStatsResponseDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the status to be OK.")
            .isEqualTo(HttpStatus.OK);

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

    @Test
    @Sql({"/db/jurorer/ErDashboardData.sql"})
    void testGetUploadStatusNoFiltersHappy() {

        ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder().build();

        ResponseEntity<ErLocalAuthorityStatusResponseDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                                    URI.create("/api/v1/moj/er-dashboard/local-authority-status")),
                                  ErLocalAuthorityStatusResponseDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the status to be OK.")
            .isEqualTo(HttpStatus.OK);

    }

    @Test
    @Sql({"/db/jurorer/ErDashboardData.sql"})
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

    }

    @Test
    @Sql({"/db/jurorer/ErDashboardData.sql"})
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

    }

    @Test
    @Sql({"/db/jurorer/ErDashboardData.sql"})
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

    }

    @Test
    @Sql({"/db/jurorer/ErDashboardData.sql"})
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
