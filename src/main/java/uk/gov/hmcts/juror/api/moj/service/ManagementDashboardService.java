package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.OverdueUtilisationReportResponseDto;

public interface ManagementDashboardService {

    OverdueUtilisationReportResponseDto getOverdueUtilisationReport();

}
