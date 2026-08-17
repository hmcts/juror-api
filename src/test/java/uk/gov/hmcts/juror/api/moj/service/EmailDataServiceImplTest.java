package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormAttribute;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.CommunicationChannel;
import uk.gov.hmcts.juror.api.moj.enumeration.DigitalByDefaultEmailTemplate;
import uk.gov.hmcts.juror.api.moj.enumeration.EmailStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.FormAttributeRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailDataServiceImplTest {

    @Mock
    private BulkPrintDataRepository bulkPrintDataRepository;

    @Mock
    private FormAttributeRepository formAttributeRepository;

    @Mock
    private JurorHistoryService jurorHistoryService;

    @InjectMocks
    private EmailDataServiceImpl emailDataService;

    private JurorPool createJurorPool(boolean welsh) {
        Juror juror = new Juror();
        juror.setJurorNumber("123456789");
        juror.setWelsh(welsh);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    @Test
    void emailExcusalGrantedLetter_englishJuror_queuesPendingEmailAndCreatesHistory() {
        JurorPool jurorPool = createJurorPool(false);
        FormAttribute formAttribute = FormAttribute.builder()
            .formType(FormCode.ENG_EXCUSAL.getCode())
            .directoryName("DIR")
            .maxRecLen(100)
            .build();

        when(formAttributeRepository.findById(FormCode.ENG_EXCUSAL.getCode()))
            .thenReturn(Optional.of(formAttribute));

        emailDataService.emailExcusalGrantedLetter(jurorPool);

        ArgumentCaptor<BulkPrintData> captor = ArgumentCaptor.forClass(BulkPrintData.class);
        verify(bulkPrintDataRepository).save(captor.capture());

        BulkPrintData emailData = captor.getValue();
        assertThat(emailData.getJurorNo()).isEqualTo("123456789");
        assertThat(emailData.getFormAttribute()).isEqualTo(formAttribute);
        assertThat(emailData.getNotifyTemplateName()).isEqualTo(
            DigitalByDefaultEmailTemplate.EXCUSAL_GRANTED_ENGLISH.getTemplateName());
        assertThat(emailData.isExtractedFlag()).isTrue();
        assertThat(emailData.isDigitalComms()).isTrue();
        assertThat(emailData.getDetailRec()).isEqualTo("N/A");
        assertThat(emailData.getCommunicationChannel()).isEqualTo(CommunicationChannel.EMAIL);
        assertThat(emailData.getEmailStatus()).isEqualTo(EmailStatus.PENDING);
        assertThat(emailData.getCreationDate()).isEqualTo(LocalDate.now());

        verify(jurorHistoryService).createExcusedLetter(jurorPool, CommunicationChannel.EMAIL);
    }

    @Test
    void emailExcusalGrantedLetter_welshJuror_queuesPendingEmailAndCreatesHistory() {
        JurorPool jurorPool = createJurorPool(true);
        FormAttribute formAttribute = FormAttribute.builder()
            .formType(FormCode.BI_EXCUSAL.getCode())
            .directoryName("DIR")
            .maxRecLen(100)
            .build();

        when(formAttributeRepository.findById(FormCode.BI_EXCUSAL.getCode()))
            .thenReturn(Optional.of(formAttribute));

        emailDataService.emailExcusalGrantedLetter(jurorPool);

        ArgumentCaptor<BulkPrintData> captor = ArgumentCaptor.forClass(BulkPrintData.class);
        verify(bulkPrintDataRepository).save(captor.capture());

        BulkPrintData emailData = captor.getValue();
        assertThat(emailData.getJurorNo()).isEqualTo("123456789");
        assertThat(emailData.getFormAttribute()).isEqualTo(formAttribute);
        assertThat(emailData.getNotifyTemplateName()).isEqualTo(
            DigitalByDefaultEmailTemplate.EXCUSAL_GRANTED_WELSH.getTemplateName());
        assertThat(emailData.isExtractedFlag()).isTrue();
        assertThat(emailData.isDigitalComms()).isTrue();
        assertThat(emailData.getDetailRec()).isEqualTo("N/A");
        assertThat(emailData.getCommunicationChannel()).isEqualTo(CommunicationChannel.EMAIL);
        assertThat(emailData.getEmailStatus()).isEqualTo(EmailStatus.PENDING);
        assertThat(emailData.getCreationDate()).isEqualTo(LocalDate.now());

        verify(jurorHistoryService).createExcusedLetter(jurorPool, CommunicationChannel.EMAIL);
    }

    @Test
    void emailWithdrawalLetter_englishJuror_queuesPendingEmailAndCreatesHistoryWithCode() {
        JurorPool jurorPool = createJurorPool(false);
        FormAttribute formAttribute = FormAttribute.builder()
            .formType(FormCode.ENG_WITHDRAWAL.getCode())
            .directoryName("DIR")
            .maxRecLen(100)
            .build();

        when(formAttributeRepository.findById(FormCode.ENG_WITHDRAWAL.getCode()))
            .thenReturn(Optional.of(formAttribute));

        emailDataService.emailWithdrawalLetter(jurorPool, "N");

        ArgumentCaptor<BulkPrintData> captor = ArgumentCaptor.forClass(BulkPrintData.class);
        verify(bulkPrintDataRepository).save(captor.capture());

        BulkPrintData emailData = captor.getValue();
        assertThat(emailData.getJurorNo()).isEqualTo("123456789");
        assertThat(emailData.getFormAttribute()).isEqualTo(formAttribute);
        assertThat(emailData.getNotifyTemplateName()).isEqualTo(
            DigitalByDefaultEmailTemplate.WITHDRAWAL_ENGLISH.getTemplateName());
        assertThat(emailData.getCommunicationChannel()).isEqualTo(CommunicationChannel.EMAIL);
        assertThat(emailData.getEmailStatus()).isEqualTo(EmailStatus.PENDING);

        verify(jurorHistoryService).createWithdrawHistory(jurorPool, "Withdrawal Letter", "N",
                                                          CommunicationChannel.EMAIL);
    }

    @Test
    void emailExcusalGrantedLetter_nullJurorPool_throwsInternalServerError() {
        assertThatThrownBy(() -> emailDataService.emailExcusalGrantedLetter(null))
            .isInstanceOf(MojException.InternalServerError.class)
            .hasMessage("Attempted to email excusal granted letter for null jurorPool");

        verifyNoInteractions(bulkPrintDataRepository);
        verifyNoInteractions(formAttributeRepository);
        verifyNoInteractions(jurorHistoryService);
    }
}
