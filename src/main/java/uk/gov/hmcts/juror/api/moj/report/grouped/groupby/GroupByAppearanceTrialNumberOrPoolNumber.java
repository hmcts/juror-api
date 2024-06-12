package uk.gov.hmcts.juror.api.moj.report.grouped.groupby;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.IReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode
public class GroupByAppearanceTrialNumberOrPoolNumber implements IReportGroupBy {
    @Override
    public String getGroupFunction(GroupedTableData groupedTableData) {
        Object trialNumber = groupedTableData.get(DataType.APPEARANCE_TRIAL_NUMBER.getId());
        if (trialNumber != null) {
            return "Trial " + trialNumber;
        }
        return "Pool " + groupedTableData.get(DataType.APPEARANCE_POOL_NUMBER.getId());
    }

    @Override
    public IReportGroupBy getNested() {
        return ReportGroupBy.builder()
            .dataType(DataType.ATTENDANCE_DATE)
            .removeGroupByFromResponse(true)
            .build();
    }

    @Override
    public GroupByResponse getGroupedByResponse() {
        return GroupByResponse.builder()
            .name("TRIAL_NUMBER_OR_POOL_NUMBER")
            .build();
    }

    @Override
    public Collection<? extends IDataType> getRequiredDataTypes() {
        return List.of(DataType.APPEARANCE_TRIAL_NUMBER,
            DataType.APPEARANCE_POOL_NUMBER,
            DataType.ATTENDANCE_DATE);
    }

    @Override
    public Collection<String> getKeysToRemove() {
        return List.of(DataType.APPEARANCE_TRIAL_NUMBER.getId(), DataType.APPEARANCE_POOL_NUMBER.getId(),
            DataType.ATTENDANCE_DATE.getId());
    }
}
