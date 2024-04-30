package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportNoPoolRepositoryTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbaccusReportTest extends AbstractStandardReportNoPoolRepositoryTestSupport<AbaccusReport> {

    private final Clock clock;


    public AbaccusReportTest() {
        super(
            QBulkPrintData.bulkPrintData,
            AbaccusReport.RequestValidator.class,
            DataType.DOCUMENT_CODE,
            DataType.TOTAL_SENT_FOR_PRINTING,
            DataType.DATE_SENT);

        String instantExpected = "2024-04-30T12:00:01Z";
        Instant instant = Instant.parse(instantExpected);

        this.clock = mock(Clock.class);
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    }

    @Override
    public AbaccusReport createReport(PoolRequestRepository poolRequestRepository) {
        return new AbaccusReport(this.clock);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(LocalDate.now().minus(7, ChronoUnit.DAYS))
            .toDate(LocalDate.now())
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        doNothing().when(report).addGroupBy(any(), any(DataType[].class));
        report.preProcessQuery(query, request);
        verify(report, times(1)).addGroupBy(query,
            DataType.DOCUMENT_CODE,
            DataType.DATE_SENT
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(StandardReportRequest request,
                                                                                        AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData,
                                                                                        List<LinkedHashMap<String,
                                                                                            Object>> data) {

        Map<String, AbstractReportResponse.DataTypeValue> expected = new ConcurrentHashMap<>();
        expected.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .dataType(LocalDate.class.getSimpleName())
            .value(request.getFromDate())
            .build());
        expected.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .dataType(LocalDate.class.getSimpleName())
            .value(request.getToDate())
            .build());
        expected.put("time_created", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Time created")
            .dataType(LocalTime.class.getSimpleName())
            .value(LocalTime.now(clock))
            .build());

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            expected);

        return map;

    }
    @Test
    public void negativeNullDateFrom() {

    }

    @Test
    public void negativeNullDateTo() {

    }
}
