package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.messages.QMessage;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OutgoingSMSMessagesReport extends AbstractStandardReport {
    public OutgoingSMSMessagesReport() {
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
                DataType.CHECK_JUNK_EMAIL,
                DataType.BRING_LUNCH,
                DataType.EXCUSED,
                DataType.TOTAL_SMS_SENT);

    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {

        query.where(QMessage.message.fileDatetime.between(
                request.getFromDate().atStartOfDay(),
                request.getToDate().atTime(LocalTime.MAX)
        ));
        query.where(QMessage.message.locationCode.locCode.in(request.getCourts()));
        query.where(QMessage.message.phone.isNotNull());

        query.orderBy(
                QMessage.message.locationCode.name.asc(),
                QMessage.message.locationCode.locCode.asc()
        );
        query.groupBy(
                QMessage.message.locationCode.locCode,
                QMessage.message.locationCode.name
        );

    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
            StandardReportRequest request,
            AbstractReportResponse.TableData<StandardTableData> tableData) {


        LocalDateTime now = LocalDateTime.now();
        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        map.put("outgoing_sms_title",StandardReportResponse.DataTypeValue.builder()
                .displayName("Outgoing SMS Messages")
                .dataType(String.class.getSimpleName())
                .value("Outgoing SMS Message Report")
                .build());

        map.put("date_from", StandardReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
                .build());

        map.put("date_to", StandardReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
                .build());

        long totalSmsSent = 0;
        if (tableData != null && tableData.getData() != null) {
            StandardTableData data = (StandardTableData) tableData.getData();
            for (java.util.LinkedHashMap<String, Object> row : data) {
                Object totalValue = row.get("total_sms_sent");
                if (totalValue instanceof Number) {
                    totalSmsSent += ((Number) totalValue).longValue();
                }
            }
        }

        map.put("total_sms_sent", StandardReportResponse.DataTypeValue.builder()
                .displayName("Total SMS sent")
                .dataType(Long.class.getSimpleName())
                .value(String.valueOf(totalSmsSent))
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
