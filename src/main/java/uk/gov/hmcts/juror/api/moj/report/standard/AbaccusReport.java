package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse.DataTypeValue;

@Component
public class AbaccusReport extends AbstractStandardReport {

    @Autowired
    public AbaccusReport() {
        super(
            null,
            QBulkPrintData.bulkPrintData,
            DataType.DOCUMENT_CODE,
            DataType.TOTAL_SENT_FOR_PRINTING,
            DataType.DATE_SENT);

        isBureauUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QBulkPrintData.bulkPrintData.creationDate.between(request.getFromDate(), request.getToDate()))
            .orderBy(QBulkPrintData.bulkPrintData.formAttribute.formType.asc());
        addGroupBy(query, DataType.DOCUMENT_CODE, DataType.DATE_SENT);

    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return AbaccusReport.RequestValidator.class;
    }

    @Override
    public Map<String, DataTypeValue> getHeadings(StandardReportRequest request,
                                  StandardReportResponse.TableData<StandardTableData> tableData) {
        Map<String, DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("date_from", DataTypeValue.builder()
            .displayName("Date from")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .build());
        map.put("date_to", DataTypeValue.builder()
            .displayName("Date to")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .build());
        return map;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate {
    }
}
