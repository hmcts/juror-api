package uk.gov.hmcts.juror.api.bureau.service;


import uk.gov.hmcts.juror.api.bureau.controller.ResponseSendToCourtController;
import uk.gov.hmcts.juror.api.bureau.exception.DisqualifyException;

/**
 * Service for send to court processing operations.
 */

public interface ResponseSendToCourtService {

    /**
     * Send response to court.
     *
     * @return Boolean representing whether disqualification was successful
     */
    boolean sendResponseToCourt(final String jurorId, ResponseSendToCourtController.SendToCourtDto sendToCourtDto,
                                String login) throws DisqualifyException;
}
