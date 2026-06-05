package uk.gov.hmcts.juror.api.juror.service;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorDetailDto;
import uk.gov.hmcts.juror.api.moj.domain.DeceasedJuror;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

import java.util.List;

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

    List<DeceasedJuror> getDeceasedJurors(List<String> postcodes);

    /**
     * Save a juror response to persistence.
     *
     * @param responseDto Response data to save
     * @return The saved entity
     */
    @Transactional
    DigitalResponse saveResponse(JurorResponseDto responseDto);
}
