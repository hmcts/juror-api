package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

public abstract class AbstractReportTestSupport<R extends AbstractReport> {

    private final EntityPath<?> from;
    private final DataType[] dataTypes;
    protected R report;
    private PoolRequestRepository poolRequestRepository;

    public abstract R createReport(PoolRequestRepository poolRequestRepository);


    public AbstractReportTestSupport(EntityPath<?> from, DataType... dataTypes) {
        this.poolRequestRepository = mock(PoolRequestRepository.class);
        this.report = createReport(poolRequestRepository);
        this.from = from;
        this.dataTypes = dataTypes;
    }

    @BeforeEach
    public void setUp() {
        this.poolRequestRepository = mock(PoolRequestRepository.class);
        this.report = spy(createReport(poolRequestRepository));
        doNothing().when(report).addGroupBy(any(), any());
    }


    @Test
    void positiveConstructor() {
        assertThat(report).isNotNull();
        assertThat(report.getPoolRequestRepository()).isEqualTo(poolRequestRepository);
        assertThat(report.getFrom()).isEqualTo(from);
        assertThat(report.getDataTypes()).containsExactly(dataTypes);
        //Other fields don't need testing as they will be tested via the dedicated AbstractReportTest
    }

    public abstract void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request);

    @Test
    @SuppressWarnings("unchecked")
    final void positivePreProcessQueryTypical() {
        JPAQuery<Tuple> query = mock(JPAQuery.class,
            withSettings().defaultAnswer(RETURNS_SELF));
        StandardReportRequest request = new StandardReportRequest();
        positivePreProcessQueryTypical(query, request);
    }

    public abstract Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        StandardReportResponse.TableData tableData,
        List<LinkedHashMap<String, Object>> data);

    @Test
    final void positiveGetHeadingsTypical() {
        StandardReportRequest request = mock(StandardReportRequest.class);
        StandardReportResponse.TableData tableData = mock(StandardReportResponse.TableData.class);
        List<LinkedHashMap<String, Object>> data = spy(new ArrayList<>());
        doReturn(data).when(tableData).getData();
        Map<String, StandardReportResponse.DataTypeValue> standardPoolMappings = getStandardPoolHeaders();
        doReturn(standardPoolMappings).when(report).loadStandardPoolHeaders(request, true, true);

        Map<String, StandardReportResponse.DataTypeValue> headings =
            positiveGetHeadingsTypical(request, tableData, data);
        //Is set via getStandardReportResponse so should not be set here
        assertThat(headings).isNotNull().doesNotContainKey("report_created");
    }

    protected final Map<String, StandardReportResponse.DataTypeValue> getStandardPoolHeaders() {
        return new HashMap<>(
            Map.of(
                "pool_number", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Number")
                    .dataType("String")
                    .value(TestConstants.VALID_POOL_NUMBER)
                    .build(),
                "pool_type", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Type")
                    .dataType("String")
                    .value("CROWN COURT")
                    .build(),
                "service_start_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Service Start Date")
                    .dataType("Date")
                    .value(DateTimeFormatter.ISO_DATE.format(LocalDate.of(2023, 1, 1)))
                    .build(),
                "court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build()
            )
        );
    }

    protected final void assertHeadingContains(Map<String, StandardReportResponse.DataTypeValue> actualMap,
                                               Map<String, StandardReportResponse.DataTypeValue> expectedData) {
        assertHeadingContains(actualMap, null, false, expectedData);
    }

    protected final void assertHeadingContains(Map<String, StandardReportResponse.DataTypeValue> actualMap,
                                               StandardReportRequest request,
                                               boolean hasStandardPoolHeaders,
                                               Map<String, StandardReportResponse.DataTypeValue> expectedData) {
        HashMap<String, StandardReportResponse.DataTypeValue> standardPoolMappings = new HashMap<>(expectedData);
        if (hasStandardPoolHeaders) {
            standardPoolMappings.putAll(getStandardPoolHeaders());
        }
        assertThat(actualMap).isEqualTo(standardPoolMappings);
        if (hasStandardPoolHeaders) {
            verify(report, times(1)).loadStandardPoolHeaders(request, true, true);
        }
    }
}
