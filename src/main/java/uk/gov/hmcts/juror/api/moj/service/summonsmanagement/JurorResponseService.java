package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPersonalDetailsDto;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorCommonResponseRepositoryMod;

import java.util.Optional;

public interface JurorResponseService {
    String TITLE = "title";
    String FIRSTNAME = "first name";
    String LASTNAME = "last name";
    String PRIMARY_PHONE = "primary phone";
    String SECONDARY_PHONE = "secondary phone";
    String EMAIL_ADDRESS = "email address";
    String ADDRESS_LINE1 = "address line 1";
    String ADDRESS_LINE2 = "address line 2";
    String ADDRESS_LINE3 = "address line 3";
    String ADDRESS_LINE4 = "address line 4";
    String ADDRESS_LINE5 = "address line 5";
    String POSTCODE = "postcode";
    String DATE_OF_BIRTH = "date of birth";

    String THIRD_PARTY_FIRSTNAME = "third party first name";

    String THIRD_PARTY_LASTNAME = "third party last name";

    String THIRD_PARTY_MAIN_PHONE = "third party main phone";

    String THIRD_PARTY_OTHER_PHONE = "third party other phone";

    String THIRD_PARTY_EMAIL_ADDRESS = "third party email address";

    Boolean THIRD_PARTY_CONTACT_JUROR_BY_PHONE = true;

    Boolean THIRD_PARTY_CONTACT_JUROR_BY_EMAIL = true;

    String THIRD_PARTY_RELATIONSHIP = "third party relationship";
    String THIRD_PARTY_REASON = "third party reason";
    String THIRD_PARTY_OTHER_REASON = "third party other reason";
    String RESIDENCY = "residency";
    String BAIL = "bail";
    String EXCUSAL = "excusal";
    String DEFERRAL = "deferral";

    void updateJurorPersonalDetails(BureauJwtPayload payload, JurorPersonalDetailsDto jurorPersonalDetailsDto,
                                    String jurorNumber);

    Optional<JurorCommonResponseRepositoryMod.AbstractResponse> getCommonJurorResponseOptional(String jurorNumber);

    JurorCommonResponseRepositoryMod.AbstractResponse getCommonJurorResponse(String jurorNumber);

    void setResponseProcessingStatusToClosed(String jurorNumber);
}
