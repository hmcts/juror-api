package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("PMD.LawOfDemeter")
class DeferredListByDateReportTest extends AbstractReportTestSupport<DeferredListByDateReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public DeferredListByDateReportTest() {
        super(QJurorPool.jurorPool,
            DeferredListByDateReport.RequestValidator.class,
            DataType.DEFERRED_TO,
            DataType.NUMBER_DEFERRED);
    }

    @BeforeEach
    void beforeEach() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @Override
    public DeferredListByDateReport createReport(PoolRequestRepository poolRequestRepository) {
        return new DeferredListByDateReport(poolRequestRepository);
    }

    @Override
    @DisplayName("positivePreProcessQueryTypicalCourt")
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);

        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.deferralDate.isNotNull());
        verify(report, times(1)).addGroupBy(query,
            DataType.DEFERRED_TO
        );
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.deferralDate.asc());
        verify(query, times(1))
            .where(QJurorPool.jurorPool.owner.eq(TestConstants.VALID_COURT_LOCATION));
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
        verify(report, times(1)).addGroupBy(query,
            DataType.DEFERRED_TO
        );
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.deferralDate.asc());
        verifyNoMoreInteractions(query);
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        StandardReportResponse.TableData tableData,
        List<LinkedHashMap<String, Object>> data) {

        data.add(new LinkedHashMap<>(Map.of(
            DataType.DEFERRED_TO.getId(), "2024-03-15",
            DataType.NUMBER_DEFERRED.getId(), 1L
        )));
        data.add(new LinkedHashMap<>(Map.of(
            DataType.DEFERRED_TO.getId(), "2024-03-16",
            DataType.NUMBER_DEFERRED.getId(), 0L
        )));
        data.add(new LinkedHashMap<>(Map.of(
            DataType.DEFERRED_TO.getId(), "2024-03-17",
            DataType.NUMBER_DEFERRED.getId(), 9L
        )));


        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "total_deferred",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total deferred")
                    .dataType(Long.class.getSimpleName())
                    .value(10L)
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

    @Override
    protected Class<?> getValidatorClass() {
        return DeferredListByDateReport.RequestValidator.class;
    }
}