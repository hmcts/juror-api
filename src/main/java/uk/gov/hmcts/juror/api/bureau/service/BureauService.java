package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseOverviewDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauYourWorkCounts;
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtYourWorkCounts;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;

/**
 * Bureau service for bureau data access operations.
 */
public interface BureauService {
    BureauJurorDetailDto getDetailsByJurorNumber(String jurorNumber);

    BureauResponseSummaryWrapper getDetailsByProcessingStatus(String status);

    BureauYourWorkCounts getCounts(String staffLogin);

    CourtYourWorkCounts getCountsForCourt(String locCode);

    BureauResponseSummaryWrapper getTodo(String staffLogin);

    CourtResponseSummaryWrapper getTodoCourt(String locCode);

    BureauResponseSummaryWrapper getPending(String staffLogin);

    BureauResponseSummaryWrapper getCompletedToday(String staffLogin);

    BureauResponseOverviewDto getOverview(String staffLogin);

    BureauJurorDetailDto mapJurorDetailsToDto(ModJurorDetail jurorDetail);
}
