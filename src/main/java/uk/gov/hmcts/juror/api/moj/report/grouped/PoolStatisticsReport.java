package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;

import java.util.Map;

@Component
public class PoolStatisticsReport extends AbstractGroupedReport {

    public PoolStatisticsReport() {
        super(QJurorPool.jurorPool,
            ReportGroupBy.builder()
                .dataType(DataType.POOL_RETURN_DATE_BY_JP)
                .removeGroupByFromResponse(true)
                .nested(ReportGroupBy.builder()
                    .dataType(DataType.POOL_NUMBER_BY_JP)
                    .removeGroupByFromResponse(true)
                    .build())
                .build(),
            DataType.STATUS,
            DataType.JUROR_POOL_COUNT);
        isBureauUserOnly();
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.returnDate.between(request.getFromDate(), request.getToDate()));

        query.orderBy(QJurorPool.jurorPool.status.statusDesc.asc());
        addGroupBy(query,
            DataType.POOL_NUMBER_BY_JP,
            DataType.POOL_RETURN_DATE_BY_JP,
            DataType.STATUS);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData) {
        return Map.of();
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate {
    }
}
