package uk.gov.hmcts.juror.api.juror.service;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorDetailDto;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;

/**
 * Public Juror service for public data access operations.
 */
public interface JurorService {
    /**
     * Return the juror details for a single juror by id.
     *
     * @param jurorNumber Juror number to find
     * @return Juror details
     */
    JurorDetailDto getJurorByJurorNumber(String jurorNumber);

    /**
     * Save a juror response to persistence.
     *
     * @param responseDto Response data to save
     * @return The saved entity
     */
    @Transactional
    JurorResponse saveResponse(JurorResponseDto responseDto);
}
