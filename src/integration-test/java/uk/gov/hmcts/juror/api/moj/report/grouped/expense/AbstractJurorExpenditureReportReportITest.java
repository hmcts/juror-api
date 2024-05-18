package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportControllerITest;

import java.time.LocalDate;

public abstract class AbstractJurorExpenditureReportReportITest extends AbstractGroupedReportControllerITest {

    protected static final LocalDate DEFAULT_FROM_DATE = LocalDate.of(2024, 5, 1);
    protected static final LocalDate DEFAULT_TO_DATE = LocalDate.of(2024, 5, 27);


    public AbstractJurorExpenditureReportReportITest(
        TestRestTemplate template,
        Class<? extends AbstractJurorExpenditureReport> reportClass) {
        super(template, reportClass);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .fromDate(DEFAULT_FROM_DATE)
            .toDate(DEFAULT_TO_DATE)
            .build());
    }


    @Test
    void negativeTypicalIsBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }
}
