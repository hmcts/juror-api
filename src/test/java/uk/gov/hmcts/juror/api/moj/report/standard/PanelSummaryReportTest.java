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
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorTrial;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.LawOfDemeter")
class PanelSummaryReportTest extends AbstractStandardReportTestSupport<PanelSummaryReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    private TrialRepository trialRepository;

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        this.trialRepository = mock(TrialRepository.class);
    }


    public PanelSummaryReportTest() {
        super(QJurorTrial.jurorTrial,
            PanelSummaryReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME);
    }


    @Override
    public PanelSummaryReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PanelSummaryReport(poolRequestRepository, trialRepository);
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {

        request.setTrialNumber(TestConstants.VALID_TRIAL_NUMBER);
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);

        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorTrial.jurorTrial.trialNumber.eq(TestConstants.VALID_TRIAL_NUMBER));
        verify(query, times(1))
            .where(QJurorTrial.jurorTrial.locCode.eq(SecurityUtil.getActiveOwner()));
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData,
        List<LinkedHashMap<String, Object>> data) {

        Trial trial = mock(Trial.class);

        doReturn(trial).when(report).getTrial(any(), any());

        CourtLocation courtLocation = mock(CourtLocation.class);

        Courtroom courtroom = mock(Courtroom.class);

        Judge judge = mock(Judge.class);

        when(trial.getCourtLocation()).thenReturn(courtLocation);
        when(trial.getCourtroom()).thenReturn(courtroom);
        when(trial.getCourtLocation().getLocCode()).thenReturn("415");
        when(trial.getCourtLocation().getName()).thenReturn("Chester");
        when(trial.getCourtroom().getDescription()).thenReturn("COURT 3");

        when(trial.getDescription()).thenReturn("Someone Name");

        when(trial.getJudge()).thenReturn(judge);

        when(trial.getJudge().getName()).thenReturn("Judge Dredd");

        when(request.getTrialNumber()).thenReturn("T000000001");


        when(data.size()).thenReturn(2);
        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "panel_summary", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Panel Summary")
                    .dataType(Long.class.getSimpleName())
                    .value(2)
                    .build(),
                "trial_number", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Trial Number")
                    .dataType(String.class.getSimpleName())
                    .value(TestConstants.VALID_TRIAL_NUMBER)
                    .build(),
                "names", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Names")
                    .dataType(String.class.getSimpleName())
                    .value("Someone Name")
                    .build(),
                "court_room", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Court Room")
                    .dataType(String.class.getSimpleName())
                    .value("COURT 3")
                    .build(),
                "judge", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Judge")
                    .dataType(String.class.getSimpleName())
                    .value("Judge Dredd")
                    .build(),
                "court_name", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType(String.class.getSimpleName())
                    .value("Chester (415)")
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
            .trialNumber(TestConstants.VALID_TRIAL_NUMBER)
            .build();
    }

    @Test
    void negativeMissingTrialNumber() {
        StandardReportRequest request = getValidRequest();
        request.setTrialNumber(null);
        assertValidationFails(request, new ValidationFailure("trialNumber", "must not be blank"));
    }

}
