package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.messages.QMessage;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OutgoingSMSMessagesReportTest extends AbstractStandardReportTestSupport<OutgoingSMSMessagesReport> {

    private static final LocalDate FROM_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2024, 1, 31);
    private static final List<String> COURTS = List.of("415", "416", "417");

    public OutgoingSMSMessagesReportTest() {
        super(QMessage.message,
              OutgoingSMSMessagesReport.RequestValidator.class,
              DataType.COURT_LOCATION_NAME_AND_CODE_MP,
              DataType.REMINDER,
              DataType.FAILED_TO_ATTEND,
              DataType.DATE_AND_TIME_CHANGED,
              DataType.TIME_CHANGED,
              DataType.COMPLETE_ATTENDED,
              DataType.COMPLETE_NOT_NEEDED,
              DataType.NEXT_DATE,
              DataType.ON_CALL_OSR,
              DataType.PLEASE_CONTACT,
              DataType.DELAYED_START,
              DataType.SELECTION,
              DataType.BAD_WEATHER,
              DataType.BRING_LUNCH,
              DataType.CHECK_JUNK_EMAIL,
              DataType.EXCUSED,
              DataType.TOTAL_SMS_SENT);
        setHasPoolRepository(false);
    }

    @Override
    public OutgoingSMSMessagesReport createReport(PoolRequestRepository poolRequestRepository) {
        return new OutgoingSMSMessagesReport();
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(FROM_DATE)
            .toDate(TO_DATE)
            .courts(COURTS)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QMessage.message.fileDatetime.between(
                FROM_DATE.atStartOfDay(),
                TO_DATE.atTime(LocalTime.MAX)
            ));

        verify(query, times(1))
            .where(QMessage.message.locationCode.locCode.in(COURTS));

        verify(query, times(1))
            .where(QMessage.message.phone.isNotNull());

        verify(query, times(1))
            .orderBy(
                QMessage.message.locationCode.name.asc(),
                QMessage.message.locationCode.locCode.asc()
            );

        verify(query, times(1))
            .groupBy(
                QMessage.message.locationCode.locCode,
                QMessage.message.locationCode.name
            );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        when(request.getFromDate()).thenReturn(FROM_DATE);
        when(request.getToDate()).thenReturn(TO_DATE);

        Map<String, AbstractReportResponse.DataTypeValue> expected = new ConcurrentHashMap<>();

        expected.put("outgoing_sms_title", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Outgoing SMS Messages")
            .dataType(String.class.getSimpleName())
            .value("Outgoing SMS Message Report")
            .build());

        expected.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .dataType(LocalDate.class.getSimpleName())
            .value("2024-01-01")
            .build());

        expected.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .dataType(LocalDate.class.getSimpleName())
            .value("2024-01-31")
            .build());

        expected.put("total_sms_sent", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Total SMS sent")
            .dataType(Long.class.getSimpleName())
            .value("0")
            .build());


        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);

        assertHeadingContains(map, request, false, expected);

        return map;
    }

    @Test
    void negativeMissingFromDate() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }

    @Test
    void negativeMissingToDate() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }

    @Test
    void negativeMissingCourts() {
        StandardReportRequest request = getValidRequest();
        request.setCourts(null);
        assertValidationFails(request,
                              new ValidationFailure("courts", "must not be empty"),
                              new ValidationFailure("courts", "must not be null"));
    }

    @Test
    void negativeEmptyCourts() {
        StandardReportRequest request = getValidRequest();
        request.setCourts(List.of());
        assertValidationFails(request, new ValidationFailure("courts", "must not be empty"));
    }
}
