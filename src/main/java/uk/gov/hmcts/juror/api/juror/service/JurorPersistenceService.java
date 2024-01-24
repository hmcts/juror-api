package uk.gov.hmcts.juror.api.juror.service;

import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;

/**
 * Service to save juror responses and allow transactions to be called correctly, through a proxied class.
 */
public interface JurorPersistenceService {
    /**
     * Save a juror response to the database and trigger any straight through processing business flows.
     *
     * @param responseDto The juror response to save
     * @return Entity saved?
     * @implNote Deliberately not transactional!
     */
    Boolean persistJurorResponse(final JurorResponseDto responseDto);
}
