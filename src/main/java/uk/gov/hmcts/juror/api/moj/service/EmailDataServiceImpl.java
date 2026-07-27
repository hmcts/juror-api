package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings("PMD.TooManyMethods")
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
