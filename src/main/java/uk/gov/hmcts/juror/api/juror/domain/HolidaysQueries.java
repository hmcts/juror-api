package uk.gov.hmcts.juror.api.juror.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.moj.utils.DateRelatedUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;


/**
 * QueryDSL queries for {@Link Holidays}.
 */
@Slf4j
public final class HolidaysQueries {
    private static final int BST_ADJUSTMENT = 1;
    private static final int STRING_POSITION_BST_TIME = 16;
    private static final QHolidays HOLIDAYS_DETAIL = QHolidays.holidays;

    private HolidaysQueries() {

    }

    /**
     * Query to match JUROR.HOLIDAYS.HOLIDAY where holidayDates.
     */

    public static BooleanExpression holidayDatesMatched(Date holidayDates) {

        String strUserSelectedDate = holidayDates.toString();
        String strBstTime = strUserSelectedDate.substring(strUserSelectedDate.length()
            - STRING_POSITION_BST_TIME);
        String strBstTimeValue = strBstTime.substring(0, 1);
        int intBstTimeCondition = Integer.parseInt(strBstTimeValue);

        if (intBstTimeCondition == BST_ADJUSTMENT) {
            return HOLIDAYS_DETAIL.holiday.eq(
                DateRelatedUtils.convertToLocalDateViaInstant(
                    Date.from(holidayDates.toInstant().atZone(ZoneId.systemDefault())
                        .toInstant().truncatedTo(ChronoUnit.DAYS).minus(1,
                            ChronoUnit.HOURS
                        ))));
        } else {
            return HOLIDAYS_DETAIL.holiday.eq(DateRelatedUtils.convertToLocalDateViaInstant(holidayDates));
        }
    }

    /**
     * Query to match HOLIDAYS records to a given date and filter by owner (court location code).
     * Also returns matching public holiday
     *
     * @param locCode 3-digit numeric string, unique identifier for court locations
     * @param date    the value to check against holidaysDetail.holiday
     * @return Predicate
     */
    @SuppressWarnings({"PMD.LinguisticNaming"}) // BooleanExpression is fine.
    public static BooleanExpression isCourtHoliday(String locCode, LocalDate date) {
        return locCode == null || locCode.isBlank()
            ? HOLIDAYS_DETAIL.publicHoliday.isTrue().and(HOLIDAYS_DETAIL.holiday.eq(date))
            : HOLIDAYS_DETAIL.holiday.eq(date)
                .and(HOLIDAYS_DETAIL.publicHoliday.isTrue()
                    .or(HOLIDAYS_DETAIL.courtLocation.locCode.eq(locCode)));
    }

}
