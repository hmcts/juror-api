package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendment;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendmentRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of operations for updating the status of a Juror's response by a Bureau officer.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseStatusUpdateServiceImpl implements ResponseStatusUpdateService, ResponseMergeService {
    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;
    private final JurorResponseAuditRepositoryMod auditRepository;
    private final JurorPoolRepository jurorDetailsRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final PartAmendmentRepository partAmendmentRepository;
    private final JurorHistoryRepository partHistRepository;
    private final EntityManager entityManager;
    private final JurorPoolRepository jurorPoolRepository;
    private final JurorHistoryRepository historyRepository;
    private final AssignOnUpdateService assignOnUpdateService;
    private final JurorReasonableAdjustmentRepository specialNeedsRepository;
    private final WelshCourtLocationRepository welshCourtLocationRepository;


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

        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);

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
                JurorPool jurorDetails = jurorPoolRepository.findByJurorJurorNumber(jurorNumber);
                jurorDetails.getJuror().setResponded(true);
                jurorDetails.setUserEdtq(auditorUsername);
                jurorDetails.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED,
                    jurorStatusRepository));
                jurorPoolRepository.save(jurorDetails);

                // audit the pool changes
                final JurorHistory history = new JurorHistory();
                history.setJurorNumber(jurorNumber);
                history.setHistoryCode(HistoryCodeMod.RESPONDED_POSITIVELY);
                history.setCreatedBy(auditorUsername);
                history.setOtherInformation(JurorHistory.RESPONDED);
                history.setPoolNumber(jurorDetails.getPoolNumber());
                history.setOtherInformationDate(LocalDate.now());
                historyRepository.save(history);
            }
            log.info("Updated juror '{}' processing status to '{}'", jurorNumber, jurorResponse.getProcessingStatus());

            //audit response status change
            JurorResponseAuditMod responseAudit = auditRepository.save(JurorResponseAuditMod.builder()
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
    public void updateJurorResponseWithoutMerge(final DigitalResponse jurorResponse) {
        log.debug("Updating juror response without merging!");
        jurorResponseRepository.save(jurorResponse);
        log.debug("Updated juror response without merging.");
    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void mergeResponse(final DigitalResponse originalDetails, final String auditorUsername) {
        if (log.isTraceEnabled()) {
            log.trace("Merging response {}", originalDetails);
        }

        final JurorPool jurorDetails = jurorDetailsRepository.findByJurorJurorNumber(originalDetails.getJurorNumber());
        if (jurorDetails == null) {
            log.error("No pool entry found for Juror {}", originalDetails.getJurorNumber());
            throw new JurorPoolEntryNotFoundException("No pool entry found");
        }

        DigitalResponse updatedDetails = new DigitalResponse();
        BeanUtils.copyProperties(originalDetails, updatedDetails);


        // JDB-3132, RA update  legacy.
        List<JurorReasonableAdjustment> specialNeedsByJurorNumber =
            specialNeedsRepository.findByJurorNumber(originalDetails.getJurorNumber());

        log.debug(
            "found special needs for juror {}, no of needs {}",
            originalDetails.getJurorNumber(),
            specialNeedsByJurorNumber.size()
        );

        if (specialNeedsByJurorNumber != null) {
            //if multiple set need to M
            if (specialNeedsByJurorNumber.size() > 1) {
                jurorDetails.getJuror().setReasonableAdjustmentCode("M");
            } else if (specialNeedsByJurorNumber.size() == 1
                && specialNeedsByJurorNumber.get(0) != null) {
                jurorDetails.getJuror()
                    .setReasonableAdjustmentCode(specialNeedsByJurorNumber.get(0).getReasonableAdjustment().getCode());
            }

            log.debug("Merging special need information  for juror {}, Special need {}", jurorDetails.getJurorNumber(),
                //   poolDetails.getSpecialNeed()
                jurorDetails.getJuror().getReasonableAdjustmentMessage()
            );

            jurorDetailsRepository.save(jurorDetails);// save the updated pool table data

            log.debug("Merged special need information  for juror {}", jurorDetails.getJurorNumber());
        }

        if ("deceased".equalsIgnoreCase(originalDetails.getThirdPartyReason())) {
            log.info("Third party deceased flow.");

            applyThirdPartyRules(updatedDetails);
            applyPhoneNumberRules(updatedDetails);

            // flag the juror digital response as completed (cannot re-copy to pool)
            originalDetails.setProcessingComplete(Boolean.TRUE);
            originalDetails.setCompletedAt(LocalDateTime.now());

            jurorResponseRepository.save(originalDetails);// save the completed response to Juror Digital DB
            //poolDetailsRepository.save(poolDetails);// save the updated pool table data
            log.info("Merged third party deceased information for juror {}", jurorDetails.getJurorNumber());
            return;//no further processing!
        }

        // perform merge or changes
        // check for changes in original values
        boolean titleChanged = false;
        if (updatedDetails.getTitle() != null) {
            if (jurorDetails.getJuror().getTitle() != null) {
                titleChanged = updatedDetails.getTitle().compareTo(jurorDetails.getJuror().getTitle()) != 0;
            } else {
                titleChanged = true;
            }
        } else {
            // updated title CAN be null as a value
            titleChanged = true;
        }

        boolean firstNameChanged = false;
        if (updatedDetails.getFirstName() != null) {
            if (jurorDetails.getJuror().getFirstName() != null) {
                firstNameChanged = updatedDetails.getFirstName().compareTo(jurorDetails.getJuror().getFirstName()) != 0;
            } else {
                firstNameChanged = true;
            }
        }

        boolean lastNameChanged = false;
        if (updatedDetails.getLastName() != null) {
            if (jurorDetails.getJuror().getLastName() != null) {
                lastNameChanged = updatedDetails.getLastName().compareTo(jurorDetails.getJuror().getLastName()) != 0;
            } else {
                lastNameChanged = true;
            }
        }

        boolean dobChanged = false;
        if (updatedDetails.getDateOfBirth() != null) {
            if (jurorDetails.getJuror().getDateOfBirth() != null) {

                // compare dates

                String updatedDob = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(updatedDetails.getDateOfBirth());
                String oldDob =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd").format(jurorDetails.getJuror().getDateOfBirth());
                dobChanged = updatedDob.compareTo(oldDob) != 0;
            } else {
                dobChanged = true;
            }
        }

        boolean postcodeChanged = false;
        if (updatedDetails.getPostcode() != null) {
            if (jurorDetails.getJuror().getPostcode() != null) {
                postcodeChanged = updatedDetails.getPostcode().compareTo(jurorDetails.getJuror().getPostcode()) != 0;
            } else {
                postcodeChanged = true;
            }
        }

        String newAddress = ""
            + Objects.toString(updatedDetails.getAddressLine1(), "").trim()
            + Objects.toString(updatedDetails.getAddressLine2(), "").trim()
            + Objects.toString(updatedDetails.getAddressLine3(), "").trim()
            + Objects.toString(updatedDetails.getAddressLine4(), "").trim()
            + Objects.toString(updatedDetails.getAddressLine5(), "").trim();
        String oldAddress = ""
            + Objects.toString(jurorDetails.getJuror().getAddressLine1(), "").trim()
            + Objects.toString(jurorDetails.getJuror().getAddressLine2(), "").trim()
            + Objects.toString(jurorDetails.getJuror().getAddressLine3(), "").trim()
            + Objects.toString(jurorDetails.getJuror().getAddressLine4(), "").trim()
            + Objects.toString(jurorDetails.getJuror().getAddressLine5(), "").trim();
        boolean addressChanged = oldAddress.compareToIgnoreCase(newAddress) != 0;

        // copy over the juror details only if this has not been completed before
        if (updatedDetails.getProcessingComplete() != null
            && !updatedDetails.getProcessingComplete()) {

            PartAmendment allAmendments = new PartAmendment();

            // 1) copy the amendment values first!
            mapJurorDetailsToAllAmendments(jurorDetails, allAmendments);

            final String fullAddress = Stream.of(
                    "",
                    jurorDetails.getJuror().getAddressLine1(),
                    jurorDetails.getJuror().getAddressLine2(),
                    jurorDetails.getJuror().getAddressLine3(),
                    jurorDetails.getJuror().getAddressLine4(),
                    jurorDetails.getJuror().getAddressLine5()
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
                BeanUtils.copyProperties(updatedDetails, jurorDetails, "phoneNumber", "altPhoneNumber", "email",
                    "juror");
                mapDetailsToJuror(updatedDetails, jurorDetails.getJuror(), false, false);
            } else {
                BeanUtils.copyProperties(updatedDetails, jurorDetails, "juror");
                mapDetailsToJuror(updatedDetails, jurorDetails.getJuror(), true, true);
            }

            //JDB- 3053
            //If Welsh then Y, English - Null
            if (originalDetails.getWelsh() == Boolean.TRUE
                && welshCourtLocationRepository.findByLocCode(jurorDetails.getCourt().getLocCode()) != null) {
                jurorDetails.getJuror().setWelsh(Boolean.TRUE);
            } else {
                jurorDetails.getJuror().setWelsh(null);
            }
            // flag the juror digital response as completed (cannot re-copy to pool)
            originalDetails.setProcessingComplete(Boolean.TRUE);
            originalDetails.setCompletedAt(LocalDateTime.now());
            log.trace("Set processing complete");

            jurorResponseRepository.save(originalDetails);// save the completed response to Juror Digital DB
            jurorDetailsRepository.save(jurorDetails);// save the updated pool table data

            // 3) audit - copy the part hist entry items
            // title
            if (titleChanged) {
                JurorHistory titleHistory = new JurorHistory();
                BeanUtils.copyProperties(jurorDetails, titleHistory);
                titleHistory.setPoolNumber(jurorDetails.getPoolNumber());
                titleHistory.setHistoryCode(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
                titleHistory.setOtherInformation("Title Changed");
                titleHistory.setCreatedBy(auditorUsername);

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
                JurorHistory firstNameHistory = new JurorHistory();
                BeanUtils.copyProperties(jurorDetails, firstNameHistory);
                firstNameHistory.setPoolNumber(jurorDetails.getPoolNumber());
                firstNameHistory.setHistoryCode(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
                firstNameHistory.setOtherInformation("First Name Changed");
                firstNameHistory.setCreatedBy(auditorUsername);

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
                JurorHistory lastNameHistory = new JurorHistory();
                BeanUtils.copyProperties(jurorDetails, lastNameHistory);
                lastNameHistory.setPoolNumber(jurorDetails.getPoolNumber());
                lastNameHistory.setHistoryCode(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
                lastNameHistory.setOtherInformation("Last Name Changed");
                lastNameHistory.setCreatedBy(auditorUsername);

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
                JurorHistory dobHistory = new JurorHistory();
                BeanUtils.copyProperties(jurorDetails, dobHistory);
                dobHistory.setPoolNumber(jurorDetails.getPoolNumber());
                dobHistory.setHistoryCode(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
                dobHistory.setOtherInformation("DOB Changed");
                dobHistory.setCreatedBy(auditorUsername);

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
                JurorHistory addressHistory = new JurorHistory();
                BeanUtils.copyProperties(jurorDetails, addressHistory);
                addressHistory.setPoolNumber(jurorDetails.getPoolNumber());
                addressHistory.setHistoryCode(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
                addressHistory.setOtherInformation("Address Changed");
                addressHistory.setCreatedBy(auditorUsername);

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
                JurorHistory postcodeHistory = new JurorHistory();
                BeanUtils.copyProperties(jurorDetails, postcodeHistory);
                postcodeHistory.setPoolNumber(jurorDetails.getPoolNumber());
                postcodeHistory.setHistoryCode(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
                postcodeHistory.setOtherInformation("Postcode Changed");
                postcodeHistory.setCreatedBy(auditorUsername);

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

    private void mapJurorDetailsToAllAmendments(JurorPool jurorDetails, PartAmendment allAmendments) {
        BeanUtils.copyProperties(jurorDetails, allAmendments, "poolNumber");
        allAmendments.setPoolNumber(jurorDetails.getPoolNumber());
        Juror juror = jurorDetails.getJuror();
        allAmendments.setTitle(juror.getTitle());
        allAmendments.setFirstName(juror.getFirstName());
        allAmendments.setLastName(juror.getLastName());
        allAmendments.setDateOfBirth(juror.getDateOfBirth());
        allAmendments.setAddress(juror.getAddressLine1());
    }

    private void mapDetailsToJuror(DigitalResponse updatedDetails, Juror juror,
                                   boolean includePhone, boolean includeEmail) {
        juror.setTitle(updatedDetails.getTitle());
        juror.setFirstName(updatedDetails.getFirstName());
        juror.setLastName(updatedDetails.getLastName());
        juror.setAddressLine1(updatedDetails.getAddressLine1());
        juror.setAddressLine2(updatedDetails.getAddressLine2());
        juror.setAddressLine3(updatedDetails.getAddressLine3());
        juror.setAddressLine4(updatedDetails.getAddressLine4());
        juror.setAddressLine5(updatedDetails.getAddressLine5());
        juror.setPostcode(updatedDetails.getPostcode());
        juror.setDateOfBirth(updatedDetails.getDateOfBirth());
        if (includePhone) {
            juror.setPhoneNumber(updatedDetails.getPhoneNumber());
            juror.setAltPhoneNumber(updatedDetails.getAltPhoneNumber());
            juror.setWorkPhone(updatedDetails.getWorkPhone());
        }
        if (includeEmail) {
            juror.setEmail(updatedDetails.getEmail());
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
    private void applyThirdPartyRules(DigitalResponse response) {

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

    private void applyPhoneNumberRules(DigitalResponse jurorResponse) {

        String primaryPhone = jurorResponse.getPhoneNumber();
        String secondaryPhone = jurorResponse.getAltPhoneNumber();
        //If the main phone number starts with an 07 then it should be allocated to the mobile phone number
        if (isMobileNumber(primaryPhone)) {
            jurorResponse.setAltPhoneNumber(primaryPhone);
            jurorResponse.setWorkPhone(secondaryPhone);
            jurorResponse.setPhoneNumber(null);
        } else if (isMobileNumber(secondaryPhone)) {
            //If the main phone number does not start with an 07 but the Another one does then the Another phone will be
            //allocated to the mobile phone number
            jurorResponse.setPhoneNumber(primaryPhone);
            jurorResponse.setAltPhoneNumber(secondaryPhone);
            jurorResponse.setWorkPhone(null);
        } else {
            //If the main phone number has not been allocated to the mobile number
            // it should be allocated to the home number
            //If the Another phone has not been allocated to the mobile number it should be allocated to the Work number
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
