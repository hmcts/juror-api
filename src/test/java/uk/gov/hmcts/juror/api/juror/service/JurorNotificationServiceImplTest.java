package uk.gov.hmcts.juror.api.juror.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.AppSetting;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.AppSettingRepository;
import uk.gov.hmcts.juror.api.validation.ResponseInspectorImpl;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorNotificationServiceImplTest {
    private static final String JUROR_NUMBER = "111222333";
    private static final String JUROR_TITLE = "JT";
    private static final String JUROR_FIRST_NAME = "JFNAME";
    private static final String JUROR_LAST_NAME = "JLNAME";
    private static final String THIRD_PARTY_FIRST_NAME = "TPFNAME";
    private static final String THIRD_PARTY_LAST_NAME = "TPLNAME";
    private static final String JUROR_EMAIL = "firstperson@email.com";
    private static final Boolean USE_JUROR_EMAIL = Boolean.TRUE;
    private static final Boolean USE_THIRDPARTY_EMAIL = Boolean.FALSE;
    private static final String TP_EMAIL = "thirdparty@thirdparty.com";

    private DigitalResponse firstPerson;
    private DigitalResponse thirdPartyJurorEmail;
    private DigitalResponse thirdPartyOwnEmail;
    private AppSetting firstPersonDevTemplateSetting;
    private AppSetting thirdPartyDevTemplateSetting;
    private final UUID firstPersonTemplateUuid = UUID.randomUUID();
    private final UUID thirdPartyTemplateUuid = UUID.randomUUID();

    @Mock
    private NotifyAdapter mockNotifyAdapter;

    @Mock
    private ResponseInspectorImpl responseInspector;

    @Mock
    private AppSettingRepository appSettingRepository;

    @InjectMocks
    private JurorNotificationServiceImpl service;

    @Before
    public void setUp() throws Exception {
        firstPerson = new DigitalResponse();
        firstPerson.setJurorNumber(JUROR_NUMBER);
        firstPerson.setTitle(JUROR_TITLE);
        firstPerson.setFirstName(JUROR_FIRST_NAME);
        firstPerson.setLastName(JUROR_LAST_NAME);
        firstPerson.setEmail(JUROR_EMAIL);


        thirdPartyJurorEmail = new DigitalResponse();
        thirdPartyJurorEmail.setJurorNumber(JUROR_NUMBER);
        thirdPartyJurorEmail.setTitle(JUROR_TITLE);
        thirdPartyJurorEmail.setFirstName(JUROR_FIRST_NAME);
        thirdPartyJurorEmail.setLastName(JUROR_LAST_NAME);
        thirdPartyJurorEmail.setThirdPartyFName(THIRD_PARTY_FIRST_NAME);
        thirdPartyJurorEmail.setThirdPartyLName(THIRD_PARTY_LAST_NAME);
        thirdPartyJurorEmail.setEmail(JUROR_EMAIL);

        //third party details
        thirdPartyJurorEmail.setJurorEmailDetails(USE_JUROR_EMAIL);
        thirdPartyJurorEmail.setEmailAddress(TP_EMAIL);


        thirdPartyOwnEmail = new DigitalResponse();
        thirdPartyOwnEmail.setJurorNumber(JUROR_NUMBER);
        thirdPartyOwnEmail.setTitle(JUROR_TITLE);
        thirdPartyOwnEmail.setFirstName(JUROR_FIRST_NAME);
        thirdPartyOwnEmail.setLastName(JUROR_LAST_NAME);
        thirdPartyOwnEmail.setThirdPartyFName(THIRD_PARTY_FIRST_NAME);
        thirdPartyOwnEmail.setThirdPartyLName(THIRD_PARTY_LAST_NAME);
        thirdPartyOwnEmail.setEmail(JUROR_EMAIL);

        //third party details
        thirdPartyOwnEmail.setJurorEmailDetails(USE_THIRDPARTY_EMAIL);
        thirdPartyOwnEmail.setEmailAddress(TP_EMAIL);


        firstPersonDevTemplateSetting = AppSetting.builder().value(firstPersonTemplateUuid.toString()).build();
        thirdPartyDevTemplateSetting = AppSetting.builder().value(thirdPartyTemplateUuid.toString()).build();

    }

    @Test
    public void sendResponseReceipt_firstPerson() {

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(firstPersonDevTemplateSetting));
        service.sendResponseReceipt(firstPerson, NotifyTemplateType.STRAIGHT_THROUGH);
        verify(mockNotifyAdapter).sendEmail(any());
        verify(responseInspector).hasAdjustments(firstPerson);
        verify(responseInspector, atLeastOnce()).isThirdPartyResponse(firstPerson);
        verify(responseInspector).activeContactEmail(firstPerson);
    }

    @Test
    public void sendResponseReceipt_thirdParty_jurorEmail() {

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(thirdPartyDevTemplateSetting));
        service.sendResponseReceipt(thirdPartyJurorEmail, NotifyTemplateType.STRAIGHT_THROUGH);
        verify(mockNotifyAdapter).sendEmail(any());
        verify(responseInspector).hasAdjustments(thirdPartyJurorEmail);
        verify(responseInspector, atLeastOnce()).isThirdPartyResponse(thirdPartyJurorEmail);
        verify(responseInspector).activeContactEmail(thirdPartyJurorEmail);
    }

    @Test
    public void sendResponseReceipt_thirdParty_ownEmail() {
        //given(appSettingRepository.findOne(any(Predicate.class))).willReturn(thirdPartyDevTemplateSetting);
        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(thirdPartyDevTemplateSetting));
        service.sendResponseReceipt(thirdPartyOwnEmail, NotifyTemplateType.STRAIGHT_THROUGH);
        verify(mockNotifyAdapter).sendEmail(any());
        verify(responseInspector).hasAdjustments(thirdPartyOwnEmail);
        verify(responseInspector, atLeastOnce()).isThirdPartyResponse(thirdPartyOwnEmail);
        verify(responseInspector).activeContactEmail(thirdPartyOwnEmail);
    }

    @Test
    public void createEmailNotification() {

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(firstPersonDevTemplateSetting));
        given(responseInspector.activeContactEmail(any(DigitalResponse.class))).willCallRealMethod();

        final EmailNotification notification = service.createEmailNotification(firstPerson,
            NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(notification.getTemplateId())
            .describedAs("Template ID is correct for response type")
            .isEqualTo(firstPersonDevTemplateSetting.getValue());
        assertThat(notification.getRecipientEmail())
            .describedAs("Correct recipient email address")
            .isEqualTo(JUROR_EMAIL);
        assertThat(notification.getReferenceNumber())
            .describedAs("Reference number equals supplied juror number")
            .isEqualTo(JUROR_NUMBER);
        assertThat(notification.getPayload())
            .describedAs("Payload is populated")
            .hasSize(1)
            .containsEntry("jurorNumber", JUROR_NUMBER)
        ;
        verifyNoInteractions(mockNotifyAdapter);
    }

    @Test
    public void thirdParty_jurorEmailMessage() {

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(thirdPartyDevTemplateSetting));
        given(responseInspector.activeContactEmail(any(DigitalResponse.class))).willCallRealMethod();

        final EmailNotification notification = service.createEmailNotification(thirdPartyJurorEmail,
            NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(notification.getTemplateId())
            .describedAs("Template ID is correct for response type")
            .isEqualTo(thirdPartyDevTemplateSetting.getValue());
        assertThat(notification.getRecipientEmail())
            .describedAs("Correct recipient email address")
            .isEqualTo(JUROR_EMAIL);
        assertThat(notification.getReferenceNumber())
            .describedAs("Reference number equals supplied juror number")
            .isEqualTo(JUROR_NUMBER);
        assertThat(notification.getPayload())
            .describedAs("Payload is populated")
            .hasSize(1)
            .containsEntry("jurorNumber", JUROR_NUMBER)
        ;
        verifyNoInteractions(mockNotifyAdapter);
    }

    @Test
    public void thirdParty_thirdPartyEmailMessage() {

        given(appSettingRepository.findById(anyString())).willReturn(Optional.of(thirdPartyDevTemplateSetting));
        given(responseInspector.activeContactEmail(any(DigitalResponse.class))).willCallRealMethod();
        given(responseInspector.isThirdPartyResponse(any(DigitalResponse.class))).willCallRealMethod();

        final EmailNotification notification = service.createEmailNotification(thirdPartyOwnEmail,
            NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(notification.getTemplateId())
            .describedAs("Template ID is correct for response type")
            .isEqualTo(thirdPartyDevTemplateSetting.getValue());
        assertThat(notification.getRecipientEmail())
            .describedAs("Correct recipient email address")
            .isEqualTo(TP_EMAIL);
        assertThat(notification.getReferenceNumber())
            .describedAs("Reference number equals supplied juror number")
            .isEqualTo(JUROR_NUMBER);
        assertThat(notification.getPayload())
            .describedAs("Payload is populated")
            .hasSize(1)
            .containsEntry("jurorNumber", JUROR_NUMBER)
        ;
        verifyNoInteractions(mockNotifyAdapter);
    }
}
