package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteServiceValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.time.LocalDate;

public interface CompleteServiceService {
    CompleteServiceValidationResponseDto validateCanCompleteService(
        String poolNumber, JurorNumberListDto jurorNumberListDto);

    void uncompleteJurorsService(String jurorNumber, String poolNumber);

    boolean completeServiceSingle(JurorPool jurorPool, LocalDate completionDate);

    void completeService(String poolNumber,
                         CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto);

    void completeDismissedJurorsService(CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto);
}
