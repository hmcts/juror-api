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
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncompleteServiceReportTest extends AbstractStandardReportTestSupport<IncompleteServiceReport> {

    private CourtLocationRepository courtLocationRepository;

    public IncompleteServiceReportTest() {
        super(QJuror.juror,
            IncompleteServiceReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.POOL_NUMBER_BY_JP,
            DataType.LAST_ATTENDANCE_DATE,
            DataType.NEXT_ATTENDANCE_DATE);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        super.beforeEach();
    }

    @AfterEach
    public void afterEach() {
        // Close the static mock created by TestUtils.mockSecurityUtil()
        // This prevents "static mocking is already registered" errors in other tests
        TestUtils.afterAll();
    }

    @Override
    public IncompleteServiceReport createReport(PoolRequestRepository poolRequestRepository) {
        return new IncompleteServiceReport(poolRequestRepository, this.courtLocationRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .locCode(TestConstants.VALID_COURT_LOCATION)
            .date(LocalDate.now())
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {

        BureauJwtPayload payload = BureauJwtPayload.builder()
            .owner("415")
            .locCode("415")
            .login("COURT_USER")
            .userLevel("1")
            .userType(UserType.COURT)
            .activeUserType(UserType.COURT)
            .permissions(Set.of()) // Empty set = no SUPER_USER permission
            .staff(BureauJwtPayload.Staff.builder()
                       .name("COURT_USER")
                       .rank(1)
                       .courts(List.of("415"))
                       .build())
            .build();

        TestUtils.mockSecurityUtil(payload);

        StandardReportRequest requestMock = mock(StandardReportRequest.class);
        when(requestMock.getDate()).thenReturn(LocalDate.now());
        when(requestMock.getLocCode()).thenReturn(TestConstants.VALID_COURT_LOCATION);

        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.returnDate.loe(requestMock.getDate()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(requestMock.getLocCode()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.getActiveOwner()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.isActive.eq(true));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.status.status.in(List.of(IJurorStatus.RESPONDED, IJurorStatus.PANEL,
                IJurorStatus.JUROR)));
        verify(query, times(1)).orderBy(QJuror.juror.jurorNumber.asc());
        verify(query, times(1))
            .groupBy(QJuror.juror.jurorNumber, QJuror.juror.firstName, QJuror.juror.lastName,
                QJurorPool.jurorPool.pool.poolNumber, QJurorPool.jurorPool.nextDate);

    }


    /**
     * Test that super users don't get the owner filter applied.
     */
    @Test
    void positivePreProcessQuerySuperUser() {
        // Setup authentication for a bureau super user with SUPER_USER permission
        // Create payload manually with permissions and then set up authentication
        BureauJwtPayload payload = BureauJwtPayload.builder()
            .owner("400")
            .locCode("400")
            .login("BUREAU_SUPER_USER")
            .userLevel("1")
            .userType(UserType.BUREAU)
            .activeUserType(UserType.BUREAU)
            .permissions(Set.of(Permission.SUPER_USER))
            .staff(BureauJwtPayload.Staff.builder()
                       .name("BUREAU_SUPER_USER")
                       .rank(1)
                       .courts(List.of("400"))
                       .build())
            .build();

        TestUtils.mockSecurityUtil(payload);

        JPAQuery<Tuple> query = mock(JPAQuery.class);

        // Mock query to return itself for chaining
        when(query.where(any(com.querydsl.core.types.Predicate.class))).thenReturn(query);
        when(query.orderBy(any(com.querydsl.core.types.OrderSpecifier.class))).thenReturn(query);
        when(query.groupBy(any(com.querydsl.core.types.Expression.class))).thenReturn(query);


        StandardReportRequest request = mock(StandardReportRequest.class);
        when(request.getDate()).thenReturn(LocalDate.now());
        when(request.getLocCode()).thenReturn(TestConstants.VALID_COURT_LOCATION);

        report.preProcessQuery(query, request);

        // Verify all WHERE clauses
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.returnDate.loe(request.getDate()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(request.getLocCode()));

        // Owner filter should NOT be applied for super users (verify it was never called)
        verify(query, times(0))
            .where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.getActiveOwner()));

        verify(query, times(1))
            .where(QJurorPool.jurorPool.isActive.eq(true));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.status.status.in(List.of(IJurorStatus.RESPONDED, IJurorStatus.PANEL,
                                                                 IJurorStatus.JUROR)));
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        when(request.getDate()).thenReturn(LocalDate.now());
        when(request.getLocCode()).thenReturn(TestConstants.VALID_COURT_LOCATION);
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(TestConstants.VALID_COURT_LOCATION);
        courtLocation.setName("CHESTER");
        when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
            .thenReturn(Optional.of(courtLocation));

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, Map.of(
            "cut_off_date", StandardReportResponse.DataTypeValue.builder()
                .displayName("Cut-off Date")
                .dataType("LocalDate")
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                .build(),
            "total_incomplete_service", StandardReportResponse.DataTypeValue.builder()
                .displayName("Total incomplete service")
                .dataType("Integer")
                .value(0)
                .build(),
            "court_name", StandardReportResponse.DataTypeValue.builder()
                .displayName("Court Name")
                .dataType("String")
                .value("CHESTER (415)")
                .build()));
        return map;
    }

    @Test
    void negativeMissingLocationCode() {
        StandardReportRequest request = getValidRequest();
        request.setLocCode(null);
        assertValidationFails(request, new ValidationFailure("locCode", "must not be null"));
    }

    @Test
    void negativeMissingDate() {
        StandardReportRequest request = getValidRequest();
        request.setDate(null);
        assertValidationFails(request, new ValidationFailure("date", "must not be null"));
    }

    @Test
    void negativeInvalidLocationCode() {
        StandardReportRequest request = getValidRequest();
        request.setLocCode(TestConstants.INVALID_COURT_LOCATION);
        assertValidationFails(request, new ValidationFailure("locCode", "must match \"^\\d{3}$\""));
    }

}
