package uk.gov.hmcts.juror.api.moj.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IReportGroupBy {
    String getGroupFunction(GroupedTableData groupedTableData);

    IReportGroupBy getNested();

    GroupByResponse getGroupedByResponse();

    Collection<? extends IDataType> getRequiredDataTypes();

    Collection<String> getKeysToRemove();

    default Collection<String> getCombinedKeysToRemove() {
        Set<String> keysToRemove = new HashSet<>(getKeysToRemove());
        IReportGroupBy nestedGroupedBy = getNested();
        if (nestedGroupedBy != null) {
            keysToRemove.addAll(nestedGroupedBy.getCombinedKeysToRemove());
        }
        return keysToRemove;
    }

    default Collection<IDataType> getCombinedRequiredDataTypes() {
        Set<IDataType> requiredDataTypes = new HashSet<>(getRequiredDataTypes());
        IReportGroupBy nestedGroupedBy = getNested();
        if (nestedGroupedBy != null) {
            requiredDataTypes.addAll(nestedGroupedBy.getCombinedRequiredDataTypes());
        }
        return requiredDataTypes;
    }

    default Map<String, List<GroupedTableData>> sortData(Map<String, List<GroupedTableData>> data) {
        return data;
    }
}
