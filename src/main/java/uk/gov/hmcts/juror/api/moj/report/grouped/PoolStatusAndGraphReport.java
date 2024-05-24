package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.grouped.groupby.GroupByPoolStatusAndGraphReport;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;
import java.util.Map;

@Component
public class PoolStatusAndGraphReport extends AbstractGroupedReport {

    @Autowired
    public PoolStatusAndGraphReport(PoolRequestRepository poolRequestRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            new GroupByPoolStatusAndGraphReport(),
            DataType.IS_ACTIVE,
            DataType.STATUS,
            DataType.JUROR_POOL_COUNT);
        isBureauUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        query.where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.BUREAU_OWNER));
        addGroupBy(query, DataType.IS_ACTIVE, DataType.STATUS);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData) {

        PoolRequest poolRequest = getPoolRequest(request.getPoolNumber());

        long totalCount = calculateTotal(tableData, GroupByPoolStatusAndGraphReport.ACTIVE_TEXT)
            + calculateTotal(tableData, GroupByPoolStatusAndGraphReport.INACTIVE_TEXT);

        return Map.of(
            "pool_number",
            AbstractReportResponse.DataTypeValue.builder()
                .displayName("Pool number")
                .dataType(String.class.getSimpleName())
                .value(request.getPoolNumber())
                .build(),
            "total_pool_members",
            AbstractReportResponse.DataTypeValue.builder()
                .displayName("Total pool members")
                .dataType(Long.class.getSimpleName())
                .value(totalCount)
                .build(),
            "originally_requested_by_court",
            AbstractReportResponse.DataTypeValue.builder()
                .displayName("Originally requested by court")
                .dataType(Long.class.getSimpleName())
                .value(poolRequest.getNumberRequested())
                .build()
        );
    }

    @SuppressWarnings("unchecked")
    long calculateTotal(AbstractReportResponse.TableData<GroupedTableData> tableData, String key) {
        List<Map<String, Object>> data = (List<Map<String, Object>>) tableData.getData().get(key);
        if (data == null) {
            return 0;
        }
        return data
            .stream()
            .map(stringObjectMap -> (Long) stringObjectMap.getOrDefault(DataType.JUROR_POOL_COUNT.getId(), 0L))
            .reduce(0L, Long::sum);
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequirePoolNumber {

    }
}
