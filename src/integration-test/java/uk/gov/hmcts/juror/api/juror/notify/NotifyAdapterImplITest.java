package uk.gov.hmcts.juror.api.juror.notify;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.juror.api.config.NotifyConfigurationProperties;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class NotifyAdapterImplITest {
    private static final String TEMPLATE_ID = "00000000-0000-0000-0000-000000000001";
    private static final String EMAIL = "juror@example.com";
    private static final String PHONE = "07700900000";
    private static final String REFERENCE = "123456789";
    private static final Map<String, String> PAYLOAD = Map.of("juror_number", REFERENCE);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(NotifyTestConfiguration.class);

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void springConfigurationControlsAllNotificationChannels(boolean disabled) {
        NotificationClientApi client = mock(NotificationClientApi.class);
        contextRunner.withBean(NotificationClientApi.class, () -> client)
            .withPropertyValues("notify.disabled=" + disabled)
            .run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(NotifyAdapter.class);
                assertThat(context.getBean(NotifyConfigurationProperties.class).isDisabled()).isEqualTo(disabled);
                NotifyAdapter adapter = context.getBean(NotifyAdapter.class);
                EmailNotification email = new EmailNotification(TEMPLATE_ID, EMAIL, PAYLOAD);
                email.setReferenceNumber(REFERENCE);
                SmsNotification sms = new SmsNotification(TEMPLATE_ID, PHONE, PAYLOAD);
                sms.setReferenceNumber(REFERENCE);

                assertThat(adapter.sendEmail(email)).isNull();
                assertThat(adapter.sendCommsEmail(email)).isNull();
                assertThat(adapter.sendCommsSms(sms)).isNull();

                if (disabled) {
                    verifyNoInteractions(client);
                } else {
                    verify(client, times(2)).sendEmail(TEMPLATE_ID, EMAIL, PAYLOAD, REFERENCE);
                    verify(client).sendSms(TEMPLATE_ID, PHONE, PAYLOAD, REFERENCE);
                    verifyNoMoreInteractions(client);
                }
            });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void springAdapterPreservesCommsFailurePropagation(boolean smsChannel) {
        NotificationClientApi client = mock(NotificationClientApi.class);
        NotificationClientException failure = new NotificationClientException("Notify rejected request");
        contextRunner.withBean(NotificationClientApi.class, () -> client)
            .withPropertyValues("notify.disabled=false")
            .run(context -> {
                assertThat(context).hasNotFailed();
                NotifyAdapter adapter = context.getBean(NotifyAdapter.class);
                if (smsChannel) {
                    SmsNotification sms = new SmsNotification(TEMPLATE_ID, PHONE, PAYLOAD);
                    sms.setReferenceNumber(REFERENCE);
                    when(client.sendSms(TEMPLATE_ID, PHONE, PAYLOAD, REFERENCE)).thenThrow(failure);

                    assertThatThrownBy(() -> adapter.sendCommsSms(sms))
                        .isInstanceOf(NotifyApiException.class).hasCause(failure);
                } else {
                    EmailNotification email = new EmailNotification(TEMPLATE_ID, EMAIL, PAYLOAD);
                    email.setReferenceNumber(REFERENCE);
                    when(client.sendEmail(TEMPLATE_ID, EMAIL, PAYLOAD, REFERENCE)).thenThrow(failure);

                    assertThat(adapter.sendEmail(email)).isNull();
                    assertThatThrownBy(() -> adapter.sendCommsEmail(email))
                        .isInstanceOf(NotifyApiException.class).hasCause(failure);
                }
            });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(NotifyConfigurationProperties.class)
    @Import(NotifyAdapterImpl.class)
    static class NotifyTestConfiguration {
    }
}
