package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.assertj.core.api.Assertions;
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
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.CourtsAndDatesReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.SittingDaysStatsReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + SittingDaysReportsITest.URL)
@Sql(
    scripts = {
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/mod/reports/SittingDaysStatsReportITest_typical.sql"
    })
class SittingDaysReportsITest extends AbstractControllerIntegrationTest<CourtsAndDatesReportRequest,
    SittingDaysStatsReportResponse> {

    public static final String URL = "/api/v1/moj/reports/sitting-days-stats";


    @Autowired
    SittingDaysReportsITest(TestRestTemplate template) {
        super(HttpMethod.POST, template, HttpStatus.OK);

    }

    @Override
    protected String getValidUrl() {
        return URL;
    }

    @Override
    protected String getValidJwt() {

        final BureauJwtPayload bureauJwtPayload = TestUtils.getJwtPayloadSuperUser("400", "Bureau");
        return mintBureauJwt(bureauJwtPayload);
    }

    private String createJwtWithPermissions(UserType userType, Set<Permission> permissions) {
        var payload = createBureauJwtPayload(
            "test_administrator",
            userType,
            Set.of(),
            "415",
            "415"
        );
        payload.setPermissions(permissions);
        return mintBureauJwt(payload);
    }

    @Override
    protected CourtsAndDatesReportRequest getValidPayload() {
        return CourtsAndDatesReportRequest.builder()
            .allCourts(false)
            .courtLocCodes(List.of("415"))
            .fromDate(LocalDate.of(2024, 5, 1))
            .toDate(LocalDate.of(2024, 5, 31))
            .build();
    }

    @Test
    void viewSittingDaysStatsHappy() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::assertValidResponse);
    }

    @Test
    void viewSittingDaysStatsInvalidUserType() {
        testBuilder()
            .jwt(createJwtWithPermissions(UserType.BUREAU, Set.of()))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    private void assertValidResponse(SittingDaysStatsReportResponse response) {
        assertThat(response).isNotNull();
        assertThat(response.getHeadings()).isNotNull();
        Assertions.assertThat(response.getHeadings()).containsKeys("date_from", "date_to", "total_sitting_days",
            "report_created");
        Assertions.assertThat(response.getHeadings().get("date_from").getValue()).isEqualTo("2024-05-01");
        Assertions.assertThat(response.getHeadings().get("date_to").getValue()).isEqualTo("2024-05-31");
        Assertions.assertThat(response.getHeadings().get("total_sitting_days").getValue()).isEqualTo(192);
        Assertions.assertThat(response.getTableData()).isNotNull();
        Assertions.assertThat(response.getTableData().getHeadings()).hasSize(15);
        Assertions.assertThat(response.getTableData().getData()).hasSize(1);

        SittingDaysStatsReportResponse.TableData.DataRow dataRow = response.getTableData().getData().get(0);
        assertThat(dataRow.getCourtLocationNameAndCode()).isEqualTo("CHESTER (415)");
        assertThat(dataRow.getZeroSittingDays()).isEqualTo(0);
        assertThat(dataRow.getOneSittingDay()).isEqualTo(2);
        assertThat(dataRow.getTwoSittingDays()).isEqualTo(3);
        assertThat(dataRow.getThreeSittingDays()).isEqualTo(50);
        assertThat(dataRow.getFourSittingDays()).isEqualTo(100);
        assertThat(dataRow.getFiveSittingDays()).isEqualTo(5);
        assertThat(dataRow.getSixSittingDays()).isEqualTo(5);
        assertThat(dataRow.getSevenSittingDays()).isEqualTo(5);
        assertThat(dataRow.getEightSittingDays()).isEqualTo(3);
        assertThat(dataRow.getNineSittingDays()).isEqualTo(2);
        assertThat(dataRow.getTenSittingDays()).isEqualTo(6);
        assertThat(dataRow.getElevenOrMoreSittingDays()).isEqualTo(11);
        assertThat(dataRow.getTotalJurors()).isEqualTo(183);
        assertThat(dataRow.getTotalSittingDays()).isEqualTo(192);

    }
}
