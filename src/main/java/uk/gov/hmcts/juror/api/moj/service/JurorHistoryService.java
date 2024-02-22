package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface JurorHistoryService {

    void createPoliceCheckDisqualifyHistory(JurorPool jurorPool);

    void createPoliceCheckQualifyHistory(JurorPool jurorPool, boolean isChecked);

    void createPoliceCheckInProgressHistory(JurorPool jurorPool);

    void createSendMessageHistory(String jurorNumber, String poolNumber, String otherInfo);

    void createCompleteServiceHistory(JurorPool jurorPool);

    void createPoliceCheckInsufficientInformationHistory(JurorPool jurorPool);

    void createExpenseApproveCash(String jurorNumber,
                                  String poolNumber,
                                  FinancialAuditDetails financialAuditDetails,
                                  LocalDate latestAppearanceDate,
                                  BigDecimal totalAmount);

    void createExpenseApproveBacs(String jurorNumber,
                                  String poolNumber,
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
}
