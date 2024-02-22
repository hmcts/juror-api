package uk.gov.hmcts.juror.api.bureau.Notify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.bureau.domain.AppSettingRepository;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetailRepository;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateMapping;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateMappingRepository;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsNotificationServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsNotifyPayLoadService;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotificationReceipt;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotification;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotificationReceipt;
import uk.gov.hmcts.juror.api.juror.service.JurorNotificationServiceImpl;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "notify.disabled=false")
public class JurorCommsNotifyAdapterImplIntegrationTest extends ContainerTest {
    private static final String DEV_FIRST_PERSON_TEMPLATE_ID = "ec33ab68-b917-4f25-918e-50d3291edef6"; // new
    // 1st_straight_through
    private static final String DEV_FIRST_PERSON_CY_TEMPLATE_ID = "aea4140b-2e2f-423b-8146-cd9615bfbc9e"; //
    // 1st_straight_through_wel
    private static final String DEV_THIRD_PARTY_TEMPLATE_ID = "1701b1b1-1b7f-4a7c-b320-41e731480d6f";  // new
    // 3rd_straight_through
    private static final String DEV_THIRD_PARTY_CY_TEMPLATE_ID = "591f6e20-bfb8-44d0-92ad-b6b8b8889f49"; //
    // 3rd_straight_through_wel
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
    //SENT_TO_COURT_ENG_SMS

    /**
     * bean under test
     */
    @Autowired
    private NotifyAdapter notifyAdapter;

    /**
     * mock provided to JurorCommsNotificationServiceImpl constructor only
     */
    @Mock
    private NotifyAdapter mockNotifyAdapter;

    /**
     * mock provided to JurorCommsNotificationServiceImpl constructor only
     */
    @Autowired
    private ResponseInspector responseInspector;

    /**
     * mock provided to JurorCommsNotificationServiceImpl constructor only
     */
    @Mock
    private AppSettingRepository appSettingRepository;

    @Mock
    private BureauJurorDetailRepository bureauJurorDetailRepository;

    /**
     * mock provided to JurorCommsNotificationServiceImpl constructor only.
     */
    @Mock
    private NotifyTemplateMappingRepository notifyTemplateMappingRepository;
    /**
     * mock provided to JurorCommsNotificationServiceImpl constructor only.
     */
    @Mock
    private JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService;

    /**
     * Used to access utility method
     * {@link JurorNotificationServiceImpl#createEmailNotification(JurorResponse, NotifyTemplateType)} only!
     */
    //private JurorNotificationServiceImpl utilService;

    /**
     * Used to access utility method only!
     */
    private JurorCommsNotificationServiceImpl utilJurorCommsService;

    @Before
    public void setUp() throws Exception {
        utilJurorCommsService = new JurorCommsNotificationServiceImpl(mockNotifyAdapter,
            notifyTemplateMappingRepository,
            jurorCommsNotifyPayLoadService);
    }

    @After
    public void tearDown() {
        verifyNoInteractions(mockNotifyAdapter);
    }

    @Test(timeout = 9000L)
    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    public void sendCommsEmail_service_confirmation_english() {
        // create the notification data class
        final String JUROR_CONFIRMATION = "CONFRIM_JUROR_ENG";
        final String JUROR_NUMBER = "111222333";
        final String TITLE = "Mr";
        final String FIRST_NAME = "Harry";
        final String LAST_NAME = "Test";
        final String EMAIL = "confirmed.test@cgi.com";

        final Pool pool = Pool.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(TITLE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .email(EMAIL)
            .welsh(false)
            .build();


        Map<String, String> payLoad = new HashMap<>();
        payLoad.put(JUROR_NUM, JUROR_NUMBER);
        payLoad.put("courtname", COURT_NAME);
        payLoad.put(SERVICES_START_DATE, VALUE_2);
        payLoad.put(SERVICES_START_TIME, "value3");
        payLoad.put(FIRST_NAME_VAL, VALUE_2);
        payLoad.put(LAST_NAME_VAL, VALUE_2);
        payLoad.put(EMAIL_ADDRESS, EMAIL);


        final LocalDateTime now = LocalDateTime.now();

        final NotifyTemplateMapping testNotifyTemplate = new NotifyTemplateMapping();
        testNotifyTemplate.setTemplateId(DEV_CONFIRM_JUROR_ENG_TEMPLATE_ID);

        final EmailNotification emailNotification = utilJurorCommsService.createEmailNotification(
            pool,
            JurorCommsNotifyTemplateType.LETTER_COMMS,
            testNotifyTemplate.getTemplateId(),
            payLoad
        );

        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(EMAIL);

        // send the email using the real Notify.gov
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendCommsEmail(emailNotification);
        assertThat(emailNotificationReceipt.getTemplateId())
            .as("Template ID is the one passed from the mock.")
            .isEqualTo(UUID.fromString(DEV_CONFIRM_JUROR_ENG_TEMPLATE_ID));
        assertThat(emailNotificationReceipt.getReference())
            .as("Juror number is the Notify reference")
            .isEqualTo(JUROR_NUMBER);
        assertThat(emailNotificationReceipt.getBody())
            .as("Body is not empty and contains payload information")
            .isNotEmpty()
            .contains(JUROR_NUMBER);
    }


    @Test(timeout = 9000L)
    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    public void sendCommsEmail_sendToCourtEmail_english() {
        // create the notification data class
        final String JUROR_CONFIRMATION = "SENT_TO_COURT";
        final String JUROR_NUMBER = "111222333";
        final String TITLE = "Mr";
        final String FIRST_NAME = "Harry";
        final String LAST_NAME = "Test";
        final String EMAIL = "confirmed.test@cgi.com";

        final Pool pool = Pool.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(TITLE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .email(EMAIL)
            .welsh(false)
            .build();


        Map<String, String> payLoad = new HashMap<>();
        payLoad.put(JUROR_NUM, JUROR_NUMBER);
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
        payLoad.put(EMAIL_ADDRESS, EMAIL);


        final LocalDateTime now = LocalDateTime.now();

        final NotifyTemplateMapping testNotifyTemplate = new NotifyTemplateMapping();
        testNotifyTemplate.setTemplateId(DEV_SENT_TO_COURT_EMAIL_ENG_TEMPLATE_ID);

        final EmailNotification emailNotification = utilJurorCommsService.createEmailNotification(
            pool,
            JurorCommsNotifyTemplateType.SENT_TO_COURT,
            testNotifyTemplate.getTemplateId(),
            payLoad
        );

        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(EMAIL);

        // send the email using the real Notify.gov
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendCommsEmail(emailNotification);
        assertThat(emailNotificationReceipt.getTemplateId())
            .as("Template ID is the one passed from the mock.")
            .isEqualTo(UUID.fromString(DEV_SENT_TO_COURT_EMAIL_ENG_TEMPLATE_ID));
        assertThat(emailNotificationReceipt.getReference())
            .as("Juror number is the Notify reference")
            .isEqualTo(JUROR_NUMBER);
        assertThat(emailNotificationReceipt.getBody())
            .as("Body is not empty and contains payload information")
            .isNotEmpty()
            .contains(JUROR_NUMBER);
    }


    @Test(timeout = 9000L)
    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    public void sendCommsSms_sendToCourtSms_english() {
        // create the notification data class
        final String JUROR_CONFIRMATION = "SENT_TO_COURT";
        final String JUROR_NUMBER = "111222333";
        final String TITLE = "Mr";
        final String FIRST_NAME = "Harry";
        final String LAST_NAME = "Test";
        final String EMAIL = "confirmed.test@cgi.com";
        final String PHONE_NUMBER = "44776-301-1119";

        final Pool pool = Pool.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(TITLE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .email(EMAIL)
            .welsh(false)
            .altPhoneNumber(PHONE_NUMBER)
            .build();


        Map<String, String> payLoad = new HashMap<>();
        payLoad.put(JUROR_NUM, JUROR_NUMBER);
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
        payLoad.put(EMAIL_ADDRESS, EMAIL);
        payLoad.put("phone number", PHONE_NUMBER);


        final LocalDateTime now = LocalDateTime.now();

        final NotifyTemplateMapping testNotifyTemplate = new NotifyTemplateMapping();
        testNotifyTemplate.setTemplateId(DEV_SENT_TO_COURT_SMS_ENG_TEMPLATE_ID);

        final SmsNotification smsNotification = utilJurorCommsService.createSmsNotification(
            pool,
            JurorCommsNotifyTemplateType.SENT_TO_COURT,
            testNotifyTemplate.getTemplateId(),
            payLoad
        );

        assertThat(smsNotification.getReceipientPhoneNumber()).as("Recipient phone number is correct")
            .isEqualTo(PHONE_NUMBER);

        // send the sms using the real Notify.gov
        final SmsNotificationReceipt smsNotificationReceipt = notifyAdapter.sendCommsSms(smsNotification);
        assertThat(smsNotificationReceipt.getTemplateId())
            .as("Template ID is the one passed from the mock.")
            .isEqualTo(UUID.fromString(DEV_SENT_TO_COURT_SMS_ENG_TEMPLATE_ID));
        assertThat(smsNotificationReceipt.getReference())
            .as("Juror number is the Notify reference")
            .isEqualTo(JUROR_NUMBER);
        assertThat(smsNotificationReceipt.getBody())
            .as("Body is not empty and contains payload information")
            .isNotEmpty()
            .contains(payLoad.get(COURT_PHONE));
    }
}