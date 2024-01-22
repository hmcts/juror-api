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
     * @return DisqualifyReasonsDto dto
     */
    List<DisqualifyCodeDto> getDisqualifyReasons() throws DisqualifyException.UnableToRetrieveDisqualifyCodeList;

    /**
     * Disqualify Juror with specified code.
     *
     * @return Boolean representing whether disqualification was successful
     */
    boolean disqualifyJuror(String jurorId, DisqualifyCodeDto disqualifyCodeDto,
                            String login) throws DisqualifyException;

}
