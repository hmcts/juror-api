package uk.gov.hmcts.juror.api.jurorer.service;

import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;

import java.util.List;
import java.util.Map;

public interface LaNotificationService {

    EmailNotification createEmailNotification(LocalAuthority laDetails,
                                              String templateId,
                                              Map<String, String> payLoad);

    void sendNotificationsToLocalAuthorities(List<String> laCodes);
}
