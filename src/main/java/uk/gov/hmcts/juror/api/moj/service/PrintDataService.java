package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.util.List;

public interface PrintDataService {
    void bulkPrintSummonsLetter(List<JurorPool> jurorPools);

    void printDeferralLetter(JurorPool jurorPool);

    void printDeferralDeniedLetter(JurorPool jurorPool);
    
    void printExcusalDeniedLetter(JurorPool jurorPool);
    
    void printConfirmationLetter(JurorPool jurorPool);

    void printPostponeLetter(JurorPool jurorPool);
}
