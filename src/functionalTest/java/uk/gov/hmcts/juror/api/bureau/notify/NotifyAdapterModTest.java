package uk.gov.hmcts.juror.api.bureau.notify;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "notify.disabled=false")
class NotifyAdapterModTest extends ContainerTest {

    public static final String RECIPIENT_EMAIL_IS_CORRECT = "Recipient email is correct";
    public static final String TEMPLATE_ID_IS_THE_ONE_PASSED_FROM_THE_MOCK =
        "Template ID is the one passed from the mock.";
    public static final String JUROR_NUMBER_IS_THE_NOTIFY_REFERENCE =
        "Juror number is the Notify reference";
    public static final String BODY_IS_NOT_EMPTY_AND_CONTAINS_PAYLOAD_INFORMATION =
        "Body is not empty and contains payload information";

    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final NotifyAdapter notifyAdapterMod;
    private final JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService;

    @Test
    @Timeout(9)
    void sendEmail4WeekReminderEng() {
        // create the notification data class
        final String jurorNumber = "888222011";
        final String title = "Mr";
        final String firstName = "Testy";
        final String lastName = "McTest";
        final String jurorEmail = "testy.mctest@cgi.com";

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
        final String templateId = "8eeb7fb5-5e02-43a7-9557-ed63b08845de";
        final String detailRec = "    McTest     Testy       YYY   " + jurorNumber + "XX     ";

        Map<String, String> payLoad = jurorCommsNotifyPayLoadService.generatePayLoadData(templateId, detailRec, jurorDetails);

        final EmailNotification emailNotification = jurorCommsNotificationService
            .createEmailNotification(jurorDetails, JurorCommsNotifyTemplateType.COMMS, templateId, payLoad);

        assertThat(emailNotification.getRecipientEmail()).as(RECIPIENT_EMAIL_IS_CORRECT).isEqualTo(jurorEmail);

        // send the email
        final EmailNotificationReceipt emailNotificationReceipt = notifyAdapterMod.sendCommsEmail(emailNotification);

        validateEmailReceipt(emailNotificationReceipt, templateId, jurorNumber);

    }


    @Test
    @Timeout(9)
    void sendEmail4WeekReminderWelsh() {
        // create the notification data class
        final String jurorNumber = "888222010";
        final String title = "Mr";
        final String firstName = "Testy";
        final String lastName = "McTest";
        final String jurorEmail = "testy.mctest@cgi.com";

        CourtLocation courtLocation = CourtLocation.builder()
            .locCode("755")
            .locCourtName("THE CROWN COURT AT CAERNARFON")
            .locationAddress("Caernarfon Crown Court,Criminal Justice Centre,Llanberis Rd,Caernarfon LL55 2DF")
            .courtAttendTime(LocalTime.of(9, 0))
            .build();

        PoolRequest pool = PoolRequest.builder()
            .courtLocation(courtLocation)
            .poolNumber("123456789")
            .returnDate(LocalDate.now().plusDays(28))
            .attendTime(LocalDateTime.of(2025, 10, 1, 9, 0))
            .build();

        Juror juror = Juror.builder()
            .jurorNumber(jurorNumber)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .email(jurorEmail)
            .welsh(true)
            .build();

        JurorPool jurorDetails = JurorPool.builder()
            .juror(juror)
            .pool(pool)
            .nextDate(LocalDate.now())
            .build();

        // corresponds to 1ST_COMMS_CY in notify_template_mapping table
        final String templateId = "2f052d6f-011d-4d5b-bc41-75358b388936";
        final String detailRec = "    McTest     Testy       YYY   " + jurorNumber + "XX     ";

        Map<String, String> payLoad = jurorCommsNotifyPayLoadService
            .generatePayLoadData(templateId, detailRec, jurorDetails);

        final EmailNotification emailNotification = jurorCommsNotificationService
            .createEmailNotification(jurorDetails, JurorCommsNotifyTemplateType.COMMS, templateId, payLoad);

        assertThat(emailNotification.getRecipientEmail()).as(RECIPIENT_EMAIL_IS_CORRECT).isEqualTo(jurorEmail);

        // send the email
        final EmailNotificationReceipt emailNotificationReceipt =  notifyAdapterMod.sendCommsEmail(emailNotification);

        validateEmailReceipt(emailNotificationReceipt, templateId, jurorNumber);

    }

    private void validateEmailReceipt(EmailNotificationReceipt emailNotificationReceipt, String templateId,
                                      String jurorNumber) {
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


}
