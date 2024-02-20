package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteJurorResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteServiceValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;

public interface CompleteServiceService {
    CompleteServiceValidationResponseDto validateCanCompleteService(
        String poolNumber, JurorNumberListDto jurorNumberListDto);

    void uncompleteJurorsService(String jurorNumber, String poolNumber);

    void completeService(String poolNumber,
                         CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto);

    void completeDismissedJurorsService(CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto);

    PaginatedList<CompleteJurorResponse> search(JurorPoolSearch request);
}
