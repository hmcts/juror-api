package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ElectronicPoliceCheckReportTest extends AbstractStandardReportTestSupport<ElectronicPoliceCheckReport> {

    public ElectronicPoliceCheckReportTest() {
        super(QJurorPool.jurorPool,
            ElectronicPoliceCheckReport.RequestValidator.class,
            DataType.POOL_NUMBER_JP,
            DataType.POLICE_CHECK_RESPONDED,
            DataType.POLICE_CHECK_SUBMITTED,
            DataType.POLICE_CHECK_COMPLETE,
            DataType.POLICE_CHECK_TIMED_OUT,
            DataType.POLICE_CHECK_DISQUALIFIED);
        setHasPoolRepository(false);
    }

    @Override
    public ElectronicPoliceCheckReport createReport(PoolRequestRepository poolRequestRepository) {
        return new ElectronicPoliceCheckReport();
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .toDate(LocalDate.now().plusDays(10))
            .fromDate(LocalDate.now())
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        doNothing().when(report).addGroupBy(any(), any());
        report.preProcessQuery(query, request);
        verify(query, times(1)).where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.BUREAU_OWNER));
        verify(query, times(1)).where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED));

        verify(query, times(1)).where(QJuror.juror.policeCheckLastUpdate.between(
            request.getFromDate().atTime(LocalTime.MIN),
            request.getToDate().atTime(LocalTime.MAX))
        );
        verify(report, times(1)).addGroupBy(query, DataType.POOL_NUMBER_JP);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 1, 30);

        when(request.getFromDate()).thenReturn(fromDate);
        when(request.getToDate()).thenReturn(toDate);

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
}
