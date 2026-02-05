package uk.gov.hmcts.juror.api.jurorer.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.jurorer.JurorErJwtPayload;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.DashboardInfoDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.DeadlineDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.FileUploadRequestDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaUserDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + UploadControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // false positive
@Sql({"/db/jurorer/teardownUsers.sql", "/db/jurorer/setupUploadControllerITest.sql"})
public class UploadControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/juror-er/upload";

    private final TestRestTemplate restTemplate;
    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initHeadersLaUsers("001");
    }


    @Test
    void getDeadLineDateHappy() {
        final String URL = BASE_URL + "/deadline";

        ResponseEntity<DeadlineDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                  DeadlineDto.class);
        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        DeadlineDto deadlineDto = response.getBody();
        assertThat(deadlineDto).isNotNull();

        // Additional assertions can be added here to verify the contents of the deadlineDto

    }


    @Test
    void getDashboardInfoHappy() {
        final String URL = BASE_URL + "/dashboard";

        ResponseEntity<DashboardInfoDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                  DashboardInfoDto.class);
        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        DashboardInfoDto dashboardInfoDto = response.getBody();
        assertThat(dashboardInfoDto).isNotNull();

        // Additional assertions can be added here to verify the contents of the response

    }


    @Test
    void uploadFileHappy() {
        final String URL = BASE_URL + "/file";

        FileUploadRequestDto fileUploadRequestDto = FileUploadRequestDto.builder()
            .filename("testfile.csv")
            .fileFormat("csv")
            .fileSizeBytes(1024L)
            .build();

        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(fileUploadRequestDto, httpHeaders, POST, URI.create(URL)),
                                  String.class);
        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        String fileUploadResponse = response.getBody();
        assertThat(fileUploadResponse).isNotNull();

        // Additional assertions can be added here to verify the contents of response

    }





//    @Nested
//    @DisplayName("GET  something")
//    class DeadLineDate  {
//        private static final String URL = BASE_URL + "/001";
//
//        @DisplayName("Positive")
//        @Nested
//        class Positive {
//
//        }
//
//        @DisplayName("Negative")
//        @Nested
//        class Negative {
//
//        }
//    }

    private void initHeadersLaUsers(String laCode) {

        JurorErJwtPayload payload = JurorErJwtPayload.builder()
            .username("test_user1@localauthority1.council.uk")
            .laCode(laCode)
            .laName("Local Authority X")
            .roles(Collections.singletonList("LA_USER"))
            .build();

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintJurorErJwt(payload));
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    }
}
