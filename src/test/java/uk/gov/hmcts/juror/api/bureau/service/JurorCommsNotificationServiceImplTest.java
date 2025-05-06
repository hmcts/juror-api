package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyAdapter;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotification;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateMappingMod;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.NotifyTemplateMappingRepositoryMod;
import uk.gov.hmcts.juror.api.validation.ResponseInspectorImpl;

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

    private DigitalResponse juror;
    private Juror jurorSet;

    private PoolRequest poolRequest;
    private JurorPool pool;
    private ModJurorDetail bureauJurorDetail;
    private NotifyTemplateMappingMod notifyCommsTemplateMapping;
    private final UUID notifyTemplateId = UUID.randomUUID();
    Map<String, String> payLoad;
    private WelshCourtLocation welshCourt;
    private CourtLocation court;

    @Mock
    private NotifyAdapter mockNotifyAdapter;

    @Mock
    private ResponseInspectorImpl responseInspector;

    @Mock
    private NotifyTemplateMappingRepositoryMod notifyTemplateMappingRepository;

    @Mock
    private JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService;

    @InjectMocks
    private JurorCommsNotificationServiceImpl service;

    @Before
    public void setUp() throws Exception {
        juror = new DigitalResponse();
        juror.setJurorNumber(JUROR_NUMBER);
        juror.setTitle(JUROR_TITLE);
        juror.setFirstName(JUROR_FIRST_NAME);
        juror.setLastName(JUROR_LAST_NAME);
        juror.setEmail(JUROR_EMAIL);



        welshCourt = new WelshCourtLocation();
        welshCourt.setLocCode(LOC_CODE_WESLH);
        welshCourt.setLocCourtName(LOC_COURT_NAME_WESLH);
        welshCourt.setAddress1(LOC_ADDRESS1_WESLH);

        court = new CourtLocation();
        court.setLocCode(LOC_CODE_ENGLISH);
        court.setLocCourtName(LOC_COURT_NAME_ENGLISH);
        court.setAddress1(LOC_ADDRESS1_ENGLISH);


        poolRequest = new PoolRequest();
        jurorSet = new Juror();
        pool = new JurorPool();
        pool.setJuror(jurorSet);
        pool.setPool(poolRequest);
        jurorSet = pool.getJuror();

        jurorSet.setJurorNumber(JUROR_NUMBER);
        jurorSet.setTitle(JUROR_TITLE);
        jurorSet.setFirstName(JUROR_FIRST_NAME);
        jurorSet.setLastName(JUROR_LAST_NAME);
        jurorSet.setEmail(JUROR_EMAIL);
        jurorSet.setWelsh(false);
        poolRequest.setCourtLocation(court);
        jurorSet.setNotifications(0);



        notifyCommsTemplateMapping = NotifyTemplateMappingMod.builder().templateId(notifyTemplateId.toString()).build();

        payLoad = new HashMap<>() {
            {
                put("juror number", JUROR_NUMBER);
                put("COURT", "PRESTON");
                put("SERVICESTARTDATE", "value2");
                put("FIRSTNAME", "value2");
                put("LASTNAME", "value2");
                put("email address", JUROR_EMAIL);
                put("phone number", JUROR_PHONENO);
            }
        };
    }

    @Test
    public void sendJurorCommsEmail_confirmation_english() {
        String detailRec = "    Farah     Lee       YYY   " + JUROR_NUMBER + "XX     ";
        given(jurorCommsNotifyPayLoadService.generatePayLoadData(anyString(), anyString(), any(JurorPool.class)))
            .willReturn(payLoad);
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.LETTER_COMMS, notifyTemplateId.toString(),
            detailRec, false);
        verify(mockNotifyAdapter).sendCommsEmail(any());
    }

    @Test
    public void sendJurorCommsEmail_comms_english() {
        given(notifyTemplateMappingRepository.findByTemplateName(anyString())).willReturn(notifyCommsTemplateMapping);
        given(jurorCommsNotifyPayLoadService.generatePayLoadData(anyString(), any(JurorPool.class)))
            .willReturn(payLoad);
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.COMMS, null, null, false);
        verify(mockNotifyAdapter).sendCommsEmail(any());
    }

    @Test
    public void sendJurorCommsEmail_sendToCourt_welsh() {
        given(notifyTemplateMappingRepository.findByTemplateName(anyString())).willReturn(notifyCommsTemplateMapping);
        given(jurorCommsNotifyPayLoadService.isWelshCourtAndComms(anyBoolean(),
            any(WelshCourtLocation.class))).willReturn(Boolean.TRUE);
        given(jurorCommsNotifyPayLoadService.getWelshCourtLocation(anyString())).willReturn(welshCourt);

        given(jurorCommsNotifyPayLoadService.generatePayLoadData(anyString(), any(JurorPool.class)))
            .willReturn(payLoad);
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.SENT_TO_COURT, null, null, false);
        verify(mockNotifyAdapter).sendCommsEmail(any());
    }



    @Test
    public void sendJurorCommsSms_sendToCourt_welsh() {
        given(notifyTemplateMappingRepository.findByTemplateName(anyString())).willReturn(notifyCommsTemplateMapping);
        given(jurorCommsNotifyPayLoadService.isWelshCourtAndComms(anyBoolean(),
            any(WelshCourtLocation.class))).willReturn(Boolean.TRUE);
        given(jurorCommsNotifyPayLoadService.getWelshCourtLocation(anyString()))
            .willReturn(welshCourt);

        given(jurorCommsNotifyPayLoadService.generatePayLoadData(anyString(), any(JurorPool.class)))
            .willReturn(payLoad);
        service.sendJurorCommsSms(pool, JurorCommsNotifyTemplateType.SENT_TO_COURT, null, null, true);
        verify(mockNotifyAdapter).sendCommsSms(any());
    }

    @Test(expected = IllegalStateException.class)
    public void sendJurorCommsEmail_letter_comms_no_template() throws Exception {
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.LETTER_COMMS, null, null, false);
    }

    @Test(expected = IllegalStateException.class)
    public void sendJurorCommsEmail_letter_comms_no_detail_rec_data() throws Exception {
        service.sendJurorComms(pool, JurorCommsNotifyTemplateType.LETTER_COMMS, notifyTemplateId.toString(), null,
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
