package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.util.function.Function;

public enum ExpenseType {
    DRAFT(appearance -> appearance.isDraftExpense()
        && appearance.getAppearanceStage() == AppearanceStage.EXPENSE_ENTERED),
    FOR_APPROVAL(appearance -> !appearance.isDraftExpense()
        && appearance.getAppearanceStage() == AppearanceStage.EXPENSE_ENTERED),
    FOR_REAPPROVAL(appearance -> !appearance.isDraftExpense()
        && appearance.getAppearanceStage() == AppearanceStage.EXPENSE_EDITED),
    APPROVED(appearance -> !appearance.isDraftExpense()
        && appearance.getAppearanceStage() == AppearanceStage.EXPENSE_AUTHORISED);

    @SuppressWarnings("PMD.LinguisticNaming")
    private final Function<Appearance, Boolean> isApplicableFunction;

    ExpenseType(Function<Appearance, Boolean> isApplicableFunction) {
        this.isApplicableFunction = isApplicableFunction;
    }

    public boolean isApplicable(Appearance appearance) {
        return isApplicableFunction.apply(appearance);
    }

    public FinancialAuditDetails.Type toEditType() {
        return switch (this) {
            case FOR_APPROVAL, FOR_REAPPROVAL -> FinancialAuditDetails.Type.FOR_APPROVAL_EDIT;
            case APPROVED -> FinancialAuditDetails.Type.APPROVED_EDIT;
            case DRAFT -> throw new IllegalArgumentException("Cannot convert DRAFT to edit type");
        };
    }
}
