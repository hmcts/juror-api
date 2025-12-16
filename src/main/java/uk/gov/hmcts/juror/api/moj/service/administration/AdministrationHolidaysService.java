package uk.gov.hmcts.juror.api.moj.service.administration;

import uk.gov.hmcts.juror.api.moj.controller.response.administration.HolidayDate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AdministrationHolidaysService {

    Map<Integer, List<HolidayDate>> viewBankHolidays();

    List<HolidayDate> viewNonSittingDays(String locCode);

    void deleteNonSittingDays(String locCode, LocalDate date);

    void addNonSittingDays(String locCode, HolidayDate holidays);

    List<LocalDate> getSundayDates(LocalDate fromDate, LocalDate toDate);

    List<LocalDate> getSaturdayDates(LocalDate fromDate, LocalDate toDate);

}
