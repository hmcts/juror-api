package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrentPoolStatusReportTest extends AbstractStandardReportTestSupport<CurrentPoolStatusReport> {

    public CurrentPoolStatusReportTest() {
        super(QJurorPool.jurorPool,
              CurrentPoolStatusReport.RequestValidator.class,
              DataType.JUROR_NUMBER,
              DataType.FIRST_NAME,
              DataType.LAST_NAME,
              DataType.STATUS,
              DataType.DEFERRALS,
              DataType.ABSENCES,
              DataType.CONTACT_DETAILS,
              DataType.WARNING);
    }

    @Override
    public CurrentPoolStatusReport createReport(PoolRequestRepository poolRequestRepository) {
        EntityManager entityManager = mock(EntityManager.class);
        return new CurrentPoolStatusReport(poolRequestRepository, entityManager);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();

        // Mock the database query methods to return test values
        doReturn(3L).when(report).getJurorsAttendedCount(anyString());
        doReturn(5L).when(report).getPoolMembersCount(anyString());
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));
        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(TestConstants.VALID_POOL_NUMBER));
        verify(query, times(1))
            .leftJoin(QAppearance.appearance);
        verify(query, times(1))
            .on(QAppearance.appearance.jurorNumber.eq(QJuror.juror.jurorNumber),
                QAppearance.appearance.attendanceType.eq(AttendanceType.ABSENT));
        verify(report, times(1)).addGroupBy(query,
                                            DataType.JUROR_NUMBER,
                                            DataType.FIRST_NAME,
                                            DataType.LAST_NAME,
                                            DataType.STATUS,
                                            DataType.DEFERRALS,
                                            DataType.CONTACT_DETAILS,
                                            DataType.WARNING
        );
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        when(data.size()).thenReturn(2);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);

        // Verify all three custom headings are present with correct values
        assertHeadingContains(map,
                              request,
                              true,
                              Map.of(
                                  "number_of_jurors_summoned",
                                  StandardReportResponse.DataTypeValue.builder()
                                      .displayName("Number of Jurors Summoned")
                                      .dataType(Long.class.getSimpleName())
                                      .value(2)
                                      .build(),
                                  "number_of_jurors_attended",
                                  StandardReportResponse.DataTypeValue.builder()
                                      .displayName("Number of Jurors Attended")
                                      .dataType(Long.class.getSimpleName())
                                      .value(3L)  // Mocked value from beforeEach()
                                      .build(),
                                  "total_pool_members",
                                  StandardReportResponse.DataTypeValue.builder()
                                      .displayName("Total Pool Members ")
                                      .dataType(Long.class.getSimpleName())
                                      .value(5L)  // Mocked value from beforeEach()
                                      .build()
                              ));

        verify(tableData, times(1)).getData();
        verify(data, times(1)).size();
        verify(report, times(1)).getJurorsAttendedCount(TestConstants.VALID_POOL_NUMBER);
        verify(report, times(1)).getPoolMembersCount(TestConstants.VALID_POOL_NUMBER);

        return map;
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
