package uk.gov.hmcts.juror.api.moj.domain.messages;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MessageTypeTest {


    @Nested
    class SendTypeTest {
        @Test
        void positiveSupportsSms() {
            assertThat(MessageType.SendType.EMAIL.supports(MessageType.SendType.SMS)).isFalse();
            assertThat(MessageType.SendType.SMS.supports(MessageType.SendType.SMS)).isTrue();
            assertThat(MessageType.SendType.EMAIL_AND_SMS.supports(MessageType.SendType.SMS)).isTrue();
        }

        @Test
        void positiveSupportsEmail() {
            assertThat(MessageType.SendType.EMAIL.supports(MessageType.SendType.EMAIL)).isTrue();
            assertThat(MessageType.SendType.SMS.supports(MessageType.SendType.EMAIL)).isFalse();
            assertThat(MessageType.SendType.EMAIL_AND_SMS.supports(MessageType.SendType.EMAIL)).isTrue();

        }

        @Test
        void positiveSupportsBoth() {
            assertThat(MessageType.SendType.EMAIL.supports(MessageType.SendType.EMAIL_AND_SMS)).isTrue();
            assertThat(MessageType.SendType.SMS.supports(MessageType.SendType.EMAIL_AND_SMS)).isTrue();
            assertThat(MessageType.SendType.EMAIL_AND_SMS.supports(MessageType.SendType.EMAIL_AND_SMS)).isTrue();
        }
    }
}
