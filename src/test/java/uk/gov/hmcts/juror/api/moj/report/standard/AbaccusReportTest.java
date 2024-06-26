package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbaccusReportTest extends AbstractStandardReportTestSupport<AbaccusReport> {

    public AbaccusReportTest() {
        super(
            QBulkPrintData.bulkPrintData,
            AbaccusReport.RequestValidator.class,
            DataType.DOCUMENT_CODE,
            DataType.TOTAL_SENT_FOR_PRINTING,
            DataType.DATE_SENT);
        setHasPoolRepository(false);
    }

    @Override
    public AbaccusReport createReport(PoolRequestRepository poolRequestRepository) {
        return new AbaccusReport();
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(LocalDate.now().minus(7, ChronoUnit.DAYS))
            .toDate(LocalDate.now())
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        LocalDate fromDate = LocalDate.now().minus(7, ChronoUnit.DAYS);
        LocalDate toDate = LocalDate.now();

        request.setFromDate(fromDate);
        request.setToDate(toDate);

        doNothing().when(report).addGroupBy(any(), any(DataType[].class));
        report.preProcessQuery(query, request);

        verify(query).where(QBulkPrintData.bulkPrintData.creationDate.between(request.getFromDate(),
            request.getToDate()));
        verify(query).groupBy(QBulkPrintData.bulkPrintData.formAttribute.formType,
            QBulkPrintData.bulkPrintData.creationDate);

        verify(report, times(1)).addGroupBy(query,
            DataType.DOCUMENT_CODE,
            DataType.DATE_SENT
        );

        verify(query).orderBy(QBulkPrintData.bulkPrintData.formAttribute.formType.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        LocalDate fromDate = LocalDate.now().minus(7, ChronoUnit.DAYS);
        LocalDate toDate = LocalDate.now();

        when(request.getFromDate()).thenReturn(fromDate);
        when(request.getToDate()).thenReturn(toDate);

        Map<String, AbstractReportResponse.DataTypeValue> expected = new ConcurrentHashMap<>();
        expected.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .build());
        expected.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .build());

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            expected);

        return map;

    }

    @Test
    void negativeNullDateFrom() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }

    @Test
    void negativeNullDateTo() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }
}
