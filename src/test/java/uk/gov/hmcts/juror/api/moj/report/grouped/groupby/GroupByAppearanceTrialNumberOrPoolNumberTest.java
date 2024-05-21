package uk.gov.hmcts.juror.api.moj.report.grouped.groupby;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupByAppearanceTrialNumberOrPoolNumberTest {

    private static GroupByAppearanceTrialNumberOrPoolNumber groupByAppearanceTrialNumberOrPoolNumber;

    @BeforeAll
    public static void beforeAll() {
        groupByAppearanceTrialNumberOrPoolNumber = new GroupByAppearanceTrialNumberOrPoolNumber();
    }


    private GroupedTableData mockGroupedTableData(String poolNumber, String trialNumber) {
        GroupedTableData groupedTableData = mock(GroupedTableData.class);
        when(groupedTableData.get("appearance_pool_number")).thenReturn(poolNumber);
        when(groupedTableData.get("appearance_trial_number")).thenReturn(trialNumber);

        return groupedTableData;
    }

    @Test
    void positiveGroupFunctionHasTrial() {
        GroupedTableData groupedTableData = mockGroupedTableData(
            TestConstants.VALID_POOL_NUMBER,
            TestConstants.VALID_TRIAL_NUMBER);
        assertThat(groupByAppearanceTrialNumberOrPoolNumber.getGroupFunction(groupedTableData))
            .isEqualTo("Trial " + TestConstants.VALID_TRIAL_NUMBER);
    }

    @Test
    void positiveGroupFunctionNoTrial() {
        GroupedTableData groupedTableData = mockGroupedTableData(
            TestConstants.VALID_POOL_NUMBER,
            null);
        assertThat(groupByAppearanceTrialNumberOrPoolNumber.getGroupFunction(groupedTableData))
            .isEqualTo("Pool " + TestConstants.VALID_POOL_NUMBER);
    }


    @Test
    void positiveGetNested() {
        assertThat(groupByAppearanceTrialNumberOrPoolNumber.getNested())
            .isEqualTo(ReportGroupBy.builder()
                .dataType(DataType.ATTENDANCE_DATE)
                .removeGroupByFromResponse(true)
                .build());
    }


    @Test
    void positiveGetGroupedByResponse() {
        assertThat(groupByAppearanceTrialNumberOrPoolNumber.getGroupedByResponse())
            .isEqualTo(GroupByResponse.builder()
                .name("TRIAL_NUMBER_OR_POOL_NUMBER")
                .build());

    }


    @Test
    void positiveGetRequiredDataTypes() {
        assertThat(groupByAppearanceTrialNumberOrPoolNumber.getRequiredDataTypes())
            .isEqualTo(List.of(DataType.APPEARANCE_TRIAL_NUMBER,
                DataType.APPEARANCE_POOL_NUMBER,
                DataType.ATTENDANCE_DATE));

    }


    @Test
    void positiveGetKeysToRemove() {
        assertThat(groupByAppearanceTrialNumberOrPoolNumber.getKeysToRemove())
            .isEqualTo(List.of("appearance_trial_number", "appearance_pool_number",
                "attendance_date"));

    }
}
