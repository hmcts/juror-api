package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.util.function.Function;

public enum ExpenseType {
    DRAFT(appearance -> appearance.isDraftExpense()
        && AppearanceStage.EXPENSE_ENTERED.equals(appearance.getAppearanceStage())),
    FOR_APPROVAL(appearance -> !appearance.isDraftExpense()
        && AppearanceStage.EXPENSE_ENTERED.equals(appearance.getAppearanceStage())),
    FOR_REAPPROVAL(appearance -> !appearance.isDraftExpense()
        && AppearanceStage.EXPENSE_EDITED.equals(appearance.getAppearanceStage())),
    APPROVED(appearance -> !appearance.isDraftExpense()
        && AppearanceStage.EXPENSE_AUTHORISED.equals(appearance.getAppearanceStage()));

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
            case FOR_APPROVAL -> FinancialAuditDetails.Type.FOR_APPROVAL_EDIT;
            case FOR_REAPPROVAL -> FinancialAuditDetails.Type.REAPPROVED_EDIT;
            case APPROVED -> FinancialAuditDetails.Type.APPROVED_EDIT;
            case DRAFT -> throw new IllegalArgumentException("Cannot convert DRAFT to edit type");
        };
    }
}
