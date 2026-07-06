package uk.gov.hmcts.juror.api.moj.service.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.CourtsAndDatesReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.SittingDaysStatsReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.StatsSittingDaysRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.COURT_LOCATION_NAME_AND_CODE;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.EIGHT_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.ELEVEN_OR_MORE_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.FIVE_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.FOUR_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.NINE_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.ONE_SITTING_DAY;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.SEVEN_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.SIX_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.TEN_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.THREE_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.TOTAL_JURORS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.TOTAL_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.TWO_SITTING_DAYS;
import static uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService.TableHeading.ZERO_SITTING_DAYS;

class SittingDaysReportServiceImplTest {

    private final StatsSittingDaysRepository statsSittingDaysRepository;
    private final SittingDaysReportServiceImpl sittingDaysReportService;

    SittingDaysReportServiceImplTest() {
        this.statsSittingDaysRepository = mock(StatsSittingDaysRepository.class);
        this.sittingDaysReportService = new SittingDaysReportServiceImpl(statsSittingDaysRepository);
    }

    @BeforeEach
    void beforeEach() {
        setSecurityContext(Set.of(Permission.SUPER_USER));
    }

    @Nested
    @DisplayName("Sitting days report tests")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    class SittingDaysReportTests {

        @Test
        void sittingDaysReportNoData() {
            CourtsAndDatesReportRequest request = getRequest();
            when(statsSittingDaysRepository.findStatsByMonthRangeAndCourtCodes("2024-05", "2024-05", List.of("415")))
                .thenReturn(List.of());

            SittingDaysStatsReportResponse response = sittingDaysReportService.getSittingDaysStats(request);

            validateReportHeadings(response.getHeadings(), 0, 0);
            validateTableHeadings(response.getTableData());
            assertThat(response.getTableData().getData()).isEmpty();
            verify(statsSittingDaysRepository, times(1))
                .findStatsByMonthRangeAndCourtCodes("2024-05", "2024-05", List.of("415"));
        }

        @Test
        void sittingDaysReportHappy() {
            CourtsAndDatesReportRequest request = getRequest();
            List<StatsSittingDaysRepository.SittingDaysStatsData> sittingDaysStatsData = getSittingDaysStatsData();
            when(statsSittingDaysRepository.findStatsByMonthRangeAndCourtCodes("2024-05", "2024-05", List.of("415")))
                .thenReturn(sittingDaysStatsData);

            SittingDaysStatsReportResponse response = sittingDaysReportService.getSittingDaysStats(request);

            validateReportHeadings(response.getHeadings(), 665, 5060);
            validateTableHeadings(response.getTableData());

            assertThat(response.getTableData().getData()).hasSize(1);
            SittingDaysStatsReportResponse.TableData.DataRow row = response.getTableData().getData().get(0);
            assertThat(row.getCourtLocationNameAndCode()).isEqualTo("CHESTER (415)");
            assertThat(row.getZeroSittingDays()).isEqualTo(5);
            assertThat(row.getOneSittingDay()).isEqualTo(10);
            assertThat(row.getTwoSittingDays()).isEqualTo(20);
            assertThat(row.getThreeSittingDays()).isEqualTo(30);
            assertThat(row.getFourSittingDays()).isEqualTo(40);
            assertThat(row.getFiveSittingDays()).isEqualTo(50);
            assertThat(row.getSixSittingDays()).isEqualTo(60);
            assertThat(row.getSevenSittingDays()).isEqualTo(70);
            assertThat(row.getEightSittingDays()).isEqualTo(80);
            assertThat(row.getNineSittingDays()).isEqualTo(90);
            assertThat(row.getTenSittingDays()).isEqualTo(100);
            assertThat(row.getElevenOrMoreSittingDays()).isEqualTo(110);
            assertThat(row.getTotalJurors()).isEqualTo(665);
            assertThat(row.getTotalSittingDays()).isEqualTo(5060);
            verify(statsSittingDaysRepository, times(1))
                .findStatsByMonthRangeAndCourtCodes("2024-05", "2024-05", List.of("415"));
        }

        @Test
        void sittingDaysReportAllCourts() {
            CourtsAndDatesReportRequest request = CourtsAndDatesReportRequest.builder()
                .allCourts(true)
                .fromDate(LocalDate.of(2024, 5, 1))
                .toDate(LocalDate.of(2024, 5, 31))
                .build();
            List<StatsSittingDaysRepository.SittingDaysStatsData> sittingDaysStatsData = getSittingDaysStatsData();
            when(statsSittingDaysRepository.findStatsByMonthRange("2024-05", "2024-05"))
                .thenReturn(sittingDaysStatsData);

            SittingDaysStatsReportResponse response = sittingDaysReportService.getSittingDaysStats(request);

            assertThat(response.getTableData().getData()).hasSize(1);
            verify(statsSittingDaysRepository, times(1)).findStatsByMonthRange("2024-05", "2024-05");
        }

        @Test
        void sittingDaysReportNoPermission() {
            setSecurityContext(Set.of());

            assertThatThrownBy(() -> sittingDaysReportService.getSittingDaysStats(getRequest()))
                .isInstanceOf(MojException.Forbidden.class)
                .hasMessage("User not allowed to access this report");
            verifyNoInteractions(statsSittingDaysRepository);
        }
    }

    private CourtsAndDatesReportRequest getRequest() {
        return CourtsAndDatesReportRequest.builder()
            .allCourts(false)
            .courtLocCodes(List.of("415"))
            .fromDate(LocalDate.of(2024, 5, 1))
            .toDate(LocalDate.of(2024, 5, 31))
            .build();
    }

    private List<StatsSittingDaysRepository.SittingDaysStatsData> getSittingDaysStatsData() {
        return List.of(
            getSittingDaysStatsData("0", 0, 5),
            getSittingDaysStatsData("1", 10, 10),
            getSittingDaysStatsData("2", 40, 20),
            getSittingDaysStatsData("3", 90, 30),
            getSittingDaysStatsData("4", 160, 40),
            getSittingDaysStatsData("5", 250, 50),
            getSittingDaysStatsData("6", 360, 60),
            getSittingDaysStatsData("7", 490, 70),
            getSittingDaysStatsData("8", 640, 80),
            getSittingDaysStatsData("9", 810, 90),
            getSittingDaysStatsData("10", 1000, 100),
            getSittingDaysStatsData("11 or more", 1210, 110)
        );
    }

    private StatsSittingDaysRepository.SittingDaysStatsData getSittingDaysStatsData(String category,
                                                                                   int sittingDays,
                                                                                   int jurors) {
        StatsSittingDaysRepository.SittingDaysStatsData data =
            mock(StatsSittingDaysRepository.SittingDaysStatsData.class);
        when(data.getCourtCode()).thenReturn("415");
        when(data.getCourtName()).thenReturn("CHESTER");
        when(data.getSittingDaysCategory()).thenReturn(category);
        when(data.getNumberOfSittingDays()).thenReturn(sittingDays);
        when(data.getNumberOfJurors()).thenReturn(jurors);
        return data;
    }

    private void validateTableHeadings(SittingDaysStatsReportResponse.TableData tableData) {
        assertThat(tableData).isNotNull();
        assertThat(tableData.getHeadings()).hasSize(15);
        assertThat(tableData.getHeadings().get(0).getId()).isEqualTo(COURT_LOCATION_NAME_AND_CODE);
        assertThat(tableData.getHeadings().get(1).getId()).isEqualTo(ZERO_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(2).getId()).isEqualTo(ONE_SITTING_DAY);
        assertThat(tableData.getHeadings().get(3).getId()).isEqualTo(TWO_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(4).getId()).isEqualTo(THREE_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(5).getId()).isEqualTo(FOUR_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(6).getId()).isEqualTo(FIVE_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(7).getId()).isEqualTo(SIX_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(8).getId()).isEqualTo(SEVEN_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(9).getId()).isEqualTo(EIGHT_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(10).getId()).isEqualTo(NINE_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(11).getId()).isEqualTo(TEN_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(12).getId()).isEqualTo(ELEVEN_OR_MORE_SITTING_DAYS);
        assertThat(tableData.getHeadings().get(13).getId()).isEqualTo(TOTAL_JURORS);
        assertThat(tableData.getHeadings().get(14).getId()).isEqualTo(TOTAL_SITTING_DAYS);
    }

    private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings,
                                        int totalJurors,
                                        int totalSittingDays) {
        assertThat(headings).containsKeys("date_from", "date_to", "total_number_of_jurors",
            "total_sitting_days", "report_created");
        assertThat(headings.get("date_from").getDisplayName()).isEqualTo("Date from");
        assertThat(headings.get("date_from").getDataType()).isEqualTo("LocalDate");
        assertThat(headings.get("date_from").getValue()).isEqualTo("2024-05-01");
        assertThat(headings.get("date_to").getDisplayName()).isEqualTo("Date to");
        assertThat(headings.get("date_to").getDataType()).isEqualTo("LocalDate");
        assertThat(headings.get("date_to").getValue()).isEqualTo("2024-05-31");
        assertThat(headings.get("total_number_of_jurors").getDisplayName()).isEqualTo("Total jurors");
        assertThat(headings.get("total_number_of_jurors").getDataType()).isEqualTo("Integer");
        assertThat(headings.get("total_number_of_jurors").getValue()).isEqualTo(totalJurors);
        assertThat(headings.get("total_sitting_days").getDisplayName()).isEqualTo("Total sitting days");
        assertThat(headings.get("total_sitting_days").getDataType()).isEqualTo("Integer");
        assertThat(headings.get("total_sitting_days").getValue()).isEqualTo(totalSittingDays);
        assertThat(headings.get("report_created").getDataType()).isEqualTo("LocalDateTime");
        assertThat(headings.get("report_created").getValue()).isNotNull();
    }

    private void setSecurityContext(Set<Permission> permissions) {
        User user = User.builder()
            .username("Administrator")
            .permissions(new HashSet<>(permissions))
            .build();
        BureauJwtPayload bureauJwtPayload = new BureauJwtPayload(user, UserType.ADMINISTRATOR, "415",
            Collections.singletonList(CourtLocation.builder()
                .locCode("415")
                .name("Chester")
                .owner("415")
                .build()));

        BureauJwtAuthentication auth = mock(BureauJwtAuthentication.class);
        when(auth.getPrincipal()).thenReturn(bureauJwtPayload);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }
}
