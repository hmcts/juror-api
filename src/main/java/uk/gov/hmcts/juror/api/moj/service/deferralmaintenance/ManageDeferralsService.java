package uk.gov.hmcts.juror.api.moj.service.deferralmaintenance;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralAllocateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.ProcessJurorPostponementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.DeferralResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.time.LocalDate;
import java.util.List;

public interface ManageDeferralsService {

    long getDeferralsCount(String owner, String locationCode, LocalDate attendanceDate);

    int useCourtDeferrals(PoolRequest target, int deferralsRequested, String userId);

    int useBureauDeferrals(PoolRequest newPool, int deferrals, String userId);

    DeferralOptionsDto findActivePoolsForDates(DeferralDatesRequestDto deferralDatesRequestDto,
                                               String jurorNumber, BureauJWTPayload payload);

    DeferralOptionsDto findActivePoolsForDatesAndLocCode(DeferralDatesRequestDto deferralDatesRequestDto,
                                                         String jurorNumber, String locationCode,
                                                         BureauJWTPayload payload);

    List<String> getPreferredDeferralDates(String jurorNumber, BureauJWTPayload payload);

    DeferralOptionsDto getAvailablePoolsByCourtLocationCodeAndJurorNumber(BureauJWTPayload payload,
                                                                          String courtLocationCode,
                                                                          String jurorNumber);

    void processJurorDeferral(BureauJWTPayload payload, String jurorNumber, DeferralReasonRequestDto deferralReasonDto);

    void allocateJurorsToActivePool(BureauJWTPayload payload, DeferralAllocateRequestDto dto);

    DeferralListDto getDeferralsByCourtLocationCode(BureauJWTPayload payload, String courtLocation);

    DeferralOptionsDto findActivePoolsForCourtLocation(BureauJWTPayload payload, String courtLocation);

    void changeJurorDeferralDate(BureauJWTPayload payload, String jurorNumber,
                                 DeferralReasonRequestDto deferralReasonRequestDto);

    void deleteDeferral(BureauJWTPayload payload, String jurorNumber);

    DeferralResponseDto processJurorPostponement(BureauJWTPayload payload,
                                                 ProcessJurorPostponementRequestDto processJurorRequestDto);
}
