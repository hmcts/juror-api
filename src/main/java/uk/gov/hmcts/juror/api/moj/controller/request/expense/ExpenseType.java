package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.util.function.Function;

public enum ExpenseType {
    FOR_APPROVAL(appearance -> !appearance.getIsDraftExpense()
        && AppearanceStage.EXPENSE_ENTERED.equals(appearance.getAppearanceStage())),
    FOR_REAPPROVAL(appearance -> !appearance.getIsDraftExpense()
        && AppearanceStage.EXPENSE_EDITED.equals(appearance.getAppearanceStage())),
    APPROVED(appearance -> !appearance.getIsDraftExpense()
        && AppearanceStage.EXPENSE_AUTHORISED.equals(appearance.getAppearanceStage()));

    @SuppressWarnings("PMD.LinguisticNaming")
    private final Function<Appearance, Boolean> isApplicableFunction;

    ExpenseType(Function<Appearance, Boolean> isApplicableFunction) {
        this.isApplicableFunction = isApplicableFunction;
    }

    public boolean isApplicable(Appearance appearance) {
        return isApplicableFunction.apply(appearance);
    }
}
