package uk.gov.hmcts.juror.api.bureau.notify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsNotificationService;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsNotifyPayLoadService;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotificationReceipt;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "notify.disabled=false")
class NotifyAdapterImplTest extends ContainerTest {

    /**
     * bean under test.
     */
    @Autowired
    private JurorCommsNotificationService jurorCommsNotificationService;


    @Autowired
    private NotifyAdapter notifyAdapter;

    @Autowired
    private JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService;

    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @BeforeEach
    void setUp() throws Exception {

    }

    @AfterEach
    void tearDown() {

    }

//    @Test
//    @Timeout(9)
//    void sendEmailFirstPersonResponse() {
//        // create the notification data class
//        final String jurorNumber = "888222011";
//        final String title = "Mr";
//        final String firstName = "Testy";
//        final String lastName = "McTest";
//        final String email = "testy.mctest@cgi.com";
//        final DigitalResponse firstPersonResponse = DigitalResponse.builder()
//            .jurorNumber(jurorNumber)
//            .title(title)
//            .firstName(firstName)
//            .lastName(lastName)
//            .email(email)
//            .build();
//
//
//        final EmailNotification emailNotification = utilService.createEmailNotification(
//            firstPersonResponse,
//            NotifyTemplateType.STRAIGHT_THROUGH
//        );
//
//        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(email);
//
//        // send the email using the real Notify.gov
//        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendEmail(emailNotification);
//        assertThat(emailNotificationReceipt.getTemplateId())
//            .as("Template ID is the one passed from the mock.")
//            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
//        assertThat(emailNotificationReceipt.getReference())
//            .as("Juror number is the Notify reference")
//            .isEqualTo(jurorNumber);
//        assertThat(emailNotificationReceipt.getBody())
//            .as("Body is not empty and contains payload information")
//            .isNotEmpty()
//            .contains(jurorNumber);
//
//        verify(appSettingRepository).findById(anyString());
//    }
//
//    @Test
//    @Timeout(9)
//    void sendEmailFirstPersonResponseWelshLanguage() {
//        // create the notification data class
//        final String jurorNumber = "888222000";
//        final String title = "Mr";
//        final String firstName = "Testy";
//        final String lastName = "Jones";
//        final String email = "testy.jones@cgi.com";
//        final DigitalResponse firstPersonResponse = DigitalResponse.builder()
//            .jurorNumber(jurorNumber)
//            .title(title)
//            .firstName(firstName)
//            .lastName(lastName)
//            .email(email)
//            .welsh(true)
//            .build();
//
//        final AppSetting testTemplateSetting = new AppSetting();
//        testTemplateSetting.setValue(DEV_FIRST_PERSON_CY_TEMPLATE_ID);
//
//        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(testTemplateSetting));
//
//        final EmailNotification emailNotification = utilService.createEmailNotification(
//            firstPersonResponse,
//            NotifyTemplateType.STRAIGHT_THROUGH
//        );
//
//        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(email);
//
//        // send the email using the real Notify.gov
//        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendEmail(emailNotification);
//        assertThat(emailNotificationReceipt.getTemplateId())
//            .as("Template ID is the one passed from the mock.")
//            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
//        assertThat(emailNotificationReceipt.getReference())
//            .as("Juror number is the Notify reference")
//            .isEqualTo(jurorNumber);
//        assertThat(emailNotificationReceipt.getBody())
//            //.as("Body contains the temporary template body pending welsh translation")
//            .as("Body is not empty and contains payload information")
//            .isNotEmpty()
//            //.contains("Awaiting translation")
//            .contains(jurorNumber)
//        ;
//
//        verify(appSettingRepository).findById(anyString());
//
//    }

    @Test
    @Timeout(9)
    void sendEmailFirstPersonResponse() {
        // create the notification data class
        final String jurorNumber = "888222011";
        final String title = "Mr";
        final String firstName = "Testy";
        final String lastName = "McTest";
        final String jurorEmail = "testy.mctest@cgi.com";
        final String tpFname = "Thirdy";
        final String tpLname = "McThird";
        final String tpEmail = "thirdy.mcthird@cgi.com";
//
//        final JurorPool jurorDetails =
//            jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(jurorNumber, true,
//                                                                      SecurityUtil.BUREAU_OWNER);



        CourtLocation courtLocation = CourtLocation.builder()
            .locCode("448")
            .locCourtName("Birmingham Crown Court")
            .locationAddress("Birmingham Crown Court, 1 Corporation St, Birmingham B4 7DJ")
            .courtAttendTime(LocalTime.of(9, 0))
            .build();

        PoolRequest pool = PoolRequest.builder()
            .courtLocation(courtLocation)
            .poolNumber("123456789")
            .returnDate(LocalDate.now().plusDays(28))
            .attendTime(LocalDateTime.of(2023, 10, 1, 9, 0))
            .build();

        Juror juror = Juror.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .email(jurorEmail)
            .welsh(false)
            .build();

        JurorPool jurorDetails = JurorPool.builder()
            .juror(juror)
            .pool(pool)
            .nextDate(LocalDate.now())
            .build();


        // corresponds to 1ST_COMMS_ENG in notify_template_mapping table
        String templateId = "8eeb7fb5-5e02-43a7-9557-ed63b08845de";
        String detailRec = "    McTest     Testy       YYY   " + jurorNumber + "XX     ";

        Map<String, String> payLoad = jurorCommsNotifyPayLoadService.generatePayLoadData(templateId, detailRec, jurorDetails);

        final EmailNotification emailNotification = jurorCommsNotificationService
            .createEmailNotification(jurorDetails, JurorCommsNotifyTemplateType.COMMS, templateId, payLoad);

        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(jurorEmail);
        // send the email
        final EmailNotificationReceipt emailNotificationReceipt =  notifyAdapter.sendCommsEmail(emailNotification);


//        assertThat(emailNotificationReceipt.getTemplateId())
//            .as("Template ID is the one passed from the mock.")
//            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
        assertThat(emailNotificationReceipt.getReference())
            .as("Juror number is the Notify reference")
            .isEqualTo(jurorNumber);
        assertThat(emailNotificationReceipt.getBody())
            .as("Body is not empty and contains payload information")
            .isNotEmpty()
            .contains(jurorNumber);

    }
//
//    @Test
//    @Timeout(9)
//    void sendEmailThirdPartyResponseJurorDetailWelshLanguage() {
//        // create the notification data class
//        final String jurorNumber = "888222000";
//        final String title = "Mr";
//        final String firstName = "Testy";
//        final String lastName = "McTest";
//        final String jurorEmail = "testy.mctest@cgi.com";
//        final String tpFname = "Thirdy";
//        final String tpLname = "McThird";
//        final String tpEmail = "thirdy.mcthird@cgi.com";
//        final DigitalResponse thirdPartyJurorDetailsResponse = DigitalResponse.builder()
//            .jurorNumber(jurorNumber)
//            .title(title)
//            .firstName(firstName)
//            .lastName(lastName)
//            .jurorEmailDetails(Boolean.TRUE)
//            .email(jurorEmail)
//            .thirdPartyFName(tpFname)
//            .thirdPartyLName(tpLname)
//            .emailAddress(tpEmail)
//            .welsh(true)
//            .build();
//        final AppSetting testTemplateSetting = new AppSetting();
//        testTemplateSetting.setValue(DEV_THIRD_PARTY_CY_TEMPLATE_ID);
//
//        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(testTemplateSetting));
//
//        final EmailNotification emailNotification =
//            utilService.createEmailNotification(thirdPartyJurorDetailsResponse, NotifyTemplateType.STRAIGHT_THROUGH);
//        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(jurorEmail);
//
//        // send the email
//        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendEmail(emailNotification);
//
//        assertThat(emailNotificationReceipt.getTemplateId())
//            .as("Template ID is the one passed from the mock.")
//            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
//        assertThat(emailNotificationReceipt.getReference())
//            .as("Juror number is the Notify reference")
//            .isEqualTo(jurorNumber);
//        assertThat(emailNotificationReceipt.getBody())
//            //.as("Body contains the temporary template body pending welsh translation")
//            .as("Body is not empty and contains payload information")
//            .isNotEmpty()
//            //.contains("Awaiting translation")
//            .contains(jurorNumber)
//        ;
//
//        verify(appSettingRepository).findById(anyString());
//
//    }
//
//    @Test
//    @Timeout(9)
//    void sendEmailThirdPartyResponseThirdPartyDetail() {
//        // create the notification data class
//        final String jurorNumber = "888222011";
//        final String title = "Mr";
//        final String firstName = "Testy";
//        final String lastName = "McTest";
//        final String jurorEmail = "testy.mctest@cgi.com";
//        final String tpFname = "Thirdy";
//        final String tpLname = "McThird";
//        final String tpEmail = "thirdy.mcthird@cgi.com";
//
//        final DigitalResponse thirdPartyJurorDetailsResponse = DigitalResponse.builder()
//            .jurorNumber(jurorNumber)
//            .title(title)
//            .firstName(firstName)
//            .lastName(lastName)
//            .jurorEmailDetails(Boolean.FALSE)
//            .email(jurorEmail)
//            .thirdPartyFName(tpFname)
//            .thirdPartyLName(tpLname)
//            .emailAddress(tpEmail)
//            .build();
//        final AppSetting testTemplateSetting = new AppSetting();
//        testTemplateSetting.setValue(DEV_THIRD_PARTY_TEMPLATE_ID);
//
//        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(testTemplateSetting));
//
//        final EmailNotification emailNotification =
//            utilService.createEmailNotification(thirdPartyJurorDetailsResponse, NotifyTemplateType.STRAIGHT_THROUGH);
//        assertThat(emailNotification.getRecipientEmail()).as("Recipient email is correct").isEqualTo(tpEmail);
//
//        // send the email
//        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapter.sendEmail(emailNotification);
//
//        assertThat(emailNotificationReceipt.getTemplateId())
//            .as("Template ID is the one passed from the mock.")
//            .isEqualTo(UUID.fromString(testTemplateSetting.getValue()));
//        assertThat(emailNotificationReceipt.getReference())
//            .as("Juror number is the Notify reference")
//            .isEqualTo(jurorNumber);
//        assertThat(emailNotificationReceipt.getBody())
//            .as("Body is not empty and contains payload information")
//            .isNotEmpty()
//            .contains(jurorNumber);
//
//        verify(appSettingRepository).findById(anyString());
//
//    }
}
