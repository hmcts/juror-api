package uk.gov.hmcts.juror.api.moj.service.deferralmaintenance;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralAllocateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.ProcessJurorPostponementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.DeferralResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.CANNOT_DEFER_JUROR_WITH_APPEARANCE;

public interface ManageDeferralsService {

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

    void processJurorDeferral(BureauJwtPayload payload, String jurorNumber, DeferralReasonRequestDto deferralReasonDto);

    void allocateJurorsToActivePool(BureauJwtPayload payload, DeferralAllocateRequestDto dto);

    DeferralListDto getDeferralsByCourtLocationCode(BureauJwtPayload payload, String courtLocation);

    DeferralOptionsDto findActivePoolsForCourtLocation(BureauJwtPayload payload, String courtLocation);

    void changeJurorDeferralDate(BureauJwtPayload payload, String jurorNumber,
                                 DeferralReasonRequestDto deferralReasonRequestDto);

    void deleteDeferral(BureauJwtPayload payload, String jurorNumber);

    DeferralResponseDto processJurorPostponement(BureauJwtPayload payload,
                                                 ProcessJurorPostponementRequestDto processJurorRequestDto);

    static void checkIfJurorHasAttendances(JurorAppearanceService jurorAppearanceService, String jurorNumber) {
        // check if the juror has already been checked in/out
        if (jurorAppearanceService.hasAttendances(jurorNumber)) {
            throw new MojException.BusinessRuleViolation("Juror has already been checked in/out",
                                                         CANNOT_DEFER_JUROR_WITH_APPEARANCE);
        }
    }
}
