package uk.gov.hmcts.juror.api.moj.service;

import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.CjsEmploymentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EligibilityDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReasonableAdjustmentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReplyTypeDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.SignatureDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactLogListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorPaperResponseDetailDto;
import uk.gov.hmcts.juror.api.moj.domain.CjsEmploymentType;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.SummonsSnapshot;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.enumeration.jurorresponse.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.juror.api.moj.exception.JurorPaperResponseException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.SummonsSnapshotRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorResponseUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Juror Paper Response service.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorPaperResponseServiceImpl implements JurorPaperResponseService {

    private static final String INVALID_CJS_EMPLOYMENT_ERROR_MESSAGE = "Invalid CJS Employment supplied for Juror %s";
    private static final String INVALID_SPECIAL_NEED_ERROR_MESSAGE = "Invalid special need supplied for Juror %s";
    static final String RESPONSE_UPDATED_LOG = "Paper response for Juror %s will be updated with new value for %s";
    private final JurorPaperResponseRepositoryMod paperResponseRepository;
    private final JurorResponseCjsEmploymentRepositoryMod jurorResponseCjsEmploymentRepository;
    private final JurorReasonableAdjustmentRepository reasonableAdjustmentsRepository;
    private final UserRepository userRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final SummonsSnapshotRepository summonsSnapshotRepository;
    private final WelshCourtLocationRepository welshCourtLocationRepository;
    private final StraightThroughProcessorService straightThroughProcessorService;
    private final JurorRepository jurorRepository;
    private final JurorPoolService jurorPoolService;

    @Override
    @Transactional
    public JurorPaperResponseDetailDto getJurorPaperResponse(final String jurorNumber, BureauJwtPayload payload) {
        log.info(String.format("Retrieving Juror paper response for juror number %s, by user %s", jurorNumber,
            payload.getLogin()));
        String owner = payload.getOwner();

        // check the user has access to the record
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber, owner);
        JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, owner);

        JurorPaperResponseDetailDto responseDto = copyPaperResponseRecordIntoDto(
            DataUtils.getJurorPaperResponse(jurorNumber, paperResponseRepository), jurorPool);

        // set the current owner.  Need to ensure the current owner is returned as the owner can change if, for
        // example, the juror is transferred to a different pool
        JurorResponseUtils.updateCurrentOwnerInResponseDto(jurorPoolRepository, responseDto);

        return responseDto;
    }

    private PaperResponse getJurorPaperResponse(final String jurorNumber) {
        PaperResponse jurorPaperResponse = paperResponseRepository.findByJurorNumber(jurorNumber);
        if (jurorPaperResponse == null) {
            log.error(String.format("Cannot find paper response for Juror %s", jurorNumber));
            throw new JurorPaperResponseException.JurorPaperResponseDoesNotExist(jurorNumber);
        }
        return jurorPaperResponse;
    }

    private JurorPaperResponseDetailDto copyPaperResponseRecordIntoDto(PaperResponse jurorPaperResponse,
                                                                       JurorPool jurorPool) {
        JurorPaperResponseDetailDto jurorPaperResponseDetailDto = new JurorPaperResponseDetailDto();

        jurorPaperResponseDetailDto.setJurorNumber(jurorPaperResponse.getJurorNumber());
        jurorPaperResponseDetailDto.setDateReceived(jurorPaperResponse.getDateReceived().toLocalDate());
        jurorPaperResponseDetailDto.setCurrentOwner(jurorPool.getOwner());

        Juror juror = jurorPool.getJuror();

        SummonsSnapshot summonsSnapshot = summonsSnapshotRepository.findById(juror.getJurorNumber())
            .orElse(null);

        if (summonsSnapshot != null) {
            jurorPaperResponseDetailDto.setPoolNumber(summonsSnapshot.getPoolNumber());
            jurorPaperResponseDetailDto.setCourtName(summonsSnapshot.getCourtLocationName());
            jurorPaperResponseDetailDto.setServiceStartDate(summonsSnapshot.getServiceStartDate());
            jurorPaperResponseDetailDto.setWelshCourt(CourtLocationUtils.isWelshCourtLocation(
                welshCourtLocationRepository,
                summonsSnapshot.getCourtLocationCode()
            ));
        } else {
            // Read required values from the Juror record
            CourtLocation courtLocation = jurorPool.getCourt();
            jurorPaperResponseDetailDto.setCourtName(courtLocation.getName());
            jurorPaperResponseDetailDto.setPoolNumber(jurorPool.getPoolNumber());
            jurorPaperResponseDetailDto.setServiceStartDate(jurorPool.getReturnDate());
            jurorPaperResponseDetailDto.setWelshCourt(CourtLocationUtils.isWelshCourtLocation(
                welshCourtLocationRepository, courtLocation.getLocCode()));
        }

        jurorPaperResponseDetailDto.setJurorStatus(jurorPool.getStatus().getStatusDesc());

        // copy optics reference
        jurorPaperResponseDetailDto.setOpticReference(juror.getOpticRef());

        // copy personal details
        jurorPaperResponseDetailDto.setTitle(jurorPaperResponse.getTitle());
        jurorPaperResponseDetailDto.setFirstName(jurorPaperResponse.getFirstName());
        jurorPaperResponseDetailDto.setLastName(jurorPaperResponse.getLastName());
        jurorPaperResponseDetailDto.setDateOfBirth(jurorPaperResponse.getDateOfBirth());

        // copy contact details
        jurorPaperResponseDetailDto.setPrimaryPhone(jurorPaperResponse.getPhoneNumber());
        jurorPaperResponseDetailDto.setSecondaryPhone(jurorPaperResponse.getAltPhoneNumber());
        jurorPaperResponseDetailDto.setEmailAddress(jurorPaperResponse.getEmail());

        if (jurorPaperResponse.getStaff() != null) {
            // set the assignee if there is one
            try {
                jurorPaperResponseDetailDto.setAssignedStaffMember(new UserDetailsDto(jurorPaperResponse.getStaff()));
            } catch (Exception e) {
                log.error("Error setting assigned staff member for response for juror {}",
                    jurorPaperResponse.getJurorNumber() + " --  " + e.getMessage());
            }

        }

        // set the contact log
        List<ContactLogListDto.ContactLogDataDto> contactLogList = new ArrayList<>();
        for (ContactLog contactLog : jurorPaperResponse.getContactLog()) {
            contactLogList.add(new ContactLogListDto.ContactLogDataDto(contactLog));
        }
        jurorPaperResponseDetailDto.setContactLog(contactLogList);

        // set the juror notes
        jurorPaperResponseDetailDto.setNotes(juror.getNotes());

        // copy third party
        jurorPaperResponseDetailDto.setThirdParty(new JurorPaperResponseDetailDto.ThirdParty(
            jurorPaperResponse.getThirdPartyFName(),jurorPaperResponse.getThirdPartyLName(),
            jurorPaperResponse.getMainPhone(),jurorPaperResponse.getEmail(),
            jurorPaperResponse.getThirdPartyOtherReason(),jurorPaperResponse.getOtherPhone(),
            jurorPaperResponse.getRelationship(), jurorPaperResponse.getThirdPartyReason(),
            jurorPaperResponse.getJurorPhoneDetails(), jurorPaperResponse.getJurorEmailDetails()
        ));


        // copy address details
        copyAddressToDto(jurorPaperResponse, jurorPaperResponseDetailDto);

        // copy eligibility
        copyEligibilityToDto(jurorPaperResponse, jurorPaperResponseDetailDto);

        // copy CJS Employment
        copyCjsEmploymentToDto(jurorPaperResponse, jurorPaperResponseDetailDto);

        // copy reasonable adjustments
        copyReasonableAdjustmentsToDto(jurorPaperResponse, jurorPaperResponseDetailDto);

        // copy reply type
        jurorPaperResponseDetailDto.setDeferral(jurorPaperResponse.getDeferral());
        jurorPaperResponseDetailDto.setExcusal(jurorPaperResponse.getExcusal());

        jurorPaperResponseDetailDto.setExcusalReason(juror.getExcusalCode());

        jurorPaperResponseDetailDto.setSigned(jurorPaperResponse.getSigned());
        jurorPaperResponseDetailDto.setProcessingStatus(jurorPaperResponse.getProcessingStatus().getDescription());
        jurorPaperResponseDetailDto.setWelsh(jurorPaperResponse.getWelsh());

        jurorPaperResponseDetailDto.setCompletedAt(jurorPaperResponse.getCompletedAt());

        // copy the existing name and address values from Juror record
        copyExistingJurorDetails(jurorPaperResponseDetailDto, juror);

        return jurorPaperResponseDetailDto;
    }

    private void copyExistingJurorDetails(JurorPaperResponseDetailDto jurorPaperResponseDetailDto,
                                          Juror juror) {

        jurorPaperResponseDetailDto.setExistingTitle(juror.getTitle());
        jurorPaperResponseDetailDto.setExistingFirstName(juror.getFirstName());
        jurorPaperResponseDetailDto.setExistingLastName(juror.getLastName());

        jurorPaperResponseDetailDto.setExistingAddressLineOne(juror.getAddressLine1());
        jurorPaperResponseDetailDto.setExistingAddressLineTwo(juror.getAddressLine2());
        jurorPaperResponseDetailDto.setExistingAddressLineThree(juror.getAddressLine3());
        jurorPaperResponseDetailDto.setExistingAddressTown(juror.getAddressLine4());
        jurorPaperResponseDetailDto.setExistingAddressCounty(juror.getAddressLine5());
        jurorPaperResponseDetailDto.setExistingAddressPostcode(juror.getPostcode());
    }

    private void copyReasonableAdjustmentsToDto(PaperResponse jurorPaperResponse,
                                                JurorPaperResponseDetailDto jurorPaperResponseDetailDto) {

        List<JurorReasonableAdjustment> jurorPaperResponseSpecialNeedList
            = jurorPaperResponse.getReasonableAdjustments();

        if (jurorPaperResponseSpecialNeedList.isEmpty()) {
            return;
        }

        List<JurorPaperResponseDetailDto.ReasonableAdjustment> reasonableAdjustmentsList = new ArrayList<>();
        jurorPaperResponseSpecialNeedList.forEach(specialNeed ->
            reasonableAdjustmentsList.add(new JurorPaperResponseDetailDto.ReasonableAdjustment(
                specialNeed.getReasonableAdjustment().getCode(),
                specialNeed.getReasonableAdjustmentDetail()
            )));
        jurorPaperResponseDetailDto.setReasonableAdjustments(reasonableAdjustmentsList);
    }

    private void copyCjsEmploymentToDto(PaperResponse jurorPaperResponse,
                                        JurorPaperResponseDetailDto jurorPaperResponseDetailDto) {

        List<JurorResponseCjsEmployment> jurorPaperResponseCjsList = jurorPaperResponse.getCjsEmployments();

        if (jurorPaperResponseCjsList.isEmpty()) {
            return;
        }

        List<JurorPaperResponseDetailDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();
        jurorPaperResponseCjsList.forEach(cjs -> cjsEmploymentList.add(new JurorPaperResponseDetailDto.CjsEmployment(
            cjs.getCjsEmployer(), cjs.getCjsEmployerDetails())));
        jurorPaperResponseDetailDto.setCjsEmployment(cjsEmploymentList);
    }

    private void copyEligibilityToDto(PaperResponse jurorPaperResponse,
                                      JurorPaperResponseDetailDto jurorPaperResponseDetailDto) {
        JurorPaperResponseDetailDto.Eligibility eligibility = new JurorPaperResponseDetailDto.Eligibility(
            jurorPaperResponse.getResidency(),
            jurorPaperResponse.getMentalHealthAct(),
            jurorPaperResponse.getMentalHealthCapacity(),
            jurorPaperResponse.getBail(),
            jurorPaperResponse.getConvictions()
        );
        jurorPaperResponseDetailDto.setEligibility(eligibility);
    }

    private void copyAddressToDto(PaperResponse jurorPaperResponse,
                                  JurorPaperResponseDetailDto jurorPaperResponseDetailDto) {
        jurorPaperResponseDetailDto.setAddressLineOne(jurorPaperResponse.getAddressLine1());
        jurorPaperResponseDetailDto.setAddressLineTwo(jurorPaperResponse.getAddressLine2());
        jurorPaperResponseDetailDto.setAddressLineThree(jurorPaperResponse.getAddressLine3());
        jurorPaperResponseDetailDto.setAddressTown(jurorPaperResponse.getAddressLine4());
        jurorPaperResponseDetailDto.setAddressCounty(jurorPaperResponse.getAddressLine5());
        jurorPaperResponseDetailDto.setAddressPostcode(jurorPaperResponse.getPostcode());
    }

    private void checkWriteAccessForCurrentUser(String jurorNumber, String owner) {
        List<JurorPool> jurorPools = getJurorPools(jurorNumber);

        JurorPool jurorPool = jurorPools.get(0);
        checkAccessForCurrentUser(jurorPool, owner);
    }

    private List<JurorPool> getJurorPools(String jurorNumber) {
        List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);
        if (jurorPools.isEmpty()) {
            // throw an exception as pool member record not found for juror
            throw new MojException.NotFound(String.format("Unable to find any juror pool associations for Juror "
                + "Number %s", jurorNumber), null);
        }
        return jurorPools;
    }

    private void checkAccessForCurrentUser(JurorPool jurorPool, String owner) {
        if (!jurorPool.getOwner().equals(owner)) {
            throw new MojException.Forbidden(String.format("Current user does not own the juror pool association for "
                + "Juror Number: %s and Pool Number: %s", jurorPool.getJurorNumber(), jurorPool.getPoolNumber()),
                null);
        }
    }

    @Override
    @Transactional
    public void saveResponse(BureauJwtPayload payload, JurorPaperResponseDto paperResponseDto) {

        final String jurorNumber = paperResponseDto.getJurorNumber();

        log.info(String.format("Saving paper response for Juror %s, by user %s", jurorNumber, payload.getLogin()));

        // Check if the current user has access to the Juror record (and also that the record exists)
        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        //check if Juror Paper response already exists - then send back error
        PaperResponse jurorPaperResponse = paperResponseRepository.findByJurorNumber(jurorNumber);
        if (jurorPaperResponse != null) {
            log.error(String.format("Paper response for Juror %s already exists", jurorNumber));
            throw new JurorPaperResponseException.JurorPaperResponseAlreadyExists(jurorNumber);
        }

        jurorPaperResponse = createJurorPaperResponseEntity(paperResponseDto, jurorPool);

        User staff = userRepository.findByUsername(payload.getLogin());
        jurorPaperResponse.setStaff(staff);

        jurorPaperResponse = updateWelshFlagBasedOnResponse(jurorPaperResponse, jurorPool);

        paperResponseRepository.save(jurorPaperResponse);

        Juror juror = jurorPool.getJuror();
        juror.setResponseEntered(true);
        jurorRepository.save(juror);

        log.info(String.format("Saved paper response for Juror %s", jurorNumber));

        jurorResponseCjsEmploymentRepository.saveAll(jurorPaperResponse.getCjsEmployments());
        log.info(String.format("Saved CJS employment for Juror %s", jurorNumber));
        reasonableAdjustmentsRepository.saveAll(jurorPaperResponse.getReasonableAdjustments());
        log.info(String.format("Saved Reasonable adjustments for Juror %s", jurorNumber));

        processStraightThroughResponse(jurorPaperResponse, jurorPool, jurorPool.getReturnDate(), payload);
    }

    private PaperResponse updateWelshFlagBasedOnResponse(PaperResponse jurorPaperResponse, JurorPool jurorPool) {
        Juror juror = jurorPool.getJuror();
        juror.setWelsh(jurorPaperResponse.getWelsh());
        jurorRepository.save(juror);

        jurorPaperResponse.setWelsh(jurorPool.getJuror().getWelsh());
        return jurorPaperResponse;
    }

    private PaperResponse createJurorPaperResponseEntity(JurorPaperResponseDto paperResponseDto, JurorPool jurorPool) {
        PaperResponse jurorPaperResponse = new PaperResponse();

        jurorPaperResponse.setJurorNumber(paperResponseDto.getJurorNumber());

        // setting the received date to now
        jurorPaperResponse.setDateReceived(LocalDateTime.now());

        // set up Juror personal details
        jurorPaperResponse.setTitle(paperResponseDto.getTitle());
        jurorPaperResponse.setFirstName(paperResponseDto.getFirstName());
        jurorPaperResponse.setLastName(paperResponseDto.getLastName());
        jurorPaperResponse.setDateOfBirth(paperResponseDto.getDateOfBirth());

        // set up Juror address
        setUpAddress(jurorPaperResponse, paperResponseDto);

        // setup Juror contact details
        jurorPaperResponse.setEmail(paperResponseDto.getEmailAddress());
        jurorPaperResponse.setPhoneNumber(paperResponseDto.getPrimaryPhone());
        jurorPaperResponse.setAltPhoneNumber(paperResponseDto.getSecondaryPhone());
        jurorPaperResponse.setWelsh(paperResponseDto.getWelsh());

        // set up third party details
        JurorPaperResponseDto.ThirdParty thirdParty = paperResponseDto.getThirdParty();
        if (thirdParty != null) {
            jurorPaperResponse.setThirdPartyFName(thirdParty.getThirdPartyFName());
            jurorPaperResponse.setThirdPartyLName(thirdParty.getThirdPartyLName());
            jurorPaperResponse.setMainPhone(thirdParty.getMainPhone());
            jurorPaperResponse.setEmailAddress(thirdParty.getEmailAddress());
            jurorPaperResponse.setThirdPartyOtherReason(thirdParty.getThirdPartyOtherReason());
            jurorPaperResponse.setRelationship(thirdParty.getRelationship());
            jurorPaperResponse.setThirdPartyReason(thirdParty.getThirdPartyReason());
        }

        // set up eligibility criteria
        JurorPaperResponseDto.Eligibility eligibility = paperResponseDto.getEligibility();
        if (eligibility != null) {
            setUpEligibility(jurorPaperResponse, eligibility);
        }

        jurorPaperResponse.setExcusal(paperResponseDto.getExcusal());
        jurorPaperResponse.setDeferral(paperResponseDto.getDeferral());

        // Set up CJS employment details
        // Need to check the same CJS employment is not entered more than once
        setUpCjsEmployment(jurorPaperResponse, paperResponseDto);

        // Set up reasonable adjustments details
        // Need to check the same reasonable adjustments is not entered more than once
        setUpReasonableAdjustments(jurorPaperResponse, paperResponseDto);

        jurorPaperResponse.setSigned(paperResponseDto.getSigned());

        return jurorPaperResponse;

    }


    private void setUpAddress(PaperResponse jurorPaperResponse,
                              JurorPaperResponseDto paperResponseDto) {
        jurorPaperResponse.setAddressLine1(paperResponseDto.getAddressLineOne());
        jurorPaperResponse.setAddressLine2(paperResponseDto.getAddressLineTwo());
        jurorPaperResponse.setAddressLine3(paperResponseDto.getAddressLineThree());
        jurorPaperResponse.setAddressLine4(paperResponseDto.getAddressTown());
        jurorPaperResponse.setAddressLine5(paperResponseDto.getAddressCounty());
        jurorPaperResponse.setPostcode(paperResponseDto.getAddressPostcode());
    }

    private void setUpEligibility(PaperResponse jurorPaperResponse,
                                  JurorPaperResponseDto.Eligibility eligibility) {
        jurorPaperResponse.setResidency(eligibility.getLivedConsecutive());
        jurorPaperResponse.setMentalHealthAct(eligibility.getMentalHealthAct());
        jurorPaperResponse.setMentalHealthCapacity(eligibility.getMentalHealthCapacity());
        jurorPaperResponse.setConvictions(eligibility.getConvicted());
        jurorPaperResponse.setBail(eligibility.getOnBail());
    }

    private void setUpCjsEmployment(PaperResponse jurorPaperResponse,
                                    JurorPaperResponseDto jurorPaperResponseDto) {

        List<JurorPaperResponseDto.CjsEmployment> cjsEmployment = jurorPaperResponseDto.getCjsEmployment();
        List<JurorResponseCjsEmployment> cjsEmployments = new ArrayList<>();
        final String jurorNumber = jurorPaperResponseDto.getJurorNumber();

        // List to ensure same employment isn't added more than once
        List<String> addedCjsEmployment = new ArrayList<>();

        if (cjsEmployment != null && !cjsEmployment.isEmpty()) {

            final List<String> cjsTypes = getCjsTypes();

            cjsEmployment.forEach(cjsEmp -> {
                JurorResponseCjsEmployment jurorPaperResponseCjsList = new JurorResponseCjsEmployment();
                jurorPaperResponseCjsList.setJurorNumber(jurorNumber);

                String employer = cjsEmp.getCjsEmployer();

                if (cjsTypes.contains(employer) && !addedCjsEmployment.contains(employer)) {
                    jurorPaperResponseCjsList.setCjsEmployer(cjsEmp.getCjsEmployer());
                    addedCjsEmployment.add(employer);
                    log.debug(String.format("Adding CJS employer, %s, for Juror %s", employer, jurorNumber));
                } else {
                    log.error(String.format(INVALID_CJS_EMPLOYMENT_ERROR_MESSAGE, jurorNumber));
                    throw new JurorPaperResponseException.InvalidCjsEmploymentEntry();
                }

                jurorPaperResponseCjsList.setCjsEmployerDetails(cjsEmp.getCjsEmployerDetails());
                cjsEmployments.add(jurorPaperResponseCjsList);
            });

            jurorPaperResponse.setCjsEmployments(cjsEmployments);
        }
    }

    private static List<String> getCjsTypes() {
        List<String> cjsTypes = new ArrayList<>();
        Arrays.stream(CjsEmploymentType.values()).forEach(s -> cjsTypes.add(s.getEmployer()));
        return cjsTypes;
    }

    private void setUpReasonableAdjustments(PaperResponse jurorPaperResponse,
                                            JurorPaperResponseDto jurorPaperResponseDto) {

        List<JurorPaperResponseDto.ReasonableAdjustment> reasonableAdjustments
            = jurorPaperResponseDto.getReasonableAdjustments();
        List<JurorReasonableAdjustment> reasonableAdjustmentsToAdd = new ArrayList<>();
        final String jurorNumber = jurorPaperResponseDto.getJurorNumber();
        // list to ensure same special need isn't stored more than once
        List<String> addedReasonableAdjustment = new ArrayList<>();

        if (reasonableAdjustments != null && !reasonableAdjustments.isEmpty()) {

            final List<String> types = getReasonableAdjustmentTypes();

            reasonableAdjustments.forEach(reasonableAdjustment -> {
                JurorReasonableAdjustment jurorPaperResponseReasonableAdjustment = new JurorReasonableAdjustment();
                jurorPaperResponseReasonableAdjustment.setJurorNumber(jurorNumber);

                final String type = reasonableAdjustment.getAssistanceType();
                final String message = reasonableAdjustment.getAssistanceTypeDetails();

                if (types.contains(type) && !addedReasonableAdjustment.contains(type)) {
                    jurorPaperResponseReasonableAdjustment.setReasonableAdjustment(
                        new ReasonableAdjustments(type, message));
                    jurorPaperResponseReasonableAdjustment.setReasonableAdjustmentDetail(
                        reasonableAdjustment.getAssistanceTypeDetails());
                    addedReasonableAdjustment.add(type);
                    log.debug(String.format("Adding a special need for Juror %s with code %s",
                        jurorNumber, type
                    ));
                } else {
                    log.error(String.format(
                        INVALID_SPECIAL_NEED_ERROR_MESSAGE,
                        jurorPaperResponseDto.getJurorNumber()
                    ));
                    throw new JurorPaperResponseException.InvalidSpecialNeedEntry();
                }
                reasonableAdjustmentsToAdd.add(jurorPaperResponseReasonableAdjustment);

                jurorPaperResponse.setReasonableAdjustments(reasonableAdjustmentsToAdd);

                //add the reasonable adjustments to the juror record
                Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
                juror.setReasonableAdjustmentCode(type);
                juror.setReasonableAdjustmentMessage(message);
                jurorPaperResponse.setReasonableAdjustmentsArrangements(message);
            });


        }
    }

    private static List<String> getReasonableAdjustmentTypes() {
        return Arrays.stream(ReasonableAdjustmentsEnum.values())
            .map(ReasonableAdjustmentsEnum::getCode).toList();
    }

    @Override
    @Transactional
    public void updateCjsDetails(BureauJwtPayload payload, CjsEmploymentDetailsDto cjsEmploymentDetailsDto,
                                 final String jurorNumber) {
        log.info(String.format("Updating paper response CJS Employment for Juror %s, by user %s",
            jurorNumber, payload.getLogin()
        ));

        // Check if the current user has access to the Juror record
        getJurorPaperResponseForWrite(payload, jurorNumber);

        // get list of current CJS Employers for juror
        List<JurorResponseCjsEmployment> jurorPaperResponseCjsList
            = jurorResponseCjsEmploymentRepository.findByJurorNumber(jurorNumber);

        if (cjsEmploymentDetailsDto.getCjsEmployment() == null || cjsEmploymentDetailsDto.getCjsEmployment()
            .isEmpty()) {
            log.debug(String.format("deleting all CJS Employment records for Juror %s, by user %s",
                jurorNumber, payload.getLogin()
            ));
            // Need to clear out the CJS records for this juror
            jurorPaperResponseCjsList.forEach(jurorResponseCjsEmploymentRepository::delete);
            log.debug(String.format(
                "Finished updating paper response CJS Employment records for Juror %s",
                jurorNumber
            ));
            //nothing more to do here.
            return;

        } else {

            List<String> cjsTypes = getCjsTypes();
            // List to ensure same employment isn't added more than once
            List<String> addedCjsEmployment = new ArrayList<>();

            cjsEmploymentDetailsDto.getCjsEmployment().forEach(cjs -> {
                String employer = cjs.getCjsEmployer();

                // cant update the same employer details more than once
                if (addedCjsEmployment.contains(employer)) {
                    log.error(String.format(INVALID_CJS_EMPLOYMENT_ERROR_MESSAGE, jurorNumber));
                    throw new JurorPaperResponseException.InvalidCjsEmploymentEntry();
                }

                JurorResponseCjsEmployment jurorPaperResponseCjsEmployment =
                    jurorResponseCjsEmploymentRepository.findByJurorNumberAndCjsEmployer(
                        jurorNumber, employer);

                if (jurorPaperResponseCjsEmployment != null) {
                    //record already exists, just update it
                    jurorPaperResponseCjsEmployment.setCjsEmployerDetails(cjs.getCjsEmployerDetails());
                    log.debug(String.format("Updating CJS employer, %s, for Juror %s",
                        employer, jurorNumber
                    ));
                    addedCjsEmployment.add(employer); // mark as updated
                } else {
                    if (!cjsTypes.contains(employer)) {
                        log.error(String.format(INVALID_CJS_EMPLOYMENT_ERROR_MESSAGE, jurorNumber));
                        throw new JurorPaperResponseException.InvalidCjsEmploymentEntry();
                    }
                    jurorPaperResponseCjsEmployment = new JurorResponseCjsEmployment();
                    jurorPaperResponseCjsEmployment.setJurorNumber(jurorNumber);
                    jurorPaperResponseCjsEmployment.setCjsEmployer(employer);
                    jurorPaperResponseCjsEmployment.setCjsEmployerDetails(cjs.getCjsEmployerDetails());
                    log.debug(String.format("Adding CJS employer, %s, for Juror %s",
                        employer, jurorNumber
                    ));
                    addedCjsEmployment.add(employer);
                }

                jurorResponseCjsEmploymentRepository.save(jurorPaperResponseCjsEmployment);
            });
        }

        // now tidy up any that might be in database from before but no longer required
        List<String> newCjsEntries = new ArrayList<>();
        cjsEmploymentDetailsDto.getCjsEmployment().forEach(cJS ->
            newCjsEntries.add(cJS.getCjsEmployer()));

        jurorPaperResponseCjsList.forEach(cjs -> {
            if (!newCjsEntries.contains(cjs.getCjsEmployer())) {
                log.debug(String.format("Deleting CJS employer, %s, for Juror %s", cjs.getCjsEmployer(), jurorNumber));
                jurorResponseCjsEmploymentRepository.delete(cjs);
            }
        });
        log.debug(String.format("Finished updating paper response CJS Employment records for Juror %s", jurorNumber));

    }

    @Override
    @Transactional
    public void updateReasonableAdjustmentsDetails(BureauJwtPayload payload,
                                                   ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto,
                                                   final String jurorNumber) {

        log.info(String.format("Updating paper response reasonable adjustments for Juror %s, by user %s",
            jurorNumber, payload.getLogin()
        ));

        // Check if the current user has access to the Juror record.
        getJurorPaperResponseForWrite(payload, jurorNumber);

        // get the current list of reasonable adjustments.
        List<JurorReasonableAdjustment> jurorPaperResponseReasonableAdjustments
            = reasonableAdjustmentsRepository.findByJurorNumber(jurorNumber);

        if (reasonableAdjustmentDetailsDto.getReasonableAdjustments() == null
            || reasonableAdjustmentDetailsDto.getReasonableAdjustments().isEmpty()) {
            log.debug(String.format("deleting reasonable adjustments records for Juror %s, by user %s",
                jurorNumber, payload.getLogin()
            ));
            // Need to clear out the reasonable adjustments records for this juror (in reasonable adjustments table).
            jurorPaperResponseReasonableAdjustments.forEach(reasonableAdjustmentsRepository::delete);
            // Removing reasonable adjustments record from juror table
            saveReasonableAdjustmentsToJurorRecord(jurorNumber, reasonableAdjustmentDetailsDto);
            log.debug(String.format(
                "Finished updating paper response reasonable adjustments records for Juror %s",
                jurorNumber
            ));
            // nothing more to do here
            return;

        } else {
            List<String> reasonableAdjustmentTypes = getReasonableAdjustmentTypes();
            // list to ensure same special need isn't stored more than once.
            List<String> addedReasonableAdjustments = new ArrayList<>();

            reasonableAdjustmentDetailsDto.getReasonableAdjustments().forEach(specialNeed -> {

                String assistanceType = specialNeed.getAssistanceType();
                // don't need the second parameter to find a special need record, code is primary key
                final ReasonableAdjustments tReasonableAdjustment = new ReasonableAdjustments(assistanceType, null);

                // cant update the same special need more than once
                if (addedReasonableAdjustments.contains(assistanceType)) {
                    log.error(String.format(INVALID_SPECIAL_NEED_ERROR_MESSAGE, jurorNumber));
                    throw new JurorPaperResponseException.InvalidSpecialNeedEntry();
                }

                JurorReasonableAdjustment jurorPaperResponseReasonableAdjustment = reasonableAdjustmentsRepository
                    .findByJurorNumberAndReasonableAdjustment(jurorNumber, tReasonableAdjustment);

                if (jurorPaperResponseReasonableAdjustment != null) {
                    //record already exists, just update it
                    jurorPaperResponseReasonableAdjustment
                        .setReasonableAdjustmentDetail(specialNeed.getAssistanceTypeDetails());
                    log.debug(String.format("Updating reasonable adjustments details for Juror %s with code %s",
                        jurorNumber, assistanceType
                    ));
                    addedReasonableAdjustments.add(tReasonableAdjustment.getCode()); // mark as updated
                } else {

                    if (!reasonableAdjustmentTypes.contains(assistanceType)) {
                        log.error(String.format(INVALID_SPECIAL_NEED_ERROR_MESSAGE, jurorNumber));
                        throw new JurorPaperResponseException.InvalidSpecialNeedEntry();
                    }
                    jurorPaperResponseReasonableAdjustment = new JurorReasonableAdjustment();
                    jurorPaperResponseReasonableAdjustment.setJurorNumber(jurorNumber);
                    jurorPaperResponseReasonableAdjustment.setReasonableAdjustment(tReasonableAdjustment);
                    jurorPaperResponseReasonableAdjustment
                        .setReasonableAdjustmentDetail(specialNeed.getAssistanceTypeDetails());
                    log.debug(String.format("Adding a special need for Juror %s with code %s",
                        jurorNumber, assistanceType
                    ));
                    addedReasonableAdjustments.add(tReasonableAdjustment.getCode());
                }

                reasonableAdjustmentsRepository.save(jurorPaperResponseReasonableAdjustment);
            });
        }

        // now tidy up any that might be in database from before but no longer required
        List<String> newSpecialNeedsEntries = new ArrayList<>();
        reasonableAdjustmentDetailsDto.getReasonableAdjustments().forEach(specialNeed ->
            newSpecialNeedsEntries.add(specialNeed.getAssistanceType()));

        jurorPaperResponseReasonableAdjustments.forEach(reasonableAdjustment -> {
            if (!newSpecialNeedsEntries.contains(reasonableAdjustment.getReasonableAdjustment().getCode())) {
                log.debug(String.format("Deleting a special need for Juror %s with code %s",
                    jurorNumber, reasonableAdjustment.getReasonableAdjustment().getCode()
                ));
                reasonableAdjustmentsRepository.delete(reasonableAdjustment);
            }
        });

        //updating the reasonable adjustments for the juror in the juror table
        saveReasonableAdjustmentsToJurorRecord(jurorNumber, reasonableAdjustmentDetailsDto);

        log.debug(String.format(
            "Finished updating paper response reasonable adjustments records for Juror %s", jurorNumber));
    }

    private void saveReasonableAdjustmentsToJurorRecord(String jurorNumber,
                                                        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto) {
        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, jurorNumber);
        if (!Collections.isEmpty(reasonableAdjustmentDetailsDto.getReasonableAdjustments())) {
            JurorPaperResponseDto.ReasonableAdjustment newReasonableAdjustmentDetailsDto =
                reasonableAdjustmentDetailsDto.getReasonableAdjustments().get(0);
            juror.setReasonableAdjustmentCode(newReasonableAdjustmentDetailsDto.getAssistanceType());
            juror.setReasonableAdjustmentMessage(newReasonableAdjustmentDetailsDto.getAssistanceTypeDetails());
        } else {
            juror.setReasonableAdjustmentMessage(null);
            juror.setReasonableAdjustmentCode(null);
        }
        jurorRepository.save(juror);
    }


    @Override
    @Transactional
    public void updateJurorEligibilityDetails(BureauJwtPayload payload, EligibilityDetailsDto eligibilityDetailsDto,
                                              final String jurorNumber) {

        log.info(String.format("Updating paper response eligibility details for Juror %s, by user %s",
            jurorNumber, payload.getLogin()
        ));

        PaperResponse jurorPaperResponse = getJurorPaperResponseForWrite(payload, jurorNumber);
        JurorPaperResponseDto.Eligibility eligibility = eligibilityDetailsDto.getEligibility();

        if (eligibility == null) {
            // object cannot be null
            log.error(String.format("The eligibility criteria object was null "
                + "for paper response update for Juror %s", jurorNumber));
            throw new JurorPaperResponseException.InvalidEligibilityEntry();
        }

        if (checkForUpdatedValue(jurorPaperResponse.getResidency(), eligibility.getLivedConsecutive(),
            RESIDENCY, jurorNumber
        )) {
            jurorPaperResponse.setResidency(eligibility.getLivedConsecutive());
        }

        if (checkForUpdatedValue(jurorPaperResponse.getMentalHealthAct(), eligibility.getMentalHealthAct(),
            MENTAL_HEALTH, jurorNumber
        )) {
            jurorPaperResponse.setMentalHealthAct(eligibility.getMentalHealthAct());
        }

        if (checkForUpdatedValue(jurorPaperResponse.getMentalHealthCapacity(), eligibility.getMentalHealthCapacity(),
            MENTAL_CAPACITY, jurorNumber
        )) {
            jurorPaperResponse.setMentalHealthCapacity(eligibility.getMentalHealthCapacity());
        }

        if (checkForUpdatedValue(jurorPaperResponse.getBail(), eligibility.getOnBail(),
            BAIL, jurorNumber
        )) {
            jurorPaperResponse.setBail(eligibility.getOnBail());
        }

        if (checkForUpdatedValue(jurorPaperResponse.getConvictions(), eligibility.getConvicted(),
            CONVICTION, jurorNumber
        )) {
            jurorPaperResponse.setConvictions(eligibility.getConvicted());
        }

        paperResponseRepository.save(jurorPaperResponse);
        log.debug(String.format("Finished updating paper response eligibility details for Juror %s", jurorNumber));
    }

    private PaperResponse getJurorPaperResponseForWrite(BureauJwtPayload payload, final String jurorNumber) {
        // Check if the current user has access to the Juror record
        checkWriteAccessForCurrentUser(jurorNumber, payload.getOwner());
        return getJurorPaperResponse(jurorNumber);
    }

    @Override
    public void updateJurorReplyTypeDetails(BureauJwtPayload payload, ReplyTypeDetailsDto replyTypeDetailsDto,
                                            final String jurorNumber) {

        log.info(String.format("Updating paper response reply type for Juror %s, by user %s",
            jurorNumber, payload.getLogin()
        ));

        // Check if the current user has access to the Juror record
        PaperResponse jurorPaperResponse = getJurorPaperResponseForWrite(payload, jurorNumber);

        Boolean excusal = replyTypeDetailsDto.getExcusal();
        Boolean deferral = replyTypeDetailsDto.getDeferral();

        if (excusal && deferral) {
            //throw an exception as we cant have both excusal and deferral true
            throw new JurorPaperResponseException.InvalidReplyTypeEntry();
        }

        if (checkForUpdatedValue(jurorPaperResponse.getExcusal(), excusal, EXCUSAL, jurorNumber)) {
            jurorPaperResponse.setExcusal(excusal);
        }

        if (checkForUpdatedValue(jurorPaperResponse.getDeferral(), deferral, DEFERRAL, jurorNumber)) {
            jurorPaperResponse.setDeferral(deferral);
        }

        paperResponseRepository.save(jurorPaperResponse);
        log.debug(String.format("Finished updating paper response reply type details for Juror %s", jurorNumber));

    }

    @Override
    public void updateJurorSignatureDetails(BureauJwtPayload payload, SignatureDetailsDto signatureDetailsDto,
                                            final String jurorNumber) {

        log.info(String.format("Updating paper response signature for Juror %s, by user %s",
            jurorNumber, payload.getLogin()
        ));

        // Check if the current user has access to the Juror record
        PaperResponse jurorPaperResponse = getJurorPaperResponseForWrite(payload, jurorNumber);
        Boolean signature = signatureDetailsDto.getSignature();

        if (checkForUpdatedValue(jurorPaperResponse.getSigned(), signature, SIGNATURE, jurorNumber)) {
            jurorPaperResponse.setSigned(signature);
        }

        paperResponseRepository.save(jurorPaperResponse);
        log.debug(String.format("Finished updating paper response signature details for Juror %s", jurorNumber));

    }

    private boolean checkForUpdatedValue(Boolean currentValue, Boolean newValue, String fieldName, String jurorNumber) {
        if ((currentValue != null && !currentValue.equals(newValue))
            || (currentValue == null && newValue != null)) {
            log.debug(String.format(RESPONSE_UPDATED_LOG,
                jurorNumber, fieldName
            ));
            return true;
        }
        return false;
    }

    private void processStraightThroughResponse(PaperResponse jurorPaperResponse, JurorPool jurorPool,
                                                LocalDate returnDate, BureauJwtPayload payload) {
        log.trace("Enter processStraightThroughResponse for {}", jurorPool.getJurorNumber());

        if (jurorPaperResponse.getDateOfBirth() != null && returnDate != null
            && straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(jurorPaperResponse,
            returnDate, jurorPool)) {
            log.info("Juror {} - processed automatically due to age disqualification", jurorPool.getJurorNumber());
            straightThroughProcessorService.processAgeDisqualification(jurorPaperResponse, returnDate, jurorPool,
                payload);
        }

        log.trace("Exit processStraightThroughResponse for {}", jurorPool.getJurorNumber());
    }
}
