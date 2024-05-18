package uk.gov.hmcts.juror.api.moj.report;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuperBuilder
@Getter
@Data
public class ReportGroupBy implements IReportGroupBy {

    private IDataType dataType;
    private boolean removeGroupByFromResponse;
    private IReportGroupBy nested;

    public String getGroupFunction(GroupedTableData groupedTableData) {
        System.out.println(dataType.getId());
        System.out.println(groupedTableData);
        return groupedTableData.get(dataType.getId()).toString();
    }


    public GroupByResponse getGroupedByResponse() {
        return GroupByResponse.builder()
            .name(dataType.name())
            .nested(nested != null ? nested.getGroupedByResponse() : null)
            .build();
    }

    @Override
    public Collection<? extends IDataType> getRequiredDataTypes() {
        List<IDataType> combinedDataTypes = new ArrayList<>();
        combinedDataTypes.add(this.dataType);
        if (nested != null) {
            combinedDataTypes.addAll(nested.getRequiredDataTypes());
        }
        return combinedDataTypes;
    }

    @Override
    public List<String> getKeysToRemove() {
        if (removeGroupByFromResponse) {
            return List.of(dataType.getId());
        } else {
            return List.of();
        }
    }
}
