package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtNotificationListDto;

public interface CourtDashboardService {


    CourtNotificationListDto getCourtNotifications(String locCode);

}
