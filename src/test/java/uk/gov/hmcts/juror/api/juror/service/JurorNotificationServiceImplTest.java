package uk.gov.hmcts.juror.api.juror.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;
import uk.gov.hmcts.juror.api.bureau.domain.AppSettingRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
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

    private JurorResponse firstPerson;
    private JurorResponse thirdPartyJurorEmail;
    private JurorResponse thirdPartyOwnEmail;
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
        firstPerson = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(JUROR_TITLE)
            .firstName(JUROR_FIRST_NAME)
            .lastName(JUROR_LAST_NAME)
            .email(JUROR_EMAIL)
            .build();

        thirdPartyJurorEmail = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(JUROR_TITLE)
            .firstName(JUROR_FIRST_NAME)
            .lastName(JUROR_LAST_NAME)
            .thirdPartyFName(THIRD_PARTY_FIRST_NAME)
            .thirdPartyLName(THIRD_PARTY_LAST_NAME)
            .email(JUROR_EMAIL)
            //third party details
            .jurorEmailDetails(USE_JUROR_EMAIL)
            .emailAddress(TP_EMAIL)
            .build();
        thirdPartyOwnEmail = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(JUROR_TITLE)
            .firstName(JUROR_FIRST_NAME)
            .lastName(JUROR_LAST_NAME)
            .thirdPartyFName(THIRD_PARTY_FIRST_NAME)
            .thirdPartyLName(THIRD_PARTY_LAST_NAME)
            .email(JUROR_EMAIL)
            //third party details
            .jurorEmailDetails(USE_THIRDPARTY_EMAIL)
            .emailAddress(TP_EMAIL)//juror email
            .build();

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
        given(responseInspector.activeContactEmail(any(JurorResponse.class))).willCallRealMethod();

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
        given(responseInspector.activeContactEmail(any(JurorResponse.class))).willCallRealMethod();

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
        given(responseInspector.activeContactEmail(any(JurorResponse.class))).willCallRealMethod();
        given(responseInspector.isThirdPartyResponse(any(JurorResponse.class))).willCallRealMethod();

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