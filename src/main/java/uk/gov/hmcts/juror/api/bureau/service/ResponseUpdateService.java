package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

/**
 * Aggregate service interface for all juror response update operations.
 */
public interface ResponseUpdateService extends ResponseNotesService, ResponsePhoneLogService {
    /**
     * Update juror details section of a <b>first person</b> juror response.
     *
     * @param firstPersonJurorDetailsDto Updated details
     * @param jurorId                    Response id
     * @param login                      Editing user
     */
    void updateJurorDetailsFirstPerson(ResponseUpdateController.FirstPersonJurorDetailsDto firstPersonJurorDetailsDto,
                                       String jurorId, String login);

    /**
     * Update juror details section of a <b>third party</b> juror response.
     *
     * @param thirdPartyJurorDetailsDto Updated details
     * @param jurorId                   Response id
     * @param login                     Editing user
     */
    void updateJurorDetailsThirdParty(ResponseUpdateController.ThirdPartyJurorDetailsDto thirdPartyJurorDetailsDto,
                                      String jurorId, String login);

    /**
     * Update the Excusal/Deferral section of a juror response.
     *
     * @param deferralExcusalDto Updated excusal/deferral
     * @param jurorId            Response id
     * @param login              Editing user
     */
    void updateExcusalDeferral(ResponseUpdateController.DeferralExcusalDto deferralExcusalDto, String jurorId,
                               String login);

    /**
     * Update the Reasonable Adjustments (Special Needs) section of a juror response.
     *
     * @param reasonableAdjustmentsDto Updated special needs
     * @param jurorId                  Response id
     * @param login                    Editing user
     */
    void updateSpecialNeeds(ResponseUpdateController.ReasonableAdjustmentsDto reasonableAdjustmentsDto,
                            String jurorId, String login);

    /**
     * Update juror eligibility section of a juror response.
     *
     * @param jurorEligibilityDto Updated details
     * @param jurorId             Response id
     * @param login               Editing user
     */
    void updateJurorEligibility(final ResponseUpdateController.JurorEligibilityDto dto,
                                final String jurorId, final String login);

    /**
     * Update the CJS Employment section of a juror response.
     *
     * @param CJSEmploymentDetailsDto Updated CJS employment details
     * @param jurorId                 Response id
     * @param login                   Editing user
     */
    void updateCjs(ResponseUpdateController.CJSEmploymentDetailsDto reasonableAdjustmentsDto, String jurorId,
                   String login);


    /**
     * Update processing_status/Processing_completed for a juror response, when.
     *
     * @param ProcessingStatus Updated processing_status/Processing_completed
     * @param jurorId          Response id
     * @param login            Editing user
     */
    void updateResponseStatus(String jurorId, ProcessingStatus status, Integer version, String login);

}
