package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.FormAttributeRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailDataServiceImplTest {

    private static final String JUROR_NUMBER = "555555561";

    @Mock
    private BulkPrintDataRepository bulkPrintDataRepository;

    @Mock
    private FormAttributeRepository formAttributeRepository;

    @Mock
    private JurorHistoryService jurorHistoryService;

    @InjectMocks
    private EmailDataServiceImpl emailDataService;

    @ParameterizedTest
    @MethodSource("reissueEmailTemplateMappings")
    void emailReissueLetterSavesExpectedEmailTemplate(FormCode requestedFormCode, boolean welsh,
                                                      FormCode expectedFormCode,
                                                      DigitalByDefaultEmailTemplate expectedTemplate) {
        when(formAttributeRepository.findById(expectedFormCode.getCode()))
            .thenReturn(Optional.of(formAttribute(expectedFormCode)));

        boolean result = emailDataService.emailReissueLetter(jurorPool(welsh), requestedFormCode);

        assertThat(result).isTrue();
        verify(formAttributeRepository).findById(expectedFormCode.getCode());

        ArgumentCaptor<BulkPrintData> bulkPrintDataCaptor = ArgumentCaptor.forClass(BulkPrintData.class);
        verify(bulkPrintDataRepository).save(bulkPrintDataCaptor.capture());

        BulkPrintData bulkPrintData = bulkPrintDataCaptor.getValue();
        assertThat(bulkPrintData.getJurorNo()).isEqualTo(JUROR_NUMBER);
        assertThat(bulkPrintData.getFormAttribute().getFormType()).isEqualTo(expectedFormCode.getCode());
        assertThat(bulkPrintData.getNotifyTemplateName()).isEqualTo(expectedTemplate.getTemplateName());
        assertThat(bulkPrintData.getCreationDate()).isEqualTo(LocalDate.now());
        assertThat(bulkPrintData.isExtractedFlag()).isTrue();
        assertThat(bulkPrintData.isDigitalComms()).isTrue();
        assertThat(bulkPrintData.getDetailRec()).isEqualTo("N/A");
        assertThat(bulkPrintData.getCommunicationChannel()).isEqualTo(CommunicationChannel.EMAIL);
        assertThat(bulkPrintData.getEmailStatus()).isEqualTo(EmailStatus.PENDING);
    }

    @Test
    void emailReissueLetterReturnsFalseForUnsupportedTemplate() {
        boolean result = emailDataService.emailReissueLetter(jurorPool(false), FormCode.ENG_REQUESTINFO);

        assertThat(result).isFalse();
        verifyNoInteractions(formAttributeRepository, bulkPrintDataRepository);
    }

    private static Stream<Arguments> reissueEmailTemplateMappings() {
        return Stream.of(
            Arguments.of(FormCode.ENG_DEFERRAL, false, FormCode.ENG_DEFERRAL,
                DigitalByDefaultEmailTemplate.DEFERRAL_GRANTED_ENGLISH),
            Arguments.of(FormCode.BI_DEFERRAL, true, FormCode.BI_DEFERRAL,
                DigitalByDefaultEmailTemplate.DEFERRAL_GRANTED_WELSH),
            Arguments.of(FormCode.ENG_DEFERRALDENIED, false, FormCode.ENG_DEFERRALDENIED,
                DigitalByDefaultEmailTemplate.DEFERRAL_DENIED_ENGLISH),
            Arguments.of(FormCode.BI_DEFERRALDENIED, true, FormCode.BI_DEFERRALDENIED,
                DigitalByDefaultEmailTemplate.DEFERRAL_DENIED_WELSH),
            Arguments.of(FormCode.ENG_EXCUSAL, false, FormCode.ENG_EXCUSAL,
                DigitalByDefaultEmailTemplate.EXCUSAL_GRANTED_ENGLISH),
            Arguments.of(FormCode.BI_EXCUSAL, true, FormCode.BI_EXCUSAL,
                DigitalByDefaultEmailTemplate.EXCUSAL_GRANTED_WELSH),
            Arguments.of(FormCode.ENG_EXCUSALDENIED, false, FormCode.ENG_EXCUSALDENIED,
                DigitalByDefaultEmailTemplate.EXCUSAL_DENIED_ENGLISH),
            Arguments.of(FormCode.BI_EXCUSALDENIED, true, FormCode.BI_EXCUSALDENIED,
                DigitalByDefaultEmailTemplate.EXCUSAL_DENIED_WELSH),
            Arguments.of(FormCode.ENG_POSTPONE, false, FormCode.ENG_POSTPONE,
                DigitalByDefaultEmailTemplate.POSTPONEMENT_ENGLISH),
            Arguments.of(FormCode.BI_POSTPONE, true, FormCode.BI_POSTPONE,
                DigitalByDefaultEmailTemplate.POSTPONEMENT_WELSH),
            Arguments.of(FormCode.ENG_CONFIRMATION, false, FormCode.ENG_CONFIRMATION,
                DigitalByDefaultEmailTemplate.CONFIRMATION_ENGLISH),
            Arguments.of(FormCode.BI_CONFIRMATION, true, FormCode.BI_CONFIRMATION,
                DigitalByDefaultEmailTemplate.CONFIRMATION_WELSH),
            Arguments.of(FormCode.ENG_WITHDRAWAL, false, FormCode.ENG_WITHDRAWAL,
                DigitalByDefaultEmailTemplate.WITHDRAWAL_ENGLISH),
            Arguments.of(FormCode.BI_WITHDRAWAL, true, FormCode.BI_WITHDRAWAL,
                DigitalByDefaultEmailTemplate.WITHDRAWAL_WELSH)
        );
    }

    private static JurorPool jurorPool(boolean welsh) {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        juror.setWelsh(welsh);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setJuror(juror);
        return jurorPool;
    }

    private static FormAttribute formAttribute(FormCode formCode) {
        return FormAttribute.builder()
            .formType(formCode.getCode())
            .directoryName(formCode.name())
            .build();
    }
}
