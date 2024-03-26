package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDeferralController;
import uk.gov.hmcts.juror.api.bureau.domain.DefDenied;
import uk.gov.hmcts.juror.api.bureau.domain.DefDeniedRepository;
import uk.gov.hmcts.juror.api.bureau.domain.DefLett;
import uk.gov.hmcts.juror.api.bureau.domain.DefLettRepository;
import uk.gov.hmcts.juror.api.bureau.domain.DeferDbf;
import uk.gov.hmcts.juror.api.bureau.domain.DeferDbfRepository;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseDeferralServiceImpl implements ResponseDeferralService {
    private final JurorStatusRepository jurorStatusRepository;
    public static final String DEFER_DBF_CHECKED_VALUE = null;// null is an intentional value
    private final JurorPoolRepository poolRepository;
    private final ResponseMergeService responseMergeService;

    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;
    private final EntityManager entityManager;
    private final JurorResponseAuditRepositoryMod auditRepository;
    private final DeferDbfRepository deferDbfRepository;
    private final JurorHistoryRepository partHistRepository;
    private final DefLettRepository defLettRepository;
    private final DefDeniedRepository defDeniedRepository;
    private final ExcusalCodeRepository excusalCodeRepository;
    private final AssignOnUpdateService assignOnUpdateService;

    @Override
    @Transactional
    public void processDeferralDecision(final String jurorNumber, final String auditorUsername,
                                        final ResponseDeferralController.DeferralDto deferralDto) {
        isValidExcusalCode(deferralDto.getDeferralReason());

        final DigitalResponse deferResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        log.debug("Processing manual deferral of juror {} with {}", jurorNumber, deferralDto);
        if (deferResponse != null) {
            if (BooleanUtils.isTrue(deferResponse.getProcessingComplete())) {
                final String message = "Response " + deferResponse.getJurorNumber() + " has previously been merged!";
                log.error("Response {} has previously been completed at {}.", deferResponse.getJurorNumber(),
                    deferResponse.getCompletedAt()
                );
                throw new JurorResponseAlreadyCompletedException(message);
            }

            //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
            entityManager.detach(deferResponse);

            // set optimistic lock version from UI
            log.debug("Version: DB={}, UI={}", deferResponse.getVersion(), deferralDto.getVersion());
            deferResponse.setVersion(deferralDto.getVersion());

            // original status value before processing the deferral for audit purposes
            final ProcessingStatus auditStatus = deferResponse.getProcessingStatus();

            // attempt to defer
            final JurorPool pool = poolRepository.findByJurorJurorNumber(jurorNumber);
            // validate credentials.
            if (pool == null) {
                log.info("Could not find juror using juror number {}", jurorNumber);
                throw new PoolNotFoundForJurorException("Juror not found");
            }

            if (deferralDto.getAcceptDeferral()) {
                // do acceptance
                final LocalDate deferTo = deferralDto.getDeferralDate();
                final LocalDate courtDate = pool.getNextDate();
                final LocalDate oneYearFromCourtDate = LocalDate.now().plusMonths(12).plusDays(1);
                if (log.isTraceEnabled()) {
                    log.trace("One year from now = {}", oneYearFromCourtDate);
                }
                // validate deferral date is within 12 months
                if (deferTo != null && !deferTo.isBefore(oneYearFromCourtDate)) {
                    log.warn("Cannot defer juror {} until {} as it is not within the next 12 months of {}", jurorNumber,
                        deferTo, oneYearFromCourtDate
                    );
                    throw new DeferralDateInvalidException(
                        "Deferral date not within the next twelve months of the court date");
                }

                pool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.DEFERRED, jurorStatusRepository));
                pool.getJuror().setResponded(true);
                pool.setDeferralDate(deferTo);
                pool.getJuror()
                    .setExcusalDate(LocalDate.now());
                pool.getJuror().setExcusalCode(deferralDto.getDeferralReason());
                pool.setUserEdtq(auditorUsername);
                pool.getJuror().setNoDefPos(1);
                pool.setNextDate(null);//this should map to POOL.NEXT_DATE


                poolRepository.save(pool);

                // Insert into defer_dbf
                deferDbfRepository.save(DeferDbf.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .partNo(deferResponse.getJurorNumber())
                    .deferTo(deferTo)
                    .checked(DEFER_DBF_CHECKED_VALUE)
                    .locCode(pool.getCourt() != null ? pool.getCourt().getLocCode() : null)
                    .build());
                log.warn("POOL {} LOC_CODE was null!", deferResponse.getJurorNumber());

                // audit log the responded status
                partHistRepository.save(JurorHistory.builder()
                    //    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .historyCode(HistoryCodeMod.RESPONDED_POSITIVELY)
                    .jurorNumber(deferResponse.getJurorNumber())
                    .dateCreated(LocalDateTime.now())
                    .createdBy(auditorUsername)
                    .otherInformation(JurorHistory.RESPONDED)
                    .poolNumber(pool.getPoolNumber())
                    .build());
                // audit log the deferral status
                partHistRepository.save(JurorHistory.builder()
                    //.owner(JurorDigitalApplication.JUROR_OWNER)
                    .historyCode(HistoryCodeMod.DEFERRED_POOL_MEMBER)
                    .jurorNumber(deferResponse.getJurorNumber())
                    .dateCreated(LocalDateTime.now())
                    .createdBy(auditorUsername)
                    .otherInformation("Add defer - " + pool.getJuror().getExcusalCode())
                    .poolNumber(pool.getPoolNumber())
                    .build());

                // setup for deferral letter printing
                defLettRepository.save(DefLett.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .partNo(deferResponse.getJurorNumber())
                    .dateDef(pool.getDeferralDate())
                    .excusalCode(pool.getJuror().getExcusalCode())
                    .build());
            } else {
                // do denial
                pool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository));
                pool.getJuror().setResponded(true);
                pool.getJuror().setExcusalCode(deferralDto.getDeferralReason());
                pool.setUserEdtq(auditorUsername);
                pool.getJuror().setExcusalRejected("Z");
                pool.setDeferralDate(null);
                pool.getJuror().setExcusalDate(null);
                poolRepository.save(pool);

                // audit log the responded status
                partHistRepository.save(JurorHistory.builder()
                    //    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .historyCode(HistoryCodeMod.RESPONDED_POSITIVELY)
                    .jurorNumber(deferResponse.getJurorNumber())
                    .dateCreated(LocalDateTime.now())
                    .createdBy(auditorUsername)
                    .otherInformation(JurorHistory.RESPONDED)
                    .poolNumber(pool.getPoolNumber())
                    .build());
                // audit log the deferral DENIED status
                partHistRepository.save(JurorHistory.builder()
                    //    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .historyCode(HistoryCodeMod.DEFERRED_POOL_MEMBER)
                    .jurorNumber(deferResponse.getJurorNumber())
                    .dateCreated(LocalDateTime.now())
                    .createdBy(auditorUsername)
                    .otherInformation("Deferral Denied - " + pool.getJuror().getExcusalCode())
                    .poolNumber(pool.getPoolNumber())
                    .build());

                // deferral denial entry
                defDeniedRepository.save(DefDenied.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .partNo(deferResponse.getJurorNumber())
                    .dateDef(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .excusalCode(pool.getJuror().getExcusalCode())
                    .datePrinted(null)
                    .printed(null)
                    .build());
            }

            //update response PROCESSING status
            deferResponse.setProcessingStatus(ProcessingStatus.CLOSED);

            // JDB-2685: if no staff assigned, assign current login
            if (null == deferResponse.getStaff()) {
                assignOnUpdateService.assignToCurrentLogin(deferResponse, auditorUsername);
            }

            // do the copy back of the response to juror legacy
            log.debug("Merging juror response for juror {}", jurorNumber);
            try {
                responseMergeService.mergeResponse(deferResponse, auditorUsername);
            } catch (OptimisticLockingFailureException olfe) {
                log.warn("Juror {} response was updated by another user!", jurorNumber);
                throw new BureauOptimisticLockingException(olfe);
            }

            log.info("Updated juror '{}' processing status to '{}'", jurorNumber, deferResponse.getProcessingStatus());

            //audit response status change
            final JurorResponseAuditMod responseAudit = auditRepository.save(JurorResponseAuditMod.builder()
                .jurorNumber(deferResponse.getJurorNumber())
                .login(auditorUsername)
                .oldProcessingStatus(auditStatus)
                .newProcessingStatus(deferResponse.getProcessingStatus())
                .build());

            if (log.isTraceEnabled()) {
                log.trace("Audit entry: {}", responseAudit);
            }
        } else {
            log.error("No juror response found for juror number {}", jurorNumber);
            throw new JurorResponseNotFoundException("No juror response found");
        }
    }

    private boolean isValidExcusalCode(String excusalCode) throws ValidationException {
        if (excusalCodeRepository.findById(excusalCode).isPresent()) {
            log.debug("Excusal code {} was found in the DB and is valid", excusalCode);
            return true;
        }
        throw new InvalidExcusalCodeException("Invalid deferral reason");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public class DeferralDateInvalidException extends RuntimeException {
        public DeferralDateInvalidException() {
            super();
        }

        DeferralDateInvalidException(String message) {
            super(message);
            log.info("Inside DeferralDateInvalidException .....");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidExcusalCodeException extends RuntimeException {
        InvalidExcusalCodeException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class PoolNotFoundForJurorException extends RuntimeException {
        PoolNotFoundForJurorException(String message) {
            super(message);
        }
    }
}
