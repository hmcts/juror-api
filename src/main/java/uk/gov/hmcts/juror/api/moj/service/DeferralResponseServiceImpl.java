package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.DeferralDecision;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.ExcusalResponseException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Excusal Response service.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeferralResponseServiceImpl implements DeferralResponseService {

    public static final String EXCUSAL_REJECTED_CODE = "Z";

    @NonNull
    private final ExcusalCodeRepository excusalCodeRepository;
    @NonNull
    private final JurorRepository jurorRepository;
    @NonNull
    private final JurorPoolRepository jurorPoolRepository;
    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;
    private final PrintDataService printDataService;


    @Override
    @Transactional
    public void respondToDeferralRequest(BureauJWTPayload payload, DeferralRequestDto deferralRequestDto) {

        final String jurorNumber = deferralRequestDto.getJurorNumber();
        final String owner = payload.getOwner();

        JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);

        checkExcusalCodeIsValid(deferralRequestDto.getDeferralReason());
        Juror juror = jurorPool.getJuror();

        boolean firstDeferral = juror.getNoDefPos() == null || juror.getNoDefPos() == 0;


        if (!firstDeferral && deferralRequestDto.getDeferralDecision().equals(DeferralDecision.REFUSE)) {
            log.debug("Begin processing decline deferral juror {} by user {}", jurorNumber, payload.getLogin());
            declineDeferralForJurorPool(payload, deferralRequestDto, jurorPool);
        } else {
            //TODO grant deferral
            log.info("Grant will be done here");
        }

        log.debug("End of deferral processing");
    }

    private void declineDeferralForJurorPool(BureauJWTPayload payload, DeferralRequestDto deferralRequestDto,
                                             JurorPool jurorPool) {

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setUserEdtq(payload.getLogin());
        jurorPool.setDeferralDate(null);
        jurorPoolRepository.save(jurorPool);

        Juror juror = jurorPool.getJuror();
        juror.setResponded(true);
        juror.setExcusalCode(deferralRequestDto.getDeferralReason());
        juror.setUserEdtq(payload.getLogin());
        juror.setExcusalRejected(EXCUSAL_REJECTED_CODE);
        juror.setExcusalDate(null);
        jurorRepository.save(juror);

        // update Part_Hist RESPONDED and DEFERRED statuses
        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorPool.getJurorNumber())
            .dateCreated(LocalDateTime.now())
            .historyCode(HistoryCodeMod.RESPONDED_POSITIVELY)
            .createdBy(payload.getLogin())
            .poolNumber(jurorPool.getPoolNumber())
            .otherInformation(JurorHistory.RESPONDED)
            .build();

        jurorHistoryRepository.save(jurorHistory);

        jurorHistory.setHistoryCode(HistoryCodeMod.DEFERRED_POOL_MEMBER);
        jurorHistory.setOtherInformation("Deferral Denied - " + juror.getExcusalCode());

        jurorHistoryRepository.save(jurorHistory);

        printDataService.printDeferralDeniedLetter(jurorPool);
        
        jurorHistoryRepository.save(JurorHistory.builder()
                                            .jurorNumber(jurorPool.getJurorNumber())
                                            .dateCreated(LocalDateTime.now())
                                            .historyCode(HistoryCodeMod.NON_DEFERRED_LETTER)
                                            .createdBy(payload.getLogin())
                                            .poolNumber(jurorPool.getPoolNumber())
                                            .otherInformation("")
                                            .build());
    }

    private void checkExcusalCodeIsValid(String excusalCode) {
        log.info(String.format("Checking excusal code %s is valid", excusalCode));

        List<String> excusalCodes = new ArrayList<>();
        // Extract just the excusal code from the ExcusalCodeEntity objects stored in ExcusalCodeRepository
        RepositoryUtils.retrieveAllRecordsFromDatabase(excusalCodeRepository)
            .forEach(excusalCodeEntity -> excusalCodes.add(excusalCodeEntity.getExcusalCode()));

        if (excusalCodes.isEmpty()) {
            log.info("Unable to retrieve list of excusal codes from database");
            throw new ExcusalResponseException.UnableToRetrieveExcusalCodeList();
        }

        if (!excusalCodes.contains(excusalCode)) {
            log.info(String.format("Excusal code %s is invalid", excusalCode));
            throw new ExcusalResponseException.InvalidExcusalCode(excusalCode);
        }
    }
}
