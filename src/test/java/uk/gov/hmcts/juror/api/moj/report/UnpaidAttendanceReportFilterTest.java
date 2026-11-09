package uk.gov.hmcts.juror.api.moj.report;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UnpaidAttendanceReportFilterTest {

    private static final LocalDate TODAY = LocalDate.of(2025, 4, 10);

    @Test
    void oldZeroTotalDueIsExcluded() {
        assertThat(UnpaidAttendanceReportFilter.includeRecentOrNonZeroTotalDue(
            LocalDate.of(2025, 1, 9), BigDecimal.ZERO, TODAY)).isFalse();
    }

    @Test
    void oldNonZeroTotalDueIsIncluded() {
        assertThat(UnpaidAttendanceReportFilter.includeRecentOrNonZeroTotalDue(
            LocalDate.of(2025, 1, 9), BigDecimal.ONE, TODAY)).isTrue();
    }

    @Test
    void recentZeroTotalDueIsIncluded() {
        assertThat(UnpaidAttendanceReportFilter.includeRecentOrNonZeroTotalDue(
            LocalDate.of(2025, 1, 11), BigDecimal.ZERO, TODAY)).isTrue();
    }

    @Test
    void thresholdDateZeroTotalDueIsIncluded() {
        assertThat(UnpaidAttendanceReportFilter.includeRecentOrNonZeroTotalDue(
            LocalDate.of(2025, 1, 10), BigDecimal.ZERO, TODAY)).isTrue();
    }

    @Test
    void zeroTotalDueComparisonIgnoresScale() {
        assertThat(UnpaidAttendanceReportFilter.includeRecentOrNonZeroTotalDue(
            LocalDate.of(2025, 1, 9), new BigDecimal("0.00"), TODAY)).isFalse();
    }
}
