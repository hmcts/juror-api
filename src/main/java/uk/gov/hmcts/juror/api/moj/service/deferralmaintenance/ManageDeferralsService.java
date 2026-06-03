package uk.gov.hmcts.juror.api.moj.service.deferralmaintenance;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralAllocateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferredJurorMoveRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.BulkDisqualifyRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.ProcessJurorPostponementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.BulkDisqualifyResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.DeferralAgeDisqualificationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.CANNOT_DEFER_JUROR_WITH_APPEARANCE;

@SuppressWarnings("PMD.TooManyMethods")
public interface ManageDeferralsService {

    int AGE_DISQUALIFICATION_THRESHOLD = 76;

    long getDeferralsCount(String owner, String locationCode, LocalDate attendanceDate);

    int useCourtDeferrals(PoolRequest target, int deferralsRequested, String userId);

    int useBureauDeferrals(PoolRequest newPool, int deferrals, String userId);

    DeferralOptionsDto findActivePoolsForDates(DeferralDatesRequestDto deferralDatesRequestDto,
                                               String jurorNumber, BureauJwtPayload payload);

    DeferralOptionsDto findActivePoolsForDatesAndLocCode(DeferralDatesRequestDto deferralDatesRequestDto,
                                                         String jurorNumber, String locationCode,
                                                         BureauJwtPayload payload);

    List<String> getPreferredDeferralDates(String jurorNumber, BureauJwtPayload payload);

    DeferralOptionsDto getAvailablePoolsByCourtLocationCodeAndJurorNumber(BureauJwtPayload payload,
                                                                          String courtLocationCode,
                                                                          String jurorNumber);

    DeferralAgeDisqualificationResponseDto processJurorDeferral(BureauJwtPayload payload, String jurorNumber,
                                                                DeferralReasonRequestDto deferralReasonDto);

    DeferralAgeDisqualificationResponseDto allocateJurorsToActivePool(BureauJwtPayload payload,
                                                                      DeferralAllocateRequestDto dto);

    DeferralListDto getDeferralsByCourtLocationCode(BureauJwtPayload payload, String courtLocation);

    DeferralOptionsDto findActivePoolsForCourtLocation(BureauJwtPayload payload, String courtLocation);

    DeferralAgeDisqualificationResponseDto changeJurorDeferralDate(BureauJwtPayload payload, String jurorNumber,
                                                                   DeferralReasonRequestDto deferralReasonRequestDto);

    void deleteDeferral(BureauJwtPayload payload, String jurorNumber);

    DeferralAgeDisqualificationResponseDto processJurorPostponement(BureauJwtPayload payload,
                                                                    ProcessJurorPostponementRequestDto
                                                                        processJurorRequestDto);

    DeferralAgeDisqualificationResponseDto moveDeferredJuror(DeferredJurorMoveRequestDto requestDto);

    BulkDisqualifyResponseDto bulkDisqualifyForAge(BureauJwtPayload payload, BulkDisqualifyRequestDto requestDto);

    static void checkIfJurorHasAttendances(JurorAppearanceService jurorAppearanceService, String jurorNumber) {
        if (jurorAppearanceService.hasAttendances(jurorNumber)) {
            throw new MojException.BusinessRuleViolation("Juror has already been checked in/out",
                                                         CANNOT_DEFER_JUROR_WITH_APPEARANCE);
        }
    }

    static void clearOnCallIfRequired(JurorPool jurorPool) {
        if (jurorPool.isOnCall()) {
            jurorPool.setOnCall(false);
        }
    }

    static boolean isAgeDisqualified(LocalDate dateOfBirth, LocalDate serviceStartDate) {
        if (dateOfBirth == null || serviceStartDate == null) {
            return false;
        }
        return Period.between(dateOfBirth, serviceStartDate).getYears() >= AGE_DISQUALIFICATION_THRESHOLD;
    }

    static LocalDate resolveDateOfBirth(JurorPool jurorPool,
                                        JurorDigitalResponseRepositoryMod digitalResponseRepository,
                                        JurorPaperResponseRepositoryMod paperResponseRepository,
                                        ReplyMethod replyMethod) {
        if (replyMethod != null) {
            if (replyMethod == ReplyMethod.DIGITAL) {
                DigitalResponse digital = digitalResponseRepository.findByJurorNumber(
                    jurorPool.getJurorNumber());
                if (digital != null && digital.getDateOfBirth() != null) {
                    return digital.getDateOfBirth();
                }
            } else if (replyMethod == ReplyMethod.PAPER) {
                PaperResponse paper = paperResponseRepository.findByJurorNumber(
                    jurorPool.getJurorNumber());
                if (paper != null && paper.getDateOfBirth() != null) {
                    return paper.getDateOfBirth();
                }
            }
        }
        // null replyMethod or response record has no DOB — fall back to juror entity,
        // then digital, then paper
        LocalDate dob = jurorPool.getJuror().getDateOfBirth();
        if (dob != null) {
            return dob;
        }
        DigitalResponse digital = digitalResponseRepository.findByJurorNumber(jurorPool.getJurorNumber());
        if (digital != null && digital.getDateOfBirth() != null) {
            return digital.getDateOfBirth();
        }
        PaperResponse paper = paperResponseRepository.findByJurorNumber(jurorPool.getJurorNumber());
        if (paper != null && paper.getDateOfBirth() != null) {
            return paper.getDateOfBirth();
        }
        return null;
    }
}
