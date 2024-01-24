package uk.gov.hmcts.juror.api.moj.service;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
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
import uk.gov.hmcts.juror.api.moj.xerox.letters.PostponeLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.ExcusalDeniedLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.SummonsLetter;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PrintDataServiceImpl implements PrintDataService {
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final FormAttributeRepository formAttributeRepository;

    private final CourtLocationService courtLocationService;
    private final WelshCourtLocationRepository welshCourtLocationRepository;

    public static final String BUREAU_LOC_CODE = "400";

    @Override
    public void bulkPrintSummonsLetter(List<JurorPool> jurorPools) {
        if (jurorPools == null || jurorPools.isEmpty()) {
            throw new MojException.InternalServerError(
                "Attempted to print summons letters for empty jurorPool list", new Exception());
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
    public void printDeferralLetter(JurorPool jurorPool) {
        if (jurorPool == null) {
            throw new MojException.InternalServerError(
                "Attempted to print deferral letter for null jurorPool", new Exception());
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
                "Attempted to print deferral denied letter for null jurorPool", new Exception());
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
                "Attempted to print excusal denied letter for null jurorPool", new Exception());
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
                "Attempted to print confirmation letter for null jurorPool", new Exception());
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
                "Attempted to print postpone letter for null jurorPool", new Exception());
        }

        commitData(new PostponeLetter(jurorPool, jurorPool.getCourt(),
                                     courtLocationService.getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE),
                                     welshCourtLocationRepository.findByLocCode(jurorPool.getCourt().getLocCode())
        ));
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
}
