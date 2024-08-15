package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.Juror;

public interface JurorThirdPartyService {
    void deleteThirdParty(Juror juror);

    void createOrUpdateThirdParty(Juror juror, ThirdPartyUpdateDto thirdPartyUpdate);


    interface ThirdPartyUpdateDto {

        String getOtherReason();

        String getReason();

        String getEmailAddress();

        String getOtherPhone();

        String getMainPhone();

        String getRelationship();

        String getLastName();

        String getFirstName();

        boolean isContactJurorByEmail();

        boolean isContactJurorByPhone();
    }
}
