package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.ExcusalDecisionDto;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalDecision;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.ExcusalResponseException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Excusal Response service.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ExcusalResponseServiceImpl implements ExcusalResponseService {

    private final ExcusalCodeRepository excusalCodeRepository;
    private final JurorRepository jurorRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorHistoryRepository jurorHistoryRepository;
    private final PrintDataService printDataService;
    private final JurorHistoryService jurorHistoryService;
    private final JurorPoolService jurorPoolService;
    private final JurorResponseService jurorResponseService;




    @Override
    @Transactional
    public void respondToExcusalRequest(BureauJwtPayload payload, ExcusalDecisionDto excusalDecisionDto,
                                        String jurorNumber) {

        final String login = payload.getLogin();
        final String owner = payload.getOwner();
        log.info(String.format("Processing excusal request for Juror %s, by user %s", jurorNumber, login));

        checkExcusalCodeIsValid(excusalDecisionDto.getExcusalReasonCode());

        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);

        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);


        if (excusalDecisionDto.getExcusalDecision().equals(ExcusalDecision.GRANT)) {
            jurorResponseService.setResponseProcessingStatusToClosed(jurorNumber);
            grantExcusalForJuror(payload, excusalDecisionDto, jurorPool);
            if (!ExcusalCodeEnum.D.getCode().equals(excusalDecisionDto.getExcusalReasonCode())
                && SecurityUtil.BUREAU_OWNER.equals(owner)) {
                // Only generate letter for non-deceased jurors and Bureau users
                sendExcusalLetter(jurorPool, jurorNumber);
            }
        } else {
            refuseExcusalForJuror(payload, excusalDecisionDto, jurorPool);
        }
    }

    public void checkExcusalCodeIsValid(String excusalCode) {
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


    private void grantExcusalForJuror(BureauJwtPayload payload, ExcusalDecisionDto excusalDecisionDto,
                                      JurorPool jurorPool) {
        Juror juror = jurorPool.getJuror();

        log.info(String.format("Processing officer decision to grant excusal for Juror %s", juror.getJurorNumber()));

        juror.setResponded(true);
        juror.setExcusalDate(LocalDate.now());
        juror.setExcusalCode(excusalDecisionDto.getExcusalReasonCode());
        juror.setUserEdtq(payload.getLogin());
        jurorRepository.save(juror);

        jurorPool.setUserEdtq(payload.getLogin());
        jurorPool.setStatus(getPoolStatus(IJurorStatus.EXCUSED));
        jurorPool.setNextDate(null);
        jurorPoolRepository.save(jurorPool);

        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorPool.getJurorNumber())
            .dateCreated(LocalDateTime.now())
            .historyCode(HistoryCodeMod.EXCUSE_POOL_MEMBER)
            .createdBy(payload.getLogin())
            .poolNumber(jurorPool.getPoolNumber())
            .otherInformation("Add Excuse - " + excusalDecisionDto.getExcusalReasonCode())
            .build();

        jurorHistoryRepository.save(jurorHistory);
    }

    private void refuseExcusalForJuror(BureauJwtPayload payload, ExcusalDecisionDto excusalDecisionDto,
                                       JurorPool jurorPool) {
        Juror juror = jurorPool.getJuror();
        log.info(String.format("Processing officer decision to refuse excusal for Juror %s", juror.getJurorNumber()));

        //Store the current status of the juror JS-367
        JurorStatus currentStatus = jurorPool.getStatus();

        juror.setResponded(true);
        if (jurorPool.getStatus().getStatus() != IJurorStatus.EXCUSED) {
            juror.setExcusalCode(excusalDecisionDto.getExcusalReasonCode());
            juror.setExcusalDate(LocalDate.now());
        }
        juror.setUserEdtq(payload.getLogin());
        juror.setExcusalRejected("Y");
        jurorRepository.save(juror);

        if (juror.getDateOfBirth() == null) {
            // check if the juror has a response and set date of birth
            jurorResponseService.getCommonJurorResponseOptional(juror.getJurorNumber())
                .ifPresent(jurorResponse -> juror.setDateOfBirth(jurorResponse.getDateOfBirth()));
        }

        // Need to avoid setting to responded without a date of birth else PNC check will fail
        if (jurorPool.getStatus().getStatus() == IJurorStatus.SUMMONED && juror.getDateOfBirth() != null) {
            jurorPool.setStatus(getPoolStatus(IJurorStatus.RESPONDED));
        }

        if (jurorPool.getNextDate() == null) {
            jurorPool.setNextDate(jurorPool.getPool().getReturnDate());
            jurorPool.setStatus(getPoolStatus(IJurorStatus.RESPONDED));
        }


        // Restore the original status of the juror
        jurorPool.setStatus(currentStatus);
        jurorPool.setUserEdtq(payload.getLogin());
        jurorPoolRepository.save(jurorPool);


        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorPool.getJurorNumber())
            .dateCreated(LocalDateTime.now())
            .historyCode(HistoryCodeMod.EXCUSE_POOL_MEMBER)
            .createdBy(payload.getLogin())
            .poolNumber(jurorPool.getPoolNumber())
            .otherInformation("Refuse Excuse")
            .build();

        jurorHistoryRepository.save(jurorHistory);

        jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorPool.getJurorNumber())
            .dateCreated(LocalDateTime.now())
            .createdBy(payload.getLogin())
            .historyCode(HistoryCodeMod.RESPONDED_POSITIVELY)
            .poolNumber(jurorPool.getPoolNumber())
            .otherInformation(JurorHistory.RESPONDED)
            .build();

        jurorHistoryRepository.save(jurorHistory);

        // bureau only - queue letter for xerox
        if (SecurityUtil.isBureau()) {
            printDataService.printExcusalDeniedLetter(jurorPool);

            jurorHistoryService.createNonExcusedLetterHistory(jurorPool, "Refused Excusal");
        }

    }

    private void sendExcusalLetter(JurorPool jurorPool, String jurorNumber) {
        log.info(String.format("Preparing an excusal letter for Juror %s", jurorNumber));

        printDataService.printExcusalLetter(jurorPool);

        jurorHistoryService.createExcusedLetter(jurorPool);

        log.info(String.format("Excusal letter enqueued for Juror %s", jurorNumber));
    }

    private JurorStatus getPoolStatus(int poolStatusId) {
        return RepositoryUtils.retrieveFromDatabase(poolStatusId, jurorStatusRepository);
    }
}
