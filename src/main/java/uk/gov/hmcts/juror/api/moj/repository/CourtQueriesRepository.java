package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;

import java.util.List;

/**
 * Custom Repository definition to extract data from multiple tables using query factory
 */
public interface CourtQueriesRepository {
    List<CourtLocationDataDto> getCourtDetailsFilteredByPostcode(String firstHalfOfPostcode);
}
