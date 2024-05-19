package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportControllerITest;

import java.util.Set;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/JurorAmendmentReportITest.sql"
})
public abstract class AbstractJurorAmendmentReportITest
    extends AbstractReportControllerITest<JurorAmendmentReportResponse> {

    public AbstractJurorAmendmentReportITest(TestRestTemplate template,
                                             Class<? extends AbstractJurorAmendmentReport> reportClass) {
        super(template, reportClass, JurorAmendmentReportResponse.class);
    }

    @Override
    protected String getValidJwt() {
        return createJwt("test_court_standard",
            "469",
            UserType.COURT, Set.of(Role.SENIOR_JUROR_OFFICER),
            "469");
    }

    @Test
    void negativeNotSJO() {
        testBuilder()
            .jwt(createJwt("test_court_standard",
                "469",
                UserType.COURT, Set.of(),
                "469"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User does not have access");
    }
}
