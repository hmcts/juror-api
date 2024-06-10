package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PoolRatioReportTest extends AbstractStandardReportTestSupport<PoolRatioReport> {
    private static final LocalDate FROM_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2024, 1, 30);
    private static final List<String> COURTS = List.of("415", "414", "413");


    public PoolRatioReportTest() {
        super(QJurorPool.jurorPool,
            PoolRatioReport.RequestValidator.class,
            DataType.COURT_LOCATION_NAME_AND_CODE_JP,
            DataType.POOL_NUMBER_BY_JP,
            DataType.TOTAL_REQUESTED,
            DataType.TOTAL_DEFERRED,
            DataType.TOTAL_SUMMONED,
            DataType.TOTAL_SUPPLIED);
        setHasPoolRepository(false);
    }


    @Override
    public PoolRatioReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PoolRatioReport();
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
            .where(QJurorPool.jurorPool.pool.returnDate
                .between(FROM_DATE, TO_DATE));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.in(COURTS));
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.pool.courtLocation.name.asc(),
                QJurorPool.jurorPool.pool.courtLocation.locCode.asc());

        verify(query, times(1))
            .groupBy(
                QJurorPool.jurorPool.pool.poolNumber,
                QJurorPool.jurorPool.pool.numberRequested,
                QJurorPool.jurorPool.pool.courtLocation.locCode,
                QJurorPool.jurorPool.pool.courtLocation.name
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
        expected.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .dataType(LocalDate.class.getSimpleName())
            .value("2024-01-01")
            .build());
        expected.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .dataType(LocalDate.class.getSimpleName())
            .value("2024-01-30")
            .build());

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            expected);

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