package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("PMD.TooManyMethods")
public interface PrintDataService {

    void bulkPrintSummonsLetter(List<JurorPool> jurorPools);

    default void printSummonsLetter(JurorPool jurorPool) {
        bulkPrintSummonsLetter(List.of(jurorPool));
    }

    void reprintSummonsLetter(JurorPool jurorPool);

    void printSummonsReminderLetter(JurorPool jurorPool);

    void printDeferralLetter(JurorPool jurorPool);

    void printDeferralDeniedLetter(JurorPool jurorPool);

    void printExcusalDeniedLetter(JurorPool jurorPool);

    void printConfirmationLetter(JurorPool jurorPool);

    void printPostponeLetter(JurorPool jurorPool);

    void printExcusalLetter(JurorPool jurorPool);

    void printRequestInfoLetter(JurorPool jurorPool, String additionalInfo);

    void reprintRequestInfoLetter(JurorPool jurorPool);

    void printWithdrawalLetter(JurorPool jurorPool);

    void checkLetterInBulkPrint(String jurorNumber, String formType, LocalDate creationDate, boolean extractedFlag);

    void removeQueuedLetterForJuror(JurorPool jurorPool, List<FormCode> formCodes);
}
