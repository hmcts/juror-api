package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.DeferralDecision;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.ExcusalResponseException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.CANNOT_REFUSE_FIRST_DEFERRAL;

/**
 * Deferral Response service.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeferralResponseServiceImpl implements DeferralResponseService {

    public static final String DEFERRAL_REJECTED_CODE = "Z";
    public static final String DEFERRAL_DENIED_INFO = "Deferral Denied - %s";
    public static final String DEFERRAL_GRANTED_INFO = "Add defer - %s";

    @NonNull
    private final ExcusalCodeRepository excusalCodeRepository;
    @NonNull
    private final JurorRepository jurorRepository;
    @NonNull
    private final JurorPoolRepository jurorPoolRepository;
    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;
    @NonNull
    private final PrintDataService printDataService;


    @Override
    @Transactional
    public void respondToDeferralRequest(BureauJwtPayload payload, DeferralRequestDto deferralRequestDto) {

        final String jurorNumber = deferralRequestDto.getJurorNumber();
        final String owner = payload.getOwner();

        JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);

        checkExcusalCodeIsValid(deferralRequestDto.getDeferralReason());
        Juror juror = jurorPool.getJuror();

        boolean firstDeferral = juror.getNoDefPos() == null || juror.getNoDefPos() == 0;

        if (firstDeferral && deferralRequestDto.getDeferralDecision().equals(DeferralDecision.REFUSE)) {
            log.debug("Cannot decline first deferral for juror {}", jurorNumber);
            throw new MojException.BusinessRuleViolation("Cannot decline first deferral request for juror",
                CANNOT_REFUSE_FIRST_DEFERRAL);
        } else if (deferralRequestDto.getDeferralDecision().equals(DeferralDecision.REFUSE)) {
            log.debug("Begin processing decline deferral juror {} by user {}", jurorNumber, payload.getLogin());
            declineDeferralForJurorPool(payload, deferralRequestDto, jurorPool);
        } else if (deferralRequestDto.getDeferralDecision().equals(DeferralDecision.GRANT)) {
            log.info("Begin processing grant deferral juror {} by user {}", jurorNumber, payload.getLogin());
            grantDeferralForJurorPool(payload, deferralRequestDto, jurorPool);
        } else {
            log.error("Invalid deferral decision for juror {}", jurorNumber);
            throw new MojException.BadRequest("Invalid deferral decision", null);
        }

        log.debug("End of deferral processing");
    }

    @SuppressWarnings("java:S125")
    private void declineDeferralForJurorPool(BureauJwtPayload payload, DeferralRequestDto deferralRequestDto,
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
                .otherInformation("Deferral Denied - " + deferralRequestDto.getDeferralReason())
                .build());
        }
    }

    private void grantDeferralForJurorPool(BureauJwtPayload payload, DeferralRequestDto deferralRequestDto,
                                           JurorPool jurorPool) {

        final String username = payload.getLogin();
        final String reasonCode = deferralRequestDto.getDeferralReason();

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.DEFERRED);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setUserEdtq(username);
        jurorPool.setDeferralCode(reasonCode);
        jurorPool.setDeferralDate(deferralRequestDto.getDeferralDate());
        jurorPool.setNextDate(null);

        jurorPoolRepository.save(jurorPool);

        Juror juror = jurorPool.getJuror();
        juror.setResponded(true);
        juror.setUserEdtq(username);

        if (Objects.isNull(juror.getNoDefPos())) {
            juror.setNoDefPos(1);
        } else {
            juror.setNoDefPos(juror.getNoDefPos() + 1);
        }
        jurorRepository.save(juror);

        // update Juror History - create deferral granted status event
        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorPool.getJurorNumber())
            .dateCreated(LocalDateTime.now())
            .historyCode(HistoryCodeMod.DEFERRED_POOL_MEMBER)
            .createdBy(username)
            .poolNumber(jurorPool.getPoolNumber())
            .otherInformation(String.format(DEFERRAL_GRANTED_INFO, reasonCode))
            .build();

        jurorHistoryRepository.save(jurorHistory);

        if (JurorDigitalApplication.JUROR_OWNER.equalsIgnoreCase(payload.getOwner())) {
            // only Bureau users should enqueue a letter automatically
            printDataService.printDeferralLetter(jurorPool);

            // update Juror History - create deferral granted letter event
            jurorHistoryRepository.save(JurorHistory.builder()
                .jurorNumber(jurorPool.getJurorNumber())
                .dateCreated(LocalDateTime.now())
                .historyCode(HistoryCodeMod.DEFERRED_LETTER)
                .createdBy(payload.getLogin())
                .poolNumber(jurorPool.getPoolNumber())
                .otherInformation("Deferral Letter Printed")
                .build());
        }
    }

    private void checkExcusalCodeIsValid(String excusalCode) {
        log.info(String.format("Checking excusal code %s is valid", excusalCode));

        List<String> excusalCodes = new ArrayList<>();
        // Extract just the excusal code from the ExcusalCodeEntity objects stored in ExcusalCodeRepository
        RepositoryUtils.retrieveAllRecordsFromDatabase(excusalCodeRepository)
            .forEach(excusalCodeEntity -> excusalCodes.add(excusalCodeEntity.getCode()));

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
