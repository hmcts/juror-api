package uk.gov.hmcts.juror.api.moj.service.report;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.HolidayDate;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationHolidaysService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AttendanceReportServiceImpl implements AttendanceReportService {

    private final AppearanceRepository appearanceRepository;
    private final AdministrationHolidaysService holidaysService;

    @Override
    @Transactional(readOnly = true)
    public WeekendAttendanceReportResponse getWeekendAttendanceReport() {

        if (!SecurityUtil.hasPermission(Permission.SUPER_USER)) {
            throw new MojException.Forbidden("User not allowed to access this report", null);
        }

        // get first day of current month
        LocalDate fromDate = LocalDate.now().withDayOfMonth(1);

        // get current date (report won't be run for future dates)
        LocalDate toDate = LocalDate.now();

        // build list of saturday dates
        List<LocalDate> saturdayDates = holidaysService.getSaturdayDates(fromDate, toDate);

        // build list of sunday dates
        List<LocalDate> sundayDates = holidaysService.getSundayDates(fromDate, toDate);

        // bank holidays fetched from a service
        Map<Integer, List<HolidayDate>> bankHolidayDates = holidaysService.viewBankHolidays();

        List<HolidayDate> bankHolidaysThisMonth = bankHolidayDates.get(LocalDate.now().getYear());

        if (bankHolidaysThisMonth == null) {
            bankHolidaysThisMonth = List.of();
        }
        var bankHolidayDatesInMonth = bankHolidaysThisMonth.stream()
                .map(HolidayDate::getDate)
                .filter(date -> !date.isBefore(fromDate) && !date.isAfter(toDate))
                .toList();

        // combine all dates into a single list
        var allRelevantDates = new ArrayList<LocalDate>();
        allRelevantDates.addAll(saturdayDates);
        allRelevantDates.addAll(sundayDates);
        allRelevantDates.addAll(bankHolidayDatesInMonth);

        List<Tuple> results = appearanceRepository.getAllWeekendAttendances(saturdayDates, sundayDates,
                bankHolidayDatesInMonth, allRelevantDates);

        List<WeekendAttendanceReportResponse.TableData.DataRow> dataRows = new ArrayList<>();

        if (!results.isEmpty()) {
            for (Tuple result : results) {
                WeekendAttendanceReportResponse.TableData.DataRow dataRow =
                    new WeekendAttendanceReportResponse.TableData.DataRow();
                dataRow.setCourtLocationNameAndCode(result.get(0, String.class) + " ("
                        + result.get(1, String.class) + ")");
                dataRow.setSaturdayTotal(result.get(2, Integer.class));
                dataRow.setSundayTotal(result.get(3, Integer.class));
                dataRow.setHolidayTotal(result.get(4, Integer.class));
                dataRow.setTotalPaid(result.get(5, BigDecimal.class));
                dataRows.add(dataRow);
            }
        }

        WeekendAttendanceReportResponse reportResponse = new WeekendAttendanceReportResponse(Map.of());

        reportResponse.getTableData().setData(dataRows);

        return reportResponse;
    }

}
