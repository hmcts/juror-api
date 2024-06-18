package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface JurorHistoryService {
    void createPoliceCheckDisqualifyHistory(JurorPool jurorPool);

    void createPoliceCheckQualifyHistory(JurorPool jurorPool, boolean isChecked);

    void createPoliceCheckInProgressHistory(JurorPool jurorPool);

    void createSendMessageHistory(String jurorNumber, String poolNumber, String otherInfo);

    void createCompleteServiceHistory(JurorPool jurorPool);

    void createPoliceCheckInsufficientInformationHistory(JurorPool jurorPool);

    void createExpenseApproveCash(JurorPool jurorPool,
                                  FinancialAuditDetails financialAuditDetails,
                                  LocalDate latestAppearanceDate,
                                  BigDecimal totalAmount);

    void createExpenseApproveBacs(JurorPool jurorPool,
                                  FinancialAuditDetails financialAuditDetails,
                                  LocalDate latestAppearanceDate,
                                  BigDecimal totalAmount);

    void createFailedToAttendHistory(JurorPool jurorPool);

    void createUndoFailedToAttendHistory(JurorPool jurorPool);

    void createPendingJurorAuthorisedHistory(JurorPool jurorPool);

    void createUncompleteServiceHistory(JurorPool jurorPool);

    void createDeferredLetterHistory(JurorPool jurorPool);

    void createEditBankSortCodeHistory(String jurorNumber);

    void createEditBankAccountNumberHistory(String jurorNumber);

    void createEditBankAccountNameHistory(String jurorNumber);

    void createExpenseForApprovalHistory(FinancialAuditDetails financialAuditDetails,
                                         Appearance appearance);

    void createExpenseEditHistory(FinancialAuditDetails financialAuditDetails,
                                  Appearance appearance);

    void createSummonsReminderLetterHistory(JurorPool jurorPool);

    void createConfirmationLetterHistory(JurorPool jurorPool, String otherInfo);

    void createWithdrawHistory(JurorPool jurorPool, String otherInfo);

    void createPostponementLetterHistory(JurorPool jurorPool, String otherInfo);

    void createIdentityConfirmedHistory(JurorPool jurorPool);

    void createSummonsLetterHistory(JurorPool jurorPool);

    void createJuryAttendanceHistory(JurorPool jurorPool, Appearance appearance, Panel panel);

    void createPoolAttendanceHistory(JurorPool jurorPool, Appearance appearance);

    String getHistoryDescription(String historyCode);

    void createJuryEmpanelmentHistory(JurorPool jurorPool, Panel panelMember);

    void createAddedToPanelHistory(JurorPool jurorPool, Panel panel);

    void createReturnFromPanelHistory(JurorPool jurorPool, Panel panelMember);

    void createUndeliveredSummonsHistory(JurorPool jurorPool);
}
