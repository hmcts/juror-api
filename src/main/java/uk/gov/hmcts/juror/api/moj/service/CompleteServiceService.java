package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteJurorResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteServiceValidationResponseDto;

import java.util.List;

public interface CompleteServiceService {
    CompleteServiceValidationResponseDto validateCanCompleteService(
        String poolNumber, JurorNumberListDto jurorNumberListDto);

    void uncompleteJurorsService(String jurorNumber, String poolNumber);

    void completeService(String poolNumber,
                         CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto);

    void completeDismissedJurorsService(CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto);

    List<CompleteJurorResponse> search(JurorPoolSearch request);
}
