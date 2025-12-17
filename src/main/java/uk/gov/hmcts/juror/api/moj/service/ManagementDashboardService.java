package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.IncompleteServiceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.OverdueUtilisationReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.WeekendAttendanceReportResponseDto;

import java.util.List;

public interface ManagementDashboardService {

    OverdueUtilisationReportResponseDto getOverdueUtilisationReport(boolean top10);

    IncompleteServiceReportResponseDto getIncompleteServiceReport();

    WeekendAttendanceReportResponseDto getWeekendAttendanceReport();

    List<String> adjustedStatsForCommas(List<String> stats);


}
