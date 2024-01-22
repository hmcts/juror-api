package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;

import java.util.List;

public interface IPendingJurorRepository {

    List<PendingJurorsResponseDto.PendingJurorsResponseData> findPendingJurorsForCourt(String locCode,
                                                                                       PendingJurorStatus status);
}
