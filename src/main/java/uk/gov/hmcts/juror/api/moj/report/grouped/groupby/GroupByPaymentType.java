package uk.gov.hmcts.juror.api.moj.report.grouped.groupby;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.IReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode
public class GroupByPaymentType implements IReportGroupBy {

    public static final String CASH_TEXT = "Cash";
    public static final String BACS_OR_CHECK_TEXT = "BACS and cheque approvals";
    @Getter
    private final boolean includeNested;


    public GroupByPaymentType(boolean includeNested) {
        this.includeNested = includeNested;
    }

    @Override
    public String getGroupFunction(GroupedTableData groupedTableData) {
        Object isCash = groupedTableData.get(ExpenseDataTypes.IS_CASH.getId());
        if (isCash == null || ((Boolean) isCash)) {
            return CASH_TEXT;
        }
        return BACS_OR_CHECK_TEXT;
    }

    @Override
    public IReportGroupBy getNested() {
        return includeNested ? ReportGroupBy.builder()
            .dataType(ExpenseDataTypes.CREATED_ON_DATE)
            .removeGroupByFromResponse(true)
            .build() : null;
    }

    @Override
    public GroupByResponse getGroupedByResponse() {
        return GroupByResponse.builder()
            .name(ExpenseDataTypes.IS_CASH.name())
            .nested(includeNested
                ? GroupByResponse.builder()
                .name(ExpenseDataTypes.CREATED_ON_DATE.name())
                .build() : null)
            .build();
    }

    @Override
    public Collection<IDataType> getRequiredDataTypes() {
        return List.of(ExpenseDataTypes.IS_CASH);
    }

    @Override
    public Collection<String> getKeysToRemove() {
        return List.of(ExpenseDataTypes.IS_CASH.getId());
    }
}
