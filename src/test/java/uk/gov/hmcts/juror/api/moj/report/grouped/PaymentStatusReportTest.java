package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QPaymentData;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.datatypes.PaymentStatusDataTypes;
import uk.gov.hmcts.juror.api.moj.report.grouped.groupby.GroupByPaymentStatus;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentStatusReportTest extends AbstractGroupedReportTestSupport<PaymentStatusReport> {


    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private CourtLocationService courtLocationService;

    public PaymentStatusReportTest() {
        super(QPaymentData.paymentData,
            PaymentStatusReport.RequestValidator.class,
            new GroupByPaymentStatus(),
            PaymentStatusDataTypes.CREATION_DATE,
            PaymentStatusDataTypes.TOTAL_AMOUNT,
            PaymentStatusDataTypes.PAYMENTS,
            PaymentStatusDataTypes.CONSOLIDATED_FILE_REFERENCE);
        setHasPoolRepository(false);
    }


    @BeforeEach
    public void beforeEach() {
        this.courtLocationService = mock(CourtLocationService.class);
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        super.beforeEach();
    }

    @AfterEach
    public void afterEach() {
        if (securityUtilMockedStatic != null) {
            securityUtilMockedStatic.close();
        }
    }

    @Override
    public PaymentStatusReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PaymentStatusReport(courtLocationService);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {

        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(TestConstants.VALID_COURT_LOCATION);

        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));

        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QPaymentData.paymentData.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION));

        verify(report, times(1)).addGroupBy(
            query,
            PaymentStatusDataTypes.CREATION_DATE,
            PaymentStatusDataTypes.CONSOLIDATED_FILE_REFERENCE,
            PaymentStatusDataTypes.EXTRACTED
        );
        securityUtilMockedStatic.verify(SecurityUtil::getLocCode, times(1));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(StandardReportRequest request,
                                                                                        AbstractReportResponse.TableData<GroupedTableData> tableData,
                                                                                        GroupedTableData data) {
        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(TestConstants.VALID_COURT_LOCATION);
        CourtLocation courtLocation = mock(CourtLocation.class);

        doNothing().when(report).addCourtNameHeader(any(), any());
        when(courtLocationService.getCourtLocation(TestConstants.VALID_COURT_LOCATION))
            .thenReturn(courtLocation);

        Map<String, AbstractReportResponse.DataTypeValue> headers = report.getHeadings(request, tableData);

        verify(courtLocationService, times(1))
            .getCourtLocation(TestConstants.VALID_COURT_LOCATION);
        verify(report, times(1))
            .addCourtNameHeader(new HashMap<>(), courtLocation);
        return headers;
    }
}
