package uk.gov.hmcts.juror.api.bureau.notify;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsNotificationServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsNotifyPayLoadService;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotificationReceipt;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotification;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotificationReceipt;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.NotifyTemplateMappingRepositoryMod;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "notify.disabled=false")
class JurorCommsNotifyAdapterImplTest extends ContainerTest {

    private static final String JUROR_NUM = "juror number";
    private static final String COURT_NAME = "PRESTON";
    private static final String VALUE_2 = "value2";
    private static final String SERVICES_START_DATE = "SERVICESTARTDATE";
    private static final String SERVICES_START_TIME = "SERVICESTARTTIME";
    private static final String FIRST_NAME_VAL = "FIRSTNAME";
    private static final String LAST_NAME_VAL = "LASTNAME";
    private static final String EMAIL_ADDRESS = "email address";
    private static final String COURT_PHONE = "COURTPHONE";


    private static final String DEV_CONFIRM_JUROR_ENG_TEMPLATE_ID = "ADBEDBA6-0955-4D79-AC75-5DD2306A10F4";
    //CONFRIM_JUROR_ENG
    private static final String DEV_SENT_TO_COURT_EMAIL_ENG_TEMPLATE_ID = "984B2883-AD40-44D6-A1B0-A0AED98AFF28";
    //SENT_TO_COURT_ENG_EMAIL
    private static final String DEV_SENT_TO_COURT_SMS_ENG_TEMPLATE_ID = "e95cdb81-e015-4f0b-b306-40d97ccb76f6";
    public static final String TEMPLATE_ID_IS_THE_ONE_PASSED_FROM_THE_MOCK =
        "Template ID is the one passed from the mock.";
    public static final String JUROR_NUMBER_IS_THE_NOTIFY_REFERENCE = "Juror number is the Notify reference";
    public static final String BODY_IS_NOT_EMPTY_AND_CONTAINS_PAYLOAD_INFORMATION =
        "Body is not empty and contains payload information";
    public static final String JUROR_EMAIL = "confirmed.test@cgi.com";
    //SENT_TO_COURT_ENG_SMS

    /**
     * bean under test.
     */
    private final NotifyAdapter notifyAdapter;

    /**
     * mock provided to JurorCommsNotificationServiceImpl constructor only.
     */
    @Mock
    private NotifyTemplateMappingRepositoryMod notifyTemplateMappingRepository;
    /**
     * mock provided to JurorCommsNotificationServiceImpl constructor only.
     */
    @Mock
    private JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService;

    /**
     * Used to access utility method only.
     */
    private JurorCommsNotificationServiceImpl utilJurorCommsService;

    @BeforeEach
    void setUp() {
        utilJurorCommsService = new JurorCommsNotificationServiceImpl(notifyAdapter,
            notifyTemplateMappingRepository,
            jurorCommsNotifyPayLoadService);
    }

    @Test
    @Timeout(9)
    void sendCommsEmailServiceConfirmationEnglish() {
        // create the notification data class
        final String jurorNumber = "111222333";
        final String title = "Mr";
        final String firstName = "Harry";
        final String lastName = "Test";


        final Juror juror = Juror.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .email(JUROR_EMAIL)
            .welsh(false)
            .build();
        final JurorPool jurorPool = JurorPool.builder()
            .juror(juror)
            .build();


        Map<String, String> payLoad = new ConcurrentHashMap<>();
        payLoad.put(JUROR_NUM, jurorNumber);
        payLoad.put("courtname", COURT_NAME);
        payLoad.put(SERVICES_START_DATE, VALUE_2);
        payLoad.put(SERVICES_START_TIME, "value3");
        payLoad.put(FIRST_NAME_VAL, VALUE_2);
        payLoad.put(LAST_NAME_VAL, VALUE_2);
        payLoad.put(EMAIL_ADDRESS, JUROR_EMAIL);


        final EmailNotification emailNotification = utilJurorCommsService.createEmailNotification(
            jurorPool,
            JurorCommsNotifyTemplateType.LETTER_COMMS,
            DEV_CONFIRM_JUROR_ENG_TEMPLATE_ID,
            payLoad
        );

        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(JUROR_EMAIL);

        // send the email using the real Notify.gov
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendCommsEmail(emailNotification);
        validateEmailNotification(emailNotificationReceipt, jurorNumber, DEV_CONFIRM_JUROR_ENG_TEMPLATE_ID);
    }


    @Test
    @Timeout(9)
    void sendCommsEmailSendToCourtEmailEnglish() {
        // create the notification data class
        final String jurorNumber = "111222323";
        final String title = "Mr";
        final String firstName = "Harry";
        final String lastName = "Test";


        final Juror juror = Juror.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .email(JUROR_EMAIL)
            .welsh(false)
            .build();
        final JurorPool jurorPool = JurorPool.builder()
            .juror(juror)
            .build();


        Map<String, String> payLoad = new ConcurrentHashMap<>();
        payLoad.put(JUROR_NUM, jurorNumber);
        payLoad.put("court", COURT_NAME);
        payLoad.put(SERVICES_START_DATE, "12 August 2019");
        payLoad.put(SERVICES_START_TIME, "09:30");
        payLoad.put("court name", COURT_NAME);
        payLoad.put("address1", "123 my street");
        payLoad.put("address2", "my area");
        payLoad.put("address3", "my town");
        payLoad.put("address4", "uk");
        payLoad.put("address5", "value5");
        payLoad.put("address6", "value6");
        payLoad.put("postcode", "MA3 4SE");
        payLoad.put(COURT_PHONE, "07834987345");
        payLoad.put(FIRST_NAME_VAL, VALUE_2);
        payLoad.put(LAST_NAME_VAL, VALUE_2);
        payLoad.put(EMAIL_ADDRESS, JUROR_EMAIL);

        final EmailNotification emailNotification = utilJurorCommsService.createEmailNotification(
            jurorPool,
            JurorCommsNotifyTemplateType.SENT_TO_COURT,
            DEV_SENT_TO_COURT_EMAIL_ENG_TEMPLATE_ID,
            payLoad
        );

        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(JUROR_EMAIL);

        // send the email using the real Notify.gov
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendCommsEmail(emailNotification);

        validateEmailNotification(emailNotificationReceipt, jurorNumber, DEV_SENT_TO_COURT_EMAIL_ENG_TEMPLATE_ID);

    }

    private void validateEmailNotification(EmailNotificationReceipt emailNotificationReceipt, String jurorNumber,
                                           String templateId) {
        assertThat(emailNotificationReceipt.getTemplateId())
            .as(TEMPLATE_ID_IS_THE_ONE_PASSED_FROM_THE_MOCK)
            .isEqualTo(UUID.fromString(templateId));
        assertThat(emailNotificationReceipt.getReference())
            .as(JUROR_NUMBER_IS_THE_NOTIFY_REFERENCE)
            .isEqualTo(jurorNumber);
        assertThat(emailNotificationReceipt.getBody())
            .as(BODY_IS_NOT_EMPTY_AND_CONTAINS_PAYLOAD_INFORMATION)
            .isNotEmpty()
            .contains(jurorNumber);
    }


    @Test
    @Timeout(9)
    void sendCommsSmsSendToCourtSmsEnglish() {
        // create the notification data class
        final String jurorNumber = "111222333";
        final String title = "Mr";
        final String firstName = "Harry";
        final String lastName = "Test";
        final String phoneNumber = "44776-301-1119";

        final Juror juror = Juror.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .email(JUROR_EMAIL)
            .welsh(false)
            .build();
        final JurorPool jurorPool = JurorPool.builder()
            .juror(juror)
            .build();


        Map<String, String> payLoad = new ConcurrentHashMap<>();
        payLoad.put(JUROR_NUM, jurorNumber);
        payLoad.put("court", COURT_NAME);
        payLoad.put(SERVICES_START_DATE, "12 August 2019");
        payLoad.put(SERVICES_START_TIME, "09:30");
        payLoad.put("court name", COURT_NAME);
        payLoad.put("address1", "123 my street");
        payLoad.put("address2", "my area");
        payLoad.put("address3", "my town");
        payLoad.put("address4", "uk");
        payLoad.put("address5", "value5");
        payLoad.put("address6", "value6");
        payLoad.put("postcode", "MA3 4SE");
        payLoad.put(COURT_PHONE, "07834987345");
        payLoad.put(FIRST_NAME_VAL, VALUE_2);
        payLoad.put(LAST_NAME_VAL, VALUE_2);
        payLoad.put(EMAIL_ADDRESS, JUROR_EMAIL);
        payLoad.put("phone number", phoneNumber);

        final SmsNotification smsNotification = utilJurorCommsService.createSmsNotification(
            jurorPool,
            JurorCommsNotifyTemplateType.SENT_TO_COURT,
            DEV_SENT_TO_COURT_SMS_ENG_TEMPLATE_ID,
            payLoad
        );

        assertThat(smsNotification.getRecipientPhoneNumber()).as("Recipient phone number is correct")
            .isEqualTo(phoneNumber);

        // send the sms using the real Notify.gov
        final SmsNotificationReceipt smsNotificationReceipt = notifyAdapter.sendCommsSms(smsNotification);
        assertThat(smsNotificationReceipt.getTemplateId())
            .as(TEMPLATE_ID_IS_THE_ONE_PASSED_FROM_THE_MOCK)
            .isEqualTo(UUID.fromString(DEV_SENT_TO_COURT_SMS_ENG_TEMPLATE_ID));
        assertThat(smsNotificationReceipt.getReference())
            .as(JUROR_NUMBER_IS_THE_NOTIFY_REFERENCE)
            .isEqualTo(jurorNumber);
        assertThat(smsNotificationReceipt.getBody())
            .as(BODY_IS_NOT_EMPTY_AND_CONTAINS_PAYLOAD_INFORMATION)
            .isNotEmpty()
            .contains(payLoad.get(COURT_PHONE));
    }
}
