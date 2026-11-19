package uk.gov.hmcts.juror.api.juror.notify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.config.NotifyConfigurationProperties;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyAdapterImplTest {
    private static final String TEMPLATE_ID = "00000000-0000-0000-0000-000000000001";
    private static final String NOTIFICATION_ID = "00000000-0000-0000-0000-000000000002";
    private static final String EMAIL = "juror@example.com";
    private static final String PHONE = "07700900000";
    private static final String REFERENCE = "123456789";
    private static final Map<String, String> PAYLOAD = Map.of("juror_number", REFERENCE);

    @Mock
    private NotificationClientApi client;

    private NotifyConfigurationProperties properties;
    private NotifyAdapter adapter;
    private EmailNotification email;
    private SmsNotification sms;

    @BeforeEach
    void setUp() {
        properties = new NotifyConfigurationProperties();
        adapter = new NotifyAdapterImpl(properties, client);
        email = new EmailNotification(TEMPLATE_ID, EMAIL, PAYLOAD);
        email.setReferenceNumber(REFERENCE);
        sms = new SmsNotification(TEMPLATE_ID, PHONE, PAYLOAD);
        sms.setReferenceNumber(REFERENCE);
    }

    @Test
    void rejectsMissingConfiguration() {
        assertThatThrownBy(() -> new NotifyAdapterImpl(null, client))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("NotifyConfigurationProperties cannot be null");
    }

    @Test
    void rejectsMissingClient() {
        assertThatThrownBy(() -> new NotifyAdapterImpl(properties, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("NotificationClient cannot be null");
    }

    @ParameterizedTest
    @EnumSource(Channel.class)
    void rejectsNullNotificationEvenWhenDisabled(Channel channel) {
        email = null;
        sms = null;
        for (boolean disabled : new boolean[]{false, true}) {
            properties.setDisabled(disabled);
            assertThatThrownBy(() -> send(channel))
                .isInstanceOf(NotifyApiException.class)
                .hasMessage(channel == Channel.COMMS_SMS
                    ? "SmsNotification cannot be null" : "EmailNotification cannot be null");
        }
        verifyNoInteractions(client);
    }

    @ParameterizedTest
    @EnumSource(Channel.class)
    void disabledReturnsNullWithoutCallingNotify(Channel channel) {
        properties.setDisabled(true);

        assertThat(send(channel)).isNull();

        verifyNoInteractions(client);
    }

    @ParameterizedTest
    @EnumSource(Channel.class)
    void nullResponseReturnsNullAndForwardsAllArguments(Channel channel) throws Exception {
        assertThat(send(channel)).isNull();

        verifyRequest(channel);
    }

    @ParameterizedTest
    @EnumSource(Channel.class)
    void preservesClientExceptionHandling(Channel channel) throws Exception {
        NotificationClientException failure = new NotificationClientException("Notify rejected request");
        failRequest(channel, failure);

        if (channel == Channel.EMAIL) {
            assertThat(send(channel)).isNull();
        } else {
            assertThatThrownBy(() -> send(channel))
                .isInstanceOf(NotifyApiException.class)
                .hasMessage(channel == Channel.COMMS_SMS
                    ? "Failed to send Juror Comms SMS via Notify: {}" : "Failed to send Juror Comms via Notify: {}")
                .hasCause(failure);
        }
        verifyRequest(channel);
    }

    @ParameterizedTest
    @EnumSource(Channel.class)
    void unexpectedExceptionReturnsNull(Channel channel) throws Exception {
        failRequest(channel, new IllegalStateException("Unexpected client failure"));

        assertThat(send(channel)).isNull();

        verifyRequest(channel);
    }

    @ParameterizedTest
    @EnumSource(Channel.class)
    void receiptConversionFailureReturnsNull(Channel channel) throws Exception {
        if (channel == Channel.COMMS_SMS) {
            SendSmsResponse response = mock(SendSmsResponse.class);
            when(response.getNotificationId()).thenThrow(new IllegalStateException("Invalid response"));
            when(client.sendSms(TEMPLATE_ID, PHONE, PAYLOAD, REFERENCE)).thenReturn(response);
        } else {
            SendEmailResponse response = mock(SendEmailResponse.class);
            when(response.getNotificationId()).thenThrow(new IllegalStateException("Invalid response"));
            when(client.sendEmail(TEMPLATE_ID, EMAIL, PAYLOAD, REFERENCE)).thenReturn(response);
        }

        assertThat(send(channel)).isNull();

        verifyRequest(channel);
    }

    @ParameterizedTest
    @EnumSource(Channel.class)
    void mapsSuccessfulResponse(Channel channel) throws Exception {
        stubResponse(channel, response(false));

        NotificationReceipt receipt = send(channel);

        assertThat(receipt).isNotNull();
        assertThat(receipt.getNotificationId()).hasToString(NOTIFICATION_ID);
        assertThat(receipt.getTemplateId()).hasToString(TEMPLATE_ID);
        assertThat(receipt.getTemplateVersion()).isEqualTo(7);
        assertThat(receipt.getTemplateUri()).isEqualTo("https://example.com/template");
        assertThat(receipt.getBody()).isEqualTo("Synthetic notification body");
        assertThat(receipt.getReference()).isEqualTo(REFERENCE);
        if (channel == Channel.COMMS_SMS) {
            assertThat(((SmsNotificationReceipt) receipt).getFromNumber()).isEqualTo("Jury service");
        } else {
            assertThat(((EmailNotificationReceipt) receipt).getSubject()).isEqualTo("Jury service");
            assertThat(((EmailNotificationReceipt) receipt).getFromEmail()).isEqualTo("sender@example.com");
        }
        verifyRequest(channel);
    }

    @ParameterizedTest
    @EnumSource(Channel.class)
    void missingOptionalResponseFieldsBecomeEmptyStrings(Channel channel) throws Exception {
        stubResponse(channel, response(true));

        NotificationReceipt receipt = send(channel);

        assertThat(receipt).isNotNull();
        assertThat(receipt.getReference()).isEmpty();
        if (channel == Channel.COMMS_SMS) {
            assertThat(((SmsNotificationReceipt) receipt).getFromNumber()).isEmpty();
        } else {
            assertThat(((EmailNotificationReceipt) receipt).getFromEmail()).isEmpty();
        }
        verifyRequest(channel);
    }

    private NotificationReceipt send(Channel channel) {
        return switch (channel) {
            case EMAIL -> adapter.sendEmail(email);
            case COMMS_EMAIL -> adapter.sendCommsEmail(email);
            case COMMS_SMS -> adapter.sendCommsSms(sms);
        };
    }

    private void failRequest(Channel channel, Exception failure) throws NotificationClientException {
        if (channel == Channel.COMMS_SMS) {
            doThrow(failure).when(client).sendSms(TEMPLATE_ID, PHONE, PAYLOAD, REFERENCE);
        } else {
            doThrow(failure).when(client).sendEmail(TEMPLATE_ID, EMAIL, PAYLOAD, REFERENCE);
        }
    }

    private void stubResponse(Channel channel, String json) throws NotificationClientException {
        if (channel == Channel.COMMS_SMS) {
            when(client.sendSms(TEMPLATE_ID, PHONE, PAYLOAD, REFERENCE)).thenReturn(new SendSmsResponse(json));
        } else {
            when(client.sendEmail(TEMPLATE_ID, EMAIL, PAYLOAD, REFERENCE)).thenReturn(new SendEmailResponse(json));
        }
    }

    private void verifyRequest(Channel channel) throws NotificationClientException {
        if (channel == Channel.COMMS_SMS) {
            verify(client).sendSms(TEMPLATE_ID, PHONE, PAYLOAD, REFERENCE);
        } else {
            verify(client).sendEmail(TEMPLATE_ID, EMAIL, PAYLOAD, REFERENCE);
        }
        verifyNoMoreInteractions(client);
    }

    private String response(boolean missingOptionalFields) {
        return """
            {
              "id": "%s",
              "reference": %s,
              "content": {
                "body": "Synthetic notification body",
                "subject": "Jury service",
                "from_email": %s,
                "from_number": %s
              },
              "template": {"id": "%s", "version": 7, "uri": "https://example.com/template"}
            }
            """.formatted(NOTIFICATION_ID, missingOptionalFields ? "null" : '"' + REFERENCE + '"',
                missingOptionalFields ? "null" : "\"sender@example.com\"",
                missingOptionalFields ? "null" : "\"Jury service\"", TEMPLATE_ID);
    }

    private enum Channel {
        EMAIL, COMMS_EMAIL, COMMS_SMS
    }
}
