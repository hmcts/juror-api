package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyApiException;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.BureauEmailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.EmailTemplateName;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.BureauEmailResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.ExcessiveImports")
class BureauMessagingServiceImplTest {

    private static final String JUROR_NUMBER = "610000050";
    private static final String EMAIL = "test.juror@example.com";
    private static final String CURRENT_USER = "BUREAU_USER1";
    private static final String CONTACT_INFO_TEMPLATE_ID = "eda6bed7-3e34-46d0-9f28-5c0fd0706e75";
    private static final String REFERRAL_TEMPLATE_ID = "a2cbeed3-4121-4fcd-b9d4-1de2f7b5c0c2";

    private NotifyAdapter notifyAdapter;
    private JurorRepository jurorRepository;
    private JurorHistoryRepository jurorHistoryRepository;
    private AppSettingService appSettingService;

    private BureauMessagingServiceImpl bureauMessagingService;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    void beforeEach() {
        notifyAdapter = mock(NotifyAdapter.class);
        jurorRepository = mock(JurorRepository.class);
        jurorHistoryRepository = mock(JurorHistoryRepository.class);
        appSettingService = mock(AppSettingService.class);

        bureauMessagingService = new BureauMessagingServiceImpl(
            notifyAdapter,
            jurorRepository,
            jurorHistoryRepository,
            appSettingService
        );

        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getActiveLogin).thenReturn(CURRENT_USER);
    }

    private void closeStatic() {
        if (securityUtilMockedStatic != null) {
            securityUtilMockedStatic.close();
        }
    }

    private BureauEmailRequestDto.JurorEmailDetail detail(String jurorNumber, String email,
                                                          EmailTemplateName templateName) {
        return BureauEmailRequestDto.JurorEmailDetail.builder()
            .jurorNumber(jurorNumber)
            .email(email)
            .emailTemplateName(templateName)
            .build();
    }

    @Nested
    @DisplayName("BureauEmailResponseDto sendEmailsToJurors(BureauEmailRequestDto request)")
    class SendEmailsToJurors {

        @Test
        void positiveTypicalSingleJurorContactInformation() {
            try {
                when(appSettingService.getWeAreGroupContactInformationTemplateId())
                    .thenReturn(CONTACT_INFO_TEMPLATE_ID);
                when(jurorRepository.findById(JUROR_NUMBER))
                    .thenReturn(Optional.of(mock(Juror.class)));

                BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                    .jurorEmails(List.of(detail(JUROR_NUMBER, EMAIL, EmailTemplateName.CONTACT_INFORMATION)))
                    .build();

                BureauEmailResponseDto response = bureauMessagingService.sendEmailsToJurors(request);

                assertThat(response).isNotNull();
                assertThat(response.getTotalJurorsRequested()).isEqualTo(1);
                assertThat(response.getSuccessfulEmailsSent()).isEqualTo(1);
                assertThat(response.getFailedNotifications()).isEmpty();

                ArgumentCaptor<EmailNotification> notificationCaptor =
                    ArgumentCaptor.forClass(EmailNotification.class);
                verify(notifyAdapter, times(1)).sendCommsEmail(notificationCaptor.capture());
                EmailNotification sentNotification = notificationCaptor.getValue();
                assertThat(sentNotification.getTemplateId()).isEqualTo(CONTACT_INFO_TEMPLATE_ID);
                assertThat(sentNotification.getRecipientEmail()).isEqualTo(EMAIL);
                assertThat(sentNotification.getPayload()).isEqualTo(Collections.emptyMap());
                assertThat(sentNotification.getReferenceNumber()).isEqualTo("BUREAU_EMAIL_" + EMAIL);

                ArgumentCaptor<JurorHistory> historyCaptor = ArgumentCaptor.forClass(JurorHistory.class);
                verify(jurorHistoryRepository, times(1)).save(historyCaptor.capture());
                JurorHistory savedHistory = historyCaptor.getValue();
                assertThat(savedHistory.getJurorNumber()).isEqualTo(JUROR_NUMBER);
                assertThat(savedHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.NOTIFY_MESSAGE_REQUESTED);
                assertThat(savedHistory.getCreatedBy()).isEqualTo(CURRENT_USER);
                assertThat(savedHistory.getOtherInformation())
                    .isEqualTo("Bureau email sent: " + EmailTemplateName.CONTACT_INFORMATION.name());
            } finally {
                closeStatic();
            }
        }

        @Test
        void positiveTypicalSingleJurorReferralConfirmed() {
            try {
                when(appSettingService.getWeAreGroupReferralConfirmedTemplateId())
                    .thenReturn(REFERRAL_TEMPLATE_ID);
                when(jurorRepository.findById(JUROR_NUMBER))
                    .thenReturn(Optional.of(mock(Juror.class)));

                BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                    .jurorEmails(List.of(detail(JUROR_NUMBER, EMAIL, EmailTemplateName.REFERRAL_CONFIRMED)))
                    .build();

                BureauEmailResponseDto response = bureauMessagingService.sendEmailsToJurors(request);

                assertThat(response.getSuccessfulEmailsSent()).isEqualTo(1);
                assertThat(response.getFailedNotifications()).isEmpty();

                ArgumentCaptor<EmailNotification> notificationCaptor =
                    ArgumentCaptor.forClass(EmailNotification.class);
                verify(notifyAdapter, times(1)).sendCommsEmail(notificationCaptor.capture());
                assertThat(notificationCaptor.getValue().getTemplateId()).isEqualTo(REFERRAL_TEMPLATE_ID);

                verify(jurorHistoryRepository, times(1)).save(any(JurorHistory.class));
            } finally {
                closeStatic();
            }
        }

        @Test
        void positiveMultipleJurorsAllSuccessful() {
            try {
                String jurorNumber2 = "610000051";
                when(appSettingService.getWeAreGroupContactInformationTemplateId())
                    .thenReturn(CONTACT_INFO_TEMPLATE_ID);
                when(appSettingService.getWeAreGroupReferralConfirmedTemplateId())
                    .thenReturn(REFERRAL_TEMPLATE_ID);
                when(jurorRepository.findById(JUROR_NUMBER)).thenReturn(Optional.of(mock(Juror.class)));
                when(jurorRepository.findById(jurorNumber2)).thenReturn(Optional.of(mock(Juror.class)));

                BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                    .jurorEmails(List.of(
                        detail(JUROR_NUMBER, EMAIL, EmailTemplateName.CONTACT_INFORMATION),
                        detail(jurorNumber2, "second.juror@example.com", EmailTemplateName.REFERRAL_CONFIRMED)
                    ))
                    .build();

                BureauEmailResponseDto response = bureauMessagingService.sendEmailsToJurors(request);

                assertThat(response.getTotalJurorsRequested()).isEqualTo(2);
                assertThat(response.getSuccessfulEmailsSent()).isEqualTo(2);
                assertThat(response.getFailedNotifications()).isEmpty();

                verify(notifyAdapter, times(2)).sendCommsEmail(any(EmailNotification.class));
                verify(jurorHistoryRepository, times(2)).save(any(JurorHistory.class));
            } finally {
                closeStatic();
            }
        }

        @Test
        void negativeTemplateNotConfigured() {
            try {
                when(appSettingService.getWeAreGroupContactInformationTemplateId())
                    .thenReturn(null);

                BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                    .jurorEmails(List.of(detail(JUROR_NUMBER, EMAIL, EmailTemplateName.CONTACT_INFORMATION)))
                    .build();

                BureauEmailResponseDto response = bureauMessagingService.sendEmailsToJurors(request);

                assertThat(response.getTotalJurorsRequested()).isEqualTo(1);
                assertThat(response.getSuccessfulEmailsSent()).isEqualTo(0);
                assertThat(response.getFailedNotifications()).hasSize(1);

                BureauEmailResponseDto.FailedNotification failure = response.getFailedNotifications().get(0);
                assertThat(failure.getJurorNumber()).isEqualTo(JUROR_NUMBER);
                assertThat(failure.getEmailAddress()).isEqualTo(EMAIL);
                assertThat(failure.getFailureReason())
                    .isEqualTo(BureauEmailResponseDto.FailureReason.TEMPLATE_NOT_CONFIGURED);

                verifyNoInteractions(notifyAdapter);
                verify(jurorRepository, never()).findById(anyString());
                verify(jurorHistoryRepository, never()).save(any());
            } finally {
                closeStatic();
            }
        }

        @Test
        void negativeTemplateBlank() {
            try {
                when(appSettingService.getWeAreGroupContactInformationTemplateId())
                    .thenReturn("   ");

                BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                    .jurorEmails(List.of(detail(JUROR_NUMBER, EMAIL, EmailTemplateName.CONTACT_INFORMATION)))
                    .build();

                BureauEmailResponseDto response = bureauMessagingService.sendEmailsToJurors(request);

                assertThat(response.getFailedNotifications()).hasSize(1);
                assertThat(response.getFailedNotifications().get(0).getFailureReason())
                    .isEqualTo(BureauEmailResponseDto.FailureReason.TEMPLATE_NOT_CONFIGURED);

                verifyNoInteractions(notifyAdapter);
            } finally {
                closeStatic();
            }
        }

        @Test
        void negativeJurorNotFound() {
            try {
                when(appSettingService.getWeAreGroupContactInformationTemplateId())
                    .thenReturn(CONTACT_INFO_TEMPLATE_ID);
                when(jurorRepository.findById(JUROR_NUMBER))
                    .thenReturn(Optional.empty());

                BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                    .jurorEmails(List.of(detail(JUROR_NUMBER, EMAIL, EmailTemplateName.CONTACT_INFORMATION)))
                    .build();

                BureauEmailResponseDto response = bureauMessagingService.sendEmailsToJurors(request);

                assertThat(response.getSuccessfulEmailsSent()).isEqualTo(0);
                assertThat(response.getFailedNotifications()).hasSize(1);

                BureauEmailResponseDto.FailedNotification failure = response.getFailedNotifications().get(0);
                assertThat(failure.getJurorNumber()).isEqualTo(JUROR_NUMBER);
                assertThat(failure.getEmailAddress()).isEqualTo(EMAIL);
                assertThat(failure.getFailureReason())
                    .isEqualTo(BureauEmailResponseDto.FailureReason.JUROR_NOT_FOUND);

                verifyNoInteractions(notifyAdapter);
                verify(jurorHistoryRepository, never()).save(any());
            } finally {
                closeStatic();
            }
        }

        @Test
        void negativeNotifyApiException() {
            try {
                when(appSettingService.getWeAreGroupContactInformationTemplateId())
                    .thenReturn(CONTACT_INFO_TEMPLATE_ID);
                when(jurorRepository.findById(JUROR_NUMBER))
                    .thenReturn(Optional.of(mock(Juror.class)));

                NotifyApiException notifyApiException = new NotifyApiException("Notify failed");
                org.mockito.Mockito.doThrow(notifyApiException)
                    .when(notifyAdapter).sendCommsEmail(any(EmailNotification.class));

                BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                    .jurorEmails(List.of(detail(JUROR_NUMBER, EMAIL, EmailTemplateName.CONTACT_INFORMATION)))
                    .build();

                BureauEmailResponseDto response = bureauMessagingService.sendEmailsToJurors(request);

                assertThat(response.getSuccessfulEmailsSent()).isEqualTo(0);
                assertThat(response.getFailedNotifications()).hasSize(1);

                BureauEmailResponseDto.FailedNotification failure = response.getFailedNotifications().get(0);
                assertThat(failure.getJurorNumber()).isEqualTo(JUROR_NUMBER);
                assertThat(failure.getEmailAddress()).isEqualTo(EMAIL);
                assertThat(failure.getFailureReason())
                    .isEqualTo(BureauEmailResponseDto.FailureReason.NOTIFY_API_ERROR);
                assertThat(failure.getFailureMessage()).contains("Notify failed");

                verify(jurorHistoryRepository, never()).save(any());
            } finally {
                closeStatic();
            }
        }

        @Test
        void positivePartialSuccessAndFailure() {
            try {
                String failingJurorNumber = "610000099";
                when(appSettingService.getWeAreGroupContactInformationTemplateId())
                    .thenReturn(CONTACT_INFO_TEMPLATE_ID);
                when(jurorRepository.findById(JUROR_NUMBER))
                    .thenReturn(Optional.of(mock(Juror.class)));
                when(jurorRepository.findById(failingJurorNumber))
                    .thenReturn(Optional.empty());

                BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                    .jurorEmails(List.of(
                        detail(JUROR_NUMBER, EMAIL, EmailTemplateName.CONTACT_INFORMATION),
                        detail(failingJurorNumber, "missing@example.com", EmailTemplateName.CONTACT_INFORMATION)
                    ))
                    .build();

                BureauEmailResponseDto response = bureauMessagingService.sendEmailsToJurors(request);

                assertThat(response.getTotalJurorsRequested()).isEqualTo(2);
                assertThat(response.getSuccessfulEmailsSent()).isEqualTo(1);
                assertThat(response.getFailedNotifications()).hasSize(1);
                assertThat(response.getFailedNotifications().get(0).getJurorNumber())
                    .isEqualTo(failingJurorNumber);
                assertThat(response.getFailedNotifications().get(0).getFailureReason())
                    .isEqualTo(BureauEmailResponseDto.FailureReason.JUROR_NOT_FOUND);

                verify(notifyAdapter, times(1)).sendCommsEmail(any(EmailNotification.class));
                verify(jurorHistoryRepository, times(1)).save(any(JurorHistory.class));
            } finally {
                closeStatic();
            }
        }

        @Test
        void negativeJurorHistorySaveFailureDoesNotFailRequest() {
            try {
                when(appSettingService.getWeAreGroupContactInformationTemplateId())
                    .thenReturn(CONTACT_INFO_TEMPLATE_ID);
                when(jurorRepository.findById(JUROR_NUMBER))
                    .thenReturn(Optional.of(mock(Juror.class)));
                org.mockito.Mockito.doThrow(new RuntimeException("DB error"))
                    .when(jurorHistoryRepository).save(any(JurorHistory.class));

                BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                    .jurorEmails(List.of(detail(JUROR_NUMBER, EMAIL, EmailTemplateName.CONTACT_INFORMATION)))
                    .build();

                BureauEmailResponseDto response = bureauMessagingService.sendEmailsToJurors(request);

                // email send still counted as successful even though history recording failed
                assertThat(response.getSuccessfulEmailsSent()).isEqualTo(1);
                assertThat(response.getFailedNotifications()).isEmpty();

                verify(notifyAdapter, times(1)).sendCommsEmail(any(EmailNotification.class));
            } finally {
                closeStatic();
            }
        }
    }
}
