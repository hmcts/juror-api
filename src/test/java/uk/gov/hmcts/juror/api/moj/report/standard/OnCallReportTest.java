package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.TestConstants.VALID_POOL_NUMBER;

public class OnCallReportTest extends AbstractStandardReportTestSupport<OnCallReport> {

    private final PoolRequestRepository poolRequestRepository;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    public void afterEach() {
        securityUtilMockedStatic.close();
    }


    public OnCallReportTest() {
        super(QJurorPool.jurorPool,
            OnCallReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.MOBILE_PHONE,
            DataType.HOME_PHONE);
        setAllowBureau(false);
        this.poolRequestRepository = mock(PoolRequestRepository.class);
    }

    @Override
    public OnCallReport createReport(PoolRequestRepository poolRequestRepository) {
        return new OnCallReport(poolRequestRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .poolNumber(VALID_POOL_NUMBER)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));

        request.setLocCode(TestConstants.VALID_COURT_LOCATION);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);

        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.getActiveOwner()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.onCall.eq(true));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.isActive.eq(true));
        verify(query, times(1))
            .orderBy(QJuror.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        when(data.size()).thenReturn(2);

        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);

        PoolRequest poolRequest = createPoolRequest(VALID_POOL_NUMBER);
        doReturn(VALID_POOL_NUMBER).when(request).getPoolNumber();
        when(report.getPoolRequestRepository().findByPoolNumber(VALID_POOL_NUMBER)).thenReturn(Optional.of
        (poolRequest));

        Map<String, AbstractReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, true,
            Map.of(
            "total_on_call", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total on call")
                    .dataType(Long.class.getSimpleName())
                    .value(2)
                    .build()
        ));
        verify(tableData, times(1)).getData();
        verify(data, times(1)).size();
        return map;
    }

    private PoolRequest createPoolRequest(String poolNumber) {
        PoolType poolType = new PoolType("CRO", "CROWN COURT");
        CourtLocation courtLocation =
            CourtLocation.builder().locCourtName("CHESTER (415)").name("CHESTER").locCode("415").build();
        return PoolRequest.builder()
            .poolNumber(poolNumber)
            .totalNoRequired(20)
            .dateCreated(LocalDateTime.now())
            .owner("415")
            .poolType(poolType)
            .returnDate(LocalDate.of(2023, 1, 1))
            .courtLocation(courtLocation)
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


