package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PanelMembersStatusReportTest extends AbstractStandardReportTestSupport<PanelMembersStatusReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private TrialRepository trialRepository;

    public PanelMembersStatusReportTest() {
        super(
            QPanel.panel,
            PanelMembersStatusReport.RequestValidator.class,
            DataType.JUROR_NUMBER_FROM_TRIAL,
            DataType.PANEL_STATUS
        );

        setHasPoolRepository(false);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        this.trialRepository = mock(TrialRepository.class);
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @Override
    public PanelMembersStatusReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PanelMembersStatusReport(trialRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
           .reportType("PanelMembersStatusReport")
           .trialNumber("111111")
           .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        request.setTrialNumber("111111");

        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        report.preProcessQuery(query, request);

        verify(query).where(QPanel.panel.trial.trialNumber.eq(request.getTrialNumber()));
        verify(query).where(QPanel.panel.trial.courtLocation.owner.eq(SecurityUtil.getActiveOwner()));
        verify(query).orderBy(QPanel.panel.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        request.setTrialNumber("111111");
        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(TestConstants.VALID_COURT_LOCATION);

        Trial trial = mock(Trial.class);
        CourtLocation courtLocation = mock(CourtLocation.class);
        Courtroom courtroom = mock(Courtroom.class);
        Judge judge = mock(Judge.class);

        doReturn(trial).when(report).getTrial(any(), any());

        when(trial.getCourtLocation()).thenReturn(courtLocation);
        when(trial.getCourtroom()).thenReturn(courtroom);
        when(trial.getJudge()).thenReturn(judge);
        when(trial.getDescription()).thenReturn("CName1 et el");
        when(trial.getCourtroom().getDescription()).thenReturn("Court Room 1");
        when(trial.getJudge().getName()).thenReturn("Judge 1");
        when(trial.getCourtLocation().getLocCode()).thenReturn("415");
        when(trial.getCourtLocation().getName()).thenReturn("CHESTER");

        when(request.getTrialNumber()).thenReturn("111111");

        Map<String, AbstractReportResponse.DataTypeValue> expected = new ConcurrentHashMap<>();
        expected.put("names", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Names")
            .dataType(String.class.getSimpleName())
            .value("CName1 et el")
            .build());
        expected.put("trial_number", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Trial Number")
            .dataType(String.class.getSimpleName())
            .value("111111")
            .build());
        expected.put("court_room", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Court Room")
            .dataType(String.class.getSimpleName())
            .value("Court Room 1")
            .build());
        expected.put("judge", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Judge")
            .dataType(String.class.getSimpleName())
            .value("Judge 1")
            .build());
        expected.put("court_name", getCourtNameEntry().getValue());

        Map<String, AbstractReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(
            map,
            request,
            false,
            expected
        );

        return map;
    }

    @Test
    void negativeMissingTrialNumber() {
        StandardReportRequest request = getValidRequest();
        request.setTrialNumber(null);

        assertValidationFails(request, new ValidationFailure("trialNumber", "must not be blank"));
    }
}
