package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyApiException;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.BureauEmailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.BureauEmailRequestDto.JurorEmailDetail;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.EmailTemplateName;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.BureauEmailResponseDto;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BureauMessagingServiceImpl implements BureauMessagingService {

    private final NotifyAdapter notifyAdapter;
    private final JurorRepository jurorRepository;
    private final JurorHistoryRepository jurorHistoryRepository;
    private final AppSettingService appSettingService;

    private static final HistoryCodeMod EMAIL_SENT_HISTORY_CODE = HistoryCodeMod.NOTIFY_MESSAGE_REQUESTED;

    @Override
    @Transactional
    public BureauEmailResponseDto sendEmailsToJurors(BureauEmailRequestDto request) {
        log.info("Processing bureau emails for {} juror(s)", request.getJurorEmails().size());

        List<BureauEmailResponseDto.FailedNotification> failedNotifications = new ArrayList<>();
        int successCount = 0;
        String currentUser = SecurityUtil.getActiveLogin();

        for (JurorEmailDetail detail : request.getJurorEmails()) {
            String jurorNumber = detail.getJurorNumber();
            try {
                String templateId = resolveTemplateId(detail.getEmailTemplateName());
                if (!StringUtils.hasText(templateId)) {
                    log.error("Notify template not configured for: {}", detail.getEmailTemplateName());
                    failedNotifications.add(createFailedNotification(
                        jurorNumber, detail.getEmail(),
                        BureauEmailResponseDto.FailureReason.TEMPLATE_NOT_CONFIGURED,
                        "Notify template not configured in APP_SETTING table for "
                            + detail.getEmailTemplateName()
                    ));
                    continue;
                }

                if (jurorRepository.findById(jurorNumber).isEmpty()) {
                    log.warn("Juror not found for juror number: {}", jurorNumber);
                    failedNotifications.add(createFailedNotification(
                        jurorNumber, detail.getEmail(),
                        BureauEmailResponseDto.FailureReason.JUROR_NOT_FOUND,
                        "Juror not found"
                    ));
                    continue;
                }

                try {
                    // Templates for these notifications have no placeholders, so an empty map is sent
                    EmailNotification emailNotification = createEmailNotification(
                        detail.getEmail(), templateId, Collections.emptyMap()
                    );

                    notifyAdapter.sendCommsEmail(emailNotification);
                    log.info("Successfully sent bureau email to juror: {} ({})", jurorNumber,
                             detail.getEmailTemplateName());

                    recordJurorHistory(jurorNumber, currentUser);
                    successCount++;

                } catch (NotifyApiException e) {
                    log.error("Failed to send bureau email to juror: {}", jurorNumber, e);
                    failedNotifications.add(createFailedNotification(
                        jurorNumber, detail.getEmail(),
                        BureauEmailResponseDto.FailureReason.NOTIFY_API_ERROR,
                        "Failed to send email via GOV.UK Notify: " + e.getMessage()
                    ));
                }

            } catch (Exception e) {
                log.error("Unexpected error processing juror number: {}", jurorNumber, e);
                failedNotifications.add(createFailedNotification(
                    jurorNumber, detail.getEmail(),
                    BureauEmailResponseDto.FailureReason.UNEXPECTED_ERROR,
                    "Unexpected error: " + e.getMessage()
                ));
            }
        }

        log.info("Bureau email processing complete. Successful: {}, Failed: {}",
                 successCount, failedNotifications.size());

        return BureauEmailResponseDto.builder()
            .totalJurorsRequested(request.getJurorEmails().size())
            .successfulEmailsSent(successCount)
            .failedNotifications(failedNotifications)
            .build();
    }

    private String resolveTemplateId(EmailTemplateName templateName) {
        return switch (templateName) {
            case CONTACT_INFORMATION -> appSettingService.getWeAreGroupContactInformationTemplateId();
            case REFERRAL_CONFIRMED -> appSettingService.getWeAreGroupReferralConfirmedTemplateId();
        };
    }

    private EmailNotification createEmailNotification(String emailAddress, String templateId,
                                                      Map<String, String> payload) {
        EmailNotification notification = new EmailNotification(templateId, emailAddress, payload);
        notification.setReferenceNumber(String.format("BUREAU_EMAIL_%s", emailAddress));
        return notification;
    }

    private BureauEmailResponseDto.FailedNotification createFailedNotification(
        String jurorNumber, String emailAddress,
        BureauEmailResponseDto.FailureReason reason, String message) {

        return BureauEmailResponseDto.FailedNotification.builder()
            .jurorNumber(jurorNumber)
            .emailAddress(emailAddress)
            .failureReason(reason)
            .failureMessage(message)
            .build();
    }

    private void recordJurorHistory(String jurorNumber, String sentBy) {
        try {
            JurorHistory history = JurorHistory.builder()
                .jurorNumber(jurorNumber)
                .historyCode(EMAIL_SENT_HISTORY_CODE)
                .createdBy(sentBy)
                .otherInformation("Bureau email sent")
                .build();

            jurorHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Failed to record juror history for juror number: {}", jurorNumber, e);
            // don't throw - history recording failure shouldn't block the email send
        }
    }
}
