package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.Juror;

public interface JurorThirdPartyService {
    void deleteThirdParty(Juror juror);

    void createOrUpdateThirdParty(Juror juror, ThirdPartyUpdateDto thirdPartyUpdate);


    interface ThirdPartyUpdateDto {

        String getThirdPartyOtherReason();

        String getThirdPartyReason();

        String getThirdPartyEmailAddress();

        String getThirdPartyOtherPhone();

        String getThirdPartyMainPhone();

        String getThirdPartyRelationship();

        String getThirdPartyLastName();

        String getThirdPartyFirstName();

        boolean isContactJurorByEmail();

        boolean isContactJurorByPhone();
    }
}
