package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtAdminInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtAttendanceInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtNotificationInfoDto;

public interface CourtDashboardService {


    CourtNotificationInfoDto getCourtNotifications(String locCode);

    CourtAdminInfoDto getCourtAdminInfo(String locCode);

    CourtAttendanceInfoDto getCourtAttendanceInfo(String locCode);

}
