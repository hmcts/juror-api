package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;

import java.util.List;

@FunctionalInterface
public interface VotersLocPostcodeTotalsService {

    List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> getCourtCatchmentSummaryItems(String locationCode,
                                                                                          boolean isCoronersPool);

}
