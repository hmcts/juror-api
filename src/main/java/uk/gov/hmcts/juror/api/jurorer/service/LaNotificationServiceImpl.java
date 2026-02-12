package uk.gov.hmcts.juror.api.jurorer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyApiException;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.domain.ReminderHistory;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.ReminderHistoryRepository;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;




@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LaNotificationServiceImpl implements LaNotificationService {

    private final NotifyAdapter notifyAdapter;
    private final LocalAuthorityRepository localAuthorityRepository;
    private final AppSettingService appSettingService;
    private final DeadlineRepository deadlineRepository;
    private final ReminderHistoryRepository reminderHistoryRepository;

    private static final String TEMPLATE_PLACEHOLDER_GREETING = "GREETING";
    private static final String TEMPLATE_PLACEHOLDER_DEADLINE = "DEADLINEDATE";

    private static final String GREETING_MORNING = "Good morning";
    private static final String GREETING_AFTERNOON = "Good afternoon";
    private static final int AFTERNOON_HOUR_THRESHOLD = 12;

    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Override
    @Transactional
    public void sendNotificationsToLocalAuthorities(List<String> laCodes) {
        log.info("Processing notifications for {} LA codes", laCodes.size());

        // Retrieve template ID once for all notifications
        String templateId = appSettingService.getNotifyErReminderTemplateId();
        if (!StringUtils.hasText(templateId)) {
            log.error("LA Reminder Template ID not configured in APP_SETTING table. Cannot send notifications.");
            throw new IllegalStateException("LA_REMINDER_TEMPLATE_ID not configured in APP_SETTING table");
        }

        String currentUser = SecurityUtil.getActiveLogin();

        for (String laCode : laCodes) {
            try {
                LocalAuthority localAuthority = localAuthorityRepository.findById(laCode)
                    .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Local Authority not found for code: %s", laCode)));

                if (!StringUtils.hasText(localAuthority.getEmail())) {
                    log.warn("No email address found for LA code: {}", laCode);
                    continue;
                }

                Map<String, String> payload = buildEmailPayload(localAuthority);
                EmailNotification emailNotification = createEmailNotification(
                    localAuthority,
                    templateId,
                    payload
                );

                notifyAdapter.sendCommsEmail(emailNotification);
                log.info("Successfully sent notification to LA: {} ({})",
                         localAuthority.getLaName(), laCode);

                recordReminderHistory(laCode, currentUser);

            } catch (NotifyApiException e) {
                log.error("Failed to send notification to LA code: {}", laCode, e);
                // Depending on requirements, you might want to collect failures and report them
            } catch (Exception e) {
                log.error("Unexpected error processing LA code: {}", laCode, e);
            }
        }
    }

    @Override
    public EmailNotification createEmailNotification(LocalAuthority laDetails,
                                                     String templateId,
                                                     Map<String, String> payload) {
        EmailNotification notification = new EmailNotification(templateId, laDetails.getEmail(), payload);
        notification.setReferenceNumber(String.format("LA_REMINDER_%s", laDetails.getLaCode()));
        return notification;
    }

    /**
     * Records a reminder notification in the reminder_history table.
     *
     * @param laCode the Local Authority code
     * @param sentBy the username of who sent the reminder
     */
    private void recordReminderHistory(String laCode, String sentBy) {
        try {
            ReminderHistory reminderHistory = ReminderHistory.builder()
                .laCode(laCode)
                .sentBy(sentBy)
                .timeSent(LocalDateTime.now())
                .build();

            reminderHistoryRepository.save(reminderHistory);
            log.debug("Recorded reminder history for LA code: {} sent by: {}", laCode, sentBy);

        } catch (Exception e) {
            log.error("Failed to record reminder history for LA code: {}", laCode, e);
            // Don't throw - we don't want history recording failure to break the notification
        }
    }


    private Map<String, String> buildEmailPayload(LocalAuthority localAuthority) {
        Map<String, String> payload = new HashMap<>();

        // Add greeting based on time of day
        payload.put(TEMPLATE_PLACEHOLDER_GREETING, getGreeting());

        // Add deadline date - get current deadline and format it
        String deadlineDate = getCurrentDeadlineDate();
        payload.put(TEMPLATE_PLACEHOLDER_DEADLINE, deadlineDate);

        log.debug("Built email payload for LA {}: greeting={}, deadline={}",
                  localAuthority.getLaCode(), getGreeting(), deadlineDate);

        return payload;
    }

    /**
     * Determines the appropriate greeting based on the current time.
     * @return "Morning" if before noon, "Afternoon" otherwise
     */
    private String getGreeting() {
        LocalTime now = LocalTime.now();
        return now.getHour() < AFTERNOON_HOUR_THRESHOLD
            ? GREETING_MORNING
            : GREETING_AFTERNOON;
    }

    /**
     * Gets the current deadline date from the repository and formats it.
     * @return Formatted deadline date string (dd/MM/yyyy)
     */
    private String getCurrentDeadlineDate() {
        Optional<Deadline> currentDeadline = deadlineRepository.getCurrentDeadline();

        if (currentDeadline.isPresent()) {
            Deadline deadline = currentDeadline.get();
            if (deadline.getDeadlineDate() != null) {
                return deadline.getDeadlineDate().format(DEADLINE_FORMATTER);
            } else {
                log.info("Deadline record exists but deadline_date is null");
                return "N/A";
            }
        }

        log.info("No current deadline found in database");
        return "N/A";
    }
}
