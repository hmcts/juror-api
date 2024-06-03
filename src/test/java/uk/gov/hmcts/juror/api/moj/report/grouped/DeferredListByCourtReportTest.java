package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;

import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class DeferredListByCourtReportTest extends AbstractGroupedReportTestSupport<DeferredListByCourtReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public DeferredListByCourtReportTest() {
        super(QJurorPool.jurorPool,
            DeferredListByCourtReport.RequestValidator.class,
            ReportGroupBy.builder()
                .dataType(DataType.COURT_LOCATION_NAME_AND_CODE)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.DEFERRED_TO,
            DataType.NUMBER_DEFERRED);
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
    public DeferredListByCourtReport createReport(PoolRequestRepository poolRequestRepository) {
        return new DeferredListByCourtReport(poolRequestRepository);
    }

    @Override
    @DisplayName("positivePreProcessQueryTypicalCourt")
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);

        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.deferralDate.isNotNull());
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.deferralDate.asc());
        verify(query, times(1))
            .where(QJurorPool.jurorPool.owner.eq(TestConstants.VALID_COURT_LOCATION));
        verify(query, times(1)).groupBy(
            QCourtLocation.courtLocation.name,
            QCourtLocation.courtLocation.locCode,
            QJurorPool.jurorPool.deferralDate
        );
        verifyNoMoreInteractions(query);
    }

    @Test
    void positivePreProcessQueryTypicalBureau() {
        JPAQuery<Tuple> query = mock(JPAQuery.class,
            withSettings().defaultAnswer(RETURNS_SELF));
        StandardReportRequest request = new StandardReportRequest();

        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(false);

        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.deferralDate.isNotNull());
        verify(query, times(1)).groupBy(
            QCourtLocation.courtLocation.name,
            QCourtLocation.courtLocation.locCode,
            QJurorPool.jurorPool.deferralDate
        );
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.deferralDate.asc());
        verifyNoMoreInteractions(query);
    }

    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {

        when(data.getSize()).thenReturn(3L);

        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "total_deferred",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total deferred")
                    .dataType(Long.class.getSimpleName())
                    .value(3L)
                    .build()
            ));
        verify(tableData, times(1)).getData();
        return map;
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .build();
    }
}