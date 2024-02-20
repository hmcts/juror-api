package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Deferral Response service.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeferralResponseServiceImpl implements DeferralResponseService {

    public static final String DEFERRAL_REJECTED_CODE = "Z";
    public static final String DEFERRAL_DENIED_INFO = "Deferral Denied - %s";

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

    @SuppressWarnings("java:S125")
    private void declineDeferralForJurorPool(BureauJWTPayload payload, DeferralRequestDto deferralRequestDto,
                                             JurorPool jurorPool) {

        String username = payload.getLogin();

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setUserEdtq(username);
        jurorPool.setDeferralCode(deferralRequestDto.getDeferralReason());
        jurorPool.setDeferralDate(null);
        jurorPoolRepository.save(jurorPool);

        Juror juror = jurorPool.getJuror();
        juror.setResponded(true);
        juror.setUserEdtq(username);
        juror.setExcusalRejected(DEFERRAL_REJECTED_CODE);
        juror.setExcusalDate(null);
        jurorRepository.save(juror);

        // update Juror History - create deferral denied status event
        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorPool.getJurorNumber())
            .dateCreated(LocalDateTime.now())
            .historyCode(HistoryCodeMod.DEFERRED_POOL_MEMBER)
            .createdBy(username)
            .poolNumber(jurorPool.getPoolNumber())
            /* Other information text is used for (re-)issuing deferral denied letters - please be aware of this
                dependency before making any changes! */
            .otherInformation(String.format(DEFERRAL_DENIED_INFO, deferralRequestDto.getDeferralReason()))
            .build();

        jurorHistoryRepository.save(jurorHistory);

        if (JurorDigitalApplication.JUROR_OWNER.equalsIgnoreCase(payload.getOwner())) {
            // only Bureau users should enqueue a letter automatically
            printDataService.printDeferralDeniedLetter(jurorPool);

            // update Juror History - create deferral denied letter event
            jurorHistoryRepository.save(JurorHistory.builder()
                .jurorNumber(jurorPool.getJurorNumber())
                .dateCreated(LocalDateTime.now())
                .historyCode(HistoryCodeMod.NON_DEFERRED_LETTER)
                .createdBy(payload.getLogin())
                .poolNumber(jurorPool.getPoolNumber())
                .otherInformation("")
                .build());
        }
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
