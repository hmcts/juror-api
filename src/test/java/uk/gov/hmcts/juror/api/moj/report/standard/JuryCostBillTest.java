package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QReportsJurorPayments;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ReportsJurorPaymentsDataTypes;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JuryCostBillTest extends AbstractStandardReportTestSupport<JuryCostBill> {

    private CourtLocationRepository courtLocationRepository;
    private TrialRepository trialRepository;

    public JuryCostBillTest() {
        super(QReportsJurorPayments.reportsJurorPayments,
              JuryCostBill.RequestValidator.class,
              ReportsJurorPaymentsDataTypes.ATTENDANCE_DATE,
              ReportsJurorPaymentsDataTypes.FINANCIAL_LOSS_DUE_SUM,
              ReportsJurorPaymentsDataTypes.TRAVEL_DUE_SUM,
              ReportsJurorPaymentsDataTypes.SUBSISTENCE_DUE_SUM,
              ReportsJurorPaymentsDataTypes.SMARTCARD_DUE_SUM,
              ReportsJurorPaymentsDataTypes.TOTAL_DUE_SUM,
              ReportsJurorPaymentsDataTypes.TOTAL_PAID_SUM);

        setHasPoolRepository(false);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        this.trialRepository = mock(TrialRepository.class);
        super.beforeEach();
    }

    @AfterEach
    public void afterEach() {
        TestUtils.afterAll();
    }

    @Override
    public JuryCostBill createReport(PoolRequestRepository poolRequestRepository) {
        return new JuryCostBill(this.trialRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .trialNumber("TRIALNUMBER")
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        TestUtils.mockSecurityUtil(BureauJwtPayload.builder()
                                       .locCode(TestConstants.VALID_COURT_LOCATION)
                                       .owner(TestConstants.VALID_COURT_LOCATION)
                                       .userType(UserType.COURT)
                                       .build());

        report.preProcessQuery(query, request);
        verify(query).where(QReportsJurorPayments.reportsJurorPayments.trialNumber.eq("TRIALNUMBER"));
        verify(query).where(QReportsJurorPayments.reportsJurorPayments.locCode.eq(TestConstants.VALID_COURT_LOCATION));
        verify(report, times(1)).addGroupBy(query, ReportsJurorPaymentsDataTypes.ATTENDANCE_DATE);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {
        TestUtils.mockSecurityUtil(BureauJwtPayload.builder()
                                       .locCode(TestConstants.VALID_COURT_LOCATION)
                                       .owner(TestConstants.VALID_COURT_LOCATION)
                                       .userType(UserType.COURT)
                                       .build());

        String trialNumber = "TRIALNUMBER";
        when(request.getTrialNumber()).thenReturn(trialNumber);

        String courtName = "CHESTER";
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(TestConstants.VALID_COURT_LOCATION);
        courtLocation.setName(courtName);
        when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
            .thenReturn(Optional.of(courtLocation));

        String description = "TRIAL_DEFENDANTS";
        String judge = "JUDGE_NAME";
        String courtroom = "ROOM_NAME";
        Trial trial = Trial.builder()
            .description(description)
            .trialType(TrialType.CRI)
            .judge(Judge.builder()
                       .name(judge)
                       .build())
            .courtroom(Courtroom.builder()
                           .description(courtroom)
                           .build())
            .courtLocation(courtLocation)
            .trialStartDate(LocalDate.now().minusDays(1))
            .build();
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode(trialNumber,
                                                                      TestConstants.VALID_COURT_LOCATION))
            .thenReturn(Optional.of(trial));


        Map<String, GroupedReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, Map.of(
            "trial_number", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Trial Number")
                .dataType("String")
                .value(trialNumber)
                .build(),
            "names", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Names")
                .dataType("String")
                .value(description)
                .build(),
            "trial_type", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Trial type")
                .dataType("String")
                .value(TrialType.CRI.getDescription())
                .build(),
            "trial_start_date", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Trial start date")
                .dataType("LocalDate")
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(1)))
                .build(),
            "court_room", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Court Room")
                .dataType("String")
                .value(courtroom)
                .build(),
            "judge", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Judge")
                .dataType("String")
                .value(judge)
                .build(),
            "court_name", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Court Name")
                .dataType("String")
                .value("CHESTER (415)")
                .build()));
        return map;
    }

    @Test
    void negativeMissingTrialNumber() {
        StandardReportRequest request = getValidRequest();
        request.setTrialNumber(null);
        assertValidationFails(request, new ValidationFailure("trialNumber", "must not be blank"));
    }
}
