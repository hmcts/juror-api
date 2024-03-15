package uk.gov.hmcts.juror.api.moj.service.trial;

import uk.gov.hmcts.juror.api.moj.controller.response.trial.JurorForExemptionListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialExemptionListDto;

import java.util.List;

public interface ExemptionCertificateService {
    List<TrialExemptionListDto> getTrialExemptionList(String courtLocation);

    List<JurorForExemptionListDto> getJurorsForExemptionList(String caseNumber, String courtLocation);
}
