package uk.gov.hmcts.juror.api.moj.service.report;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.HolidayDate;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationHolidaysService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AttendanceReportServiceImpl implements AttendanceReportService {

    private AppearanceRepository appearanceRepository;
    private AdministrationHolidaysService holidaysService;

    public AttendanceReportServiceImpl(
        AppearanceRepository appearanceRepository,
        AdministrationHolidaysService holidaysService) {
        this.appearanceRepository = appearanceRepository;
        this.holidaysService = holidaysService;
    }

    @Override
    @Transactional(readOnly = true)
    public WeekendAttendanceReportResponse getWeekendAttendanceReport() {

        // get first day of current month
        LocalDate fromDate = LocalDate.now().withDayOfMonth(1);
        // get current date (report won't be run for future dates)
        LocalDate toDate = LocalDate.now();

        // build list of saturday dates
        var saturdayDates = fromDate.datesUntil(toDate.plusDays(1))
                .filter(date -> date.getDayOfWeek().getValue() == 6)
                .toList();

        // build list of sunday dates
        var sundayDates = fromDate.datesUntil(toDate.plusDays(1))
                .filter(date -> date.getDayOfWeek().getValue() == 7)
                .toList();

        // bank holidays fetched from a service
        var bankHolidayDates = holidaysService.viewBankHolidays();

        List<HolidayDate> bankHolidaysThisMonth = bankHolidayDates.get(LocalDate.now().getYear());

        if (bankHolidaysThisMonth == null) {
            bankHolidaysThisMonth = List.of();
        }
        var bankHolidayDatesInMonth = bankHolidaysThisMonth.stream()
                .map(HolidayDate::getDate)
                .filter(date -> !date.isBefore(fromDate) && !date.isAfter(toDate))
                .toList();

        // combine all dates into a single list
        var allRelevantDates = new java.util.ArrayList<LocalDate>();
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
                dataRow.setTotalPaid(result.get(5, Double.class));
                dataRows.add(dataRow);
            }
        }

        WeekendAttendanceReportResponse reportResponse =
                new WeekendAttendanceReportResponse(Map.of());

        reportResponse.getTableData().setData(dataRows);

        return reportResponse;
    }
}
