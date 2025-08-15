package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.bureaudashboard.BureauNotificationManagementInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.bureaudashboard.BureauPoolManagementInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.bureaudashboard.BureauPoolsUnderRespondedInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.bureaudashboard.BureauSummonsManagementInfoDto;

public interface BureauDashboardService {



    /**
     * Retrieves the pool management information for the bureau dashboard.
     *
     * @return BureauPoolManagementInfoDto containing pool management details.
     */
    BureauPoolManagementInfoDto getBureauPoolManagementInfo(String locCode);

    /**
     * Retrieves the summons management information for the bureau dashboard.
     *
     * @return BureauSummonsManagementInfoDto containing summons management details.
     */
    BureauSummonsManagementInfoDto getBureauSummonsManagementInfo(String locCode);

    /**
     * Retrieves the notification management information for the bureau dashboard.
     *
     * @return BureauNotificationManagementInfoDto containing notification management details.
     */
    BureauNotificationManagementInfoDto getBureauNotificationManagementInfo(String locCode);

    BureauPoolsUnderRespondedInfoDto getBureauPoolsUnderRespondedInfo();
}
