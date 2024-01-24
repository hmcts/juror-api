package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetail;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateMapping;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateMappingRepository;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotification;
import uk.gov.hmcts.juror.api.validation.ResponseInspectorImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorCommsNotificationServiceImplTest {
    private static final String JUROR_NUMBER = "123456789";
    private static final String JUROR_TITLE = "JT";
    private static final String JUROR_FIRST_NAME = "JFNAME";
    private static final String JUROR_LAST_NAME = "JLNAME";
    private static final String JUROR_EMAIL = "jurorperson@email.com";
    private static final String JUROR_PHONENO = "07818123123";

    private static final String LOC_CODE_WESLH = "457";
    private static final String LOC_COURT_NAME_WESLH = "ABERTAWE";
    private static final String LOC_ADDRESS1_WESLH = "Y LLYSOEDD BARN";

    private static final String LOC_CODE_ENGLISH = "448";
    private static final String LOC_COURT_NAME_ENGLISH = "PRESTON";
    private static final String LOC_ADDRESS1_ENGLISH = "THE LAW COURTS";

    private JurorResponse juror;
    private Pool pool;
    private BureauJurorDetail bureauJurorDetail;
    private NotifyTemplateMapping notifyCommsTemplateMapping;
    private final UUID NOTIFY_TEMPLATE_ID = UUID.randomUUID();
    Map<String, String> payLoad;
    private WelshCourtLocation welshCourt;
    private CourtLocation court;

    @Mock
    private NotifyAdapter mockNotifyAdapter;

    @Mock
    private ResponseInspectorImpl responseInspector;

    @Mock
    private NotifyTemplateMappingRepository notifyTemplateMappingRepository;

    @Mock
    private JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService;

    @InjectMocks
    private JurorCommsNotificationServiceImpl service;

    @Before
    public void setUp() throws Exception {
        juror = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(JUROR_TITLE)
            .firstName(JUROR_FIRST_NAME)
            .lastName(JUROR_LAST_NAME)
            .email(JUROR_EMAIL)
            .build();

        welshCourt = new WelshCourtLocation();
        welshCourt.setLocCode(LOC_CODE_WESLH);
        welshCourt.setLocCourtName(LOC_COURT_NAME_WESLH);
        welshCourt.setAddress1(LOC_ADDRESS1_WESLH);

        court = new CourtLocation();
        court.setLocCode(LOC_CODE_ENGLISH);
        court.setLocCourtName(LOC_COURT_NAME_ENGLISH);
        court.setAddress1(LOC_ADDRESS1_ENGLISH);

        pool = Pool.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(JUROR_TITLE)
            .firstName(JUROR_FIRST_NAME)
            .lastName(JUROR_LAST_NAME)
            .email(JUROR_EMAIL)
            .welsh(false)
            .court(court)
            .notifications(0)
            .build();

        notifyCommsTemplateMapping = NotifyTemplateMapping.builder().templateId(NOTIFY_TEMPLATE_ID.toString()).build();

        LocalDateTime now = LocalDateTime.now();

        payLoad = new HashMap<String, String>() {{
            put("juror number", JUROR_NUMBER);
            put("COURT", "PRESTON");
            put("SERVICESTARTDATE", "value2");
            put("FIRSTNAME", "value2");
            put("LASTNAME", "value2");
            put("email address", JUROR_EMAIL);
            put("phone number", JUROR_PHONENO);
        }};
    }

    @Test
    public void sendJurorCommsEmail_confirmation_english() {
        String detail_rec = "    Farah     Lee       YYY   " + JUROR_NUMBER + "XX     ";
        given(jurorCommsNotifyPayLoadService.generatePayLoadData(anyString(), anyString(), any(Pool.class))).willReturn(
            payLoad);
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.LETTER_COMMS, NOTIFY_TEMPLATE_ID.toString(),
            detail_rec, false);
        verify(mockNotifyAdapter).sendCommsEmail(any());
    }

    @Test
    public void sendJurorCommsEmail_comms_english() {
        given(notifyTemplateMappingRepository.findByTemplateName(anyString())).willReturn(notifyCommsTemplateMapping);
        given(jurorCommsNotifyPayLoadService.generatePayLoadData(anyString(), any(Pool.class))).willReturn(payLoad);
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.COMMS, null, null, false);
        verify(mockNotifyAdapter).sendCommsEmail(any());
    }

    @Test
    public void sendJurorCommsEmail_sendToCourt_welsh() {
        given(notifyTemplateMappingRepository.findByTemplateName(anyString())).willReturn(notifyCommsTemplateMapping);
        given(jurorCommsNotifyPayLoadService.isWelshCourtAndComms(anyBoolean(),
            any(WelshCourtLocation.class))).willReturn(Boolean.TRUE);
        given(jurorCommsNotifyPayLoadService.getWelshCourtLocation(anyString())).willReturn(welshCourt);

        given(jurorCommsNotifyPayLoadService.generatePayLoadData(anyString(), any(Pool.class))).willReturn(payLoad);
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.SENT_TO_COURT, null, null, false);
        verify(mockNotifyAdapter).sendCommsEmail(any());
    }

    @Test
    public void sendJurorCommsEmail_superUrgent_sendToCourt_welsh() {
        given(notifyTemplateMappingRepository.findByTemplateName(anyString())).willReturn(notifyCommsTemplateMapping);
        given(jurorCommsNotifyPayLoadService.isWelshCourtAndComms(anyBoolean(),
            any(WelshCourtLocation.class))).willReturn(Boolean.TRUE);
        given(jurorCommsNotifyPayLoadService.getWelshCourtLocation(anyString())).willReturn(welshCourt);

        given(jurorCommsNotifyPayLoadService.generatePayLoadData(anyString(), any(Pool.class))).willReturn(payLoad);
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.SU_SENT_TO_COURT, null, null, false);
        verify(mockNotifyAdapter).sendCommsEmail(any());
    }

    @Test
    public void sendJurorCommsSms_sendToCourt_welsh() {
        given(notifyTemplateMappingRepository.findByTemplateName(anyString())).willReturn(notifyCommsTemplateMapping);
        given(jurorCommsNotifyPayLoadService.isWelshCourtAndComms(anyBoolean(),
            any(WelshCourtLocation.class))).willReturn(Boolean.TRUE);
        given(jurorCommsNotifyPayLoadService.getWelshCourtLocation(anyString())).willReturn(welshCourt);

        given(jurorCommsNotifyPayLoadService.generatePayLoadData(anyString(), any(Pool.class))).willReturn(payLoad);
        service.sendJurorCommsSms(pool, JurorCommsNotifyTemplateType.SENT_TO_COURT, null, null, true);
        verify(mockNotifyAdapter).sendCommsSms(any());
    }

    @Test(expected = IllegalStateException.class)
    public void sendJurorCommsEmail_letter_comms_no_template() throws Exception {
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.LETTER_COMMS, null, null, false);
    }

    @Test(expected = IllegalStateException.class)
    public void sendJurorCommsEmail_letter_comms_no_detail_rec_data() throws Exception {
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.LETTER_COMMS, NOTIFY_TEMPLATE_ID.toString(), null,
            false);
    }

    @Test
    public void createEmailNotification() {
        final EmailNotification notification = service.createEmailNotification(pool,
            JurorCommsNotifyTemplateType.LETTER_COMMS,
            notifyCommsTemplateMapping.getTemplateId(), payLoad);
        assertThat(notification.getTemplateId())
            .describedAs("Template ID is correct")
            .isEqualTo(notifyCommsTemplateMapping.getTemplateId());
        assertThat(notification.getRecipientEmail())
            .describedAs("Correct recipient email address")
            .isEqualTo(JUROR_EMAIL);
        assertThat(notification.getReferenceNumber())
            .describedAs("Reference number is the supplied juror number")
            .isEqualTo(JUROR_NUMBER);
        assertThat(notification.getPayload())
            .describedAs("Payload has values")
            .hasSize(7)
            .containsEntry("juror number", JUROR_NUMBER)
            .containsEntry("COURT", "PRESTON")
        ;
        verifyNoInteractions(mockNotifyAdapter);
    }

    @Test
    public void createSmsNotification() {
        final SmsNotification notification = service.createSmsNotification(pool,
            JurorCommsNotifyTemplateType.LETTER_COMMS,
            notifyCommsTemplateMapping.getTemplateId(), payLoad);
        assertThat(notification.getTemplateId())
            .describedAs("Template ID is correct")
            .isEqualTo(notifyCommsTemplateMapping.getTemplateId());
        assertThat(notification.getReceipientPhoneNumber())
            .describedAs("Correct recipient phone number")
            .isEqualTo(JUROR_PHONENO);
        assertThat(notification.getReferenceNumber())
            .describedAs("Reference number is the supplied juror number")
            .isEqualTo(JUROR_NUMBER);
        assertThat(notification.getPayload())
            .describedAs("Payload has values")
            .hasSize(7)
            .containsEntry("juror number", JUROR_NUMBER)
            .containsEntry("COURT", "PRESTON")
        ;
        verifyNoInteractions(mockNotifyAdapter);
    }
}