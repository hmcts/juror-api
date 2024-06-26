package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


class SummonedRespondedReportTest extends AbstractStandardReportTestSupport<SummonedRespondedReport> {
    public SummonedRespondedReportTest() {
        super(QJurorPool.jurorPool,
              SummonedRespondedReport.RequestValidator.class,
              DataType.JUROR_NUMBER,
              DataType.FIRST_NAME,
              DataType.LAST_NAME,
              DataType.JUROR_POSTAL_ADDRESS,
              DataType.JUROR_POSTCODE,
              DataType.SUMMONED_RESPONDED);
    }

    @Override
    public SummonedRespondedReport createReport(PoolRequestRepository poolRequestRepository) {
        return new SummonedRespondedReport(poolRequestRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(TestConstants.VALID_POOL_NUMBER));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.status.status.in(List.of(IJurorStatus.RESPONDED, IJurorStatus.SUMMONED)));
        verify(query, times(1)).orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
        verifyNoMoreInteractions(query);
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "pool_number",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool number")
                    .dataType("String")
                    .value(request.getPoolNumber())
                    .build()
            ));

        return map;
    }

    @Test
    void negativeMissingPoolNumber() {
        StandardReportRequest request = getValidRequest();
        request.setPoolNumber(null);
        assertValidationFails(request, new ValidationFailure("poolNumber", "must not be null"));
    }

    @Test
    void negativeInvalidPoolNumber() {
        StandardReportRequest request = getValidRequest();
        request.setPoolNumber(TestConstants.INVALID_POOL_NUMBER);
        assertValidationFails(request, new ValidationFailure("poolNumber", "must match \"^\\d{9}$\""));
    }
}
