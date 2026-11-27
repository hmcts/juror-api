package uk.gov.hmcts.juror.api.jurorer.service;

import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaNotificationRequestDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaNotificationResponseDto;
import uk.gov.hmcts.juror.api.jurorer.enumeration.LaEmailNotificationType;

import java.util.Map;

public interface LaNotificationService {

    EmailNotification createEmailNotification(String emailAddress,
                                              String templateId,
                                              Map<String, String> payload,
                                              LaEmailNotificationType type);

    LaNotificationResponseDto sendNotificationsToLocalAuthorities(LaNotificationRequestDto request);
}
