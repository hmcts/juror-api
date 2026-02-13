package uk.gov.hmcts.juror.api.jurorer.service;

import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;


import java.util.List;
import java.util.Map;

public interface LaNotificationService {

    EmailNotification createEmailNotification(String emailAddress,
                                              String templateId,
                                              Map<String, String> payLoad);

    void sendNotificationsToLocalAuthorities(List<String> laCodes);
}
