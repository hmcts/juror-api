package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class BallotPanelTrialReportTest extends AbstractStandardReportTestSupport<BallotPanelTrialReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public BallotPanelTrialReportTest() {
        super(
            QTrial.trial,
            BallotPanelTrialReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.JUROR_POSTCODE
        );
        setHasPoolRepository(false);
    }

    @Override
    public BallotPanelTrialReport createReport(PoolRequestRepository poolRequestRepository) {
        return new BallotPanelTrialReport();
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    public void afterEach() {
        TestUtils.afterAll();
        securityUtilMockedStatic.close();
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .trialNumber(TestConstants.VALID_TRIAL_NUMBER)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {

        request.setTrialNumber(TestConstants.VALID_TRIAL_NUMBER);
        securityUtilMockedStatic.when(SecurityUtil::getCourts).thenReturn(List.of(TestConstants.VALID_COURT_LOCATION));
        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QTrial.trial.trialNumber.eq(request.getTrialNumber()));
        verify(query, times(1))
            .where(QTrial.trial.courtLocation.locCode.in(SecurityUtil.getCourts()));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, Map.of());
        return map;
    }

    @Test
    void negativeMissingTrialNumber() {
        StandardReportRequest request = getValidRequest();
        request.setTrialNumber(null);
        assertValidationFails(request, new ValidationFailure("trialNumber", "must not be blank"));
    }
}
