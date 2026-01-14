package uk.gov.hmcts.juror.api.moj.report.bespoke;

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
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DigitalSummonsRepliesReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.ResponsesCompletedReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Summons replies Reports Integration Tests at " + SummonsRepliesReportsITest.URL_BASE)
class SummonsRepliesReportsITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    public static final String URL_BASE = "/api/v1/moj/reports";

    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initHeaders();
    }

    private void initHeaders() {
        String bureauJwt = createJwt(
            "test_Bureau_standard",
            "400",
            UserType.BUREAU,
            Set.of(),
            "400"
        );

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    @Nested
    @DisplayName("Get Digital Summons Replies Report Integration Tests")
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/mod/reports/DigitalSummonsRepliesReportsITest_typical.sql"
    })
    class GetDigitalSummonsRepliesReportTests {

        @Test
        void digitalSummonsRepliesReportsHappyAug2025() {

            ResponseEntity<DigitalSummonsRepliesReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(URL_BASE + "/digital-summons-replies-report/2025-08-01")),
                                      DigitalSummonsRepliesReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            DigitalSummonsRepliesReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            assertThat(responseBody.getHeadings()).isNotNull();
            assertThat(responseBody.getHeadings().size()).isEqualTo(3);

            assertThat(responseBody.getTableData()).isNotNull();
            assertThat(responseBody.getTableData().getHeadings()).isNotNull();
            assertThat(responseBody.getTableData().getHeadings().size()).isEqualTo(2);
            assertThat(responseBody.getTableData().getData()).isNotNull();
            assertThat(responseBody.getTableData().getData().size()).isEqualTo(7);

            // Data rows should be in date order ascending
            DigitalSummonsRepliesReportResponse.TableData.DataRow row = responseBody.getTableData().getData().get(0);
            assertThat(row.getDate().toString()).isEqualTo("2025-08-04");
            assertThat(row.getNoOfReplies()).isEqualTo(2);
            row = responseBody.getTableData().getData().get(1);
            assertThat(row.getDate().toString()).isEqualTo("2025-08-05");
            assertThat(row.getNoOfReplies()).isEqualTo(1);
            row = responseBody.getTableData().getData().get(2);
            assertThat(row.getDate().toString()).isEqualTo("2025-08-06");
            assertThat(row.getNoOfReplies()).isEqualTo(1);
            row = responseBody.getTableData().getData().get(3);
            assertThat(row.getDate().toString()).isEqualTo("2025-08-09");
            assertThat(row.getNoOfReplies()).isEqualTo(1);
            row = responseBody.getTableData().getData().get(4);
            assertThat(row.getDate().toString()).isEqualTo("2025-08-10");
            assertThat(row.getNoOfReplies()).isEqualTo(4);
            row = responseBody.getTableData().getData().get(5);
            assertThat(row.getDate().toString()).isEqualTo("2025-08-11");
            assertThat(row.getNoOfReplies()).isEqualTo(3);
            row = responseBody.getTableData().getData().get(6);
            assertThat(row.getDate().toString()).isEqualTo("2025-08-20");
            assertThat(row.getNoOfReplies()).isEqualTo(2);
        }

        @Test
        void digitalSummonsRepliesReportsHappySep2025() {

            ResponseEntity<DigitalSummonsRepliesReportResponse> responseEntity =
                    restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                                    URI.create(URL_BASE + "/digital-summons-replies-report/2025-09-01")),
                            DigitalSummonsRepliesReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            DigitalSummonsRepliesReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            assertThat(responseBody.getHeadings()).isNotNull();
            assertThat(responseBody.getHeadings().size()).isEqualTo(3);

            assertThat(responseBody.getTableData()).isNotNull();
            assertThat(responseBody.getTableData().getHeadings()).isNotNull();
            assertThat(responseBody.getTableData().getHeadings().size()).isEqualTo(2);
            assertThat(responseBody.getTableData().getData()).isNotNull();
            assertThat(responseBody.getTableData().getData().size()).isEqualTo(1);

            DigitalSummonsRepliesReportResponse.TableData.DataRow row = responseBody.getTableData().getData().get(0);
            assertThat(row.getDate().toString()).isEqualTo("2025-09-01");
            assertThat(row.getNoOfReplies()).isEqualTo(1);
        }


        @Test
        void digitalSummonsRepliesReportsNoData() {

            ResponseEntity<DigitalSummonsRepliesReportResponse> responseEntity =
                    restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                                    URI.create(URL_BASE + "/digital-summons-replies-report/2025-10-01")),
                            DigitalSummonsRepliesReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            DigitalSummonsRepliesReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            assertThat(responseBody.getHeadings()).isNotNull();
            assertThat(responseBody.getHeadings().size()).isEqualTo(3);

            assertThat(responseBody.getTableData()).isNotNull();
            assertThat(responseBody.getTableData().getHeadings()).isNotNull();
            assertThat(responseBody.getTableData().getHeadings().size()).isEqualTo(2);
            assertThat(responseBody.getTableData().getData()).isNotNull();
            assertThat(responseBody.getTableData().getData().size()).isEqualTo(0);

        }


        @Test
        void digitalSummonsRepliesReportsInvalidDate() {

            ResponseEntity<DigitalSummonsRepliesReportResponse> responseEntity =
                    restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                                    URI.create(URL_BASE + "/digital-summons-replies-report/20sdf")),
                    DigitalSummonsRepliesReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP Bad Request response")
                    .isEqualTo(HttpStatus.BAD_REQUEST);

        }


        @Test
        void digitalSummonsRepliesReportsInvalidUserType() {

            final String courtJwt = createCourtJwt();
            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

            ResponseEntity<DigitalSummonsRepliesReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(URL_BASE + "/digital-summons-replies-report/2025-09-01")),
                                      DigitalSummonsRepliesReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP FORBIDDEN response")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }

        private String createCourtJwt() {
            return createJwt(
                "test_court_standard",
                "415",
                UserType.COURT,
                Set.of(),
                "415"
            );
        }

    }

    @Nested
    @DisplayName("Get responses completed for month Report Integration Tests")
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/mod/reports/DigitalSummonsRepliesReportsITest_typical.sql"
    })
    class GetResponsesCompletedReportTests {

        @Test
        void responsesCompletedReportsHappy() {

            final String courtJwt = createBureauManagerJwt();
            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

            ResponseEntity<ResponsesCompletedReportResponse> responseEntity =
                restTemplate.exchange(
                    new RequestEntity<Void>(
                        httpHeaders, HttpMethod.GET,
                        URI.create(URL_BASE + "/responses-completed/2025-08-01")
                    ),
                    ResponsesCompletedReportResponse.class
                );

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            ResponsesCompletedReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            //            assertThat(responseBody.getHeadings()).isNotNull();
            //            assertThat(responseBody.getHeadings().size()).isEqualTo(3);
            //
            //            assertThat(responseBody.getTableData()).isNotNull();
            //            assertThat(responseBody.getTableData().getHeadings()).isNotNull();
            //            assertThat(responseBody.getTableData().getHeadings().size()).isEqualTo(2);
            //            assertThat(responseBody.getTableData().getData()).isNotNull();
            //            assertThat(responseBody.getTableData().getData().size()).isEqualTo(7);

        }

        private String createBureauManagerJwt() {
            return createJwt(
                "bureau_manager",
                "400",
                UserType.BUREAU,
                Set.of(Role.MANAGER),
                "400"
            );
        }
    }

}
