package uk.gov.hmcts.juror.api.moj.report.grouped;

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
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QReportsJurorPayments;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
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


class TrialAttendanceReportTest extends AbstractGroupedReportTestSupport<TrialAttendanceReport> {

    private CourtLocationRepository courtLocationRepository;
    private TrialRepository trialRepository;

    public TrialAttendanceReportTest() {
        super(
            QReportsJurorPayments.reportsJurorPayments,
            TrialAttendanceReport.RequestValidator.class,
            ReportGroupBy.builder()
                .dataType(ReportsJurorPaymentsDataTypes.ATTENDANCE_DATE)
                .removeGroupByFromResponse(true)
                .build(),
            ReportsJurorPaymentsDataTypes.JUROR_NUMBER,
            ReportsJurorPaymentsDataTypes.FIRST_NAME,
            ReportsJurorPaymentsDataTypes.LAST_NAME,
            ReportsJurorPaymentsDataTypes.CHECKED_IN,
            ReportsJurorPaymentsDataTypes.CHECKED_OUT,
            ReportsJurorPaymentsDataTypes.HOURS_ATTENDED,
            ReportsJurorPaymentsDataTypes.ATTENDANCE_AUDIT,
            ReportsJurorPaymentsDataTypes.PAYMENT_AUDIT,
            ReportsJurorPaymentsDataTypes.TOTAL_DUE,
            ReportsJurorPaymentsDataTypes.TOTAL_PAID);

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
    public TrialAttendanceReport createReport(PoolRequestRepository poolRequestRepository) {
        return new TrialAttendanceReport(this.trialRepository);
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
        String locCode = "415";
        TestUtils.mockSecurityUtil(BureauJwtPayload.builder().locCode(locCode).userType(UserType.COURT).build());

        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QReportsJurorPayments.reportsJurorPayments.trialNumber.eq("TRIALNUMBER"));
        verify(query, times(1))
            .where(QReportsJurorPayments.reportsJurorPayments.locCode.eq(locCode));
        verify(query, times(1)).orderBy(
            QReportsJurorPayments.reportsJurorPayments.jurorNumber.asc());
    }

    @Override
    public Map<String, GroupedReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        GroupedReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {
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
