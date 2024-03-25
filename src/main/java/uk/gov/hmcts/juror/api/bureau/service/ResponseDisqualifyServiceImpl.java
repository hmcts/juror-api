package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController.DisqualifyCodeDto;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.exception.DisqualifyException;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.DisqualifiedCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.letter.DisqualificationLetterMod;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.DisqualifiedCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.DisqualifyLetterModRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseDisqualifyServiceImpl implements ResponseDisqualifyService {

    private final JurorDigitalResponseRepositoryMod responseRepository;
    private final JurorResponseAuditRepositoryMod jurorResponseAuditRepository;
    private final JurorPoolRepository detailsRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorHistoryRepository historyRepository;
    private final DisqualifiedCodeRepository disqualifyCodeRepository;
    private final DisqualifyLetterModRepository disqualificationLetterRepository;
    private final ResponseMergeService mergeService;
    private final EntityManager entityManager;
    private final AssignOnUpdateService assignOnUpdateService;


    @Override
    public List<ResponseDisqualifyController.DisqualifyCodeDto> getDisqualifyReasons()
        throws DisqualifyException.UnableToRetrieveDisqualifyCodeList {
        Iterable<DisqualifiedCode> disqualifyReasonsList = disqualifyCodeRepository.findAll();
        if (!disqualifyReasonsList.iterator().hasNext()) {
            throw new DisqualifyException.UnableToRetrieveDisqualifyCodeList();
        }

        List<ResponseDisqualifyController.DisqualifyCodeDto> myList = new ArrayList<>();
        for (DisqualifiedCode disqualifyCodeEntity : disqualifyReasonsList) {
            myList.add(new ResponseDisqualifyController.DisqualifyCodeDto(disqualifyCodeEntity));
        }
        return myList;
    }

    @Transactional
    @Override
    public boolean disqualifyJuror(String jurorId, DisqualifyCodeDto disqualifyCodeDto,
                                   String login) throws DisqualifyException {
        if (!isValidDisqualifyCode(jurorId, disqualifyCodeDto.getDisqualifyCode())) {
            return false;
        }

        log.debug("Begin processing manual disqualification of juror.");
        try {
            final DigitalResponse savedResponse = responseRepository.findByJurorNumber(jurorId);
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
            final ProcessingStatus oldProcessingStatus = savedResponse.getProcessingStatus();
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
            jurorResponseAuditRepository.save(JurorResponseAuditMod.builder()
                .jurorNumber(jurorId)
                .login(login)
                .oldProcessingStatus(oldProcessingStatus)
                .newProcessingStatus(savedResponse.getProcessingStatus())
                .build());

            // update juror pool entry
            JurorPool jurorDetails = detailsRepository.findByJurorJurorNumber(savedResponse.getJurorNumber());
            jurorDetails.getJuror().setResponded(true);
            jurorDetails.getJuror().setDisqualifyDate(LocalDate.now());
            jurorDetails.getJuror().setDisqualifyCode(disqualifyCodeDto.getDisqualifyCode());
            jurorDetails.setUserEdtq(login);
            jurorDetails.setStatus(
                RepositoryUtils.retrieveFromDatabase(IJurorStatus.DISQUALIFIED, jurorStatusRepository));
            jurorDetails.setNextDate(null);
            detailsRepository.save(jurorDetails);

            // audit pool
            JurorHistory history = new JurorHistory();
            //history.setOwner("400");
            history.setJurorNumber(jurorId);
            history.setDateCreated(LocalDateTime.now());
            history.setHistoryCode(HistoryCodeMod.DISQUALIFY_POOL_MEMBER);
            history.setCreatedBy(login);
            history.setPoolNumber(jurorDetails.getPoolNumber());
            // Age disqualifications require a different OTHER_INFORMATION entry
            if (DisCode.AGE.equalsIgnoreCase(disqualifyCodeDto.getDisqualifyCode())) {
                // this is an age disqualification
                history.setOtherInformation("Disqualify Code A");
            } else {
                history.setOtherInformation("Code " + disqualifyCodeDto.getDisqualifyCode());
            }
            historyRepository.save(history);

            // Age disqualifications require a second PART_HIST entry
            if (DisCode.AGE.equalsIgnoreCase(disqualifyCodeDto.getDisqualifyCode())) {
                // this is an age disqualification
                history = new JurorHistory();
                history.setJurorNumber(jurorId);
                history.setDateCreated(LocalDateTime.now());
                history.setCreatedBy(login);
                history.setPoolNumber(jurorDetails.getPoolNumber());
                history.setHistoryCode(HistoryCodeMod.WITHDRAWAL_LETTER);
                history.setOtherInformation("Disqualify Letter Code A");
                historyRepository.save(history);
            }

            // disq_lett table entry
            DisqualificationLetterMod disqualificationLetter = new DisqualificationLetterMod();
            disqualificationLetter.setOwner(jurorDetails.getOwner());
            disqualificationLetter.setJurorNumber(jurorId);
            disqualificationLetter.setDisqCode(disqualifyCodeDto.getDisqualifyCode());
            disqualificationLetter.setDateDisq(LocalDate.now());
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
