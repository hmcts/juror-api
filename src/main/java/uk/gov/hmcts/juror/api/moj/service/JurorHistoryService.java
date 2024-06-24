package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
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
                                  Appearance appearance,
                                  FinancialAuditDetails.Type type);

    void createSummonsReminderLetterHistory(JurorPool jurorPool);

    void createConfirmationLetterHistory(JurorPool jurorPool, String otherInfo);

    void createWithdrawHistory(JurorPool jurorPool, String otherInfo, String code);

    void createPostponementLetterHistory(JurorPool jurorPool, String otherInfo);

    void createWithdrawHistoryUser(JurorPool jurorPool, String otherInfo, String code);

    void createIdentityConfirmedHistory(JurorPool jurorPool);

    void createJuryAttendanceHistory(JurorPool jurorPool, Appearance appearance, Panel panel);

    void createPoolAttendanceHistory(JurorPool jurorPool, Appearance appearance);

    void createJuryEmpanelmentHistory(JurorPool jurorPool, Panel panelMember);

    void createPanelCreationHistory(JurorPool jurorPool, Panel panelMember);

    void createAddedToPanelHistory(JurorPool jurorPool, Panel panel);

    void createReturnFromPanelHistory(JurorPool jurorPool, Panel panelMember);

    void createDisqualifyHistory(JurorPool jurorPool, String code);

    void createReassignPoolMemberHistory(JurorPool sourceJurorPool, String targetPoolNumber,
                                         CourtLocation receivingCourtLocation);

    void createNonExcusedLetterHistory(JurorPool jurorPool, String refusedExcusal);

    void createExcusedLetter(JurorPool jurorPool);

    void createPoolEditHistory(JurorPool updatedPool);

    void createUndeliveredSummonsHistory(JurorPool jurorPool);

    void createAwaitingFurtherInformationHistory(JurorPool jurorPool, String missingInformation);

    void createContactDetailsExportedHistory(String jurorNumber, String poolNumber);

    void createSummonLetterReprintedHistory(JurorPool jurorPool);

    void createTransferCourtHistory(JurorPool sourceJurorPool, JurorPool targetJurorPool);

    void createDeleteAdditionalInfoLetterHistory(JurorPool jurorPool);
}
