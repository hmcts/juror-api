package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController.DisqualifyCodeDto;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.domain.DisqualifyCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.DisqualifyCodeRepository;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.domain.THistoryCode;
import uk.gov.hmcts.juror.api.bureau.exception.DisqualifyException;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetter;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetterRepository;
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

@Slf4j
@Service
public class ResponseDisqualifyServiceImpl implements ResponseDisqualifyService {

    private final JurorResponseRepository responseRepository;
    private final JurorResponseAuditRepository jurorResponseAuditRepository;
    private final PoolRepository detailsRepository;
    private final PartHistRepository historyRepository;
    private final DisqualifyCodeRepository disqualifyCodeRepository;
    private final DisqualificationLetterRepository disqualificationLetterRepository;
    private final ResponseMergeService mergeService;
    private final EntityManager entityManager;
    private final AssignOnUpdateService assignOnUpdateService;

    @Autowired
    public ResponseDisqualifyServiceImpl(final JurorResponseRepository responseRepository,
                                         final JurorResponseAuditRepository jurorResponseAuditRepository,
                                         final PoolRepository detailsRepository,
                                         final PartHistRepository historyRepository,
                                         final DisqualifyCodeRepository disqualifyCodeRepository,
                                         final DisqualificationLetterRepository disqualificationLetterRepository,
                                         final ResponseMergeService mergeService,
                                         final EntityManager entityManager,
                                         final AssignOnUpdateService assignOnUpdateService) {
        Assert.notNull(responseRepository, "JurorResponseRepository cannot be null");
        Assert.notNull(jurorResponseAuditRepository, "JurorResponseAuditRepository cannot be null");
        Assert.notNull(detailsRepository, "PoolDetailsRepository cannot be null");
        Assert.notNull(historyRepository, "RetrieveHistoryRepository cannot be null");
        Assert.notNull(disqualifyCodeRepository, "DisqualifyCodeRepository cannot be null");
        Assert.notNull(disqualificationLetterRepository, "DisqualificationLetterRepository cannot be null");
        Assert.notNull(mergeService, "ResponseMergeService cannot be null");
        Assert.notNull(entityManager, "EntityManager cannot be null");
        Assert.notNull(assignOnUpdateService, "AssignOnUpdateService cannot be null");
        this.responseRepository = responseRepository;
        this.jurorResponseAuditRepository = jurorResponseAuditRepository;
        this.detailsRepository = detailsRepository;
        this.historyRepository = historyRepository;
        this.disqualifyCodeRepository = disqualifyCodeRepository;
        this.disqualificationLetterRepository = disqualificationLetterRepository;
        this.mergeService = mergeService;
        this.entityManager = entityManager;
        this.assignOnUpdateService = assignOnUpdateService;
    }

    public List<ResponseDisqualifyController.DisqualifyCodeDto> getDisqualifyReasons() throws DisqualifyException.UnableToRetrieveDisqualifyCodeList {
        Iterable<DisqualifyCodeEntity> disqualifyReasonsList = disqualifyCodeRepository.findAll();
        if (!disqualifyReasonsList.iterator().hasNext()) {
            throw new DisqualifyException.UnableToRetrieveDisqualifyCodeList();
        }

        List<ResponseDisqualifyController.DisqualifyCodeDto> myList = new ArrayList<>();
        for (DisqualifyCodeEntity disqualifyCodeEntity : disqualifyReasonsList) {
            myList.add(new ResponseDisqualifyController.DisqualifyCodeDto(disqualifyCodeEntity));
        }
        return myList;
    }

    @Transactional
    public boolean disqualifyJuror(String jurorId, DisqualifyCodeDto disqualifyCodeDto,
                                   String login) throws DisqualifyException {
        if (!isValidDisqualifyCode(jurorId, disqualifyCodeDto.getDisqualifyCode())) {
            return false;
        }

        log.debug("Begin processing manual disqualification of juror.");
        try {
            final JurorResponse savedResponse = responseRepository.findByJurorNumber(jurorId);
            if (savedResponse == null) {
                throw new DisqualifyException.JurorNotFound(jurorId);
            }

            if (BooleanUtils.isTrue(savedResponse.getProcessingComplete())) {
                final String message = "Response " + savedResponse.getJurorNumber() + " has previously been merged!";
                log.error("Response {} has previously been completed at {}.", savedResponse.getJurorNumber(),
                    savedResponse.getCompletedAt()
                );
                throw new DisqualifyException.ResponseAlreadyCompleted(message);
            }

            //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
            entityManager.detach(savedResponse);

            // set optimistic lock version from UI
            log.debug("Version: DB={}, UI={}", savedResponse.getVersion(), disqualifyCodeDto.getVersion());
            savedResponse.setVersion(disqualifyCodeDto.getVersion());

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
                throw new DisqualifyException.OptimisticLockingFailure(jurorId);
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
            poolDetails.setDisqualifyDate(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            poolDetails.setDisqualifyCode(disqualifyCodeDto.getDisqualifyCode());
            poolDetails.setUserEdtq(login);
            poolDetails.setStatus(IPoolStatus.DISQUALIFIED);
            poolDetails.setHearingDate(null);
            detailsRepository.save(poolDetails);

            // audit pool
            PartHist history = new PartHist();
            history.setOwner("400");
            history.setJurorNumber(jurorId);
            history.setDatePart(Date.from(Instant.now()));
            history.setHistoryCode("PDIS");
            history.setUserId(login);
            history.setPoolNumber(poolDetails.getPoolNumber());
            // Age disqualifications require a different OTHER_INFORMATION entry
            if (DisCode.AGE.equalsIgnoreCase(disqualifyCodeDto.getDisqualifyCode())) {
                // this is an age disqualification
                history.setInfo("Disqualify Code A");
            } else {
                history.setInfo("Code " + disqualifyCodeDto.getDisqualifyCode());
            }
            historyRepository.save(history);

            // Age disqualifications require a second PART_HIST entry
            if (DisCode.AGE.equalsIgnoreCase(disqualifyCodeDto.getDisqualifyCode())) {
                // this is an age disqualification
                history.setHistoryCode(THistoryCode.DISQUALIFY_RESPONSE);
                history.setInfo("Disqualify Letter Code A");
                historyRepository.save(history);
            }

            // disq_lett table entry
            DisqualificationLetter disqualificationLetter = new DisqualificationLetter();
            disqualificationLetter.setJurorNumber(jurorId);
            disqualificationLetter.setDisqCode(disqualifyCodeDto.getDisqualifyCode());
            disqualificationLetter.setDateDisq(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            disqualificationLetterRepository.save(disqualificationLetter);
        } catch (DisqualifyException.JurorNotFound e) {
            log.debug("Error while attempting to disqualify Juror {}: {}", jurorId, e.getMessage());
            throw e;
        }
        log.info("Disqualified juror {} using code {}, by user {}", jurorId, disqualifyCodeDto, login);
        return true;
    }

    private boolean isValidDisqualifyCode(String jurorId, String disqualifyCodeToCheck) throws DisqualifyException {
        List<ResponseDisqualifyController.DisqualifyCodeDto> disqualifyCodeDtos = getDisqualifyReasons();

        for (ResponseDisqualifyController.DisqualifyCodeDto disqualifyCodeDto : disqualifyCodeDtos) {
            if (disqualifyCodeDto.getDisqualifyCode().equals(disqualifyCodeToCheck)) {
                return true;
            }
        }
        throw new DisqualifyException.RequestedCodeNotValid(jurorId, disqualifyCodeToCheck);
    }
}
