package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.standard.NonRespondedReport;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NonRespondedReportTest extends AbstractReportTestSupport<NonRespondedReport> {

    public NonRespondedReportTest() {
        super(QJurorPool.jurorPool,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.MOBILE_PHONE,
            DataType.HOME_PHONE);
    }


    @Override
    public NonRespondedReport createReport(PoolRequestRepository poolRequestRepository) {
        return new NonRespondedReport(poolRequestRepository);
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJuror.juror.responded.isNull()
                .or(QJuror.juror.responded.eq(false)));
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(StandardReportRequest request,
                                                                                        StandardReportResponse.TableData tableData,
                                                                                        List<LinkedHashMap<String,
                                                                                            Object>> data) {
        when(data.size()).thenReturn(2);
        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            true,
            Map.of(
                "total_non_responded",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total non-responded")
                    .dataType(Integer.class.getSimpleName())
                    .value(2)
                    .build()
            ));
        verify(tableData, times(1)).getData();
        verify(data, times(1)).size();
        return map;
    }
}
