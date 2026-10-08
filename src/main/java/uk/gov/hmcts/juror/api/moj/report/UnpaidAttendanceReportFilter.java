package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class UnpaidAttendanceReportFilter {

    private static final int ZERO_TOTAL_DUE_EXCLUSION_MONTHS = 3;

    private UnpaidAttendanceReportFilter() {
    }

    public static BooleanExpression includeRecentOrNonZeroTotalDue(LocalDate today) {
        LocalDate thresholdDate = today.minusMonths(ZERO_TOTAL_DUE_EXCLUSION_MONTHS);
        return QAppearance.appearance.attendanceDate.goe(thresholdDate)
            .or(QAppearance.appearance.totalDue.ne(BigDecimal.ZERO));
    }

    public static boolean includeRecentOrNonZeroTotalDue(LocalDate attendanceDate, BigDecimal totalDue,
                                                         LocalDate today) {
        Objects.requireNonNull(attendanceDate);
        Objects.requireNonNull(totalDue);
        Objects.requireNonNull(today);

        LocalDate thresholdDate = today.minusMonths(ZERO_TOTAL_DUE_EXCLUSION_MONTHS);
        return !attendanceDate.isBefore(thresholdDate) || totalDue.compareTo(BigDecimal.ZERO) != 0;
    }
}
