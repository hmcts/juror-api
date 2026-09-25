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
    void getGroupStatisticsRoundsPercentagesToNearestWholeNumber() {
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
    void getGroupStatisticsReturnsNullPercentagesWhenThereIsNoDenominator() {
        when(dbdResponseStatsRepository.findByLocCodeInAndSummonsDateBetween(
            anyLocCodes(), eq(START_DATE), eq(END_DATE)))
            .thenReturn(List.of());

        DbdDashboardResponseDto.LocationMetrics result = getFirstLocationMetrics();

        assertThat(result.getResponseRatePercent()).isNull();
        assertThat(result.getDigitalResponsesPercent()).isNull();
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
