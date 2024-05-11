package uk.gov.hmcts.juror.api.moj.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;

import java.util.Collection;

public interface IReportGroupBy {
    String getGroupFunction(GroupedTableData groupedTableData);

    IReportGroupBy getNested();

    GroupByResponse getGroupedByResponse();

    Collection<? extends IDataType> getRequiredDataTypes();

    Collection<String> getKeysToRemove();
}
