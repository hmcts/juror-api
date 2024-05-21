package uk.gov.hmcts.juror.api.moj.report.grouped.groupby;

import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;

import java.util.function.Function;

@SuperBuilder
public class ReportGroupByWithPreProcessor extends ReportGroupBy {

    private Function<GroupedTableData, String> groupByFunction;

    @Override
    public String getGroupFunction(GroupedTableData groupedTableData) {
        return groupByFunction.apply(groupedTableData);
    }
}
