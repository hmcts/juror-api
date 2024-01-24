package uk.gov.hmcts.juror.api.bureau.service;

import io.jsonwebtoken.lang.Assert;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
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
import uk.gov.hmcts.juror.api.bureau.domain.DeferDBF;
import uk.gov.hmcts.juror.api.bureau.domain.DeferDBFRepository;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.domain.THistoryCode;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@Slf4j
public class ResponseDeferralServiceImpl implements ResponseDeferralService {
    public static final String DEFER_DBF_CHECKED_VALUE = null;// null is an intentional value
    private final PoolRepository poolRepository;
    private final ResponseMergeService responseMergeService;
    private final JurorResponseRepository jurorResponseRepository;
    private final EntityManager entityManager;
    private final JurorResponseAuditRepository auditRepository;
    private final DeferDBFRepository deferDBFRepository;
    private final PartHistRepository partHistRepository;
    private final DefLettRepository defLettRepository;
    private final DefDeniedRepository defDeniedRepository;
    private final ExcusalCodeRepository excusalCodeRepository;
    private final AssignOnUpdateService assignOnUpdateService;

    @Autowired
    public ResponseDeferralServiceImpl(final PoolRepository poolRepository,
                                       final ResponseMergeService responseMergeService,
                                       final JurorResponseRepository jurorResponseRepository,
                                       final EntityManager entityManager,
                                       final JurorResponseAuditRepository auditRepository,
                                       final DeferDBFRepository deferDBFRepository,
                                       final PartHistRepository partHistRepository,
                                       final DefLettRepository defLettRepository,
                                       final DefDeniedRepository defDeniedRepository,
                                       final ExcusalCodeRepository excusalCodeRepository,
                                       final AssignOnUpdateService assignOnUpdateService) {
        Assert.notNull(poolRepository, "PoolRepository cannot be null.");
        Assert.notNull(responseMergeService, "ResponseMergeService cannot be null.");
        Assert.notNull(jurorResponseRepository, "JurorResponseRepository cannot be null.");
        Assert.notNull(entityManager, "EntityManager cannot be null.");
        Assert.notNull(auditRepository, "JurorResponseAuditRepository cannot be null.");
        Assert.notNull(deferDBFRepository, "DeferDBFRepository cannot be null.");
        Assert.notNull(partHistRepository, "PartHistRepository cannot be null.");
        Assert.notNull(defLettRepository, "DefLettRepository cannot be null.");
        Assert.notNull(defDeniedRepository, "DefDeniedRepository cannot be null.");
        Assert.notNull(excusalCodeRepository, "ExcusalCodeRepository cannot be null.");
        Assert.notNull(assignOnUpdateService, "AssignOnUpdateService cannot be null");
        this.poolRepository = poolRepository;
        this.responseMergeService = responseMergeService;
        this.jurorResponseRepository = jurorResponseRepository;
        this.entityManager = entityManager;
        this.auditRepository = auditRepository;
        this.deferDBFRepository = deferDBFRepository;
        this.partHistRepository = partHistRepository;
        this.defLettRepository = defLettRepository;
        this.defDeniedRepository = defDeniedRepository;
        this.excusalCodeRepository = excusalCodeRepository;
        this.assignOnUpdateService = assignOnUpdateService;
    }

    @Override
    @Transactional
    public void processDeferralDecision(final String jurorNumber, final String auditorUsername,
                                        final ResponseDeferralController.DeferralDto deferralDto) {
        isValidExcusalCode(deferralDto.getDeferralReason());

        final JurorResponse deferResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
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
            final Pool pool = poolRepository.findByJurorNumber(jurorNumber);
            // validate credentials.
            if (pool == null) {
                log.info("Could not find juror using juror number {}", jurorNumber);
                throw new PoolNotFoundForJurorException("Juror not found");
            }

            if (deferralDto.getAcceptDeferral()) {
                // do acceptance
                final Date deferTo = deferralDto.getDeferralDate();
                final Date courtDate = pool.getHearingDate();
                final Date oneYearFromCourtDate = Date.from(ZonedDateTime.ofInstant(
                        courtDate.toInstant(),
                        ZoneId.systemDefault()
                    )
                    .plus(6, ChronoUnit.HOURS)
                    .truncatedTo(ChronoUnit.DAYS)
                    .plus(12, ChronoUnit.MONTHS)
                    .plus(1, ChronoUnit.DAYS)
                    .toInstant());
                if (log.isTraceEnabled()) {
                    log.trace("One year from now = {}", oneYearFromCourtDate);
                }
                // validate deferral date is within 12 months
                if (deferTo != null && !deferTo.before(oneYearFromCourtDate)) {
                    log.warn("Cannot defer juror {} until {} as it is not within the next 12 months of {}", jurorNumber,
                        deferTo, oneYearFromCourtDate
                    );
                    throw new DeferralDateInvalidException(
                        "Deferral date not within the next twelve months of the court date");
                }

                pool.setStatus(IPoolStatus.DEFERRED);
                pool.setResponded("Y");
                pool.setDeferralDate(deferTo);
                pool.setExcusalDate(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(
                    ChronoUnit.DAYS)));
                pool.setExcusalCode(deferralDto.getDeferralReason());
                pool.setUserEdtq(auditorUsername);
                pool.setNoDefPos(1L);
                pool.setHearingDate(null);//this should map to POOL.NEXT_DATE


                poolRepository.save(pool);

                // Insert into defer_dbf
                deferDBFRepository.save(DeferDBF.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .partNo(deferResponse.getJurorNumber())
                    .deferTo(deferTo)
                    .checked(DEFER_DBF_CHECKED_VALUE)
                    .locCode(pool.getCourt() != null
                        ?
                        pool.getCourt().getLocCode()
                        :
                            null)
                    .build());
                log.warn("POOL {} LOC_CODE was null!", deferResponse.getJurorNumber());

                // audit log the responded status
                partHistRepository.save(PartHist.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .historyCode(THistoryCode.RESPONDED)
                    .jurorNumber(deferResponse.getJurorNumber())
                    .datePart(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .userId(auditorUsername)
                    .info(PartHist.RESPONDED)
                    .poolNumber(pool.getPoolNumber())
                    .build());
                // audit log the deferral status
                partHistRepository.save(PartHist.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .historyCode(THistoryCode.DEFERRED)
                    .jurorNumber(deferResponse.getJurorNumber())
                    .datePart(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .userId(auditorUsername)
                    .info("Add defer - " + pool.getExcusalCode())
                    .poolNumber(pool.getPoolNumber())
                    .build());

                // setup for deferral letter printing
                defLettRepository.save(DefLett.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .partNo(deferResponse.getJurorNumber())
                    .dateDef(pool.getDeferralDate())
                    .excusalCode(pool.getExcusalCode())
                    .build());
            } else {
                // do denial
                pool.setStatus(IPoolStatus.RESPONDED);
                pool.setResponded("Y");
                pool.setExcusalCode(deferralDto.getDeferralReason());
                pool.setUserEdtq(auditorUsername);
                pool.setExcusalRejected("Z");
                pool.setDeferralDate(null);
                pool.setExcusalDate(null);
                poolRepository.save(pool);

                // audit log the responded status
                partHistRepository.save(PartHist.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .historyCode(THistoryCode.RESPONDED)
                    .jurorNumber(deferResponse.getJurorNumber())
                    .datePart(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .userId(auditorUsername)
                    .info(PartHist.RESPONDED)
                    .poolNumber(pool.getPoolNumber())
                    .build());
                // audit log the deferral DENIED status
                partHistRepository.save(PartHist.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .historyCode(THistoryCode.DEFERRED)
                    .jurorNumber(deferResponse.getJurorNumber())
                    .datePart(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .userId(auditorUsername)
                    .info("Deferral Denied - " + pool.getExcusalCode())
                    .poolNumber(pool.getPoolNumber())
                    .build());

                // deferral denial entry
                defDeniedRepository.save(DefDenied.builder()
                    .owner(JurorDigitalApplication.JUROR_OWNER)
                    .partNo(deferResponse.getJurorNumber())
                    .dateDef(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .excusalCode(pool.getExcusalCode())
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
            final JurorResponseAudit responseAudit = auditRepository.save(JurorResponseAudit.builder()
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
