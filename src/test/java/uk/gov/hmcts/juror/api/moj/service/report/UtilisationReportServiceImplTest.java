package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
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

@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.AssertionsShouldIncludeMessage"
})
class UtilisationReportServiceImplTest {
    private final CourtLocationRepository courtLocationRepository;
    private final JurorRepository jurorRepository;
    private final UtilisationReportService utilisationReportService;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public UtilisationReportServiceImplTest() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        this.jurorRepository = mock(JurorRepository.class);
        this.utilisationReportService = new UtilisationReportServiceImpl(courtLocationRepository, jurorRepository);
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

    @Test
    @SneakyThrows
    void viewDailyUtilisationReportNoResultsAndValidHeadings() {

        final String locCode = "415";
        final LocalDate reportFromDate = LocalDate.of(2024, 4, 20);
        final LocalDate reportToDate = LocalDate.of(2024, 5, 13);

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setName("Test Court");
        courtLocation.setLocCode(locCode);
        courtLocation.setOwner("415");
        when(courtLocationRepository.findById(locCode))
            .thenReturn(Optional.of(courtLocation));

        mockCurrentUser(locCode);

        when(jurorRepository.callDailyUtilStats(locCode, reportFromDate, reportToDate))
            .thenReturn(List.of());

        DailyUtilisationReportResponse response = utilisationReportService.viewDailyUtilisationReport(locCode,
            reportFromDate,
            reportToDate);

        assertThat(response.getHeadings()).isNotNull();
        Map<String, AbstractReportResponse.DataTypeValue> headings = response.getHeadings();

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

        DailyUtilisationReportResponse.TableData.TableHeading[] expectedHeadingsArray =
            DailyUtilisationReportResponse.TableData.TableHeading.values();
        for (int i = 0;
             i < expectedHeadingsArray.length;
             i++) {
            DailyUtilisationReportResponse.TableData.TableHeading expectedHeading = expectedHeadingsArray[i];
            DailyUtilisationReportResponse.TableData.Heading actualHeading = tableData.getHeadings().get(i);
            assertThat(actualHeading.getId()).isEqualTo(expectedHeading);
            assertThat(actualHeading.getName()).isEqualTo(expectedHeading.getDisplayName());
            assertThat(actualHeading.getDataType()).isEqualTo(expectedHeading.getDataType());
        }

        Assertions.assertThat(tableData.getWeeks()).isEmpty();
        assertThat(tableData.getOverallTotalJurorWorkingDays()).isZero();
        assertThat(tableData.getOverallTotalSittingDays()).isZero();
        assertThat(tableData.getOverallTotalAttendanceDays()).isZero();
        assertThat(tableData.getOverallTotalNonAttendanceDays()).isZero();
        assertThat(tableData.getOverallTotalUtilisation()).isZero();

        verify(courtLocationRepository, times(1)).findById(locCode);
        verify(jurorRepository, times(1)).callDailyUtilStats(locCode, reportFromDate, reportToDate);

    }

    @Test
    void viewDailyUtilisationReportInvalidCourtLocation() {

        final String locCode = "416";
        final LocalDate reportFromDate = LocalDate.of(2024, 4, 20);
        final LocalDate reportToDate = LocalDate.of(2024, 5, 13);

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setName("Test Court");
        courtLocation.setLocCode(locCode);
        courtLocation.setOwner("416");
        when(courtLocationRepository.findById(locCode))
            .thenReturn(Optional.of(courtLocation));

        mockCurrentUser("415");

        assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> utilisationReportService.viewDailyUtilisationReport(locCode, reportFromDate,
                reportToDate));

        verify(courtLocationRepository, times(1)).findById(locCode);
        verifyNoInteractions(jurorRepository);

    }


    private void mockCurrentUser(String owner) {
        securityUtilMockedStatic = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
            .thenReturn(owner);
    }


}
