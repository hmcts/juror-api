package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DeferredListByDateReportTest extends AbstractReportTestSupport<DeferredListByDateReport> {

    public DeferredListByDateReportTest() {
        super(QJurorPool.jurorPool,
            DataType.DEFERRED_TO,
            DataType.NUMBER_DEFERRED);
    }


    @Override
    public DeferredListByDateReport createReport(PoolRequestRepository poolRequestRepository) {
        return new DeferredListByDateReport(poolRequestRepository);
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorPool.jurorPool.deferralDate.isNotNull());
        verify(report, times(1)).addGroupBy(query,
            DataType.DEFERRED_TO
        );
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(StandardReportRequest request,
                                                                                        StandardReportResponse.TableData tableData,
                                                                                        List<LinkedHashMap<String,
                                                                                            Object>> data) {
       data.add(new LinkedHashMap<>(Map.of(
            DataType.DEFERRED_TO.getId(), "2024-03-15",
            DataType.NUMBER_DEFERRED.getId(), 1
        )));
        data.add(new LinkedHashMap<>(Map.of(
            DataType.DEFERRED_TO.getId(), "2024-03-16",
            DataType.NUMBER_DEFERRED.getId(), 0
        )));
        data.add(new LinkedHashMap<>(Map.of(
            DataType.DEFERRED_TO.getId(), "2024-03-17",
            DataType.NUMBER_DEFERRED.getId(), 9
        )));


        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            true,
            Map.of(
                "total_deferred",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total deferred")
                    .dataType(Integer.class.getSimpleName())
                    .value(10)
                    .build()
            ));
        verify(tableData, times(1)).getData();
        return map;
    }
}