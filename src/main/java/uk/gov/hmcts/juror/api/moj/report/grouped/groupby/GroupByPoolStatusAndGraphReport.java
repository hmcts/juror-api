package uk.gov.hmcts.juror.api.moj.report.grouped.groupby;

import uk.gov.hmcts.juror.api.moj.report.DataType;

public class GroupByPoolStatusAndGraphReport extends ReportGroupByWithPreProcessor {
    public static final String ACTIVE_TEXT = "Active pool members";
    public static final String INACTIVE_TEXT = "Inactive pool members";

    public GroupByPoolStatusAndGraphReport() {
        super(ReportGroupByWithPreProcessor.builder()
            .dataType(DataType.IS_ACTIVE)
            .removeGroupByFromResponse(true)
            .groupByFunction(
                groupedTableData -> {
                    Object isActive = groupedTableData.get(DataType.IS_ACTIVE.getId());
                    if (isActive != null && (Boolean) isActive) {
                        return ACTIVE_TEXT;
                    }
                    return INACTIVE_TEXT;
                })
        );
    }
}
