package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeed;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeedsRepository;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendment;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendmentRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.domain.THistoryCode;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of operations for updating the status of a Juror's response by a Bureau officer.
 */
@Service
@Slf4j
public class ResponseStatusUpdateServiceImpl implements ResponseStatusUpdateService, ResponseMergeService {
    private final JurorResponseRepository jurorResponseRepository;
    private final JurorResponseAuditRepository auditRepository;
    private final PoolRepository poolDetailsRepository;
    private final PartAmendmentRepository partAmendmentRepository;
    private final PartHistRepository partHistRepository;
    private final EntityManager entityManager;
    private final PoolRepository poolRepository;
    private final PartHistRepository historyRepository;
    private final AssignOnUpdateService assignOnUpdateService;
    private final BureauJurorSpecialNeedsRepository specialNeedsRepository;
    private final WelshCourtLocationRepository welshCourtLocationRepository;


    @Autowired
    public ResponseStatusUpdateServiceImpl(final JurorResponseRepository jurorResponseRepository,
                                           final JurorResponseAuditRepository auditRepository,
                                           final PoolRepository poolDetailsRepository,
                                           final PartAmendmentRepository partAmendmentRepository,
                                           final PartHistRepository partHistRepository,
                                           final EntityManager entityManager,
                                           final PoolRepository poolRepository,
                                           final PartHistRepository historyRepository,
                                           final AssignOnUpdateService assignOnUpdateService,
                                           final BureauJurorSpecialNeedsRepository specialNeedsRepository,
                                           final WelshCourtLocationRepository welshCourtLocationRepository) {
        Assert.notNull(jurorResponseRepository, "JurorResponseRepository cannot be null");
        Assert.notNull(auditRepository, "JurorResponseAuditRepository cannot be null");
        Assert.notNull(poolDetailsRepository, "PoolRepository cannot be null");
        Assert.notNull(partAmendmentRepository, "RetrieveAmendmentRepository cannot be null");
        Assert.notNull(partHistRepository, "RetrieveHistoryRepository cannot be null");
        Assert.notNull(entityManager, "EntityManager cannot be null");
        Assert.notNull(poolRepository, "PoolRepository cannot be null");
        Assert.notNull(historyRepository, "PartHistRepository cannot be null");
        Assert.notNull(assignOnUpdateService, "AssignOnUpdateService cannot be null");
        Assert.notNull(specialNeedsRepository, " cannot be null");
        Assert.notNull(welshCourtLocationRepository, " cannot be null");
        this.jurorResponseRepository = jurorResponseRepository;
        this.auditRepository = auditRepository;
        this.poolDetailsRepository = poolDetailsRepository;
        this.partAmendmentRepository = partAmendmentRepository;
        this.partHistRepository = partHistRepository;
        this.entityManager = entityManager;
        this.poolRepository = poolRepository;
        this.historyRepository = historyRepository;
        this.assignOnUpdateService = assignOnUpdateService;
        this.specialNeedsRepository = specialNeedsRepository;
        this.welshCourtLocationRepository = welshCourtLocationRepository;
    }

    /**
     * Update the processing status of a Juror response within Juror Digital.
     *
     * @param jurorNumber     The juror number of the response
     * @param status          The processing status to change to
     * @param version         Optimistic locking version of the record to update (maintained across requests by UI)
     * @param auditorUsername Username performing the update
     */
    @Override
    @Transactional
    public void updateJurorResponseStatus(final String jurorNumber, final ProcessingStatus status,
                                          final Integer version, final String auditorUsername) {

        JurorResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);

        log.debug("Updating status for juror {} to {}", jurorNumber, status.getDescription());
        if (jurorResponse != null) {
            //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
            entityManager.detach(jurorResponse);

            // set optimistic lock version from UI
            log.debug("Version: DB={}, UI={}", jurorResponse.getVersion(), version);
            jurorResponse.setVersion(version);

            final ProcessingStatus auditProcessingStatus = jurorResponse.getProcessingStatus();

            //update response PROCESSING status
            jurorResponse.setProcessingStatus(status);

            // JDB-2685: if no staff assigned, assign current login
            if (null == jurorResponse.getStaff()) {
                assignOnUpdateService.assignToCurrentLogin(jurorResponse, auditorUsername);
            }

            // merge the changes if required/allowed
            if (jurorResponse.getProcessingComplete()) {
                log.debug(
                    "Unable to update status for juror {} response as processing is already complete.",
                    jurorNumber
                );
                throw new ResponseAlreadyCompleted(jurorNumber);
            } else if (status != ProcessingStatus.CLOSED) {
                // we're not closing the response, so update without merging
                log.debug("Updating juror '{}' status to '{}' without merge.", jurorNumber, status);
                updateJurorResponseWithoutMerge(jurorResponse);
            } else {
                log.debug("Merging juror response for juror {}", jurorNumber);
                mergeResponse(jurorResponse, auditorUsername);

                // JDB-2487 AC16.6: Changing to a CLOSED processingStatus, so set required "Positive Response" values
                Pool poolDetails = poolRepository.findByJurorNumber(jurorNumber);
                poolDetails.setResponded(Pool.RESPONDED);
                poolDetails.setUserEdtq(auditorUsername);
                poolDetails.setStatus(IPoolStatus.RESPONDED);
                poolRepository.save(poolDetails);

                // audit the pool changes
                final PartHist history = new PartHist();
                history.setJurorNumber(jurorNumber);
                history.setOwner(JurorDigitalApplication.JUROR_OWNER);
                history.setHistoryCode(THistoryCode.RESPONDED);
                history.setUserId(auditorUsername);
                history.setInfo(PartHist.RESPONDED);
                history.setPoolNumber(poolDetails.getPoolNumber());
                history.setDatePart(Date.from(Instant.now()));
                historyRepository.save(history);
            }
            log.info("Updated juror '{}' processing status to '{}'", jurorNumber, jurorResponse.getProcessingStatus());

            //audit response status change
            JurorResponseAudit responseAudit = auditRepository.save(JurorResponseAudit.builder()
                .jurorNumber(jurorResponse.getJurorNumber())
                .login(auditorUsername)
                .oldProcessingStatus(auditProcessingStatus)
                .newProcessingStatus(jurorResponse.getProcessingStatus())
                .build());

            if (log.isTraceEnabled()) {
                log.trace("Audit entry: {}", responseAudit);
            }
        } else {
            log.error("No juror response found for juror number {}", jurorNumber);
            throw new JurorResponseNotFoundException("No juror response found");
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateJurorResponseWithoutMerge(final JurorResponse jurorResponse) {
        log.debug("Updating juror response without merging!");
        jurorResponseRepository.save(jurorResponse);
        log.debug("Updated juror response without merging.");
    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void mergeResponse(final JurorResponse originalDetails, final String auditorUsername) {
        if (log.isTraceEnabled()) {
            log.trace("Merging response {}", originalDetails);
        }

        final Pool poolDetails = poolDetailsRepository.findByJurorNumber(originalDetails.getJurorNumber());
        if (poolDetails == null) {
            log.error("No pool entry found for Juror {}", originalDetails.getJurorNumber());
            throw new JurorPoolEntryNotFoundException("No pool entry found");
        }

        JurorResponse updatedDetails = new JurorResponse();
        BeanUtils.copyProperties(originalDetails, updatedDetails);


        // JDB-3132, RA update  legacy.
        List<BureauJurorSpecialNeed> specialNeedsByJurorNumber =
            specialNeedsRepository.findByJurorNumber(originalDetails.getJurorNumber());

        log.debug(
            "found special needs for juror {}, no of needs {}",
            originalDetails.getJurorNumber(),
            specialNeedsByJurorNumber.size()
        );

        if (specialNeedsByJurorNumber != null) {
            //if multiple set need to M
            if (specialNeedsByJurorNumber.size() > 1) {
                poolDetails.setSpecialNeed("M");
            } else if (specialNeedsByJurorNumber.size() == 1
                && specialNeedsByJurorNumber.get(0) != null) {
                poolDetails.setSpecialNeed(specialNeedsByJurorNumber.get(0).getSpecialNeed().getCode());
            }

            log.debug("Merging special need information  for juror {}, Special need {}", poolDetails.getJurorNumber(),
                poolDetails.getSpecialNeed()
            );

            poolDetailsRepository.save(poolDetails);// save the updated pool table data

            log.debug("Merged special need information  for juror {}", poolDetails.getJurorNumber());
        }

        if ("deceased".equalsIgnoreCase(originalDetails.getThirdPartyReason())) {
            log.info("Third party deceased flow.");

            applyThirdPartyRules(updatedDetails);
            applyPhoneNumberRules(updatedDetails);

            // flag the juror digital response as completed (cannot re-copy to pool)
            originalDetails.setProcessingComplete(Boolean.TRUE);
            originalDetails.setCompletedAt(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));

            jurorResponseRepository.save(originalDetails);// save the completed response to Juror Digital DB
            //poolDetailsRepository.save(poolDetails);// save the updated pool table data
            log.info("Merged third party deceased information for juror {}", poolDetails.getJurorNumber());
            return;//no further processing!
        }

        // perform merge or changes
        // check for changes in original values
        boolean titleChanged = false;
        if (updatedDetails.getTitle() != null) {
            if (poolDetails.getTitle() != null) {
                titleChanged = updatedDetails.getTitle().compareTo(poolDetails.getTitle()) != 0;
            } else {
                titleChanged = true;
            }
        } else {
            // updated title CAN be null as a value
            titleChanged = true;
        }

        boolean firstNameChanged = false;
        if (updatedDetails.getFirstName() != null) {
            if (poolDetails.getFirstName() != null) {
                firstNameChanged = updatedDetails.getFirstName().compareTo(poolDetails.getFirstName()) != 0;
            } else {
                firstNameChanged = true;
            }
        }

        boolean lastNameChanged = false;
        if (updatedDetails.getLastName() != null) {
            if (poolDetails.getLastName() != null) {
                lastNameChanged = updatedDetails.getLastName().compareTo(poolDetails.getLastName()) != 0;
            } else {
                lastNameChanged = true;
            }
        }

        boolean dobChanged = false;
        if (updatedDetails.getDateOfBirth() != null) {
            if (poolDetails.getDateOfBirth() != null) {

                // compare dates
                String updatedDob = new SimpleDateFormat("yyyy-MM-dd").format(updatedDetails.getDateOfBirth());
                String oldDob = new SimpleDateFormat("yyyy-MM-dd").format(poolDetails.getDateOfBirth());
                dobChanged = updatedDob.compareTo(oldDob) != 0;
            } else {
                dobChanged = true;
            }
        }

        boolean postcodeChanged = false;
        if (updatedDetails.getPostcode() != null) {
            if (poolDetails.getPostcode() != null) {
                postcodeChanged = updatedDetails.getPostcode().compareTo(poolDetails.getPostcode()) != 0;
            } else {
                postcodeChanged = true;
            }
        }

        String newAddress = ""
            + Objects.toString(updatedDetails.getAddress(), "").trim()
            + Objects.toString(updatedDetails.getAddress2(), "").trim()
            + Objects.toString(updatedDetails.getAddress3(), "").trim()
            + Objects.toString(updatedDetails.getAddress4(), "").trim()
            + Objects.toString(updatedDetails.getAddress5(), "").trim();
        String oldAddress = ""
            + Objects.toString(poolDetails.getAddress(), "").trim()
            + Objects.toString(poolDetails.getAddress2(), "").trim()
            + Objects.toString(poolDetails.getAddress3(), "").trim()
            + Objects.toString(poolDetails.getAddress4(), "").trim()
            + Objects.toString(poolDetails.getAddress5(), "").trim();
        boolean addressChanged = oldAddress.compareToIgnoreCase(newAddress) != 0;

        // copy over the juror details only if this has not been completed before
        if (updatedDetails.getProcessingComplete() != null
            && !updatedDetails.getProcessingComplete()) {

            PartAmendment allAmendments = new PartAmendment();

            // 1) copy the amendment values first!
            BeanUtils.copyProperties(poolDetails, allAmendments);
            final String fullAddress = Stream.of(
                    "",
                    poolDetails.getAddress(),
                    poolDetails.getAddress2(),
                    poolDetails.getAddress3(),
                    poolDetails.getAddress4(),
                    poolDetails.getAddress5()
                )
                .filter(string -> string != null && !string.isEmpty())
                .map(","::concat)
                .collect(Collectors.joining())
                .replaceFirst(",", "");
            allAmendments.setAddress(fullAddress);
            allAmendments.setEditUserId(auditorUsername);
            allAmendments.setOwner(JurorDigitalApplication.JUROR_OWNER);

            applyThirdPartyRules(updatedDetails);

            applyPhoneNumberRules(updatedDetails);

            // copy the actual details to pool. Avoid coping 3rd party details.
            if (!ObjectUtils.isEmpty(updatedDetails.getThirdPartyReason())) {
                log.debug("Response is a third-party response");
                BeanUtils.copyProperties(updatedDetails, poolDetails, "phoneNumber", "altPhoneNumber", "email");
            } else {
                BeanUtils.copyProperties(updatedDetails, poolDetails);
            }

            //JDB- 3053
            //If Welsh then Y, English - Null
            if (originalDetails.getWelsh() == Boolean.TRUE
                && welshCourtLocationRepository.findByLocCode(poolDetails.getCourt().getLocCode()) != null) {
                poolDetails.setWelsh(Boolean.TRUE);
            } else {
                poolDetails.setWelsh(null);
            }

            // flag the juror digital response as completed (cannot re-copy to pool)
            originalDetails.setProcessingComplete(Boolean.TRUE);
            originalDetails.setCompletedAt(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
            log.trace("Set processing complete");

            jurorResponseRepository.save(originalDetails);// save the completed response to Juror Digital DB
            poolDetailsRepository.save(poolDetails);// save the updated pool table data

            // 3) audit - copy the part hist entry items
            // title
            if (titleChanged) {
                PartHist titleHistory = new PartHist();
                BeanUtils.copyProperties(poolDetails, titleHistory);
                titleHistory.setPoolNumber(poolDetails.getPoolNumber());
                titleHistory.setHistoryCode(THistoryCode.CHANGE_PERSON_DETAILS);
                titleHistory.setInfo("Title Changed");
                titleHistory.setUserId(auditorUsername);

                PartAmendment titleAmendments = new PartAmendment();
                BeanUtils.copyProperties(allAmendments, titleAmendments);
                if (titleAmendments.getTitle() == null) {
                    log.debug("Title amendment recording a old null, setting to space.");
                    titleAmendments.setTitle(" ");
                }
                titleAmendments.setFirstName(null);
                titleAmendments.setLastName(null);
                titleAmendments.setDateOfBirth(null);
                titleAmendments.setAddress(null);
                titleAmendments.setPostcode(null);

                partAmendmentRepository.save(titleAmendments);
                partHistRepository.save(titleHistory);
            }

            if (firstNameChanged) {
                PartHist firstNameHistory = new PartHist();
                BeanUtils.copyProperties(poolDetails, firstNameHistory);
                firstNameHistory.setPoolNumber(poolDetails.getPoolNumber());
                firstNameHistory.setHistoryCode(THistoryCode.CHANGE_PERSON_DETAILS);
                firstNameHistory.setInfo("First Name Changed");
                firstNameHistory.setUserId(auditorUsername);

                PartAmendment firstNameAmendments = new PartAmendment();
                BeanUtils.copyProperties(allAmendments, firstNameAmendments);
                firstNameAmendments.setTitle(null);
                firstNameAmendments.setLastName(null);
                firstNameAmendments.setDateOfBirth(null);
                firstNameAmendments.setAddress(null);
                firstNameAmendments.setPostcode(null);

                partAmendmentRepository.save(firstNameAmendments);
                partHistRepository.save(firstNameHistory);
            }

            if (lastNameChanged) {
                PartHist lastNameHistory = new PartHist();
                BeanUtils.copyProperties(poolDetails, lastNameHistory);
                lastNameHistory.setPoolNumber(poolDetails.getPoolNumber());
                lastNameHistory.setHistoryCode(THistoryCode.CHANGE_PERSON_DETAILS);
                lastNameHistory.setInfo("Last Name Changed");
                lastNameHistory.setUserId(auditorUsername);

                PartAmendment lastNameAmendments = new PartAmendment();
                BeanUtils.copyProperties(allAmendments, lastNameAmendments);
                lastNameAmendments.setTitle(null);
                lastNameAmendments.setFirstName(null);
                lastNameAmendments.setDateOfBirth(null);
                lastNameAmendments.setAddress(null);
                lastNameAmendments.setPostcode(null);

                partAmendmentRepository.save(lastNameAmendments);
                partHistRepository.save(lastNameHistory);
            }

            if (dobChanged) {
                PartHist dobHistory = new PartHist();
                BeanUtils.copyProperties(poolDetails, dobHistory);
                dobHistory.setPoolNumber(poolDetails.getPoolNumber());
                dobHistory.setHistoryCode(THistoryCode.CHANGE_PERSON_DETAILS);
                dobHistory.setInfo("DOB Changed");
                dobHistory.setUserId(auditorUsername);

                PartAmendment dobAmendments = new PartAmendment();
                BeanUtils.copyProperties(allAmendments, dobAmendments);
                dobAmendments.setTitle(null);
                dobAmendments.setFirstName(null);
                dobAmendments.setLastName(null);
                if (dobAmendments.getDateOfBirth() == null) {
                    log.debug("DOB amendment recording old null, setting to 01-01-1901");
                    dobAmendments.setDateOfBirth(LocalDate.of(1901, 1, 1));
                }
                dobAmendments.setAddress(null);
                dobAmendments.setPostcode(null);

                partAmendmentRepository.save(dobAmendments);
                partHistRepository.save(dobHistory);
            }

            if (addressChanged) {
                PartHist addressHistory = new PartHist();
                BeanUtils.copyProperties(poolDetails, addressHistory);
                addressHistory.setPoolNumber(poolDetails.getPoolNumber());
                addressHistory.setHistoryCode(THistoryCode.CHANGE_PERSON_DETAILS);
                addressHistory.setInfo("Address Changed");
                addressHistory.setUserId(auditorUsername);

                PartAmendment addressAmendments = new PartAmendment();
                BeanUtils.copyProperties(allAmendments, addressAmendments);
                addressAmendments.setTitle(null);
                addressAmendments.setFirstName(null);
                addressAmendments.setLastName(null);
                addressAmendments.setDateOfBirth(null);
                addressAmendments.setPostcode(null);

                partAmendmentRepository.save(addressAmendments);
                partHistRepository.save(addressHistory);
            }

            if (postcodeChanged) {
                PartHist postcodeHistory = new PartHist();
                BeanUtils.copyProperties(poolDetails, postcodeHistory);
                postcodeHistory.setPoolNumber(poolDetails.getPoolNumber());
                postcodeHistory.setHistoryCode(THistoryCode.CHANGE_PERSON_DETAILS);
                postcodeHistory.setInfo("Postcode Changed");
                postcodeHistory.setUserId(auditorUsername);

                PartAmendment postcodeAmendments = new PartAmendment();
                BeanUtils.copyProperties(allAmendments, postcodeAmendments);
                postcodeAmendments.setTitle(null);
                postcodeAmendments.setFirstName(null);
                postcodeAmendments.setLastName(null);
                postcodeAmendments.setDateOfBirth(null);
                postcodeAmendments.setAddress(null);

                partAmendmentRepository.save(postcodeAmendments);
                partHistRepository.save(postcodeHistory);
            }
        }
    }

    /**
     * Checks for the presence of third-party contact details.
     *
     * <p>If the relevant override flags are set, we should copy the third party phone number / email address into
     * the 'juror contact' fields so that these are what gets saved to the pool (which has no 'third party' fields)
     *
     * @param response the response object to process
     */
    private void applyThirdPartyRules(JurorResponse response) {

        //if this is a third party response put the correct phone numbers and email addresses in pool
        if (!ObjectUtils.isEmpty(response.getThirdPartyReason())) {
            log.debug("Response is a third-party response");
            if (!ObjectUtils.isEmpty(response.getJurorEmailDetails()) && response.getJurorEmailDetails()) {
                log.debug("Using juror email address");
                //juror email is already in the correct column
            } else {
                log.debug("Using third party email address");
                response.setEmail(response.getEmailAddress());// 3rd party email contact email
            }
            if (!ObjectUtils.isEmpty(response.getJurorPhoneDetails()) && response.getJurorPhoneDetails()) {
                log.debug("Using juror phone numbers");
                // juror phone is already in the correct column
            } else {
                log.debug("Using third party phone numbers");
                response.setPhoneNumber(response.getMainPhone());// 3rd party main phone
                response.setAltPhoneNumber(response.getOtherPhone());// 3rd party secondary phone
            }
        }
    }

    private void applyPhoneNumberRules(JurorResponse jurorResponse) {

        String primaryPhone = jurorResponse.getPhoneNumber();
        String secondaryPhone = jurorResponse.getAltPhoneNumber();

        /*
        If the main phone number starts with an 07 then it should be allocated to the mobile phone number
         */
        if (isMobileNumber(primaryPhone)) {
            jurorResponse.setAltPhoneNumber(primaryPhone);
            jurorResponse.setWorkPhone(secondaryPhone);
            jurorResponse.setPhoneNumber(null);
        }
        /*
        If the main phone number does not start with an 07 but the Another one does then the Another phone will be
         allocated to the mobile phone number
         */
        else if (isMobileNumber(secondaryPhone)) {
            jurorResponse.setPhoneNumber(primaryPhone);
            jurorResponse.setAltPhoneNumber(secondaryPhone);
            jurorResponse.setWorkPhone(null);
        }
        /*
        If the main phone number has not been allocated to the mobile number it should be allocated to the home number

        If the Another phone has not been allocated to the mobile number it should be allocated to the Work number
         */
        else {
            jurorResponse.setPhoneNumber(primaryPhone);
            jurorResponse.setWorkPhone(secondaryPhone);
            jurorResponse.setAltPhoneNumber(null);
        }

    }

    private boolean isMobileNumber(String number) {
        return !ObjectUtils.isEmpty(number) && number.startsWith("07");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class JurorPoolEntryNotFoundException extends RuntimeException {
        public JurorPoolEntryNotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ResponseAlreadyCompleted extends RuntimeException {
        public ResponseAlreadyCompleted(final String jurorId) {
            super(String.format("Unable to update status for Juror %s as Juror's response processing is already"
                + " completed, further changes should be made in Juror Legacy", jurorId));
        }
    }
}
