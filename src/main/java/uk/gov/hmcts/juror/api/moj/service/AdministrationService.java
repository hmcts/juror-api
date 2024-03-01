package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.response.administration.BankHolidayDate;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CodeDescriptionResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CourtDetailsReduced;
import uk.gov.hmcts.juror.api.moj.domain.CodeType;
import uk.gov.hmcts.juror.api.moj.domain.CourtDetailsDto;

import java.util.List;
import java.util.Map;

public interface AdministrationService {
    List<CodeDescriptionResponse> viewCodeAndDescriptions(CodeType codeType);

    List<CourtDetailsReduced> viewCourts();

    CourtDetailsDto viewCourt(String courtCode);

    Map<Integer, List<BankHolidayDate>> viewBankHolidays();
}
