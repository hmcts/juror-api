package uk.gov.hmcts.juror.api.moj.service.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportJurorsResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.MonthlyUtilisationReportResponse;

import java.time.LocalDate;

public interface UtilisationReportService {
    DailyUtilisationReportResponse viewDailyUtilisationReport(String locCode, LocalDate reportFromDate,
                                                              LocalDate reportToDate);

    DailyUtilisationReportJurorsResponse viewDailyUtilisationJurors(String locCode, LocalDate reportDate);

    MonthlyUtilisationReportResponse generateMonthlyUtilisationReport(String locCode, LocalDate reportDate);

    MonthlyUtilisationReportResponse viewMonthlyUtilisationReport(String locCode, LocalDate reportDate,
                                                                 boolean previousMonths);
}
