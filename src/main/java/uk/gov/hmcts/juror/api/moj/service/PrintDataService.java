package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.util.List;

@SuppressWarnings("PMD.TooManyMethods")
public interface PrintDataService {

    void bulkPrintSummonsLetter(List<JurorPool> jurorPools);

    default void printSummonsLetter(JurorPool jurorPool) {
        bulkPrintSummonsLetter(List.of(jurorPool));
    }

    void printSummonsReminderLetter(JurorPool jurorPool);

    void printDeferralLetter(JurorPool jurorPool);

    void printDeferralDeniedLetter(JurorPool jurorPool);

    void printExcusalDeniedLetter(JurorPool jurorPool);

    void printConfirmationLetter(JurorPool jurorPool);

    void printPostponeLetter(JurorPool jurorPool);

    void printExcusalLetter(JurorPool jurorPool);

    void printRequestInfoLetter(JurorPool jurorPool, String additionalInfo);

    void printWithdrawalLetter(JurorPool jurorPool);

}
