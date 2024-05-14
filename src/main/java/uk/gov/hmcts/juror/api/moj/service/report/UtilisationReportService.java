package uk.gov.hmcts.juror.api.moj.service.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;

import java.time.LocalDate;

public interface UtilisationReportService {
    DailyUtilisationReportResponse viewDailyUtilisationReport(String locCode, LocalDate reportFromDate,
                                                              LocalDate reportToDate);
}
