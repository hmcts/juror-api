package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController.ExcusalCodeDto;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCode;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.domain.THistoryCode;
import uk.gov.hmcts.juror.api.bureau.exception.ExcusalException;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalDeniedLetter;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalDeniedLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalLetter;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("Duplicates")
@Slf4j
@Service
public class ResponseExcusalServiceImpl implements ResponseExcusalService {

    private final JurorResponseRepository responseRepository;
    private final JurorResponseAuditRepository jurorResponseAuditRepository;
    private final PoolRepository detailsRepository;
    private final PartHistRepository historyRepository;
    private final ExcusalCodeRepository excusalCodeRepository;
    private final ExcusalLetterRepository excusalLetterRepository;
    private final ExcusalDeniedLetterRepository excusalDeniedLetterRepository;
    private final ResponseMergeService mergeService;
    private final EntityManager entityManager;
    private final AssignOnUpdateService assignOnUpdateService;

    @Autowired
    public ResponseExcusalServiceImpl(final JurorResponseRepository responseRepository,
                                      final JurorResponseAuditRepository jurorResponseAuditRepository,
                                      final PoolRepository detailsRepository,
                                      final PartHistRepository historyRepository,
                                      final ExcusalCodeRepository excusalCodeRepository,
                                      final ExcusalLetterRepository excusalLetterRepository,
                                      final ExcusalDeniedLetterRepository excusalDeniedLetterRepository,
                                      final ResponseMergeService mergeService,
                                      final EntityManager entityManager,
                                      final AssignOnUpdateService assignOnUpdateService) {
        Assert.notNull(responseRepository, "JurorResponseRepository cannot be null");
        Assert.notNull(jurorResponseAuditRepository, "JurorResponseAuditRepository cannot be null");
        Assert.notNull(detailsRepository, "PoolDetailsRepository cannot be null");
        Assert.notNull(historyRepository, "RetrieveHistoryRepository cannot be null");
        Assert.notNull(excusalCodeRepository, "ExcusalCodeRepository cannot be null");
        Assert.notNull(excusalLetterRepository, "ExcusalLetterRepository cannot be null");
        Assert.notNull(excusalDeniedLetterRepository, "ExcusalDeniedLetterRepository cannot be null");
        Assert.notNull(mergeService, "ResponseMergeService cannot be null");
        Assert.notNull(entityManager, "EntityManager cannot be null");
        Assert.notNull(assignOnUpdateService, "AssignOnUpdateService cannot be null");
        this.responseRepository = responseRepository;
        this.jurorResponseAuditRepository = jurorResponseAuditRepository;
        this.detailsRepository = detailsRepository;
        this.historyRepository = historyRepository;
        this.excusalCodeRepository = excusalCodeRepository;
        this.excusalLetterRepository = excusalLetterRepository;
        this.excusalDeniedLetterRepository = excusalDeniedLetterRepository;
        this.mergeService = mergeService;
        this.entityManager = entityManager;
        this.assignOnUpdateService = assignOnUpdateService;
    }

    @Override
    public List<ExcusalCodeDto> getExcusalReasons() throws ExcusalException.UnableToRetrieveExcusalCodeList {
        Iterable<ExcusalCodeEntity> excusalReasonsList = excusalCodeRepository.findAll();
        if (!excusalReasonsList.iterator().hasNext()) {
            throw new ExcusalException.UnableToRetrieveExcusalCodeList();
        }

        List<ResponseExcusalController.ExcusalCodeDto> myList = new ArrayList<>();
        for (ExcusalCodeEntity excusalCodeEntity : excusalReasonsList) {
            myList.add(new ResponseExcusalController.ExcusalCodeDto(excusalCodeEntity));
        }
        return myList;
    }

    @Transactional
    @Override
    public boolean excuseJuror(String jurorId, ExcusalCodeDto excusalCodeDto, String login) throws ExcusalException {
        if (!isValidExcusalCode(jurorId, excusalCodeDto.getExcusalCode())) {
            return false;
        }

        log.debug("Begin processing manual excusal of juror.");
        try {
            final JurorResponse savedResponse = responseRepository.findByJurorNumber(jurorId);
            if (savedResponse == null) {
                throw new ExcusalException.JurorNotFound(jurorId);
            }

            if (BooleanUtils.isTrue(savedResponse.getProcessingComplete())) {
                final String message = "Response " + savedResponse.getJurorNumber() + " has previously been merged!";
                log.error("Response {} has previously been completed at {}.",
                    savedResponse.getJurorNumber(), savedResponse.getCompletedAt()
                );
                throw new ExcusalException.ResponseAlreadyCompleted(message);
            }

            //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
            entityManager.detach(savedResponse);

            // set optimistic lock version from UI
            log.debug("Version: DB={}, UI={}", savedResponse.getVersion(), excusalCodeDto.getVersion());
            savedResponse.setVersion(excusalCodeDto.getVersion());

            //update response
            ProcessingStatus oldProcessingStatus = savedResponse.getProcessingStatus();
            savedResponse.setProcessingStatus(ProcessingStatus.CLOSED);

            // JDB-2685: if no staff assigned, assign current login
            if (null == savedResponse.getStaff()) {
                assignOnUpdateService.assignToCurrentLogin(savedResponse, login);
            }

            // save response
            try {
                log.debug("Merging juror response for juror {}", savedResponse.getJurorNumber());
                mergeService.mergeResponse(savedResponse, login);
                log.debug("Juror response for juror {} merged successfully", savedResponse.getJurorNumber());
            } catch (ObjectOptimisticLockingFailureException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Optimistic locking failure:", e);
                }
                throw new ExcusalException.OptimisticLockingFailure(jurorId, e);
            }

            //audit response status change
            jurorResponseAuditRepository.save(JurorResponseAudit.builder()
                .jurorNumber(jurorId)
                .login(login)
                .oldProcessingStatus(oldProcessingStatus)
                .newProcessingStatus(savedResponse.getProcessingStatus())
                .build());

            // update juror pool entry
            Pool poolDetails = detailsRepository.findByJurorNumber(savedResponse.getJurorNumber());
            poolDetails.setResponded(Pool.RESPONDED);
            poolDetails.setExcusalDate(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            poolDetails.setExcusalCode(excusalCodeDto.getExcusalCode());
            poolDetails.setUserEdtq(login);
            poolDetails.setStatus(IPoolStatus.EXCUSED);
            poolDetails.setHearingDate(null);
            detailsRepository.save(poolDetails);

            // audit pool
            PartHist history = new PartHist();
            history.setOwner("400");
            history.setJurorNumber(jurorId);
            history.setDatePart(Date.from(Instant.now()));
            history.setHistoryCode(THistoryCode.EXCUSE_POOL_MEMBER);
            history.setUserId(login);
            history.setPoolNumber(poolDetails.getPoolNumber());
            history.setInfo("Add Excuse - " + excusalCodeDto.getExcusalCode());
            historyRepository.save(history);

            if (!ExcusalCode.DECEASED.equalsIgnoreCase(excusalCodeDto.getExcusalCode())) {
                // only non-deceased jurors get a letter
                ExcusalLetter excusalLetter = new ExcusalLetter();
                excusalLetter.setJurorNumber(jurorId);
                excusalLetter.setExcusalCode(excusalCodeDto.getExcusalCode());
                excusalLetter.setDateExcused(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
                excusalLetterRepository.save(excusalLetter);
            }
        } catch (ExcusalException.JurorNotFound e) {
            log.debug("Error while attempting to excuse Juror {}: {}", jurorId, e.getMessage());
            throw e;
        } catch (TransactionSystemException e) {
            // this exception occurs when optimistic locking fails
            if (log.isDebugEnabled()) {
                log.debug("Error while attempting to excuse Juror {}: {}", jurorId, e);
            }
            throw new ExcusalException.OptimisticLockingFailure(jurorId, e);
        }
        log.info("Excused juror {} using code {}, by user {}", jurorId, excusalCodeDto.getExcusalCode(), login);
        return true;
    }

    @Transactional
    @Override
    public boolean rejectExcusalRequest(String jurorId, ExcusalCodeDto excusalCodeDto,
                                        String login) throws ExcusalException {
        if (!isValidExcusalCode(jurorId, excusalCodeDto.getExcusalCode())) {
            return false;
        }

        log.debug("Begin processing excusal-rejection of juror {} with code {}, by user {}.",
            jurorId, excusalCodeDto.getExcusalCode(), login
        );
        try {
            final JurorResponse savedResponse = responseRepository.findByJurorNumber(jurorId);
            if (savedResponse == null) {
                throw new ExcusalException.JurorNotFound(jurorId);
            }

            if (BooleanUtils.isTrue(savedResponse.getProcessingComplete())) {
                final String message = "Response " + savedResponse.getJurorNumber() + " has previously been merged!";
                log.error("Response {} has previously been completed at {}.",
                    savedResponse.getJurorNumber(), savedResponse.getCompletedAt()
                );
                throw new ExcusalException.ResponseAlreadyCompleted(message);
            }

            //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
            entityManager.detach(savedResponse);

            // set optimistic lock version from UI
            log.debug("Version: DB={}, UI={}", savedResponse.getVersion(), excusalCodeDto.getVersion());
            savedResponse.setVersion(excusalCodeDto.getVersion());

            //update response
            ProcessingStatus oldProcessingStatus = savedResponse.getProcessingStatus();
            savedResponse.setProcessingStatus(ProcessingStatus.CLOSED);

            // JDB-2685: if no staff assigned, assign current login
            if (null == savedResponse.getStaff()) {
                assignOnUpdateService.assignToCurrentLogin(savedResponse, login);
            }

            // save response
            try {
                log.debug("Merging juror response for juror {}", savedResponse.getJurorNumber());
                mergeService.mergeResponse(savedResponse, login);
                log.debug("Juror response for juror {} merged successfully", savedResponse.getJurorNumber());
            } catch (ObjectOptimisticLockingFailureException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Optimistic locking failure:", e);
                }
                throw new ExcusalException.OptimisticLockingFailure(jurorId, e);
            }

            //audit response status change
            jurorResponseAuditRepository.save(JurorResponseAudit.builder()
                .jurorNumber(jurorId)
                .login(login)
                .oldProcessingStatus(oldProcessingStatus)
                .newProcessingStatus(savedResponse.getProcessingStatus())
                .build());

            // update juror pool entry
            Pool poolDetails = detailsRepository.findByJurorNumber(savedResponse.getJurorNumber());
            poolDetails.setResponded(Pool.RESPONDED);
            poolDetails.setExcusalDate(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            poolDetails.setExcusalCode(excusalCodeDto.getExcusalCode());
            poolDetails.setUserEdtq(login);
            poolDetails.setExcusalRejected("Y");
            poolDetails.setStatus(IPoolStatus.RESPONDED);
            detailsRepository.save(poolDetails);

            // audit pool
            PartHist history = new PartHist();
            history.setOwner("400");
            history.setJurorNumber(jurorId);
            history.setDatePart(Date.from(Instant.now()));
            history.setHistoryCode(THistoryCode.EXCUSE_POOL_MEMBER);
            history.setUserId(login);
            history.setPoolNumber(poolDetails.getPoolNumber());
            history.setInfo("Refuse Excuse");
            historyRepository.save(history);

            // denied excusals require a second entry into the audit table
            history.setHistoryCode(THistoryCode.RESPONDED);
            history.setInfo(PartHist.RESPONDED);
            historyRepository.save(history);

            ExcusalDeniedLetter excusalDeniedLetter = new ExcusalDeniedLetter();
            excusalDeniedLetter.setJurorNumber(jurorId);
            excusalDeniedLetter.setExcusalCode(excusalCodeDto.getExcusalCode());
            excusalDeniedLetter.setDateExcused(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            excusalDeniedLetterRepository.save(excusalDeniedLetter);

        } catch (ExcusalException.JurorNotFound e) {
            log.debug("Error while attempting to reject excusal request from Juror {}: {}", jurorId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(
                "Error occurred while trying to reject excusal request from Juror {}: {}",
                jurorId,
                e.getMessage()
            );
            throw e;
        }
        log.info("Successfully rejected excusal-request for juror {} using code {}, by user {}",
            jurorId, excusalCodeDto.getExcusalCode(), login
        );
        return true;
    }

    private boolean isValidExcusalCode(String jurorId, String excusalCodeToCheck) throws ExcusalException {
        List<ResponseExcusalController.ExcusalCodeDto> excusalCodeDtos = getExcusalReasons();

        for (ResponseExcusalController.ExcusalCodeDto excusalCodeDto : excusalCodeDtos) {
            if (excusalCodeDto.getExcusalCode().equals(excusalCodeToCheck)) {
                return true;
            }
        }
        throw new ExcusalException.RequestedCodeNotValid(jurorId, excusalCodeToCheck);
    }
}
