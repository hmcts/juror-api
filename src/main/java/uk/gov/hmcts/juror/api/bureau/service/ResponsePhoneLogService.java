package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController;

public interface ResponsePhoneLogService {
    /**
     * Default phone code for inserted phone logs.
     */
    String DEFAULT_PHONE_CODE = "GE";

    /**
     * Insert a new phone log entry for a specific juror response.
     *
     * @param phoneLogDto Phone log data to insert
     * @param jurorId     Juror number to associate the phone log to.
     * @param auditUser   Username of actioning bureau officer
     */
    void updatePhoneLog(ResponseUpdateController.JurorPhoneLogDto phoneLogDto, String jurorId, String auditUser);
}
