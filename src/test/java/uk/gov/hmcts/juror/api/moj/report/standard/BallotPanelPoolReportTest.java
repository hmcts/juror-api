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
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class BallotPanelPoolReportTest extends AbstractStandardReportTestSupport<BallotPanelPoolReport> {

    private CourtLocationRepository courtLocationRepository;

    public BallotPanelPoolReportTest() {
        super(QJurorPool.jurorPool,
            PersonAttendingSummaryReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
              DataType.JUROR_POSTCODE);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        super.beforeEach();
    }

    @AfterEach
    public void afterEach() {
        TestUtils.afterAll();
    }

    @Override
    public BallotPanelPoolReport createReport(PoolRequestRepository poolRequestRepository) {
        return new BallotPanelPoolReport(poolRequestRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .date(LocalDate.now())
            .includeSummoned(false)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {

        request.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
        request.setLocCode(TestConstants.VALID_COURT_LOCATION);
        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(request.getLocCode()));
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData,
        List<LinkedHashMap<String, Object>> data) {

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, Map.of());
        return map;
    }

    @Test
    void negativeMissingIncludeSummoned() {
        StandardReportRequest request = getValidRequest();
        request.setIncludeSummoned(null);
        assertValidationFails(request, new ValidationFailure("includeSummoned", "must not be null"));
    }

    @Test
    void negativeMissingDate() {
        StandardReportRequest request = getValidRequest();
        request.setDate(null);
        assertValidationFails(request, new ValidationFailure("date", "must not be null"));
    }
}
