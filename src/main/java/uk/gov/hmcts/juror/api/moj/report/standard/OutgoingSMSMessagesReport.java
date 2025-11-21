package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.messages.QMessage;
import uk.gov.hmcts.juror.api.moj.domain.messages.QMessageTemplate;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OutgoingSMSMessagesReport extends AbstractStandardReport {
    public OutgoingSMSMessagesReport(){
        super(QMessage.message,
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
              DataType.EXCUSED);

    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.leftJoin(QMessageTemplate.messageTemplate)
                .on(QMessageTemplate.messageTemplate.id.eq(QMessage.message.messageId));

        query.where(QMessage.message.fileDatetime.between(
                request.getFromDate().atStartOfDay(),
                request.getToDate().atTime(LocalTime.MAX)
        ));
        query.where(QMessage.message.locationCode.locCode.eq(request.getLocCode()));
        query.where(QMessage.message.phone.isNotNull());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
            StandardReportRequest request,
            AbstractReportResponse.TableData<StandardTableData> tableData) {


        LocalDateTime now = LocalDateTime.now();
        Map<String, AbstractReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        map.put("report_title",AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report Title")
                        .dataType(String.class.getSimpleName())
                                .value("Outgoing SMS Report")
                                        .build());
        map.put("date_from", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
                .build());
        map.put("date_to", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
                .build());
        return map;
    }




        @Override
    public Class<OutgoingSMSMessagesReport.RequestValidator> getRequestValidatorClass() {
        return OutgoingSMSMessagesReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate,
        AbstractReport.Validators.RequireCourts {

    }
}
