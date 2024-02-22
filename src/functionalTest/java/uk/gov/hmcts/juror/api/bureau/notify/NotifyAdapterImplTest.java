package uk.gov.hmcts.juror.api.bureau.notify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;
import uk.gov.hmcts.juror.api.bureau.domain.AppSettingRepository;
import uk.gov.hmcts.juror.api.bureau.service.AppSettingService;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotificationReceipt;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.service.JurorNotificationServiceImpl;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "notify.disabled=false")
class NotifyAdapterImplTest {
    private static final String DEV_FIRST_PERSON_TEMPLATE_ID = "ec33ab68-b917-4f25-918e-50d3291edef6"; // new
    // 1st_straight_through
    private static final String DEV_FIRST_PERSON_CY_TEMPLATE_ID = "aea4140b-2e2f-423b-8146-cd9615bfbc9e"; //
    // 1st_straight_through_wel
    private static final String DEV_THIRD_PARTY_TEMPLATE_ID = "1701b1b1-1b7f-4a7c-b320-41e731480d6f";  // new
    // 3rd_straight_through
    private static final String DEV_THIRD_PARTY_CY_TEMPLATE_ID = "591f6e20-bfb8-44d0-92ad-b6b8b8889f49"; //
    // 3rd_straight_through_wel

    /**
     * bean under test.
     */
    @Autowired
    private NotifyAdapter notifyAdapter;

    /**
     * mock provided to JurorNotificationServiceImpl constructor only.
     */
    @Mock
    private NotifyAdapter mockNotifyAdapter;

    @Mock
    private AppSettingService mockAppSettingService;

    /**
     * mock provided to JurorNotificationServiceImpl constructor only.
     */
    @Autowired
    private ResponseInspector responseInspector;

    /**
     * mock provided to JurorNotificationServiceImpl constructor only.
     */
    @Mock
    private AppSettingRepository appSettingRepository;

    /**
     * Used to access utility method.
     * {@link JurorNotificationServiceImpl#createEmailNotification(JurorResponse, NotifyTemplateType)} only!
     */
    private JurorNotificationServiceImpl utilService;

    @BeforeEach
    void setUp() throws Exception {
        utilService = new JurorNotificationServiceImpl(mockNotifyAdapter, responseInspector, appSettingRepository);
    }

    @AfterEach
    void tearDown() {
        verifyNoInteractions(mockNotifyAdapter);
    }

    @Test
    @Timeout(9)
    @Sql("/db/welsh_enabled.sql")
    @Sql("/db/notify_adapter_pool.sql")
    void sendEmailFirstPersonResponse() {
        // create the notification data class
        final String jurorNumber = "888222011";
        final String title = "Mr";
        final String firstName = "Testy";
        final String lastName = "McTest";
        final String email = "testy.mctest@cgi.com";
        final JurorResponse firstPersonResponse = JurorResponse.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .build();
        final AppSetting testTemplateSetting = new AppSetting();
        testTemplateSetting.setValue(DEV_FIRST_PERSON_TEMPLATE_ID);

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(testTemplateSetting));

        final EmailNotification emailNotification = utilService.createEmailNotification(
            firstPersonResponse,
            NotifyTemplateType.STRAIGHT_THROUGH
        );

        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(email);

        // send the email using the real Notify.gov
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendEmail(emailNotification);
        assertThat(emailNotificationReceipt.getTemplateId())
            .as("Template ID is the one passed from the mock.")
            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
        assertThat(emailNotificationReceipt.getReference())
            .as("Juror number is the Notify reference")
            .isEqualTo(jurorNumber);
        assertThat(emailNotificationReceipt.getBody())
            .as("Body is not empty and contains payload information")
            .isNotEmpty()
            .contains(jurorNumber);

        verify(appSettingRepository).findById(anyString());
    }

    @Test
    @Timeout(9)
    @Sql("/db/welsh_enabled.sql")
    @Sql("/db/notify_adapter_pool.sql")
    void sendEmailFirstPersonResponseWelshLanguage() {
        // create the notification data class
        final String jurorNumber = "888222000";
        final String title = "Mr";
        final String firstName = "Testy";
        final String lastName = "Jones";
        final String email = "testy.jones@cgi.com";
        final JurorResponse firstPersonResponse = JurorResponse.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .welsh(true)
            .build();

        final AppSetting testTemplateSetting = new AppSetting();
        testTemplateSetting.setValue(DEV_FIRST_PERSON_CY_TEMPLATE_ID);

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(testTemplateSetting));

        final EmailNotification emailNotification = utilService.createEmailNotification(
            firstPersonResponse,
            NotifyTemplateType.STRAIGHT_THROUGH
        );

        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(email);

        // send the email using the real Notify.gov
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendEmail(emailNotification);
        assertThat(emailNotificationReceipt.getTemplateId())
            .as("Template ID is the one passed from the mock.")
            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
        assertThat(emailNotificationReceipt.getReference())
            .as("Juror number is the Notify reference")
            .isEqualTo(jurorNumber);
        assertThat(emailNotificationReceipt.getBody())
            //.as("Body contains the temporary template body pending welsh translation")
            .as("Body is not empty and contains payload information")
            .isNotEmpty()
            //.contains("Awaiting translation")
            .contains(jurorNumber)
        ;

        verify(appSettingRepository).findById(anyString());

    }

    @Test
    @Timeout(9)
    @Sql("/db/welsh_enabled.sql")
    @Sql("/db/notify_adapter_pool.sql")
    void sendEmailThirdPartyResponseJurorDetail() {
        // create the notification data class
        final String jurorNumber = "888222011";
        final String title = "Mr";
        final String firstName = "Testy";
        final String lastName = "McTest";
        final String jurorEmail = "testy.mctest@cgi.com";
        final String tpFname = "Thirdy";
        final String tpLname = "McThird";
        final String tpEmail = "thirdy.mcthird@cgi.com";
        final JurorResponse thirdPartyJurorDetailsResponse = JurorResponse.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .jurorEmailDetails(Boolean.TRUE)
            .email(jurorEmail)
            .thirdPartyFName(tpFname)
            .thirdPartyLName(tpLname)
            .emailAddress(tpEmail)
            .build();
        final AppSetting testTemplateSetting = new AppSetting();
        testTemplateSetting.setValue(DEV_THIRD_PARTY_TEMPLATE_ID);

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(testTemplateSetting));

        final EmailNotification emailNotification =
            utilService.createEmailNotification(thirdPartyJurorDetailsResponse, NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(jurorEmail);

        // send the email
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendEmail(emailNotification);

        assertThat(emailNotificationReceipt.getTemplateId())
            .as("Template ID is the one passed from the mock.")
            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
        assertThat(emailNotificationReceipt.getReference())
            .as("Juror number is the Notify reference")
            .isEqualTo(jurorNumber);
        assertThat(emailNotificationReceipt.getBody())
            .as("Body is not empty and contains payload information")
            .isNotEmpty()
            .contains(jurorNumber);

        verify(appSettingRepository).findById(anyString());
    }

    @Test
    @Timeout(9)
    @Sql("/db/welsh_enabled.sql")
    @Sql("/db/notify_adapter_pool.sql")
    void sendEmailThirdPartyResponseJurorDetailWelshLanguage() {
        // create the notification data class
        final String jurorNumber = "888222000";
        final String title = "Mr";
        final String firstName = "Testy";
        final String lastName = "McTest";
        final String jurorEmail = "testy.mctest@cgi.com";
        final String tpFname = "Thirdy";
        final String tpLname = "McThird";
        final String tpEmail = "thirdy.mcthird@cgi.com";
        final JurorResponse thirdPartyJurorDetailsResponse = JurorResponse.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .jurorEmailDetails(Boolean.TRUE)
            .email(jurorEmail)
            .thirdPartyFName(tpFname)
            .thirdPartyLName(tpLname)
            .emailAddress(tpEmail)
            .welsh(true)
            .build();
        final AppSetting testTemplateSetting = new AppSetting();
        testTemplateSetting.setValue(DEV_THIRD_PARTY_CY_TEMPLATE_ID);

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(testTemplateSetting));

        final EmailNotification emailNotification =
            utilService.createEmailNotification(thirdPartyJurorDetailsResponse, NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(jurorEmail);

        // send the email
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendEmail(emailNotification);

        assertThat(emailNotificationReceipt.getTemplateId())
            .as("Template ID is the one passed from the mock.")
            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
        assertThat(emailNotificationReceipt.getReference())
            .as("Juror number is the Notify reference")
            .isEqualTo(jurorNumber);
        assertThat(emailNotificationReceipt.getBody())
            //.as("Body contains the temporary template body pending welsh translation")
            .as("Body is not empty and contains payload information")
            .isNotEmpty()
            //.contains("Awaiting translation")
            .contains(jurorNumber)
        ;

        verify(appSettingRepository).findById(anyString());

    }

    @Test
    @Timeout(9)
    @Sql("/db/welsh_enabled.sql")
    @Sql("/db/notify_adapter_pool.sql")
    void sendEmailThirdPartyResponseThirdPartyDetail() {
        // create the notification data class
        final String jurorNumber = "888222011";
        final String title = "Mr";
        final String firstName = "Testy";
        final String lastName = "McTest";
        final String jurorEmail = "testy.mctest@cgi.com";
        final String tpFname = "Thirdy";
        final String tpLname = "McThird";
        final String tpEmail = "thirdy.mcthird@cgi.com";

        final JurorResponse thirdPartyJurorDetailsResponse = JurorResponse.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .jurorEmailDetails(Boolean.FALSE)
            .email(jurorEmail)
            .thirdPartyFName(tpFname)
            .thirdPartyLName(tpLname)
            .emailAddress(tpEmail)
            .build();
        final AppSetting testTemplateSetting = new AppSetting();
        testTemplateSetting.setValue(DEV_THIRD_PARTY_TEMPLATE_ID);

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(testTemplateSetting));

        final EmailNotification emailNotification =
            utilService.createEmailNotification(thirdPartyJurorDetailsResponse, NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(tpEmail);

        // send the email
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendEmail(emailNotification);

        assertThat(emailNotificationReceipt.getTemplateId())
            .as("Template ID is the one passed from the mock.")
            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
        assertThat(emailNotificationReceipt.getReference())
            .as("Juror number is the Notify reference")
            .isEqualTo(jurorNumber);
        assertThat(emailNotificationReceipt.getBody())
            .as("Body is not empty and contains payload information")
            .isNotEmpty()
            .contains(jurorNumber);

        verify(appSettingRepository).findById(anyString());

    }
}
