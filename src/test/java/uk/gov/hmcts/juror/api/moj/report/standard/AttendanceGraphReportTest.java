package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
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

public class AttendanceGraphReportTest extends AbstractStandardReportTestSupport<AttendanceGraphReport> {

    private static final LocalDate FROM_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2024, 1, 30);

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public AttendanceGraphReportTest() {
        super(QAppearance.appearance,
            AttendanceGraphReport.RequestValidator.class,
            DataType.ATTENDANCE_DATE,
            DataType.ATTENDANCE_COUNT);
        setHasPoolRepository(false);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void afterEach() {
        this.securityUtilMockedStatic.close();
    }

    @Override
    public AttendanceGraphReport createReport(PoolRequestRepository poolRequestRepository) {
        return new AttendanceGraphReport();
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
        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(TestConstants.VALID_COURT_LOCATION);

        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));

        report.preProcessQuery(query, request);

        verify(query, times(1)).where(
            QAppearance.appearance.attendanceDate.between(FROM_DATE, TO_DATE));
        verify(query, times(1)).where(QAppearance.appearance.locCode.eq(TestConstants.VALID_COURT_LOCATION));
        verify(query, times(1)).where(QAppearance.appearance.attendanceType.notIn(
            AttendanceType.ABSENT,
            AttendanceType.NON_ATTENDANCE,
            AttendanceType.NON_ATTENDANCE_LONG_TRIAL));
        verify(report, times(1))
            .addGroupBy(query, DataType.ATTENDANCE_DATE);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        Map<String, AbstractReportResponse.DataTypeValue> headings = report.getHeadings(request, tableData);
        assertHeadingContains(headings,
            request,
            false,
            Map.of());
        return headings;
    }

    @Test
    void negativeMissingFromDate() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }

    @Test
    void negativeMissingToDate() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }
}
