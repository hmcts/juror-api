package uk.gov.hmcts.juror.api.moj.report.grouped.groupby;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupByPaymentTypeTest {

    @Test
    void positiveConstructorTest() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.isIncludeNested()).isFalse();
        groupByPaymentType = new GroupByPaymentType(true);
        assertThat(groupByPaymentType.isIncludeNested()).isTrue();
    }

    private GroupedTableData mockGroupedTableData(Boolean isCash) {
        GroupedTableData groupedTableData = mock(GroupedTableData.class);
        when(groupedTableData.get("is_cash")).thenReturn(isCash);
        return groupedTableData;
    }

    @Test
    void positiveGroupFunctionIsCashNull() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.getGroupFunction(mockGroupedTableData(null)))
            .isEqualTo("Cash");
    }

    @Test
    void positiveGroupFunctionIsCashTrue() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.getGroupFunction(mockGroupedTableData(true)))
            .isEqualTo("Cash");

    }

    @Test
    void positiveGroupFunctionIsCashFalse() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.getGroupFunction(mockGroupedTableData(false)))
            .isEqualTo("BACS and cheque approvals");
    }

    @Test
    void positiveGetNestedIncludeNestedTrue() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(true);
        assertThat(groupByPaymentType.getNested())
            .isEqualTo(ReportGroupBy.builder()
                .dataType(ExpenseDataTypes.CREATED_ON_DATE)
                .removeGroupByFromResponse(true)
                .build());
    }

    @Test
    void positiveGetNestedIncludeNestedFalse() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.getNested()).isNull();
    }

    @Test
    void positiveGetGroupedResponseIncludeNestedTrue() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(true);
        assertThat(groupByPaymentType.getGroupedByResponse())
            .isEqualTo(GroupByResponse.builder()
                .name(ExpenseDataTypes.IS_CASH.name())
                .nested(
                    GroupByResponse.builder()
                        .name(ExpenseDataTypes.CREATED_ON_DATE.name())
                        .build())
                .build());
    }

    @Test
    void positiveGetGroupedResponseIncludeNestedFalse() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.getGroupedByResponse())
            .isEqualTo(GroupByResponse.builder()
                .name(ExpenseDataTypes.IS_CASH.name())
                .nested(null)
                .build());
    }

    @Test
    void positiveGetRequiredDataTypes() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.getRequiredDataTypes())
            .containsExactlyInAnyOrder(ExpenseDataTypes.IS_CASH);

    }

    @Test
    void positiveGetKeysToRemove() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.getKeysToRemove())
            .containsExactlyInAnyOrder("is_cash");

    }


    @Test
    void positiveGetCombinedKeysToRemoveIncludeNestedTrue() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(true);
        assertThat(groupByPaymentType.getCombinedKeysToRemove())
            .containsExactlyInAnyOrder("is_cash", "created_on_date");

    }

    @Test
    void positiveGetCombinedKeysToRemoveIncludeNestedFalse() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.getCombinedKeysToRemove())
            .containsExactlyInAnyOrder("is_cash");

    }

    @Test
    void positiveGetCombinedRequiredDataTypesIncludeNestedTrue() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(true);
        assertThat(groupByPaymentType.getCombinedRequiredDataTypes())
            .containsExactlyInAnyOrder(ExpenseDataTypes.IS_CASH, ExpenseDataTypes.CREATED_ON_DATE);

    }

    @Test
    void positiveGetCombinedRequiredDataTypesIncludeNestedFalse() {
        GroupByPaymentType groupByPaymentType = new GroupByPaymentType(false);
        assertThat(groupByPaymentType.getCombinedRequiredDataTypes())
            .containsExactlyInAnyOrder(ExpenseDataTypes.IS_CASH);
    }
}
