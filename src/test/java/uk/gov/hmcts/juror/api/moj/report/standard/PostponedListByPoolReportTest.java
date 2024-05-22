package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostponedListByPoolReportTest extends AbstractStandardReportTestSupport<PostponedListByPoolReport> {

    public PostponedListByPoolReportTest() {
        super(QJurorPool.jurorPool,
            PostponedListByPoolReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.JUROR_POSTCODE,
            DataType.POSTPONED_TO);
    }


    @Override
    public PostponedListByPoolReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PostponedListByPoolReport(poolRequestRepository);
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        request.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(TestConstants.VALID_POOL_NUMBER)
                .and(QJurorPool.jurorPool.deferralDate.isNotNull())
                .and(QJurorPool.jurorPool.deferralCode.eq(ExcusalCodeEnum.P.getCode())));
        verify(query, times(1)).orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        when(data.size()).thenReturn(2);
        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            true,
            Map.of(
                "total_postponed",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total postponed")
                    .dataType(Long.class.getSimpleName())
                    .value(2)
                    .build()
            ));
        verify(tableData, times(1)).getData();
        verify(data, times(1)).size();
        return map;
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .build();
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