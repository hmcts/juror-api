package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.ResponseDeferralController;

/**
 * Service for deferral processing operations.
 */
public interface ResponseDeferralService {
    /**
     * Process a deferral for a juror response.
     *
     * @param jurorId     Juror response to process deferral
     * @param login       Bureau officer performing the operation
     * @param deferralDto Deferral decision DTO.
     */
    void processDeferralDecision(String jurorId, String login, ResponseDeferralController.DeferralDto deferralDto);
}
