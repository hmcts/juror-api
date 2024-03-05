package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CodeDescriptionResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CourtDetailsReduced;
import uk.gov.hmcts.juror.api.moj.domain.CodeType;
import uk.gov.hmcts.juror.api.moj.domain.CourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.UpdateCourtDetailsDto;

import java.util.List;

public interface AdministrationService {
    List<CodeDescriptionResponse> viewCodeAndDescriptions(CodeType codeType);

    CourtDetailsDto viewCourt(String locCode);

    void updateCourt(String locCode, UpdateCourtDetailsDto updateCourtDetailsDto);

    List<CourtDetailsReduced> viewCourts();

    void updateCourtRates(String courtCode, CourtRates courtRates);
}
