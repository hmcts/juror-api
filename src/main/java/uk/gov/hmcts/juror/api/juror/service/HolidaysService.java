package uk.gov.hmcts.juror.api.juror.service;

import uk.gov.hmcts.juror.api.juror.controller.request.JurorHolidaysRequestDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorHolidaysResponseDto;


public interface HolidaysService {


    /**
     * Get the Holiday dates from the Juror Date picker.
     *
     * @param request contains the dates selected
     * @return JurorHolidaysResponseDto matched dates from JUROR.Holidays.holidays
     */


    JurorHolidaysResponseDto.MatchingHolidayDates getMatchingHolidayDates(JurorHolidaysRequestDto requestDto);


}
