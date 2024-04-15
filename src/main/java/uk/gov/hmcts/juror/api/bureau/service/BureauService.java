package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseOverviewDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauYourWorkCounts;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;

/**
 * Bureau service for bureau data access operations.
 */
public interface BureauService {
    BureauJurorDetailDto getDetailsByJurorNumber(String jurorNumber);

    BureauResponseSummaryWrapper getDetailsByProcessingStatus(String status);

    BureauYourWorkCounts getCounts(String staffLogin);

    BureauResponseSummaryWrapper getTodo(String staffLogin);

    BureauResponseSummaryWrapper getPending(String staffLogin);

    BureauResponseSummaryWrapper getCompletedToday(String staffLogin);

    BureauResponseOverviewDto getOverview(String staffLogin);

    BureauJurorDetailDto mapJurorDetailsToDto(ModJurorDetail jurorDetail);
}
