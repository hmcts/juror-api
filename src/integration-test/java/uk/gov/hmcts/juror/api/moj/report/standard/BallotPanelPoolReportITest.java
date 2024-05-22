package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;

public class BallotPanelPoolReportITest  extends AbstractStandardReportControllerITest {
    @Autowired
    public BallotPanelPoolReportITest(TestRestTemplate template) {
        super(template, BallotPanelPoolReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
                                 .poolNumber("415230103")
                                 .build());
    }
    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(null);
    }
}
