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
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaNotificationRequestDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaNotificationResponseDto;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.domain.ReminderHistory;
import uk.gov.hmcts.juror.api.jurorer.enumeration.LaEmailNotificationType;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LaUserRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.ReminderHistoryRepository;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings({"PMD.CouplingBetweenObjects"})
public class LaNotificationServiceImpl implements LaNotificationService {

    private final NotifyAdapter notifyAdapter;
    private final LocalAuthorityRepository localAuthorityRepository;
    private final LaUserRepository laUserRepository;
    private final AppSettingService appSettingService;
    private final DeadlineRepository deadlineRepository;
    private final ReminderHistoryRepository reminderHistoryRepository;

    // Template placeholders
    private static final String TEMPLATE_PLACEHOLDER_GREETING = "GREETING";
    private static final String TEMPLATE_PLACEHOLDER_DEADLINE = "DEADLINEDATE";
    private static final String TEMPLATE_PLACEHOLDER_OPEN_PORTAL = "OPENPORTALDATE";
    // Greeting values
    private static final String GREETING_MORNING = "Morning";
    private static final String GREETING_AFTERNOON = "Afternoon";
    private static final int AFTERNOON_HOUR_THRESHOLD = 12;

    // Date formatter for deadline
    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.CognitiveComplexity"})
    public LaNotificationResponseDto sendNotificationsToLocalAuthorities(LaNotificationRequestDto request) {
        List<LaNotificationResponseDto.FailedNotification> failedNotifications = new ArrayList<>();

        LaEmailNotificationType type = request.getNotificationType();
        log.info("Processing {} notifications for {} LA codes", type, request.getLaCodes().size());
        String templateId = resolveTemplateId(type);
        Map<String, String> payload = buildEmailPayload(type);


        // Get current user for audit
        String currentUser = SecurityUtil.getActiveLogin();

        int successCount = 0;
        for (String laCode : request.getLaCodes()) {
            try {
                // Verify LA exists
                Optional<LocalAuthority> localAuthorityOpt = localAuthorityRepository.findById(laCode);

                if (localAuthorityOpt.isEmpty()) {
                    log.warn("Local Authority not found for code: {}", laCode);
                    failedNotifications.add(createFailedNotification(
                        laCode,
                        null,
                        null,
                        LaNotificationResponseDto.FailureReason.LA_NOT_FOUND,
                        "Local Authority not found"
                    ));
                    continue;
                }

                LocalAuthority localAuthority = localAuthorityOpt.get();

                // Get all users for this LA and filter to active only
                List<LaUser> allUsers = laUserRepository.findByLocalAuthority(localAuthority);
                List<LaUser> activeUsers = allUsers.stream()
                    .filter(LaUser::isActive)
                    .collect(Collectors.toList());

                if (activeUsers.isEmpty()) {
                    // Check if there are inactive users
                    List<LaUser> inactiveUsers = allUsers.stream()
                        .filter(user -> !user.isActive())
                        .collect(Collectors.toList());

                    if (inactiveUsers.isEmpty()) {
                        log.warn("No users found for LA code: {} ({})", laCode, localAuthority.getLaName());
                        failedNotifications.add(createFailedNotification(
                            laCode,
                            localAuthority.getLaName(),
                            null,
                            LaNotificationResponseDto.FailureReason.NO_USERS_FOR_LA,
                            "No users exist for this Local Authority"
                        ));
                    } else {
                        // There are users but all are inactive
                        for (LaUser inactiveUser : inactiveUsers) {
                            String email = inactiveUser.getUsername();
                            failedNotifications.add(createFailedNotification(
                                laCode,
                                localAuthority.getLaName(),
                                email,
                                LaNotificationResponseDto.FailureReason.USER_INACTIVE,
                                "User account is inactive"
                            ));
                        }
                    }
                    continue;
                }

                log.info("Found {} active user(s) for LA: {} ({})",
                         activeUsers.size(), localAuthority.getLaName(), laCode);

                // Send notification to each active user
                for (LaUser user : activeUsers) {
                    try {
                        if (!StringUtils.hasText(user.getUsername())) {
                            log.warn("User has blank email address for LA code: {}", laCode);
                            failedNotifications.add(createFailedNotification(
                                laCode,
                                localAuthority.getLaName(),
                                null,
                                LaNotificationResponseDto.FailureReason.EMAIL_ADDRESS_BLANK,
                                "User has no email address"
                            ));
                            continue;
                        }

                        EmailNotification emailNotification = createEmailNotification(
                            user.getUsername(),
                            templateId,
                            payload,
                            type
                        );

                        notifyAdapter.sendCommsEmail(emailNotification);
                        log.info("Successfully sent {} notification for LA code: {}", type, laCode);

                        // Record the sent notification in reminder_history
                        recordReminderHistory(laCode, currentUser, user.getUsername(), type);

                        successCount++;

                    } catch (NotifyApiException e) {
                        log.error("Failed to send {} notification for LA code: {}", type, laCode);
                        failedNotifications.add(createFailedNotification(
                            laCode,
                            localAuthority.getLaName(),
                            user.getUsername(),
                            LaNotificationResponseDto.FailureReason.NOTIFY_API_ERROR,
                            "Failed to send email via GOV.UK Notify"
                        ));
                    }
                }

            } catch (Exception e) {
                log.error("Unexpected error processing LA code: {}", laCode, e);
                failedNotifications.add(createFailedNotification(
                    laCode,
                    null,
                    null,
                    LaNotificationResponseDto.FailureReason.UNEXPECTED_ERROR,
                    "Unexpected error processing notification"
                ));
            }
        }

        log.info("Notification processing complete. Successful: {}, Failed: {}",
                 successCount, failedNotifications.size());

        return LaNotificationResponseDto.builder()
            .totalLaCodesRequested(request.getLaCodes().size())
            .successfulNotificationsSent(successCount)
            .failedNotifications(failedNotifications)
            .build();
    }

    @Override
    public EmailNotification createEmailNotification(String emailAddress,
                                                     String templateId,
                                                     Map<String, String> payload,
                                                     LaEmailNotificationType type) {
        EmailNotification notification = new EmailNotification(templateId, emailAddress, payload);
        notification.setReferenceNumber(type.reference(emailAddress));
        return notification;
    }

    /**
     * Creates a failed notification record.
     */
    private LaNotificationResponseDto.FailedNotification createFailedNotification(
        String laCode,
        String laName,
        String emailAddress,
        LaNotificationResponseDto.FailureReason reason,
        String message) {

        return LaNotificationResponseDto.FailedNotification.builder()
            .laCode(laCode)
            .laName(laName)
            .emailAddress(emailAddress)
            .failureReason(reason)
            .failureMessage(message)
            .build();
    }

    /**
     * Records a reminder notification in the reminder_history table.
     *
     * @param laCode the Local Authority code
     * @param sentBy the username of who sent the reminder
     * @param sentTo the email address (username) of the recipient
     */
    private void recordReminderHistory(String laCode, String sentBy, String sentTo,
                                       LaEmailNotificationType type) {
        try {
            ReminderHistory reminderHistory = ReminderHistory.builder()
                .laCode(laCode)
                .sentBy(sentBy)
                .sentTo(sentTo)
                .timeSent(LocalDateTime.now())
                .notificationType(type)
                .build();
            reminderHistoryRepository.save(reminderHistory);
        } catch (Exception e) {
            log.error("Failed to record {} history for LA code: {}", type, laCode);
        }
    }


    private Map<String, String> buildEmailPayload(LaEmailNotificationType type) {
        Deadline deadline = deadlineRepository.getCurrentDeadline()
            .orElseThrow(() -> new IllegalStateException("No current deadline found - cannot send " + type));

        Map<String, String> payload = new ConcurrentHashMap<>();
        payload.put(TEMPLATE_PLACEHOLDER_GREETING, getGreeting());
        payload.put(TEMPLATE_PLACEHOLDER_DEADLINE, formatRequired(deadline.getDeadlineDate(), "deadline_date"));

        if (type == LaEmailNotificationType.INITIAL_EMAIL) {
            payload.put(TEMPLATE_PLACEHOLDER_OPEN_PORTAL,
                        formatRequired(deadline.getUploadStartDate(), "upload_start_date"));
        }
        return payload;
    }

    private String formatRequired(LocalDate date, String column) {
        if (date == null) {
            throw new IllegalStateException("Current deadline has no " + column);
        }
        return date.format(DEADLINE_FORMATTER);
    }


    private String resolveTemplateId(LaEmailNotificationType type) {
        String templateId = switch (type) {
            case INITIAL_EMAIL -> appSettingService.getNotifyErInitialTemplateId();
            case REMINDER_EMAIL -> appSettingService.getNotifyErReminderTemplateId();
        };
        if (!StringUtils.hasText(templateId)) {
            throw new IllegalStateException("Notify template ID for " + type + " not configured in APP_SETTING table");
        }
        return templateId;
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

}
