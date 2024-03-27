package uk.gov.hmcts.juror.api.moj.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + AbstractStandardReportControllerITest.BASE_URL)
public abstract class AbstractStandardReportControllerITest
    extends AbstractReportControllerITest<StandardReportResponse> {
    public AbstractStandardReportControllerITest(TestRestTemplate template,
                                                 Class<? extends AbstractStandardReport> reportClass) {
        super(template, reportClass, StandardReportResponse.class);
    }
}
