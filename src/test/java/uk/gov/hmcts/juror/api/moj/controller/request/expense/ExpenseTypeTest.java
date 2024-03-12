package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.LawOfDemeter")
class ExpenseTypeTest {


    private Appearance mockAppearance(boolean isDraft, AppearanceStage stage) {
        Appearance appearance = mock(Appearance.class);
        when(appearance.isDraftExpense()).thenReturn(isDraft);
        when(appearance.getAppearanceStage()).thenReturn(stage);
        return appearance;
    }

    @Test
    void positiveIsApplicableForApprovalTrue() {
        Appearance appearance = mockAppearance(false, AppearanceStage.EXPENSE_ENTERED);
        assertThat(ExpenseType.FOR_APPROVAL.isApplicable(appearance)).isTrue();
    }

    @Test
    void positiveIsApplicableForApprovalFalseIsDraft() {
        Appearance appearance = mockAppearance(true, AppearanceStage.EXPENSE_ENTERED);
        assertThat(ExpenseType.FOR_APPROVAL.isApplicable(appearance)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = AppearanceStage.class, names = {"EXPENSE_ENTERED"},
        mode = EnumSource.Mode.EXCLUDE)
    void positiveIsApplicableForApprovalFalseIsNotExpenseEntered(AppearanceStage stag) {
        Appearance appearance = mockAppearance(true, stag);
        assertThat(ExpenseType.FOR_APPROVAL.isApplicable(appearance)).isFalse();
    }

    @Test
    void positiveIsApplicableForReApprovalTrue() {
        Appearance appearance = mockAppearance(false, AppearanceStage.EXPENSE_EDITED);
        assertThat(ExpenseType.FOR_REAPPROVAL.isApplicable(appearance)).isTrue();
    }

    @Test
    void positiveIsApplicableForReApprovalFalseIsDraft() {
        Appearance appearance = mockAppearance(true, AppearanceStage.EXPENSE_EDITED);
        assertThat(ExpenseType.FOR_REAPPROVAL.isApplicable(appearance)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = AppearanceStage.class, names = {"EXPENSE_EDITED"},
        mode = EnumSource.Mode.EXCLUDE)
    void positiveIsApplicableForReApprovalFalseIsNotExpenseEdited(AppearanceStage stage) {
        Appearance appearance = mockAppearance(false, stage);
        assertThat(ExpenseType.FOR_REAPPROVAL.isApplicable(appearance)).isFalse();
    }

    @Test
    void positiveIsApplicableApprovalTrue() {
        Appearance appearance = mockAppearance(false, AppearanceStage.EXPENSE_AUTHORISED);
        assertThat(ExpenseType.APPROVED.isApplicable(appearance)).isTrue();
    }

    @Test
    void positiveIsApplicableApprovalFalseIsDraft() {
        Appearance appearance = mockAppearance(true, AppearanceStage.EXPENSE_AUTHORISED);
        assertThat(ExpenseType.APPROVED.isApplicable(appearance)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = AppearanceStage.class, names = {"EXPENSE_AUTHORISED"},
        mode = EnumSource.Mode.EXCLUDE)
    void positiveIsApplicableApprovalFalseIsNotExpenseAuthorised(AppearanceStage stage) {
        Appearance appearance = mockAppearance(false, stage);
        assertThat(ExpenseType.APPROVED.isApplicable(appearance)).isFalse();
    }
}
