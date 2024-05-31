package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController.ExcusalCodeDto;

import java.util.List;

/**
 * Service to handle all Bureau tasks related to excusing jurors.
 * {@link ResponseExcusalServiceImpl}
 */
public interface ResponseExcusalService {
    /**
     * Retrieve the list of reasons a juror can be excused for.
     *
     * @return ExcusalReasonsDto dto
     */
    List<ResponseExcusalController.ExcusalCodeDto> getExcusalReasons();

    /**
     * Excuse Juror with specified code.
     *
     * @return Boolean representing whether excusal was successful
     */
    boolean excuseJuror(String jurorId, ExcusalCodeDto excusalCodeDto, String login);

    /**
     * Reject Jurors excusal request with specified code.
     *
     * @return Boolean representing whether rejection was successful
     */
    boolean rejectExcusalRequest(String jurorId, ExcusalCodeDto excusalCodeDto, String login);

}
