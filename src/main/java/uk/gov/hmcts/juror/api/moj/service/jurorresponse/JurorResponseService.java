package uk.gov.hmcts.juror.api.moj.service.jurorresponse;

import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;

public interface JurorResponseService<J extends AbstractJurorResponse, D> {
    void saveResponse(J response);

    void updatePersonalDetails(D responseDto);

    void updateCJSEmployment(D cjsDto);

    void updateResponseStatus();
}
