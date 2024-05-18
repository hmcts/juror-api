package uk.gov.hmcts.juror.api.moj.report.grouped.groupby;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.IReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode
@SuppressWarnings("PMD.LawOfDemeter")
public class GroupByPaymentType implements IReportGroupBy {

    public static final String CASH_TEXT = "Cash";
    public static final String BACS_OR_CHECK_TEXT = "BACS and cheque approvals";

    @Override
    public String getGroupFunction(GroupedTableData groupedTableData) {
        Object isCash = groupedTableData.get(ExpenseDataTypes.IS_CASH.getId());
        if (isCash != null && ((Boolean) isCash)) {
            return CASH_TEXT;
        }
        return BACS_OR_CHECK_TEXT;
    }

    @Override
    public IReportGroupBy getNested() {
        return ReportGroupBy.builder()
            .dataType(ExpenseDataTypes.ATTENDANCE_DATE)
            .removeGroupByFromResponse(true)
            .build();
    }

    @Override
    public GroupByResponse getGroupedByResponse() {
        return GroupByResponse.builder()
            .name(ExpenseDataTypes.IS_CASH.name())
            .nested(GroupByResponse.builder()
                .name(ExpenseDataTypes.ATTENDANCE_DATE.name())
                .build())
            .build();
    }

    @Override
    public Collection<? extends IDataType> getRequiredDataTypes() {
        return List.of(ExpenseDataTypes.IS_CASH);
    }

    @Override
    public Collection<String> getKeysToRemove() {
        return List.of(ExpenseDataTypes.IS_CASH.getId());
    }
}
