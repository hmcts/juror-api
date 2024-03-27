package uk.gov.hmcts.juror.api.moj.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + AbstractGroupedReportControllerITest.BASE_URL)
public abstract class AbstractGroupedReportControllerITest
    extends AbstractReportControllerITest<GroupedReportResponse> {
    public AbstractGroupedReportControllerITest(TestRestTemplate template,
                                                Class<? extends AbstractGroupedReport> reportClass) {
        super(template, reportClass,GroupedReportResponse.class);
    }
}
