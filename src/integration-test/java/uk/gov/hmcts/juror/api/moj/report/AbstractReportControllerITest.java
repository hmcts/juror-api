package uk.gov.hmcts.juror.api.moj.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractControllerIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.service.report.IReport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + AbstractReportControllerITest.BASE_URL)
public abstract class AbstractReportControllerITest<R extends AbstractReportResponse<?>>
    extends AbstractControllerIntegrationTest<StandardReportRequest, R> {
    public static final String BASE_URL = "/api/v1/moj/reports/standard";
    private final String reportType;

    public AbstractReportControllerITest(TestRestTemplate template, Class<? extends IReport> reportClass,
                                         Class<R> responseClass) {
        super(HttpMethod.POST, template, HttpStatus.OK, responseClass);
        this.reportType = reportClass.getSimpleName();
    }

    @Override
    protected final String getValidUrl() {
        return BASE_URL;
    }

    protected StandardReportRequest addReportType(StandardReportRequest request) {
        request.setReportType(reportType);
        return request;
    }

    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")//False positive
    public void verifyAndRemoveReportCreated(R response) {
        assertThat(response).isNotNull();
        assertThat(response.getHeadings()).isNotNull();
        assertThat(response.getHeadings().containsKey("report_created")).isTrue();

        AbstractReportResponse.DataTypeValue reportCreated =
            response.getHeadings().get("report_created");
        assertThat(reportCreated).isNotNull();
        assertThat(reportCreated.getDisplayName()).isNull();
        assertThat(reportCreated.getDataType()).isEqualTo("LocalDateTime");
        assertThat(reportCreated.getValue()).isNotNull();
        LocalDateTime localDateTime = LocalDateTime.parse((String) reportCreated.getValue(),
            DateTimeFormatter.ISO_DATE_TIME);
        assertThat(localDateTime).isCloseTo(LocalDateTime.now(),
            within(10, ChronoUnit.SECONDS));
        response.getHeadings().remove("report_created");
    }
}
