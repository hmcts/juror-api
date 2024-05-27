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
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrialStatisticsReportTest extends AbstractStandardReportTestSupport<TrialStatisticsReport> {

    private static final LocalDate FROM_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2024, 1, 30);
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public TrialStatisticsReportTest() {
        super(QTrial.trial,
            TrialStatisticsReport.RequestValidator.class,
            DataType.TRIAL_JUDGE_NAME,
            DataType.TRIAL_TYPE,
            DataType.TRIAL_NUMBER,
            DataType.TRIAL_PANELLED_COUNT,
            DataType.TRIAL_JURORS_COUNT,
            DataType.TRIAL_NUMBER_START_DATE,
            DataType.TRIAL_NUMBER_END_DATE);
        setHasPoolRepository(false);
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
    public TrialStatisticsReport createReport(PoolRequestRepository poolRequestRepository) {
        return new TrialStatisticsReport();
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(FROM_DATE)
            .toDate(TO_DATE)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {

        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));


        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(TestConstants.VALID_COURT_LOCATION);


        report.preProcessQuery(query, request);

        verify(query, times(1)).where(
            QTrial.trial.trialStartDate.between(request.getFromDate(), request.getToDate())
                .or(QTrial.trial.trialEndDate.between(request.getFromDate(), request.getToDate()))
        );
        verify(query, times(1)).where(QTrial.trial.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION));


        verify(report, times(1)).addGroupBy(query,
            DataType.TRIAL_JUDGE_NAME,
            DataType.TRIAL_TYPE,
            DataType.TRIAL_NUMBER,
            DataType.TRIAL_NUMBER_START_DATE,
            DataType.TRIAL_NUMBER_END_DATE,
            DataType.TRIAL_COURT_LOCATION
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {
        when(request.getFromDate()).thenReturn(FROM_DATE);
        when(request.getToDate()).thenReturn(TO_DATE);
        Map<String, StandardReportResponse.DataTypeValue> headings = report.getHeadings(request, tableData);
        assertHeadingContains(headings,
            request,
            false,
            Map.of(
                "date_from",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date from")
                    .dataType("LocalDate")
                    .value("2024-01-01")
                    .build(),
                "date_to",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date to")
                    .dataType("LocalDate")
                    .value("2024-01-30")
                    .build()
            )
        );
        return headings;
    }

    @Test
    void negativeMissingToDate() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }

    @Test
    void negativeMissingFromDate() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }
}
