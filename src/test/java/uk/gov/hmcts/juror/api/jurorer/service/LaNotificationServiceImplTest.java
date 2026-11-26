package uk.gov.hmcts.juror.api.jurorer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaNotificationServiceImplTest {
    @Mock
    private NotifyAdapter notifyAdapter;
    @Mock
    private LocalAuthorityRepository localAuthorityRepository;
    @Mock
    private LaUserRepository laUserRepository;
    @Mock
    private AppSettingService appSettingService;
    @Mock
    private DeadlineRepository deadlineRepository;
    @Mock
    private ReminderHistoryRepository reminderHistoryRepository;
    @InjectMocks
    private LaNotificationServiceImpl service;

    private final Deadline deadline = Deadline.builder()
        .deadlineDate(java.time.LocalDate.of(2026, 12, 1))
        .uploadStartDate(java.time.LocalDate.of(2026, 10, 1)).build();

    @ParameterizedTest
    @EnumSource(LaEmailNotificationType.class)
    void sendsSelectedTemplateAndRecordsType(LaEmailNotificationType type) {
        configureTemplate(type);
        when(deadlineRepository.getCurrentDeadline()).thenReturn(Optional.of(deadline));
        configureRecipient();
        try (org.mockito.MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getActiveLogin).thenReturn("bureau");
            assertThat(service.sendNotificationsToLocalAuthorities(request(type))
                .getSuccessfulNotificationsSent()).isEqualTo(1);
        }
        ArgumentCaptor<EmailNotification> email = ArgumentCaptor.forClass(EmailNotification.class);
        verify(notifyAdapter).sendCommsEmail(email.capture());
        assertThat(email.getValue().getTemplateId()).isEqualTo(type.name());
        assertThat(email.getValue().getRecipientEmail()).isEqualTo("synthetic@example.com");
        assertThat(email.getValue().getReferenceNumber()).isEqualTo("LA_" + type.name() + "_synthetic@example.com");
        assertThat(email.getValue().getPayload()).containsEntry("DEADLINEDATE", "01/12/2026");
        assertThat(email.getValue().getPayload().get("GREETING")).isIn("Morning", "Afternoon");
        if (type == LaEmailNotificationType.INITIAL_EMAIL) {
            assertThat(email.getValue().getPayload()).hasSize(3).containsEntry("OPENPORTALDATE", "01/10/2026");
        } else {
            assertThat(email.getValue().getPayload()).hasSize(2).doesNotContainKey("OPENPORTALDATE");
        }
        ArgumentCaptor<ReminderHistory> history = ArgumentCaptor.forClass(ReminderHistory.class);
        verify(reminderHistoryRepository).save(history.capture());
        assertThat(history.getValue().getNotificationType()).isEqualTo(type);
        assertThat(history.getValue().getSentBy()).isEqualTo("bureau");
    }

    @ParameterizedTest
    @EnumSource(LaEmailNotificationType.class)
    void rejectsMissingTemplate(LaEmailNotificationType type) {
        assertThatIllegalStateException().isThrownBy(() -> service.sendNotificationsToLocalAuthorities(request(type)));
        verifyNoInteractions(notifyAdapter, reminderHistoryRepository, deadlineRepository);
    }

    @Test
    void rejectsMissingDeadline() {
        configureTemplate(LaEmailNotificationType.INITIAL_EMAIL);
        when(deadlineRepository.getCurrentDeadline()).thenReturn(Optional.empty());
        assertThatIllegalStateException().isThrownBy(() -> service.sendNotificationsToLocalAuthorities(
            request(LaEmailNotificationType.INITIAL_EMAIL)));
        verifyNoInteractions(notifyAdapter, reminderHistoryRepository);
    }

    @Test
    void rejectsMissingPortalDateForInitialEmail() {
        configureTemplate(LaEmailNotificationType.INITIAL_EMAIL);
        deadline.setUploadStartDate(null);
        when(deadlineRepository.getCurrentDeadline()).thenReturn(Optional.of(deadline));
        assertThatIllegalStateException().isThrownBy(() -> service.sendNotificationsToLocalAuthorities(
            request(LaEmailNotificationType.INITIAL_EMAIL))).withMessageContaining("upload_start_date");
        verifyNoInteractions(notifyAdapter, reminderHistoryRepository);
    }

    @Test
    void notifyFailureDoesNotRecordHistory() {
        configureTemplate(LaEmailNotificationType.REMINDER_EMAIL);
        when(deadlineRepository.getCurrentDeadline()).thenReturn(Optional.of(deadline));
        configureRecipient();
        doThrow(new NotifyApiException("Failure")).when(notifyAdapter).sendCommsEmail(any());
        try (org.mockito.MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getActiveLogin).thenReturn("bureau");
            LaNotificationResponseDto response = service.sendNotificationsToLocalAuthorities(
                request(LaEmailNotificationType.REMINDER_EMAIL));
            assertThat(response.getSuccessfulNotificationsSent()).isZero();
            assertThat(response.getFailedNotifications()).extracting(
                LaNotificationResponseDto.FailedNotification::getFailureReason)
                .containsExactly(LaNotificationResponseDto.FailureReason.NOTIFY_API_ERROR);
        }
        verifyNoInteractions(reminderHistoryRepository);
    }

    @Test
    void rejectsMissingDeadlineDate() {
        configureTemplate(LaEmailNotificationType.INITIAL_EMAIL);
        deadline.setDeadlineDate(null);
        when(deadlineRepository.getCurrentDeadline()).thenReturn(Optional.of(deadline));
        assertThatIllegalStateException().isThrownBy(() -> service.sendNotificationsToLocalAuthorities(
            request(LaEmailNotificationType.INITIAL_EMAIL))).withMessageContaining("deadline_date");
        verifyNoInteractions(notifyAdapter, reminderHistoryRepository);
    }

    @Test
    void missingAuthorityDoesNotSend() {
        configureTemplate(LaEmailNotificationType.INITIAL_EMAIL);
        when(deadlineRepository.getCurrentDeadline()).thenReturn(Optional.of(deadline));
        when(localAuthorityRepository.findById("001")).thenReturn(Optional.empty());
        try (org.mockito.MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getActiveLogin).thenReturn("bureau");
            LaNotificationResponseDto response = service.sendNotificationsToLocalAuthorities(
                request(LaEmailNotificationType.INITIAL_EMAIL));
            assertThat(response.getFailedNotifications()).extracting(
                LaNotificationResponseDto.FailedNotification::getFailureReason)
                .containsExactly(LaNotificationResponseDto.FailureReason.LA_NOT_FOUND);
        }
        verifyNoInteractions(notifyAdapter, reminderHistoryRepository);
    }

    private void configureTemplate(LaEmailNotificationType type) {
        if (type == LaEmailNotificationType.INITIAL_EMAIL) {
            when(appSettingService.getNotifyErInitialTemplateId()).thenReturn(type.name());
        } else {
            when(appSettingService.getNotifyErReminderTemplateId()).thenReturn(type.name());
        }
    }

    private void configureRecipient() {
        LocalAuthority authority = LocalAuthority.builder().laCode("001").build();
        when(localAuthorityRepository.findById("001")).thenReturn(Optional.of(authority));
        LaUser user = new LaUser();
        user.setUsername("synthetic@example.com");
        user.setActive(true);
        when(laUserRepository.findByLocalAuthority(authority)).thenReturn(List.of(user));
    }

    private LaNotificationRequestDto request(LaEmailNotificationType type) {
        return LaNotificationRequestDto.builder().laCodes(List.of("001")).notificationType(type).build();
    }
}
