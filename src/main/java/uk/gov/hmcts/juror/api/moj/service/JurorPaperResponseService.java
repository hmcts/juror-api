package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.CJSEmploymentDetailsDto;
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

    void saveResponse(BureauJWTPayload payload, JurorPaperResponseDto paperResponseDto);

    JurorPaperResponseDetailDto getJurorPaperResponse(String jurorNumber, BureauJWTPayload payload);

    void updateCjsDetails(BureauJWTPayload payload, CJSEmploymentDetailsDto cjsEmploymentDetailsDto,
                          String jurorNumber);

    void updateReasonableAdjustmentsDetails(BureauJWTPayload payload, ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto,
                                            String jurorNumber);

    void updateJurorEligibilityDetails(BureauJWTPayload payload, EligibilityDetailsDto eligibilityDetailsDto,
                                       String jurorNumber);

    void updateJurorReplyTypeDetails(BureauJWTPayload payload, ReplyTypeDetailsDto replyTypeDetailsDto,
                                     String jurorNumber);

    void updateJurorSignatureDetails(BureauJWTPayload payload, SignatureDetailsDto signatureDetailsDto,
                                     String jurorNumber);
}
