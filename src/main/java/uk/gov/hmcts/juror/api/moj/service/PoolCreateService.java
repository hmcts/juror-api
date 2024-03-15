package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolAddCitizenRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.NilPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolAdditionalSummonsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolMemberFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.SummonsFormRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CoronerPoolItemDto;
import uk.gov.hmcts.juror.api.moj.controller.response.NilPoolResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestItemDto;
import uk.gov.hmcts.juror.api.moj.controller.response.SummonsFormResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.FilterPoolMember;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;

import java.time.LocalDate;
import java.util.List;

public interface PoolCreateService {

    String DISQUALIFIED_ON_SELECTION = "Disq. on selection";

    PoolRequestItemDto getPoolRequest(String poolNumber, String owner);

    SummonsFormResponseDto summonsForm(SummonsFormRequestDto summonsFormRequestDto);

    int getBureauDeferrals(String locationCode, LocalDate deferredTo);

    void createPool(BureauJWTPayload payload, PoolCreateRequestDto poolCreateRequestDto) throws Exception;

    PaginatedList<FilterPoolMember> getJurorPoolsList(BureauJWTPayload payload, PoolMemberFilterRequestQuery search);

    List<String> getThinJurorPoolsList(String poolNumber, String owner);

    List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> getAvailableVotersByLocation(String areaCode,
                                                                                         boolean isCoronersPool);

    NilPoolResponseDto checkForDeferrals(String owner, NilPoolRequestDto nilPoolRequestDto);

    void createNilPool(String owner, NilPoolRequestDto nilPoolRequestDto);

    void lockVotersAndCreatePool(BureauJWTPayload payload, PoolCreateRequestDto poolCreateRequestDto);

    void lockVotersAndSummonAdditionalCitizens(BureauJWTPayload payload,
                                               PoolAdditionalSummonsDto poolAdditionalSummonsDto);

    void convertNilPool(PoolRequestDto poolRequestDto, BureauJWTPayload payload);

    String createCoronerPool(String owner, CoronerPoolRequestDto coronerPoolRequestDto);

    CoronerPoolItemDto getCoronerPool(String poolNumber);

    void addCitizensToCoronerPool(String owner, CoronerPoolAddCitizenRequestDto coronerPoolAddCitizenRequestDto);
}
