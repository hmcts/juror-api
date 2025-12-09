package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.HolidayDate;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationHolidaysService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WeekendAttendanceReport extends AbstractStandardReport {


    private final AdministrationHolidaysService holidaysService;
    private final CourtLocationService courtLocationService;

    public WeekendAttendanceReport(AdministrationHolidaysService holidaysService,
                                   CourtLocationService courtLocationService) {
        super(QAppearance.appearance,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.ATTENDANCE_DATE,
            DataType.DAY,
            DataType.APPEARANCE_POOL_NUMBER,
            DataType.APPEARANCE_TRIAL_NUMBER
        );
        this.holidaysService = holidaysService;
        this.courtLocationService = courtLocationService;
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {

        query.where(QAppearance.appearance.locCode.eq(request.getLocCode()));

        // need to work out the weekend and bank holiday dates in the range
        // get first day of current month
        LocalDate fromDate = LocalDate.now().withDayOfMonth(1);

        // get current date (report won't be run for future dates)
        LocalDate toDate = LocalDate.now();
        // build list of saturday dates
        var saturdayDates = holidaysService.getSaturdayDates(fromDate, toDate);

        // build list of sunday dates
        var sundayDates = holidaysService.getSundayDates(fromDate, toDate);

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
        var allRelevantDates = new ArrayList<LocalDate>();
        allRelevantDates.addAll(saturdayDates);
        allRelevantDates.addAll(sundayDates);
        allRelevantDates.addAll(bankHolidayDatesInMonth);

        query.where(QAppearance.appearance.attendanceDate.in(allRelevantDates));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        CourtLocation courtLocation = courtLocationService.getCourtLocation(request.getLocCode());

        if (courtLocation == null) {
            throw new MojException.BadRequest("Invalid loc code: " + request.getLocCode(), null);
        }

        Map<String, AbstractReportResponse.DataTypeValue> headings = new ConcurrentHashMap<>(Map.of(
            "date_from", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().withDayOfMonth(1)))
                .build(),
            "date_to", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                .build(),
            "total", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Total")
                .dataType(Long.class.getSimpleName())
                .value(tableData.getData().size())
                .build(),
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report created")
                .dataType(LocalDate.class.getSimpleName())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Time created")
                .dataType(LocalDateTime.class.getSimpleName())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build()
        ));

        Map.Entry<String, AbstractReportResponse.DataTypeValue> entry =
            getCourtNameHeader(courtLocation);
        headings.put(entry.getKey(), entry.getValue());
        return headings;
    }


    @Override
    protected void postProcessTableData(StandardReportRequest request,
                                        AbstractReportResponse.TableData<StandardTableData> tableData) {
        final String id = "day";

        tableData.getData()
            .forEach(stringObjectLinkedHashMap -> {
                Object attendanceDate = stringObjectLinkedHashMap.get(DataType.ATTENDANCE_DATE.getId());
                if (attendanceDate == null) {
                    return;
                }
                LocalDate date = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(attendanceDate.toString()));
                stringObjectLinkedHashMap.put(id, date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL,
                                                                                        java.util.Locale.ENGLISH));

            });
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }


    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireLocCode {

    }
}
