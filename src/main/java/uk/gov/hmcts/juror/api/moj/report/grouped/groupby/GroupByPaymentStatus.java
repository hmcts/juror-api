package uk.gov.hmcts.juror.api.moj.report.grouped.groupby;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.IReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.datatypes.PaymentStatusDataTypes;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode
public class GroupByPaymentStatus implements IReportGroupBy {

    public static final String APPROVED_NOT_SENT = "Approved but not yet sent for payment";
    public static final String SENT_FOR_PAYMENT = "Sent for payment";

    public GroupByPaymentStatus() {
    }

    @Override
    public String getGroupFunction(GroupedTableData groupedTableData) {
        Object extracted = groupedTableData.get(PaymentStatusDataTypes.EXTRACTED.getId());
        if (extracted == null || ((Boolean) extracted)) {
            return SENT_FOR_PAYMENT;
        }
        return APPROVED_NOT_SENT;
    }

    @Override
    public IReportGroupBy getNested() {
        return null;
    }

    @Override
    public GroupByResponse getGroupedByResponse() {
        return GroupByResponse.builder()
            .name(PaymentStatusDataTypes.EXTRACTED.name())
            .nested(null)
            .build();
    }

    @Override
    public Collection<IDataType> getRequiredDataTypes() {
        return List.of(PaymentStatusDataTypes.EXTRACTED);
    }

    @Override
    public Collection<String> getKeysToRemove() {
        return List.of(PaymentStatusDataTypes.EXTRACTED.getId());
    }
}
