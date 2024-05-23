package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportJurorsResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.MonthlyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService.TableHeading.ATTENDANCE_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService.TableHeading.DATE;
import static uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService.TableHeading.JUROR;
import static uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService.TableHeading.JUROR_WORKING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService.TableHeading.MONTH;
import static uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService.TableHeading.NON_ATTENDANCE_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService.TableHeading.SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService.TableHeading.UTILISATION;

@SuppressWarnings({
    "PMD.AssertionsShouldIncludeMessage",
    "PMD.UnnecessaryFullyQualifiedName"
})
class UtilisationReportServiceImplTest {
    private final CourtLocationRepository courtLocationRepository;
    private final JurorRepository jurorRepository;
    private final UtilisationStatsRepository utilisationStatsRepository;
    private final UtilisationReportService utilisationReportService;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public UtilisationReportServiceImplTest() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        this.jurorRepository = mock(JurorRepository.class);
        this.utilisationStatsRepository = mock(UtilisationStatsRepository.class);
        this.utilisationReportService = new UtilisationReportServiceImpl(courtLocationRepository, jurorRepository,
            utilisationStatsRepository);
    }

    @BeforeEach
    void beforeEach() {
        TestUtils.setUpMockAuthentication("415", "TEST_USER", "1", List.of("415"));
    }

    @AfterEach
    void afterEach() {
        if (securityUtilMockedStatic != null) {
            securityUtilMockedStatic.close();
        }
    }

    @Nested
    @DisplayName("Daily Utilisation report tests")
    class DailyUtilisationTests {

        @Test
        @SneakyThrows
        void viewDailyUtilisationReportNoResultsAndValidHeadings() {

            final String locCode = "415";
            final LocalDate reportFromDate = LocalDate.of(2024, 4, 20);
            final LocalDate reportToDate = LocalDate.of(2024, 5, 13);

            setupCourt(locCode, "415", locCode);

            when(jurorRepository.callDailyUtilStats(locCode, reportFromDate, reportToDate))
                .thenReturn(List.of());

            DailyUtilisationReportResponse response = utilisationReportService.viewDailyUtilisationReport(locCode,
                reportFromDate,
                reportToDate);

            assertThat(response.getHeadings()).isNotNull();
            Map<String, AbstractReportResponse.DataTypeValue> headings = response.getHeadings();

            validateReportHeadings(headings);

            AbstractReportResponse.DataTypeValue timeCreated = headings.get("time_created");
            assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
            assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
            LocalDateTime createdTime = LocalDateTime.parse((String) timeCreated.getValue(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            assertThat(createdTime).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));

            assertThat(response.getTableData()).isNotNull();
            DailyUtilisationReportResponse.TableData tableData = response.getTableData();
            assertThat(tableData.getHeadings()).isNotNull();
            Assertions.assertThat(tableData.getHeadings()).hasSize(6);

            validateTableHeadings(tableData);

            Assertions.assertThat(tableData.getWeeks()).isEmpty();
            assertThat(tableData.getOverallTotalJurorWorkingDays()).isZero();
            assertThat(tableData.getOverallTotalSittingDays()).isZero();
            assertThat(tableData.getOverallTotalAttendanceDays()).isZero();
            assertThat(tableData.getOverallTotalNonAttendanceDays()).isZero();
            assertThat(tableData.getOverallTotalUtilisation()).isZero();

            verify(courtLocationRepository, times(1)).findById(locCode);
            verify(jurorRepository, times(1)).callDailyUtilStats(locCode, reportFromDate, reportToDate);

        }

        private void validateTableHeadings(DailyUtilisationReportResponse.TableData tableData) {
            DailyUtilisationReportResponse.TableData.Heading tablHeading = tableData.getHeadings().get(0);
            assertThat(tablHeading.getId()).isEqualTo(DATE);
            assertThat(tablHeading.getName()).isEqualTo("Date");
            assertThat(tablHeading.getDataType()).isEqualTo("LocalDate");

            DailyUtilisationReportResponse.TableData.Heading tablHeading1 = tableData.getHeadings().get(1);
            assertThat(tablHeading1.getId()).isEqualTo(JUROR_WORKING_DAYS);
            assertThat(tablHeading1.getName()).isEqualTo("Juror working days");
            assertThat(tablHeading1.getDataType()).isEqualTo("Integer");

            DailyUtilisationReportResponse.TableData.Heading tablHeading2 = tableData.getHeadings().get(2);
            assertThat(tablHeading2.getId()).isEqualTo(SITTING_DAYS);
            assertThat(tablHeading2.getName()).isEqualTo("Sitting days");
            assertThat(tablHeading2.getDataType()).isEqualTo("Integer");

            DailyUtilisationReportResponse.TableData.Heading tablHeading3 = tableData.getHeadings().get(3);
            assertThat(tablHeading3.getId()).isEqualTo(ATTENDANCE_DAYS);
            assertThat(tablHeading3.getName()).isEqualTo("Attendance days");
            assertThat(tablHeading3.getDataType()).isEqualTo("Integer");

            DailyUtilisationReportResponse.TableData.Heading tablHeading4 = tableData.getHeadings().get(4);
            assertThat(tablHeading4.getId()).isEqualTo(NON_ATTENDANCE_DAYS);
            assertThat(tablHeading4.getName()).isEqualTo("Non-attendance days");
            assertThat(tablHeading4.getDataType()).isEqualTo("Integer");

            DailyUtilisationReportResponse.TableData.Heading tablHeading5 = tableData.getHeadings().get(5);
            assertThat(tablHeading5.getId()).isEqualTo(UTILISATION);
            assertThat(tablHeading5.getName()).isEqualTo("Utilisation");
            assertThat(tablHeading5.getDataType()).isEqualTo("Double");
        }

        private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings) {
            Assertions.assertThat(headings.get("date_from")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType("LocalDate")
                .value("2024-04-20")
                .build());
            assertThat(headings.get("date_to")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType("LocalDate")
                .value("2024-05-13")
                .build());
            assertThat(headings.get("report_created")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report created")
                .dataType("LocalDate")
                .value(LocalDate.now().toString())
                .build());
            assertThat(headings.get("court_name")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Court name")
                .dataType("String")
                .value("Test Court")
                .build());
        }

        @Test
        void viewDailyUtilisationReportInvalidCourtLocation() {

            final String locCode = "416";
            final LocalDate reportFromDate = LocalDate.of(2024, 4, 20);
            final LocalDate reportToDate = LocalDate.of(2024, 5, 13);

            setupCourt(locCode, "416", "415");

            assertThatExceptionOfType(MojException.Forbidden.class)
                .isThrownBy(() -> utilisationReportService.viewDailyUtilisationReport(locCode, reportFromDate,
                    reportToDate));

            verify(courtLocationRepository, times(1)).findById(locCode);
            verifyNoInteractions(jurorRepository);

        }
    }


    @Nested
    @DisplayName("Daily Utilisation Jurors tests")
    class DailyUtilisationJurorsTests {

        @Test
        @SneakyThrows
        void viewDailyUtilisationJurorsNoResultsAndValidHeadings() {

            final String locCode = "415";
            final LocalDate reportDate = LocalDate.of(2024, 4, 20);

            setupCourt(locCode, "415", locCode);

            when(jurorRepository.callDailyUtilJurorsStats(locCode, reportDate))
                .thenReturn(List.of());

            DailyUtilisationReportJurorsResponse response = utilisationReportService.viewDailyUtilisationJurors(locCode,
                reportDate);

            assertThat(response.getHeadings()).isNotNull();
            Map<String, AbstractReportResponse.DataTypeValue> headings = response.getHeadings();

            validateReportHeadings(headings);

            AbstractReportResponse.DataTypeValue timeCreated = headings.get("time_created");
            assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
            assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
            LocalDateTime createdTime = LocalDateTime.parse((String) timeCreated.getValue(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            assertThat(createdTime).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));

            assertThat(response.getTableData()).isNotNull();
            DailyUtilisationReportJurorsResponse.TableData tableData = response.getTableData();
            assertThat(tableData.getHeadings()).isNotNull();
            Assertions.assertThat(tableData.getHeadings()).hasSize(5);

            validateTableHeadings(tableData);

            assertThat(tableData.getTotalJurorWorkingDays()).isZero();
            assertThat(tableData.getTotalSittingDays()).isZero();
            assertThat(tableData.getTotalAttendanceDays()).isZero();
            assertThat(tableData.getTotalNonAttendanceDays()).isZero();

            verify(courtLocationRepository, times(1)).findById(locCode);
            verify(jurorRepository, times(1)).callDailyUtilJurorsStats(locCode, reportDate);

        }

        private void validateTableHeadings(DailyUtilisationReportJurorsResponse.TableData tableData) {
            DailyUtilisationReportJurorsResponse.TableData.Heading tableHeading = tableData.getHeadings().get(0);
            assertThat(tableHeading.getId()).isEqualTo(JUROR);
            assertThat(tableHeading.getName()).isEqualTo("Juror");
            assertThat(tableHeading.getDataType()).isEqualTo("String");

            DailyUtilisationReportJurorsResponse.TableData.Heading tableHeading1 = tableData.getHeadings().get(1);
            assertThat(tableHeading1.getId()).isEqualTo(JUROR_WORKING_DAYS);
            assertThat(tableHeading1.getName()).isEqualTo("Juror working days");
            assertThat(tableHeading1.getDataType()).isEqualTo("Integer");

            DailyUtilisationReportJurorsResponse.TableData.Heading tableHeading2 = tableData.getHeadings().get(2);
            assertThat(tableHeading2.getId()).isEqualTo(SITTING_DAYS);
            assertThat(tableHeading2.getName()).isEqualTo("Sitting days");
            assertThat(tableHeading2.getDataType()).isEqualTo("Integer");

            DailyUtilisationReportJurorsResponse.TableData.Heading tableHeading3 = tableData.getHeadings().get(3);
            assertThat(tableHeading3.getId()).isEqualTo(ATTENDANCE_DAYS);
            assertThat(tableHeading3.getName()).isEqualTo("Attendance days");
            assertThat(tableHeading3.getDataType()).isEqualTo("Integer");

            DailyUtilisationReportJurorsResponse.TableData.Heading tableHeading4 = tableData.getHeadings().get(4);
            assertThat(tableHeading4.getId()).isEqualTo(NON_ATTENDANCE_DAYS);
            assertThat(tableHeading4.getName()).isEqualTo("Non-attendance days");
            assertThat(tableHeading4.getDataType()).isEqualTo("Integer");

        }

        private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings) {
            Assertions.assertThat(headings.get("date")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report date")
                .dataType("LocalDate")
                .value("2024-04-20")
                .build());
            assertThat(headings.get("report_created")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report created")
                .dataType("LocalDate")
                .value(LocalDate.now().toString())
                .build());
            assertThat(headings.get("court_name")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Court name")
                .dataType("String")
                .value("Test Court")
                .build());
        }

        @Test
        void viewDailyUtilisationReportInvalidCourtLocation() {

            final String locCode = "416";
            final LocalDate reportDate = LocalDate.of(2024, 4, 20);

            setupCourt(locCode, "416", "415");

            assertThatExceptionOfType(MojException.Forbidden.class)
                .isThrownBy(() -> utilisationReportService.viewDailyUtilisationJurors(locCode, reportDate));

            verify(courtLocationRepository, times(1)).findById(locCode);
            verifyNoInteractions(jurorRepository);

        }
    }


    @Nested
    @DisplayName("Generate Monthly Utilisation tests")
    class GenerateMonthlyUtilisationTests {

        @Test
        @SneakyThrows
        void generateMonthlyUtilisationNoResultsAndValidHeadings() {

            final String locCode = "415";
            final LocalDate reportDate = LocalDate.of(2024, 4, 01);

            setupCourt(locCode, "415", locCode);

            when(jurorRepository.callDailyUtilJurorsStats(locCode, reportDate))
                .thenReturn(List.of());

            MonthlyUtilisationReportResponse response = utilisationReportService.generateMonthlyUtilisationReport(
                locCode, reportDate);

            assertThat(response.getHeadings()).isNotNull();
            Map<String, AbstractReportResponse.DataTypeValue> headings = response.getHeadings();

            validateReportHeadings(headings);

            AbstractReportResponse.DataTypeValue timeCreated = headings.get("time_created");
            assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
            assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
            LocalDateTime createdTime = LocalDateTime.parse((String) timeCreated.getValue(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            assertThat(createdTime).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));

            assertThat(response.getTableData()).isNotNull();
            MonthlyUtilisationReportResponse.TableData tableData = response.getTableData();
            assertThat(tableData.getHeadings()).isNotNull();
            Assertions.assertThat(tableData.getHeadings()).hasSize(6);

            validateTableHeadings(tableData);

            assertThat(tableData.getTotalJurorWorkingDays()).isZero();
            assertThat(tableData.getTotalSittingDays()).isZero();
            assertThat(tableData.getTotalAttendanceDays()).isZero();
            assertThat(tableData.getTotalNonAttendanceDays()).isZero();

            verify(courtLocationRepository, times(1)).findById(locCode);
            verify(jurorRepository, times(1)).callDailyUtilStats(locCode, reportDate,
                LocalDate.of(2024, 4, 30));
            verifyNoInteractions(utilisationStatsRepository);

        }

        private void validateTableHeadings(MonthlyUtilisationReportResponse.TableData tableData) {
            MonthlyUtilisationReportResponse.TableData.Heading tableHeading = tableData.getHeadings().get(0);
            assertThat(tableHeading.getId()).isEqualTo(MONTH);
            assertThat(tableHeading.getName()).isEqualTo("Month");
            assertThat(tableHeading.getDataType()).isEqualTo("String");

            MonthlyUtilisationReportResponse.TableData.Heading tableHeading1 = tableData.getHeadings().get(1);
            assertThat(tableHeading1.getId()).isEqualTo(JUROR_WORKING_DAYS);
            assertThat(tableHeading1.getName()).isEqualTo("Juror working days");
            assertThat(tableHeading1.getDataType()).isEqualTo("Integer");

            MonthlyUtilisationReportResponse.TableData.Heading tableHeading2 = tableData.getHeadings().get(2);
            assertThat(tableHeading2.getId()).isEqualTo(SITTING_DAYS);
            assertThat(tableHeading2.getName()).isEqualTo("Sitting days");
            assertThat(tableHeading2.getDataType()).isEqualTo("Integer");

            MonthlyUtilisationReportResponse.TableData.Heading tableHeading3 = tableData.getHeadings().get(3);
            assertThat(tableHeading3.getId()).isEqualTo(ATTENDANCE_DAYS);
            assertThat(tableHeading3.getName()).isEqualTo("Attendance days");
            assertThat(tableHeading3.getDataType()).isEqualTo("Integer");

            MonthlyUtilisationReportResponse.TableData.Heading tableHeading4 = tableData.getHeadings().get(4);
            assertThat(tableHeading4.getId()).isEqualTo(NON_ATTENDANCE_DAYS);
            assertThat(tableHeading4.getName()).isEqualTo("Non-attendance days");
            assertThat(tableHeading4.getDataType()).isEqualTo("Integer");

        }

        private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings) {
            Assertions.assertThat(headings.get("date_from")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType("LocalDate")
                .value("2024-04-01")
                .build());
            assertThat(headings.get("date_to")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType("LocalDate")
                .value("2024-04-30")
                .build());
            assertThat(headings.get("report_created")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report created")
                .dataType("LocalDate")
                .value(LocalDate.now().toString())
                .build());
            assertThat(headings.get("court_name")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Court name")
                .dataType("String")
                .value("Test Court")
                .build());
        }

        @Test
        void generateMonthlyUtilisationReportInvalidCourtLocation() {

            final String locCode = "416";
            final LocalDate reportDate = LocalDate.of(2024, 4, 20);

            setupCourt(locCode, "416", "415");

            assertThatExceptionOfType(MojException.Forbidden.class)
                .isThrownBy(() -> utilisationReportService.generateMonthlyUtilisationReport(locCode, reportDate));

            verify(courtLocationRepository, times(1)).findById(locCode);
            verifyNoInteractions(jurorRepository);
            verifyNoInteractions(utilisationStatsRepository);

        }
    }


    @Nested
    @DisplayName("View Monthly Utilisation tests")
    class ViewMonthlyUtilisationTests {

        @Test
        @SneakyThrows
        void viewMonthlyUtilisationNoResultsAndValidHeadings() {

            final String locCode = "415";
            final LocalDate reportDate = LocalDate.of(2024, 4, 01);

            setupCourt(locCode, "415", locCode);

            when(jurorRepository.callDailyUtilStats(locCode, reportDate, LocalDate.of(2024, 4, 30)))
                .thenReturn(List.of());

            MonthlyUtilisationReportResponse response = utilisationReportService.viewMonthlyUtilisationReport(locCode,
                reportDate, false);

            assertThat(response.getHeadings()).isNotNull();
            Map<String, AbstractReportResponse.DataTypeValue> headings = response.getHeadings();

            validateReportHeadings(headings);

            AbstractReportResponse.DataTypeValue timeCreated = headings.get("time_created");
            assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
            assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
            LocalDateTime createdTime = LocalDateTime.parse((String) timeCreated.getValue(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            assertThat(createdTime).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));

            assertThat(response.getTableData()).isNotNull();
            MonthlyUtilisationReportResponse.TableData tableData = response.getTableData();
            assertThat(tableData.getHeadings()).isNotNull();
            Assertions.assertThat(tableData.getHeadings()).hasSize(6);

            validateTableHeadings(tableData);

            assertThat(tableData.getTotalJurorWorkingDays()).isZero();
            assertThat(tableData.getTotalSittingDays()).isZero();
            assertThat(tableData.getTotalAttendanceDays()).isZero();
            assertThat(tableData.getTotalNonAttendanceDays()).isZero();

            verify(courtLocationRepository, times(1)).findById(locCode);
            verify(utilisationStatsRepository, times(1)).findByMonthStartBetweenAndLocCode(reportDate, reportDate,
                locCode);

        }

        private void validateTableHeadings(MonthlyUtilisationReportResponse.TableData tableData) {
            MonthlyUtilisationReportResponse.TableData.Heading tableHeading = tableData.getHeadings().get(0);
            assertThat(tableHeading.getId()).isEqualTo(MONTH);
            assertThat(tableHeading.getName()).isEqualTo("Month");
            assertThat(tableHeading.getDataType()).isEqualTo("String");

            MonthlyUtilisationReportResponse.TableData.Heading tableHeading1 = tableData.getHeadings().get(1);
            assertThat(tableHeading1.getId()).isEqualTo(JUROR_WORKING_DAYS);
            assertThat(tableHeading1.getName()).isEqualTo("Juror working days");
            assertThat(tableHeading1.getDataType()).isEqualTo("Integer");

            MonthlyUtilisationReportResponse.TableData.Heading tableHeading2 = tableData.getHeadings().get(2);
            assertThat(tableHeading2.getId()).isEqualTo(SITTING_DAYS);
            assertThat(tableHeading2.getName()).isEqualTo("Sitting days");
            assertThat(tableHeading2.getDataType()).isEqualTo("Integer");

            MonthlyUtilisationReportResponse.TableData.Heading tableHeading3 = tableData.getHeadings().get(3);
            assertThat(tableHeading3.getId()).isEqualTo(ATTENDANCE_DAYS);
            assertThat(tableHeading3.getName()).isEqualTo("Attendance days");
            assertThat(tableHeading3.getDataType()).isEqualTo("Integer");

            MonthlyUtilisationReportResponse.TableData.Heading tableHeading4 = tableData.getHeadings().get(4);
            assertThat(tableHeading4.getId()).isEqualTo(NON_ATTENDANCE_DAYS);
            assertThat(tableHeading4.getName()).isEqualTo("Non-attendance days");
            assertThat(tableHeading4.getDataType()).isEqualTo("Integer");

        }

        private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings) {
            assertThat(headings.get("report_created")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report created")
                .dataType("LocalDate")
                .value(LocalDate.now().toString())
                .build());
            assertThat(headings.get("court_name")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Court name")
                .dataType("String")
                .value("Test Court")
                .build());
        }

        @Test
        void viewMonthlyUtilisationReportInvalidCourtLocation() {

            final String locCode = "416";
            final LocalDate reportDate = LocalDate.of(2024, 4, 20);

            setupCourt(locCode, "416", "415");

            assertThatExceptionOfType(MojException.Forbidden.class)
                .isThrownBy(() -> utilisationReportService.viewMonthlyUtilisationReport(locCode, reportDate, false));

            verify(courtLocationRepository, times(1)).findById(locCode);
            verifyNoInteractions(utilisationStatsRepository);

        }
    }


    @Nested
    @DisplayName("Get Monthly Utilisation tests")
    class GetMonthlyUtilisationTests {

        @Test
        @SneakyThrows
        void monthlyUtilisationNoResults() {

            final String locCode = "415";
            final LocalDate reportDate = LocalDate.of(2024, 4, 01);

            setupCourt(locCode, "415", locCode);

            when(jurorRepository.callDailyUtilStats(locCode, reportDate, LocalDate.of(2024, 4, 30)))
                .thenReturn(List.of());

            String response = utilisationReportService.getMonthlyUtilisationReports(locCode);

            assertThat(response).isNotNull();
            assertThat(response).isEmpty();

            verify(courtLocationRepository, times(1)).findById(locCode);
            verify(utilisationStatsRepository, times(1))
                .findTop12ByLocCodeOrderByMonthStartDesc(locCode);
        }

        @Test
        void monthlyUtilisationReportInvalidCourtLocation() {

            final String locCode = "416";
            final LocalDate reportDate = LocalDate.of(2024, 4, 20);

            setupCourt(locCode, "416", "415");

            assertThatExceptionOfType(MojException.Forbidden.class)
                .isThrownBy(() -> utilisationReportService.getMonthlyUtilisationReports(locCode));

            verify(courtLocationRepository, times(1)).findById(locCode);
            verifyNoInteractions(utilisationStatsRepository);

        }
    }

    private void setupCourt(String locCode, String owner, String owner1) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setName("Test Court");
        courtLocation.setLocCode(locCode);
        courtLocation.setOwner(owner);
        when(courtLocationRepository.findById(locCode))
            .thenReturn(Optional.of(courtLocation));

        mockCurrentUser(owner1);
    }


    private void mockCurrentUser(String owner) {
        securityUtilMockedStatic = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
            .thenReturn(owner);
    }

}
