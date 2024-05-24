package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractControllerIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.JurySummoningMonitorReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.JurySummoningMonitorReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + JurySummoningMonitorReportsITest.URL)
@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql"
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert"//False positive
)
class JurySummoningMonitorReportsITest extends AbstractControllerIntegrationTest<JurySummoningMonitorReportRequest,
    JurySummoningMonitorReportResponse> {
    public static final String URL = "/api/v1/moj/reports/jury-summoning-monitor";

    @Autowired
    public JurySummoningMonitorReportsITest(TestRestTemplate template) {
        super(HttpMethod.POST, template, HttpStatus.OK);
    }

    @Override
    protected String getValidUrl() {
        return URL;
    }

    @Override
    protected String getValidJwt() {
        return createJwt(
            "test_bureau_standard",
            "400",
            UserType.BUREAU,
            Set.of(),
            "400"
        );
    }

    @Override
    protected JurySummoningMonitorReportRequest getValidPayload() {
        return null;
    }

    @Test
    void viewByPool() {

        JurySummoningMonitorReportRequest payload =  JurySummoningMonitorReportRequest.builder()
            .searchBy("POOL")
            .poolNumber("123456789")
            .build();

        testBuilder()
            .payload(payload)
            .triggerValid()
            .assertEquals(JurySummoningMonitorReportResponse.builder()
                .totalJurorsNeeded(0)
                .build());
    }

}
