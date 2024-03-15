package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Map;

@Component
public class DeferredListByDateReport extends AbstractReport {

    @Autowired
    public DeferredListByDateReport(PoolRequestRepository poolRequestRepository) {
        super(
            poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.DEFERRED_TO,
            DataType.NUMBER_DEFERRED);
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.deferralDate.isNotNull());
        addGroupBy(query, DataType.DEFERRED_TO);
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(StandardReportRequest request,
                                                                         StandardReportResponse.TableData tableData) {
        Map<String, StandardReportResponse.DataTypeValue> map = loadStandardPoolHeaders(request, true, true);
        map.put("total_deferred", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total deferred")
            .dataType(Integer.class.getSimpleName())
            .value(
                tableData.getData()
                    .stream()
                    .map(row -> (Integer) row.get(DataType.NUMBER_DEFERRED.getId()))
                    .reduce(0, Integer::sum))
            .build());
        return map;
    }
}