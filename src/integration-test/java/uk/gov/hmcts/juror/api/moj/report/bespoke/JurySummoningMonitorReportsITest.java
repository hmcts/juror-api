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
import uk.gov.hmcts.juror.api.moj.controller.reports.request.JurySummoningMonitorReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.JurySummoningMonitorReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.BDDAssertions.within;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + JurySummoningMonitorReportsITest.URL)
@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/mod/reports/JurySummoningMonitor_typical.sql",
})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert"//False positive
)
class JurySummoningMonitorReportsITest extends AbstractControllerIntegrationTest<JurySummoningMonitorReportRequest,
    JurySummoningMonitorReportResponse> {
    public static final String URL = "/api/v1/moj/reports/jury-summoning-monitor";
    public static final String LOCAL_DATE = "LocalDate";
    public static final String STRING = "String";

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
    void viewByPoolTypical() {

        JurySummoningMonitorReportRequest payload =  JurySummoningMonitorReportRequest.builder()
            .searchBy("POOL")
            .poolNumber("415240801")
            .build();

        testBuilder()
            .payload(payload)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveTimeCreated)
            .assertEquals(JurySummoningMonitorReportResponse.builder()
                .headings(Map.of("court", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Court")
                        .dataType(STRING)
                        .value("CHESTER (415)")
                        .build(),
                    "pool_number", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Pool number")
                        .dataType(STRING)
                        .value("415240801")
                        .build(),
                    "pool_type", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Pool type")
                        .dataType(STRING)
                        .value("CROWN COURT")
                        .build(),
                    "service_start_date", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Service start date")
                        .dataType(LOCAL_DATE)
                        .value("2024-08-05")
                        .build(),
                    "report_created", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Report created")
                        .dataType(LOCAL_DATE)
                        .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build()))
                .totalJurorsNeeded(15)
                .bureauDeferralsIncluded(1)
                .bureauToSupply(14)
                .initiallySummoned(16)
                .ratio(1.14)
                .additionalSummonsIssued(10)
                .reminderLettersIssued(2)
                .totalConfirmedJurors(4)
                .deferralsRefused(1)
                .excusalsRefused(1)
                .totalUnavailable(22)
                .nonResponded(12)
                .undeliverable(1)
                .awaitingInformation(1)
                .disqualifiedPoliceCheck(1)
                .disqualifiedOther(3)
                .deferred(2)
                .postponed(1)
                .excused(3)
                .bereavement(0)
                .carer(0)
                .childcare(0)
                .cjsEmployment(0)
                .criminalRecord(0)
                .deceased(1)
                .deferredByCourt(0)
                .excusedByBureau(1)
                .financialHardship(0)
                .forces(0)
                .holiday(0)
                .ill(0)
                .languageDifficulties(0)
                .medical(0)
                .mentalHealth(1)
                .movedFromArea(0)
                .other(0)
                .personalEngagement(0)
                .recentlyServed(0)
                .religiousReasons(0)
                .student(0)
                .travellingDifficulties(0)
                .workRelated(0)
                .build());
    }


    @Test
    void viewByPoolTransferredPool() {

        JurySummoningMonitorReportRequest payload =  JurySummoningMonitorReportRequest.builder()
            .searchBy("POOL")
            .poolNumber("416240801")
            .build();

        testBuilder()
            .payload(payload)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveTimeCreated)
            .assertEquals(JurySummoningMonitorReportResponse.builder()
                .headings(Map.of("court", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Court")
                        .dataType(STRING)
                        .value("LEWES SITTING AT CHICHESTER (416)")
                        .build(),
                    "pool_number", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Pool number")
                        .dataType(STRING)
                        .value("416240801")
                        .build(),
                    "pool_type", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Pool type")
                        .dataType(STRING)
                        .value("CROWN COURT")
                        .build(),
                    "service_start_date", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Service start date")
                        .dataType(LOCAL_DATE)
                        .value("2024-08-06")
                        .build(),
                    "report_created", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Report created")
                        .dataType(LOCAL_DATE)
                        .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build()))
                .totalJurorsNeeded(10)
                .bureauDeferralsIncluded(0)
                .bureauToSupply(10)
                .initiallySummoned(10)
                .ratio(1.0)
                .additionalSummonsIssued(0)
                .reminderLettersIssued(1)
                .totalConfirmedJurors(3)
                .deferralsRefused(1)
                .excusalsRefused(1)
                .totalUnavailable(3)
                .nonResponded(1)
                .undeliverable(0)
                .awaitingInformation(0)
                .disqualifiedPoliceCheck(0)
                .disqualifiedOther(1)
                .deferred(1)
                .postponed(0)
                .excused(1)
                .bereavement(0)
                .carer(0)
                .childcare(0)
                .cjsEmployment(0)
                .criminalRecord(0)
                .deceased(0)
                .deferredByCourt(0)
                .excusedByBureau(0)
                .financialHardship(1)
                .forces(0)
                .holiday(0)
                .ill(0)
                .languageDifficulties(0)
                .medical(0)
                .mentalHealth(0)
                .movedFromArea(0)
                .other(0)
                .personalEngagement(0)
                .recentlyServed(0)
                .religiousReasons(0)
                .student(0)
                .travellingDifficulties(0)
                .workRelated(0)
                .build());
    }

    // TODO - Update the following tests with more data once service is created
    @Test
    void viewByCourts() {

        JurySummoningMonitorReportRequest payload =  JurySummoningMonitorReportRequest.builder()
            .searchBy("COURT")
            .allCourts(false)
            .courtLocCodes(List.of("415"))
            .fromDate(LocalDate.parse("2024-01-01"))
            .toDate(LocalDate.parse("2024-05-01"))
            .build();

        testBuilder()
            .payload(payload)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveTimeCreated)
            .assertEquals(JurySummoningMonitorReportResponse.builder()
                .headings(Map.of("courts", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Courts")
                        .dataType(STRING)
                        .value("CHESTER (415)")
                        .build(),
                    "date_from", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date from")
                        .dataType(LOCAL_DATE)
                        .value("2024-01-01")
                        .build(),
                    "date_to", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date to")
                        .dataType(LOCAL_DATE)
                        .value("2024-05-01")
                        .build(),
                    "report_created", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Report created")
                        .dataType(LOCAL_DATE)
                        .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build()))
                .totalJurorsNeeded(0)
                .bureauDeferralsIncluded(0)
                .bureauToSupply(0)
                .initiallySummoned(0)
                .ratio(0.0)
                .additionalSummonsIssued(0)
                .reminderLettersIssued(0)
                .totalConfirmedJurors(0)
                .deferralsRefused(0)
                .excusalsRefused(0)
                .totalUnavailable(0)
                .nonResponded(0)
                .undeliverable(0)
                .awaitingInformation(0)
                .disqualifiedPoliceCheck(0)
                .disqualifiedOther(0)
                .deferred(0)
                .postponed(0)
                .excused(0)
                .bereavement(0)
                .carer(0)
                .childcare(0)
                .cjsEmployment(0)
                .criminalRecord(0)
                .deceased(0)
                .deferredByCourt(0)
                .excusedByBureau(0)
                .financialHardship(0)
                .forces(0)
                .holiday(0)
                .ill(0)
                .languageDifficulties(0)
                .medical(0)
                .mentalHealth(0)
                .movedFromArea(0)
                .other(0)
                .personalEngagement(0)
                .recentlyServed(0)
                .religiousReasons(0)
                .student(0)
                .travellingDifficulties(0)
                .workRelated(0)
                .build());
    }

    @Test
    void viewByAllCourts() {

        JurySummoningMonitorReportRequest payload =  JurySummoningMonitorReportRequest.builder()
            .searchBy("COURT")
            .allCourts(true)
            .fromDate(LocalDate.parse("2024-01-01"))
            .toDate(LocalDate.parse("2024-05-01"))
            .build();

        testBuilder()
            .payload(payload)
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveTimeCreated)
            .assertEquals(JurySummoningMonitorReportResponse.builder()
                .headings(Map.of("courts", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Courts")
                        .dataType(STRING)
                        .value("All courts")
                        .build(),
                    "date_from", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date from")
                        .dataType(LOCAL_DATE)
                        .value("2024-01-01")
                        .build(),
                    "date_to", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Date to")
                        .dataType(LOCAL_DATE)
                        .value("2024-05-01")
                        .build(),
                    "report_created", AbstractReportResponse.DataTypeValue.builder()
                        .displayName("Report created")
                        .dataType(LOCAL_DATE)
                        .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build()))
                .totalJurorsNeeded(0)
                .bureauDeferralsIncluded(0)
                .bureauToSupply(0)
                .initiallySummoned(0)
                .ratio(0.0)
                .additionalSummonsIssued(0)
                .reminderLettersIssued(0)
                .totalConfirmedJurors(0)
                .deferralsRefused(0)
                .excusalsRefused(0)
                .totalUnavailable(0)
                .nonResponded(0)
                .undeliverable(0)
                .awaitingInformation(0)
                .disqualifiedPoliceCheck(0)
                .disqualifiedOther(0)
                .deferred(0)
                .postponed(0)
                .excused(0)
                .bereavement(0)
                .carer(0)
                .childcare(0)
                .cjsEmployment(0)
                .criminalRecord(0)
                .deceased(0)
                .deferredByCourt(0)
                .excusedByBureau(0)
                .financialHardship(0)
                .forces(0)
                .holiday(0)
                .ill(0)
                .languageDifficulties(0)
                .medical(0)
                .mentalHealth(0)
                .movedFromArea(0)
                .other(0)
                .personalEngagement(0)
                .recentlyServed(0)
                .religiousReasons(0)
                .student(0)
                .travellingDifficulties(0)
                .workRelated(0)
                .build());
    }

    @Test
    void unhappyCourtUser() {

        JurySummoningMonitorReportRequest payload =  JurySummoningMonitorReportRequest.builder()
            .searchBy("POOL")
            .poolNumber("415230701")
            .build();

        testBuilder()
            .payload(payload)
            .jwt(getCourtJwt())
            .triggerInvalid()
            .assertForbiddenResponse();
    }

    private String getCourtJwt() {
        return createJwt(
            "test_court_standard",
            "415",
            UserType.COURT,
            Set.of(),
            "415","462"
        );
    }


    public void verifyAndRemoveTimeCreated(JurySummoningMonitorReportResponse response) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getHeadings()).isNotNull();
        Assertions.assertThat(response.getHeadings().containsKey("report_created")).isTrue();

        AbstractReportResponse.DataTypeValue timeCreated =
            response.getHeadings().get("time_created");
        Assertions.assertThat(timeCreated).isNotNull();
        Assertions.assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
        Assertions.assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
        Assertions.assertThat(timeCreated.getValue()).isNotNull();
        LocalDateTime localDateTime = LocalDateTime.parse((String) timeCreated.getValue(),
            DateTimeFormatter.ISO_DATE_TIME);
        Assertions.assertThat(localDateTime).isCloseTo(LocalDateTime.now(),
            within(10, ChronoUnit.SECONDS));
        response.getHeadings().remove("time_created");
    }

}
