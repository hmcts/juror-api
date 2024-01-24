package uk.gov.hmcts.juror.api.moj.service;

import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationListDto;

import java.math.BigDecimal;
import java.util.List;

public interface CourtLocationService {

    CourtLocationListDto buildCourtLocationDataResponse(BureauJWTPayload payload);

    CourtLocationListDto buildAllCourtLocationDataResponse();

    CourtLocation getCourtLocation(String locCode);

    CourtLocation getCourtLocationByName(String locName);

    boolean getVotersLock(String locCode);

    boolean releaseVotersLock(String locCode);

    BigDecimal getYieldForCourtLocation(String locCode);

    List<CourtLocationDataDto> getCourtLocationsByPostcode(
        @Length(max = 4) String postcode);

}
