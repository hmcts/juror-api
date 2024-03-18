package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CurrentPoolStatusReportTest extends AbstractReportTestSupport<CurrentPoolStatusReport> {
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
        return new CurrentPoolStatusReport(poolRequestRepository);
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        request.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
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
        StandardReportResponse.TableData tableData,
        List<LinkedHashMap<String, Object>> data) {
        when(data.size()).thenReturn(2);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            true,
            Map.of(
                "total_pool_members",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Pool Members")
                    .dataType(Long.class.getSimpleName())
                    .value(2)
                    .build()
            ));
        verify(tableData, times(1)).getData();
        verify(data, times(1)).size();
        return map;
    }
}
