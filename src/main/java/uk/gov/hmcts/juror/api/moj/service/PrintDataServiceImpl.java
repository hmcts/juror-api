package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.FormAttributeRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.xerox.LetterBase;
import uk.gov.hmcts.juror.api.moj.xerox.letters.ConfirmLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.DeferralDeniedLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.DeferralLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.ExcusalDeniedLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.ExcusalLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.PostponeLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.RequestInfoLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.SummonsLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.SummonsReminderLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.WithdrawalLetter;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.DAY_ALREADY_EXISTS;
import static uk.gov.hmcts.juror.api.moj.xerox.LetterBase.getDateOfLetter;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings("PMD.TooManyMethods")
public class PrintDataServiceImpl implements PrintDataService {
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final FormAttributeRepository formAttributeRepository;

    private final CourtLocationService courtLocationService;
    private final WelshCourtLocationRepository welshCourtLocationRepository;

    public static final String BUREAU_LOC_CODE = "400";
    private final JurorHistoryService jurorHistoryService;

    @Override
    public void bulkPrintSummonsLetter(List<JurorPool> jurorPools) {
        if (jurorPools == null || jurorPools.isEmpty()) {
            throw new MojException.InternalServerError(
                "Attempted to print summons letters for empty jurorPool list", null);
        }

        jurorPools.forEach(jurorPool -> {
            //queue a letter if juror is not disqualified
            if (!Objects.equals(jurorPool.getStatus().getStatus(), IJurorStatus.DISQUALIFIED)) {
                commitData(new SummonsLetter(jurorPool,
                    jurorPool.getCourt(),
                    courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
                    welshCourtLocationRepository.findByLocCode(jurorPool.getCourt().getLocCode())
                ));
            }
        });
    }

    @Override
    public void reprintSummonsLetter(JurorPool jurorPool) {
        bulkPrintSummonsLetter(List.of(jurorPool));
        jurorHistoryService.createSummonLetterReprintedHistory(jurorPool);
    }

    @Override
    public void printSummonsReminderLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print summons reminder letter for null jurorPool", null);
        }

        commitData(new SummonsReminderLetter(
            jurorPool, jurorPool.getCourt(),
            courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
            welshCourtLocationRepository.findByLocCode(jurorPool.getCourt().getLocCode())
        ));
    }

    @Override
    public void printDeferralLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print deferral letter for null jurorPool", null);
        }

        commitData(new DeferralLetter(jurorPool, jurorPool.getCourt(),
            courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
            welshCourtLocationRepository.findByLocCode(jurorPool.getCourt().getLocCode())
        ));
    }

    @Override
    public void printDeferralDeniedLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print deferral denied letter for null jurorPool", null);
        }

        commitData(new DeferralDeniedLetter(jurorPool, jurorPool.getCourt(),
            courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
            welshCourtLocationRepository
                .findByLocCode(jurorPool.getCourt().getLocCode())
        ));
    }

    @Override
    public void printExcusalDeniedLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print excusal denied letter for null jurorPool", null);
        }

        commitData(new ExcusalDeniedLetter(jurorPool, jurorPool.getCourt(),
            courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
            welshCourtLocationRepository
                .findByLocCode(jurorPool.getCourt().getLocCode())
        ));
    }

    @Override
    public void printConfirmationLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print confirmation letter for null jurorPool", null);
        }

        commitData(new ConfirmLetter(jurorPool, jurorPool.getCourt(),
            courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
            welshCourtLocationRepository.findByLocCode(jurorPool.getCourt().getLocCode())
        ));
    }

    @Override
    public void printPostponeLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print postpone letter for null jurorPool", null);
        }

        commitData(new PostponeLetter(jurorPool, jurorPool.getCourt(),
            courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
            welshCourtLocationRepository.findByLocCode(jurorPool.getCourt().getLocCode())
        ));
    }

    @Override
    public void printExcusalLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print excusal letter for null jurorPool", null);
        }

        commitData(new ExcusalLetter(jurorPool, jurorPool.getCourt(),
            courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
            welshCourtLocationRepository.findByLocCode(jurorPool.getCourt().getLocCode())
        ));
    }

    @Override
    public void printRequestInfoLetter(JurorPool jurorPool, String additionalInfo) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print postpone letter for null jurorPool", null);
        }

        commitData(new RequestInfoLetter(jurorPool, additionalInfo, jurorPool.getCourt(),
            courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
            welshCourtLocationRepository.findByLocCode(jurorPool.getCourt().getLocCode())
        ));
    }

    @Override
    public void reprintRequestInfoLetter(JurorPool jurorPool) {
        // need to look up the current record in bulk print data and update the detailRec field
        List<BulkPrintData> bulkPrintDataList =
            bulkPrintDataRepository.findByJurorNoAndFormAttributeFormTypeInOrderByCreationDateDesc(
            jurorPool.getJuror().getJurorNumber(),
            List.of("5227", "5227C")
        );

        if (bulkPrintDataList.isEmpty()) {
            throw new MojException.InternalServerError(
                "Attempted to reprint request info letter for juror with no existing request info letter", null);
        }

        BulkPrintData bulkPrintData = bulkPrintDataList.get(0);
        BulkPrintData newBulkPrintData = BulkPrintData.builder()
            .creationDate(LocalDate.now())
            .formAttribute(bulkPrintData.getFormAttribute())
            .jurorNo(bulkPrintData.getJurorNo())
            .extractedFlag(false)
            .build();
        StringBuilder detailRec = new StringBuilder(bulkPrintData.getDetailRec());
        detailRec.replace(0, 18, StringUtils.rightPad(getDateOfLetter(), 18, ""));
        newBulkPrintData.setDetailRec(detailRec.toString());
        bulkPrintDataRepository.save(newBulkPrintData);

    }

    @Override
    public void printWithdrawalLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print withdrawal letter for null jurorPool", null);
        }

        commitData(new WithdrawalLetter(jurorPool, jurorPool.getCourt(),
            courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
            welshCourtLocationRepository.findByLocCode(jurorPool.getCourt().getLocCode())
        ));
    }

    @Override
    public void checkLetterInBulkPrint(String jurorNumber, String formType, LocalDate creationDate,
                                       boolean extractedFlag) {
        if (!bulkPrintDataRepository.findByJurorNumberAndFormTypeAndCreationDateAndExtractedFlag(jurorNumber, formType,
            creationDate, extractedFlag).isEmpty()) {
            throw new MojException.BusinessRuleViolation(
                "Letter already exists in bulk print queue for the same day", DAY_ALREADY_EXISTS);
        }
    }

    public void commitData(LetterBase letter) {
        final LocalDate date = LocalDate.now();

        BulkPrintData bulkPrintData = new BulkPrintData();
        bulkPrintData.setCreationDate(date);
        bulkPrintData.setFormAttribute(RepositoryUtils.retrieveFromDatabase(letter.getFormCode(),
            formAttributeRepository));
        bulkPrintData.setDetailRec(letter.getLetterString());
        bulkPrintData.setJurorNo(letter.getJurorNumber());
        bulkPrintDataRepository.save(bulkPrintData);
    }

    @Override
    public void removeQueuedLetterForJuror(JurorPool jurorPool, List<FormCode> formCodes) {

        List<BulkPrintData> bulkPrintDataList = bulkPrintDataRepository
            .findByJurorNoAndCreationDateAndFormAttributeFormTypeIn(
                jurorPool.getJuror().getJurorNumber(), LocalDate.now(),
                formCodes.stream().map(FormCode::getCode).toList()
            );

        bulkPrintDataRepository.deleteAll(bulkPrintDataList);
    }
}
