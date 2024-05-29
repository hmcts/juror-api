package uk.gov.hmcts.juror.api.moj.report.standard;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/PoolAnalysisReportITest_Typical.sql"
})
public class PoolAnalysisReportITest extends AbstractStandardReportControllerITest {

    public PoolAnalysisReportITest(TestRestTemplate template) {
        super(template, PoolAnalysisReport.class);
    }

    @Override
    protected String getValidJwt() {
        return null;
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return null;
    }
}
