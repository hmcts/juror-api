package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.CjsEmploymentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EligibilityDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReasonableAdjustmentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReplyTypeDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.SignatureDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorPaperResponseDetailDto;

public interface JurorPaperResponseService {

    String TITLE = "title";
    String FIRSTNAME = "first name";
    String LASTNAME = "last name";
    String EMAIL_ADDRESS = "email address";
    String POSTCODE = "postcode";
    String DATE_OF_BIRTH = "date of birth";
    String THIRD_PARTY_REASON = "third party reason";
    String RESIDENCY = "residency";
    String CONVICTION = "conviction";
    String MENTAL_HEALTH = "mental health";
    String MENTAL_CAPACITY = "mental capacity";
    String BAIL = "bail";
    String EXCUSAL = "excusal";
    String DEFERRAL = "deferral";
    String SIGNATURE = "signature";

    void saveResponse(BureauJwtPayload payload, JurorPaperResponseDto paperResponseDto);

    JurorPaperResponseDetailDto getJurorPaperResponse(String jurorNumber, BureauJwtPayload payload);

    void updateCjsDetails(BureauJwtPayload payload, CjsEmploymentDetailsDto cjsEmploymentDetailsDto,
                          String jurorNumber);

    void updateReasonableAdjustmentsDetails(BureauJwtPayload payload,
                                            ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto,
                                            String jurorNumber);

    void updateJurorEligibilityDetails(BureauJwtPayload payload, EligibilityDetailsDto eligibilityDetailsDto,
                                       String jurorNumber);

    void updateJurorReplyTypeDetails(BureauJwtPayload payload, ReplyTypeDetailsDto replyTypeDetailsDto,
                                     String jurorNumber);

    void updateJurorSignatureDetails(BureauJwtPayload payload, SignatureDetailsDto signatureDetailsDto,
                                     String jurorNumber);
}
