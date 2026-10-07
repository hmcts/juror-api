package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.CommunicationChannel;
import uk.gov.hmcts.juror.api.moj.enumeration.EmailStatus;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JurorCommsEmailServiceImplTest {
    private static final String JUROR_NUMBER = "111222333";
    private static final String DIGITAL_DEFERRAL_TEMPLATE = "DIGITAL_DEF_GRANTED_ENG";
    private static final LocalDate CREATION_DATE = LocalDate.of(2026, 7, 28);

    @Mock
    private JurorCommsNotificationService jurorCommsNotificationService;
    @Mock
    private BulkPrintDataRepository bulkPrintDataRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;

    @InjectMocks
    private JurorCommsEmailServiceImpl service;

    private BulkPrintData pendingEmail;
    private JurorPool jurorPool;

    @Before
    public void setUp() {
        pendingEmail = new BulkPrintData();
        pendingEmail.setId(1L);
        pendingEmail.setJurorNo(JUROR_NUMBER);
        pendingEmail.setCreationDate(CREATION_DATE);
        pendingEmail.setCommunicationChannel(CommunicationChannel.EMAIL);
        pendingEmail.setEmailStatus(EmailStatus.PENDING);
        pendingEmail.setNotifyTemplateName(DIGITAL_DEFERRAL_TEMPLATE);
        pendingEmail.setDetailRec("N/A");
        pendingEmail.setDigitalComms(true);
        pendingEmail.setExtractedFlag(true);

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        juror.setEmail("juror@example.com");

        jurorPool = new JurorPool();
        jurorPool.setJuror(juror);
    }

    @Test
    public void process_EmailCommsHappyPath_SendsPendingEmailAndMarksSent() {
        given(bulkPrintDataRepository.findByCommunicationChannelAndEmailStatus(
            CommunicationChannel.EMAIL, EmailStatus.PENDING)).willReturn(List.of(pendingEmail));
        given(jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(anyString(), anyBoolean(), anyString()))
            .willReturn(jurorPool);
        given(bulkPrintDataRepository.findByJurorNoAndIdAndCreationDate(anyString(), anyLong(), any(LocalDate.class)))
            .willReturn(List.of(pendingEmail));

        SchedulerServiceClient.Result result = service.process();

        verify(bulkPrintDataRepository).deleteDbdEmails();
        verify(jurorCommsNotificationService).sendJurorEmailComms(jurorPool, DIGITAL_DEFERRAL_TEMPLATE);

        verify(bulkPrintDataRepository).saveAll(List.of(pendingEmail));
        assertThat(pendingEmail.getEmailStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(result.getStatus()).isEqualTo(SchedulerServiceClient.Result.Status.SUCCESS);
        assertThat(result.getMetaData()).containsEntry("COMMNS_SENT", "1");
    }

    @Test
    public void process_EmailCommsNotifyFailure_LeavesEmailPending() {
        given(bulkPrintDataRepository.findByCommunicationChannelAndEmailStatus(
            CommunicationChannel.EMAIL, EmailStatus.PENDING)).willReturn(List.of(pendingEmail));
        given(jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(anyString(), anyBoolean(), anyString()))
            .willReturn(jurorPool);
        willThrow(new JurorCommsNotificationServiceException("Notify failed"))
            .given(jurorCommsNotificationService).sendJurorEmailComms(jurorPool, DIGITAL_DEFERRAL_TEMPLATE);

        SchedulerServiceClient.Result result = service.process();

        verify(bulkPrintDataRepository, never()).saveAll(any());
        assertThat(pendingEmail.getEmailStatus()).isEqualTo(EmailStatus.PENDING);
        assertThat(result.getStatus()).isEqualTo(SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS);
        assertThat(result.getMetaData()).containsEntry("COMMS_FAILED", "1");
    }
}
