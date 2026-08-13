package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.CommunicationChannel;
import uk.gov.hmcts.juror.api.moj.enumeration.DigitalByDefaultEmailTemplate;
import uk.gov.hmcts.juror.api.moj.enumeration.EmailStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.FormAttributeRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EmailDataServiceImpl implements EmailDataService {
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final FormAttributeRepository formAttributeRepository;
    private final JurorHistoryService jurorHistoryService;


    @Override
    public void emailDeferralLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to email deferral letter for null jurorPool", null);
        }

        boolean welsh = jurorPool.getJuror().isWelsh();
        FormCode formCode = welsh ? FormCode.BI_DEFERRAL : FormCode.ENG_DEFERRAL;

        DigitalByDefaultEmailTemplate template = welsh
            ? DigitalByDefaultEmailTemplate.DEFERRAL_GRANTED_WELSH
            : DigitalByDefaultEmailTemplate.DEFERRAL_GRANTED_ENGLISH;

        BulkPrintData bulkPrintData = createPendingEmail(jurorPool, formCode, template);
        bulkPrintDataRepository.save(bulkPrintData);

        jurorHistoryService.createDeferredLetterHistory(jurorPool, CommunicationChannel.EMAIL);
    }

    @Override
    public boolean emailReissueLetter(JurorPool jurorPool, FormCode requestedFormCode) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to email reissue letter for null jurorPool", null);
        }

        Optional<EmailTemplateData> templateData = getEmailTemplateData(
            requestedFormCode,
            jurorPool.getJuror().isWelsh()
        );
        if (templateData.isEmpty()) {
            return false;
        }

        BulkPrintData bulkPrintData = createPendingEmail(
            jurorPool,
            templateData.get().formCode(),
            templateData.get().template()
        );
        bulkPrintDataRepository.save(bulkPrintData);
        return true;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    private static Optional<EmailTemplateData> getEmailTemplateData(FormCode requestedFormCode, boolean welsh) {
        return Optional.ofNullable(switch (requestedFormCode) {
            case ENG_DEFERRAL, BI_DEFERRAL -> new EmailTemplateData(
                welsh ? FormCode.BI_DEFERRAL : FormCode.ENG_DEFERRAL,
                welsh
                    ? DigitalByDefaultEmailTemplate.DEFERRAL_GRANTED_WELSH
                    : DigitalByDefaultEmailTemplate.DEFERRAL_GRANTED_ENGLISH
            );
            case ENG_DEFERRALDENIED, BI_DEFERRALDENIED -> new EmailTemplateData(
                welsh ? FormCode.BI_DEFERRALDENIED : FormCode.ENG_DEFERRALDENIED,
                welsh
                    ? DigitalByDefaultEmailTemplate.DEFERRAL_DENIED_WELSH
                    : DigitalByDefaultEmailTemplate.DEFERRAL_DENIED_ENGLISH
            );
            case ENG_EXCUSAL, BI_EXCUSAL -> new EmailTemplateData(
                welsh ? FormCode.BI_EXCUSAL : FormCode.ENG_EXCUSAL,
                welsh
                    ? DigitalByDefaultEmailTemplate.EXCUSAL_GRANTED_WELSH
                    : DigitalByDefaultEmailTemplate.EXCUSAL_GRANTED_ENGLISH
            );
            case ENG_EXCUSALDENIED, BI_EXCUSALDENIED -> new EmailTemplateData(
                welsh ? FormCode.BI_EXCUSALDENIED : FormCode.ENG_EXCUSALDENIED,
                welsh
                    ? DigitalByDefaultEmailTemplate.EXCUSAL_DENIED_WELSH
                    : DigitalByDefaultEmailTemplate.EXCUSAL_DENIED_ENGLISH
            );
            case ENG_POSTPONE, BI_POSTPONE -> new EmailTemplateData(
                welsh ? FormCode.BI_POSTPONE : FormCode.ENG_POSTPONE,
                welsh
                    ? DigitalByDefaultEmailTemplate.POSTPONEMENT_WELSH
                    : DigitalByDefaultEmailTemplate.POSTPONEMENT_ENGLISH
            );
            case ENG_CONFIRMATION, BI_CONFIRMATION -> new EmailTemplateData(
                welsh ? FormCode.BI_CONFIRMATION : FormCode.ENG_CONFIRMATION,
                welsh
                    ? DigitalByDefaultEmailTemplate.CONFIRMATION_WELSH
                    : DigitalByDefaultEmailTemplate.CONFIRMATION_ENGLISH
            );
            case ENG_WITHDRAWAL, BI_WITHDRAWAL -> new EmailTemplateData(
                welsh ? FormCode.BI_WITHDRAWAL : FormCode.ENG_WITHDRAWAL,
                welsh
                    ? DigitalByDefaultEmailTemplate.WITHDRAWAL_WELSH
                    : DigitalByDefaultEmailTemplate.WITHDRAWAL_ENGLISH
            );
            default -> null;
        });
    }

    private record EmailTemplateData(FormCode formCode, DigitalByDefaultEmailTemplate template) {
    }

    private BulkPrintData createPendingEmail(
        JurorPool jurorPool,
        FormCode formCode,
        DigitalByDefaultEmailTemplate template
    ) {
        BulkPrintData bulkPrintData = new BulkPrintData();
        bulkPrintData.setJurorNo(jurorPool.getJurorNumber());
        bulkPrintData.setFormAttribute(RepositoryUtils.retrieveFromDatabase(
            formCode.getCode(),
            formAttributeRepository
        ));
        bulkPrintData.setNotifyTemplateName(template.getTemplateName());
        setDefaults(bulkPrintData);
        return bulkPrintData;
    }

    private static void setDefaults(BulkPrintData bulkPrintData) {
        bulkPrintData.setCreationDate(LocalDate.now());
        // Don't want any other comms going out so setting flags to true
        bulkPrintData.setExtractedFlag(true);
        bulkPrintData.setDigitalComms(true);
        bulkPrintData.setDetailRec("N/A"); // cannot be null
        bulkPrintData.setCommunicationChannel(CommunicationChannel.EMAIL);
        bulkPrintData.setEmailStatus(EmailStatus.PENDING);
    }


}
