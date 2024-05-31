package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExcusedAndDisqualifiedListReportTest extends AbstractGroupedReportTestSupport<ExcusedAndDisqualifiedListReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public ExcusedAndDisqualifiedListReportTest() {
        super(QJurorPool.jurorPool,
            ExcusedAndDisqualifiedListReport.RequestValidator.class,
            ReportGroupBy.builder()
                .dataType(DataType.EXCUSAL_DISQUAL_TYPE)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.EXCUSAL_DISQUAL_CODE,
            DataType.EXCUSAL_DISQUAL_DECISION_DATE);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        super.beforeEach();
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @Override
    public ExcusedAndDisqualifiedListReport createReport(PoolRequestRepository poolRequestRepository) {
        return new ExcusedAndDisqualifiedListReport(poolRequestRepository);
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

        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
            .thenReturn(TestConstants.VALID_COURT_LOCATION);
        securityUtilMockedStatic.when(SecurityUtil::isCourt)
            .thenReturn(true);

        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(TestConstants.VALID_POOL_NUMBER));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.owner.eq(TestConstants.VALID_COURT_LOCATION));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.juror.disqualifyCode.isNotNull()
                .or(QJurorPool.jurorPool.juror.excusalCode.isNotNull()));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {

        when(data.getSize()).thenReturn(5L);

        Map<String, AbstractReportResponse.DataTypeValue> headings = report.getHeadings(request, tableData);
        assertHeadingContains(headings,
            request,
            true,
            Map.of("total_excused_and_disqualified",
                AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total excused and disqualified")
                    .dataType("Long")
                    .value(5L)
                    .build())
        );

        return headings;
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
