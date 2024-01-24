package uk.gov.hmcts.juror.api.juror.service;

import uk.gov.hmcts.juror.api.juror.domain.Holidays;

import java.util.Date;
import java.util.List;

public interface HolidaysDateService {

    /**
     * Get Holiday dates from the JUROR_HOLIDAYS.holiday that match Selected Juror Date
     *
     * @param holidays
     * @return List
     */

    List<Holidays> getHolidayDates(Date holidays);

}
