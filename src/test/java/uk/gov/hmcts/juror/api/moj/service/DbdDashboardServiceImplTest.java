package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.moj.controller.request.DbdDashboardRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.DbdResponseStats;
import uk.gov.hmcts.juror.api.moj.repository.DbdResponseStatsRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DbdDashboardServiceImplTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 1, 31);

    @Mock
    private DbdResponseStatsRepository dbdResponseStatsRepository;

    private DbdDashboardServiceImpl dbdDashboardService;

    @BeforeEach
    void beforeEach() {
        dbdDashboardService = new DbdDashboardServiceImpl(dbdResponseStatsRepository);
    }

    @Test
    void shouldRoundPercentagesToNearestWholeNumber() {
        when(dbdResponseStatsRepository.findByLocCodeInAndSummonsDateBetween(
            anyLocCodes(), eq(START_DATE), eq(END_DATE)))
            .thenReturn(List.of(
                responseStats("415", "Online", 8),
                responseStats("415", "Paper", 6),
                responseStats("415", "None", 9)
            ));

        DbdDashboardResponseDto.LocationMetrics result = getFirstLocationMetrics();

        assertThat(result.getTotalResponses()).isEqualTo(14);
        assertThat(result.getResponseRatePercent()).isEqualTo(61);
        assertThat(result.getDigitalResponsesPercent()).isEqualTo(57);
    }

    @Test
    void shouldReturnNullPercentagesWhenThereIsNoDenominator() {
        when(dbdResponseStatsRepository.findByLocCodeInAndSummonsDateBetween(
            anyLocCodes(), eq(START_DATE), eq(END_DATE)))
            .thenReturn(List.of());

        DbdDashboardResponseDto.LocationMetrics result = getFirstLocationMetrics();

        assertThat(result.getResponseRatePercent()).isNull();
        assertThat(result.getDigitalResponsesPercent()).isNull();
    }

    @Test
    void shouldSumLocationsWhenRequested() {
        when(dbdResponseStatsRepository.findByLocCodeInAndSummonsDateBetween(
            anyLocCodes(), eq(START_DATE), eq(END_DATE)))
            .thenReturn(List.of(
                responseStats("415", "Online", 8),
                responseStats("415", "Paper", 6),
                responseStats("416", "Online", 3),
                responseStats("416", "None", 5)
            ));

        DbdDashboardResponseDto response = dbdDashboardService.getGroupStatistics(
            DbdDashboardRequestDto.builder()
                .courtGroups(List.of(DbdDashboardRequestDto.CourtGroupDto.builder()
                                         .groupName("Test group")
                                         .groupLocations(List.of(415, 416))
                                         .build()))
                .dateRangeA(DbdDashboardRequestDto.DateRangeDto.builder()
                                .startDate(START_DATE)
                                .endDate(END_DATE)
                                .build())
                .sumGroups(true)
                .build()
        );

        DbdDashboardResponseDto.LocationMetrics result =
            response.getCourtGroups().get(0).getPeriodA().getLocations().get(0);

        assertThat(result.getLocationCode()).isNull();
        assertThat(result.getOnlineResponseTotal()).isEqualTo(11);
        assertThat(result.getPaperResponseTotal()).isEqualTo(6);
        assertThat(result.getNotRespondedTotal()).isEqualTo(5);
        assertThat(result.getTotalResponses()).isEqualTo(17);
        assertThat(result.getResponseRatePercent()).isEqualTo(77);
        assertThat(result.getDigitalResponsesPercent()).isEqualTo(65);
    }

    @Test
    void shouldReturnPeriodBWhenSecondDateRangeProvided() {
        LocalDate periodBStartDate = LocalDate.of(2026, 2, 1);
        LocalDate periodBEndDate = LocalDate.of(2026, 2, 28);

        when(dbdResponseStatsRepository.findByLocCodeInAndSummonsDateBetween(
            anyLocCodes(), eq(START_DATE), eq(END_DATE)))
            .thenReturn(List.of(responseStats("415", "Online", 8)));
        when(dbdResponseStatsRepository.findByLocCodeInAndSummonsDateBetween(
            anyLocCodes(), eq(periodBStartDate), eq(periodBEndDate)))
            .thenReturn(List.of(responseStats("415", "Paper", 4)));

        DbdDashboardResponseDto response = dbdDashboardService.getGroupStatistics(
            DbdDashboardRequestDto.builder()
                .courtGroups(List.of(DbdDashboardRequestDto.CourtGroupDto.builder()
                                         .groupName("Test group")
                                         .groupLocations(List.of(415))
                                         .build()))
                .dateRangeA(DbdDashboardRequestDto.DateRangeDto.builder()
                                .startDate(START_DATE)
                                .endDate(END_DATE)
                                .build())
                .dateRangeB(DbdDashboardRequestDto.DateRangeDto.builder()
                                .startDate(periodBStartDate)
                                .endDate(periodBEndDate)
                                .build())
                .build()
        );

        assertThat(response.getCourtGroups().get(0).getPeriodA().getLocations().get(0).getOnlineResponseTotal())
            .isEqualTo(8);
        assertThat(response.getCourtGroups().get(0).getPeriodB().getLocations().get(0).getPaperResponseTotal())
            .isEqualTo(4);

        verify(dbdResponseStatsRepository).findByLocCodeInAndSummonsDateBetween(
            anyLocCodes(), eq(START_DATE), eq(END_DATE));
        verify(dbdResponseStatsRepository).findByLocCodeInAndSummonsDateBetween(
            anyLocCodes(), eq(periodBStartDate), eq(periodBEndDate));
    }

    private DbdDashboardResponseDto.LocationMetrics getFirstLocationMetrics() {
        DbdDashboardResponseDto response = dbdDashboardService.getGroupStatistics(
            DbdDashboardRequestDto.builder()
                .courtGroups(List.of(DbdDashboardRequestDto.CourtGroupDto.builder()
                                         .groupName("Test group")
                                         .groupLocations(List.of(415))
                                         .build()))
                .dateRangeA(DbdDashboardRequestDto.DateRangeDto.builder()
                                .startDate(START_DATE)
                                .endDate(END_DATE)
                                .build())
                .build()
        );

        return response.getCourtGroups().get(0).getPeriodA().getLocations().get(0);
    }

    private DbdResponseStats responseStats(String locCode, String responseMethod, int jurorCount) {
        return new DbdResponseStats(
            null,
            START_DATE,
            START_DATE,
            "Within 7 days",
            locCode,
            responseMethod,
            "18-24",
            jurorCount
        );
    }

    private Collection<String> anyLocCodes() {
        return any();
    }
}
