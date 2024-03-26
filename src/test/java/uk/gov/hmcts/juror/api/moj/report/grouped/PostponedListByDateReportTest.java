package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.QPool;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class PostponedListByDateReportTest extends AbstractGroupedReportTestSupport<PostponedListByDateReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public PostponedListByDateReportTest() {
        super(QJurorPool.jurorPool,
            PostponedListByDateReport.RequestValidator.class,
            DataType.POOL_NUMBER,
            true,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.POSTCODE,
            DataType.POSTPONED_TO);
    }

    @BeforeEach
    void beforeEach() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @Override
    public PostponedListByDateReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PostponedListByDateReport(poolRequestRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(LocalDate.of(2020, 1, 1))
            .toDate(LocalDate.of(2020, 1, 2))
            .build();
    }

    @Override
    @DisplayName("positivePreProcessQueryTypicalCourt")
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        request.setToDate(LocalDate.of(2023, 1, 1));
        request.setToDate(LocalDate.of(2023, 1, 2));
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        report.preProcessQuery(query, request);

        verify(query).where(
            QJurorPool.jurorPool.deferralDate.between(request.getFromDate(), request.getToDate())
                .and(QJurorPool.jurorPool.deferralDate.isNotNull())
                .and(QJurorPool.jurorPool.deferralCode.eq(ExcusalCodeEnum.P.getCode()))
        );

        verify(query).where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));

        verify(query)
            .orderBy(QPool.pool.poolNumber.asc(), QJuror.juror.jurorNumber.asc());
    }

    @Test
    @SuppressWarnings("unchecked")
    void positivePreProcessQueryTypicalBureau() {
        final JPAQuery<Tuple> query = mock(JPAQuery.class,
            withSettings().defaultAnswer(RETURNS_SELF));
        StandardReportRequest request = new StandardReportRequest();

        request.setToDate(LocalDate.of(2023, 2, 1));
        request.setToDate(LocalDate.of(2023, 2, 2));
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(false);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        report.preProcessQuery(query, request);

        verify(query).where(
            QJurorPool.jurorPool.deferralDate.between(request.getFromDate(), request.getToDate())
                .and(QJurorPool.jurorPool.deferralDate.isNotNull())
                .and(QJurorPool.jurorPool.deferralCode.eq(ExcusalCodeEnum.P.getCode()))
        );
        verify(query)
            .orderBy(QPool.pool.poolNumber.asc(), QJuror.juror.jurorNumber.asc());
        verifyNoMoreInteractions(query);
    }

    @Override
    @DisplayName("positiveGetHeadingsTypicalCourt")
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<Map<String, List<LinkedHashMap<String, Object>>>> tableData,
        Map<String, List<LinkedHashMap<String, Object>>> data) {

        when(request.getFromDate()).thenReturn(LocalDate.of(2023, 3, 1));
        when(request.getToDate()).thenReturn(LocalDate.of(2023, 3, 2));
        when(data.size()).thenReturn(2);
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);

        doReturn(getCourtNameEntry()).when(report).getCourtNameHeader(TestConstants.VALID_COURT_LOCATION);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "date_from",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2023-03-01")
                    .build(),
                "date_to",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2023-03-02")
                    .build(),
                "total_postponed",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total postponed")
                    .dataType(Long.class.getSimpleName())
                    .value(2)
                    .build(),
                "court_name",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType(String.class.getSimpleName())
                    .value("CHESTER (415)")
                    .build()
            )
        );
        verify(report).getCourtNameHeader(TestConstants.VALID_COURT_LOCATION);
        verify(tableData, times(1)).getData();
        verify(data, times(1)).size();
        return map;
    }

    @Test
    @SuppressWarnings("unchecked")
    void positiveGetHeadingsTypicalBureau() {
        StandardReportRequest request = mock(StandardReportRequest.class);
        AbstractReportResponse.TableData<Map<String, List<LinkedHashMap<String, Object>>>> tableData =
            mock(AbstractReportResponse.TableData.class);
        Map<String, List<LinkedHashMap<String, Object>>> data = spy(createData());
        doReturn(data).when(tableData).getData();
        Map<String, AbstractReportResponse.DataTypeValue> standardPoolMappings = getStandardPoolHeaders();


        when(request.getFromDate()).thenReturn(LocalDate.of(2023, 3, 1));
        when(request.getToDate()).thenReturn(LocalDate.of(2023, 3, 2));
        when(data.size()).thenReturn(2);


        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(false);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);

        doReturn(getCourtNameEntry()).when(report).getCourtNameHeader(TestConstants.VALID_COURT_LOCATION);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "date_from",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2023-03-01")
                    .build(),
                "date_to",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2023-03-02")
                    .build(),
                "total_postponed",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total postponed")
                    .dataType(Long.class.getSimpleName())
                    .value(2)
                    .build()
            )
        );
        verify(report, never()).getCourtNameHeader(anyString());
        verify(tableData, times(1)).getData();
        verify(data, times(1)).size();
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
