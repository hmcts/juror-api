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
public class HolidaysQueries {
    private static final int BST_ADJUSTMENT = 1;
    private static final int STRING_POSITION_BST_TIME = 16;

    private HolidaysQueries() {

    }

    private static final QHolidays holidaysDetail = QHolidays.holidays;

    /**
     * Query to match JUROR.HOLIDAYS.HOLIDAY where holidayDates
     *
     * @param holidayDates
     * @return
     */

    public static BooleanExpression HolidayDatesMatched(Date holidayDates) {

        String strUserSelectedDate = holidayDates.toString();
        String strBstTime = strUserSelectedDate.substring(strUserSelectedDate.length() - STRING_POSITION_BST_TIME);
        String strBstTimeValue = strBstTime.substring(0, 1);
        int intBstTimeCondition = Integer.parseInt(strBstTimeValue);

        if (intBstTimeCondition == BST_ADJUSTMENT) {
            return holidaysDetail.holiday.eq(
                DateRelatedUtils.convertToLocalDateViaInstant(Date.from(holidayDates.toInstant().atZone(ZoneId.systemDefault())
                    .toInstant().truncatedTo(ChronoUnit.DAYS).minus(
                        1,
                        ChronoUnit.HOURS
                    ))));
        } else {
            return holidaysDetail.holiday.eq(DateRelatedUtils.convertToLocalDateViaInstant(holidayDates));
        }
    }

    /**
     * Query to match HOLIDAYS records to a given date and filter by owner (court location code).
     * Also returns matching public holiday
     *
     * @param owner 3-digit numeric string, unique identifier for court locations
     * @param date  the value to check against holidaysDetail.holiday
     * @return Predicate
     */
    public static BooleanExpression isCourtHoliday(String owner, LocalDate date) {
        return owner == null || owner.isBlank()
            ? holidaysDetail.publicHoliday.isTrue().and(holidaysDetail.holiday.eq(date))
            : holidaysDetail.holiday.eq(date)
                .and(holidaysDetail.owner.eq(owner).or(holidaysDetail.publicHoliday.isTrue()));
    }
    
}
