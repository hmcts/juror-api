package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController.ExcusalCodeDto;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCode;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.bureau.exception.ExcusalException;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalDeniedLetter;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalDeniedLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalLetter;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("Duplicates")
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseExcusalServiceImpl implements ResponseExcusalService {

    private final JurorDigitalResponseRepositoryMod responseRepository;

    private final JurorStatusRepository jurorStatusRepository;
    private final JurorResponseAuditRepositoryMod jurorResponseAuditRepository;
    private final JurorPoolRepository detailsRepository;
    private final JurorHistoryRepository historyRepository;
    private final ExcusalCodeRepository excusalCodeRepository;
    private final ExcusalLetterRepository excusalLetterRepository;
    private final ExcusalDeniedLetterRepository excusalDeniedLetterRepository;
    private final ResponseMergeService mergeService;
    private final EntityManager entityManager;
    private final AssignOnUpdateService assignOnUpdateService;


    @Override
    public List<ExcusalCodeDto> getExcusalReasons() throws ExcusalException.UnableToRetrieveExcusalCodeList {
        Iterable<uk.gov.hmcts.juror.api.moj.domain.ExcusalCode> excusalReasonsList = excusalCodeRepository.findAll();
        if (!excusalReasonsList.iterator().hasNext()) {
            throw new ExcusalException.UnableToRetrieveExcusalCodeList();
        }

        List<ResponseExcusalController.ExcusalCodeDto> myList = new ArrayList<>();
        excusalReasonsList.forEach(excusalCode -> {
            if (!excusalCode.isEnabled()) {
                return;
            }
            myList
                .add(new ResponseExcusalController.ExcusalCodeDto(excusalCode));
        });
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
            final DigitalResponse savedResponse = responseRepository.findByJurorNumber(jurorId);
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
                throw new ExcusalException.OptimisticLockingFailure(jurorId, e);
            }

            //audit response status change
            jurorResponseAuditRepository.save(JurorResponseAuditMod.builder()
                .jurorNumber(jurorId)
                .login(login)
                .oldProcessingStatus(oldProcessingStatus)
                .newProcessingStatus(savedResponse.getProcessingStatus())
                .build());

            // update juror pool entry
            JurorPool poolDetails = detailsRepository.findByJurorJurorNumber(savedResponse.getJurorNumber());
            poolDetails.getJuror().setResponded(true);
            poolDetails.getJuror().setExcusalDate(LocalDate.now());
            poolDetails.getJuror().setExcusalCode(excusalCodeDto.getExcusalCode());
            poolDetails.setUserEdtq(login);
            poolDetails.setStatus(
                RepositoryUtils.retrieveFromDatabase(IJurorStatus.EXCUSED, jurorStatusRepository));
            poolDetails.setNextDate(null);
            detailsRepository.save(poolDetails);

            // audit pool
            JurorHistory history = new JurorHistory();
            //  history.setOwner("400");
            history.setJurorNumber(jurorId);
            history.setOtherInformationDate(LocalDate.now());
            history.setHistoryCode(HistoryCodeMod.EXCUSE_POOL_MEMBER);
            history.setCreatedBy(login);
            history.setPoolNumber(poolDetails.getPoolNumber());
            history.setOtherInformation("Add Excuse - " + excusalCodeDto.getExcusalCode());
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
            final DigitalResponse savedResponse = responseRepository.findByJurorNumber(jurorId);
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
                throw new ExcusalException.OptimisticLockingFailure(jurorId, e);
            }

            //audit response status change
            jurorResponseAuditRepository.save(JurorResponseAuditMod.builder()
                .jurorNumber(jurorId)
                .login(login)
                .oldProcessingStatus(oldProcessingStatus)
                .newProcessingStatus(savedResponse.getProcessingStatus())
                .build());

            // update juror pool entry
            JurorPool poolDetails = detailsRepository.findByJurorJurorNumber(savedResponse.getJurorNumber());
            poolDetails.getJuror().setResponded(true);
            poolDetails.getJuror().setExcusalDate(LocalDate.now());
            poolDetails.getJuror().setExcusalCode(excusalCodeDto.getExcusalCode());
            poolDetails.setUserEdtq(login);
            poolDetails.getJuror().setExcusalRejected("Y");
            poolDetails.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository));
            detailsRepository.save(poolDetails);

            // audit pool
            JurorHistory history = new JurorHistory();
            //  history.setOwner("400");
            history.setJurorNumber(jurorId);
            history.setOtherInformationDate(LocalDate.now());
            history.setHistoryCode(HistoryCodeMod.EXCUSE_POOL_MEMBER);
            history.setCreatedBy(login);
            history.setPoolNumber(poolDetails.getPoolNumber());
            history.setOtherInformation("Refuse Excuse");
            historyRepository.save(history);
            history = new JurorHistory();
            //  history.setOwner("400");
            history.setJurorNumber(jurorId);
            history.setOtherInformationDate(LocalDate.now());
            history.setCreatedBy(login);
            history.setPoolNumber(poolDetails.getPoolNumber());
            // denied excusals require a second entry into the audit table
            history.setHistoryCode(HistoryCodeMod.RESPONDED_POSITIVELY);
            history.setOtherInformation(JurorHistory.RESPONDED);
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
