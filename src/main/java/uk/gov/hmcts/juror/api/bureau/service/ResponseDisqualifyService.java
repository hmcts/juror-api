package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController.DisqualifyCodeDto;
import uk.gov.hmcts.juror.api.bureau.exception.DisqualifyException;

import java.util.List;

/**
 * Service to handle all Bureau tasks related to disqualifying jurors.
 * {@link ResponseDisqualifyServiceImpl}
 */
public interface ResponseDisqualifyService {
    /**
     * Retrieve the list of reasons a juror can be disqualified for.
     *
     * @return DisqualifyReasonsDto dto.
     * @throws DisqualifyException.UnableToRetrieveDisqualifyCodeList if disqualify codes cannot be retrieved.
     */
    List<DisqualifyCodeDto> getDisqualifyReasons();

    /**
     * Disqualify Juror with specified code.
     *
     * @return Boolean representing whether disqualification was successful.
     * @throws DisqualifyException if disqualification fails.
     */
    boolean disqualifyJuror(String jurorId, DisqualifyCodeDto disqualifyCodeDto,
                            String login);

}
