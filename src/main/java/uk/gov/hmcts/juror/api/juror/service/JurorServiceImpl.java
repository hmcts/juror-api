package uk.gov.hmcts.juror.api.juror.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorCJS;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorCJSRepository;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeed;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeedsRepository;
import uk.gov.hmcts.juror.api.bureau.domain.TSpecialRepository;
import uk.gov.hmcts.juror.api.bureau.service.UniquePoolService;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorDetailDto;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of Public Juror service for public data access operations.
 */
@Service
@Slf4j
public class JurorServiceImpl implements JurorService {
    private final UrgencyService urgencyService;
    private final JurorResponseRepository jurorResponseRepository;
    private final BureauJurorCJSRepository cjsEmploymentRepository;
    private final BureauJurorSpecialNeedsRepository specialNeedRepository;
    private final PoolRepository poolDetailsRepository;
    private final UniquePoolService uniquePoolService;
    private final TSpecialRepository tSpecialRepository;

    @Autowired
    public JurorServiceImpl(final JurorResponseRepository jurorResponseRepository,
                            final BureauJurorCJSRepository cjsEmploymentRepository,
                            final BureauJurorSpecialNeedsRepository specialNeedRepository,
                            final PoolRepository poolDetailsRepository,
                            final UrgencyService urgencyService,
                            final UniquePoolService uniquePoolService,
                            final TSpecialRepository tSpecialRepository) {
        Assert.notNull(cjsEmploymentRepository, "CJSEmploymentRepository cannot be null.");
        Assert.notNull(jurorResponseRepository, "JurorResponseRepository cannot be null.");
        Assert.notNull(specialNeedRepository, "BureauJurorSpecialNeedsRepository cannot be null.");
        Assert.notNull(poolDetailsRepository, "PoolRepository cannot be null.");
        Assert.notNull(urgencyService, "UrgencyService cannot be null.");
        Assert.notNull(uniquePoolService, "UniquePoolService cannot be null.");
        Assert.notNull(tSpecialRepository, "TSpecialRepository cannot be null.");
        this.jurorResponseRepository = jurorResponseRepository;
        this.cjsEmploymentRepository = cjsEmploymentRepository;
        this.specialNeedRepository = specialNeedRepository;
        this.poolDetailsRepository = poolDetailsRepository;
        this.urgencyService = urgencyService;
        this.uniquePoolService = uniquePoolService;
        this.tSpecialRepository = tSpecialRepository;
    }

    @Override
    public JurorDetailDto getJurorByJurorNumber(final String number) {
        log.debug("Getting juror {} details", number);
        Pool poolDetails = poolDetailsRepository.findByJurorNumber(number);

        JurorDetailDto.JurorDetailDtoBuilder builder = JurorDetailDto.builder();

        if (poolDetails != null) {
            builder = builder.jurorNumber(poolDetails.getJurorNumber())
                .title(poolDetails.getTitle())
                .firstName(poolDetails.getFirstName())
                .lastName(poolDetails.getLastName())
                .processingStatus(poolDetails.getStatus())
                .address(poolDetails.getAddress())
                .address2(poolDetails.getAddress2())
                .address3(poolDetails.getAddress3())
                .address4(poolDetails.getAddress4())
                .address5(poolDetails.getAddress5())
                .address6(null)
                .postcode(poolDetails.getPostcode())
                .hearingDate(poolDetails.getHearingDate())
                .locCode(poolDetails.getCourt().getLocCode())
                .locCourtName(poolDetails.getCourt().getLocCourtName())
                .courtAttendTime(getAttendTime(poolDetails))
                .courtAddress1(poolDetails.getCourt().getAddress1())
                .courtAddress2(poolDetails.getCourt().getAddress2())
                .courtAddress3(poolDetails.getCourt().getAddress3())
                .courtAddress4(poolDetails.getCourt().getAddress4())
                .courtAddress5(poolDetails.getCourt().getAddress5())
                .courtAddress6(poolDetails.getCourt().getAddress6())
                .courtPostcode(poolDetails.getCourt().getPostcode());
        } else {
            log.debug("Pool entry not found for {}", number);
            return null;
        }

        return builder.build();
    }

    /**
     * Gets the attendance time for a summons
     * If the attend time in JUROR.UNIQUE_POOL is populated, this value will be returned. Otherwise the 'default' attend
     * time for the court will be used.
     *
     * @param poolDetails pool details to transform, not null
     * @return attendance time, nullable
     * @since JDB-2042
     */
    private String getAttendTime(Pool poolDetails) {
        final String uniquePoolAttendTime = uniquePoolService.getPoolAttendanceTime(poolDetails.getPoolNumber());

        if (uniquePoolAttendTime != null) {
            if (log.isTraceEnabled()) {
                log.trace("Attend time is set in unique pool, using pool attend time of {}", uniquePoolAttendTime);
            }
            return uniquePoolAttendTime;
        } else {
            final String courtAttendTime = DataUtils.asStringHHmm(poolDetails.getCourt().getCourtAttendTime());
            if (log.isTraceEnabled()) {
                log.trace("Attend time is not set in unique pool, using court attend time of {}", courtAttendTime);
            }
            return courtAttendTime;
        }
    }

    @Transactional
    @Override
    public JurorResponse saveResponse(final JurorResponseDto responseDto) {
        //checks
        if (jurorResponseRepository.findByJurorNumber(responseDto.getJurorNumber()) != null) {
            log.error("Juror {} has already responded!", responseDto.getJurorNumber());
            throw new PublicAuthenticationServiceImpl.JurorAlreadyRespondedException();
        }

        if (!ObjectUtils.isEmpty(responseDto.getThirdParty()) && responseDto.getPrimaryPhone() == null
            && responseDto.getSecondaryPhone() == null
            && noThirdPartyPhoneNumbers(responseDto.getThirdParty())) {
            throw new NoPhoneNumberProvided();
        }

        if (responseDto.getThirdParty() != null
            && BooleanUtils.isFalse(responseDto.getThirdParty().getUseJurorPhoneDetails())
            && responseDto.getThirdParty().getMainPhone() == null
            && responseDto.getThirdParty().getOtherPhone() == null) {
            throw new NoPhoneNumberProvided();
        }

        if (!ObjectUtils.isEmpty(responseDto.getThirdParty()) && !ObjectUtils.isEmpty(
            responseDto.getThirdParty().getThirdPartyReason())
            && "deceased".equalsIgnoreCase(responseDto.getThirdParty().getThirdPartyReason())) {
            log.debug("Juror {} third party response for deceased.");
            final Pool poolDetails = poolDetailsRepository.findByJurorNumber(responseDto.getJurorNumber());
            //copy fields from poolDetails into jurorResponse as they are not supplied by the frontend!
            responseDto.setTitle(poolDetails.getTitle());
            responseDto.setFirstName(poolDetails.getFirstName());
            responseDto.setLastName(poolDetails.getLastName());
            responseDto.setAddressLineOne(poolDetails.getAddress());
            responseDto.setAddressLineTwo(poolDetails.getAddress2());
            responseDto.setAddressLineThree(poolDetails.getAddress3());
            responseDto.setAddressTown(poolDetails.getAddress4());
            responseDto.setAddressCounty(poolDetails.getAddress5());
            responseDto.setAddressPostcode(poolDetails.getPostcode());
            responseDto.setDateOfBirth(poolDetails.getDateOfBirth());
            responseDto.setPrimaryPhone(poolDetails.getPhoneNumber());
            responseDto.setSecondaryPhone(poolDetails.getAltPhoneNumber());
            responseDto.setEmailAddress(poolDetails.getEmail());
        }

        try {
            log.debug("Saving juror response for juror {}", responseDto.getJurorNumber());
            final JurorResponse responseEntity = convertJurorResponseDtoToEntity(responseDto);
            final JurorResponse savedJurorResponse = jurorResponseRepository.save(responseEntity);

            log.info("Juror response saved for juror {}", responseEntity.getJurorNumber());
            return savedJurorResponse;
        } catch (DuplicateKeyException dke) {
            log.error(
                "Primary key violation writing Juror response {}: {}",
                responseDto.getJurorNumber(),
                dke.getMessage()
            );
            throw new JurorResponseAlreadyExistsException("Database already contains response for juror", dke);
        }
    }

    private boolean noThirdPartyPhoneNumbers(JurorResponseDto.ThirdParty thirdParty) {
        return thirdParty.getMainPhone() == null
            && thirdParty.getOtherPhone() == null;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class NoPhoneNumberProvided extends RuntimeException {

    }

    /**
     * Convert a {@link JurorResponseDto} to entity form for saving to the database.
     *
     * @param dto Valid juror response
     * @return Persisted entity of the response
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public JurorResponse convertJurorResponseDtoToEntity(JurorResponseDto dto) {
        if (log.isTraceEnabled()) {
            log.trace("Consuming: {}", dto);
        }

        final String jurorNumber = dto.getJurorNumber();
        final JurorResponseDto.Deferral deferral = dto.getDeferral();
        final JurorResponseDto.Excusal excusal = dto.getExcusal();
        final JurorResponseDto.Qualify qualify = dto.getQualify();
        JurorResponseDto.Answerable livedConsecutive = new JurorResponseDto.Answerable(true, null);
        JurorResponseDto.Answerable onBail = null;
        JurorResponseDto.Answerable convicted = null;
        JurorResponseDto.Answerable mentalHealthAct = null;
        if (qualify != null) {
            livedConsecutive = qualify.getLivedConsecutive();
            onBail = qualify.getOnBail();
            convicted = qualify.getConvicted();
            mentalHealthAct = qualify.getMentalHealthAct();
        }

        //convert the dto to the entity type
        final List<BureauJurorSpecialNeed> specialNeedEntities = new ArrayList<>();
        if (dto.getSpecialNeeds() != null) {
            dto.getSpecialNeeds().forEach(specialNeed ->
                specialNeedEntities.add(
                    // get attached version of the special need entity after conversion
                    specialNeedRepository.save(
                        BureauJurorSpecialNeed.builder()
                            .jurorNumber(jurorNumber)
                            .specialNeed(tSpecialRepository.findByCode(specialNeed.getAssistanceType()))
                            .detail(specialNeed.getAssistanceTypeDetails())
                            .build()
                    )
                )
            );
        }
        final List<BureauJurorCJS> cjsEmployerEntities = new ArrayList<>();
        if (dto.getCjsEmployment() != null) {
            dto.getCjsEmployment().forEach(cjsEmployment ->
                cjsEmployerEntities.add(
                    // get attached version of the special need entity after conversion
                    cjsEmploymentRepository.save(
                        BureauJurorCJS.builder()
                            .jurorNumber(jurorNumber)
                            .employer(cjsEmployment.getCjsEmployer())
                            .details(cjsEmployment.getCjsEmployerDetails())
                            .build()
                    )
                )
            );
        }

        final Pool poolDetails = poolDetailsRepository.findByJurorNumber(jurorNumber);

        JurorResponse.JurorResponseBuilder builder = JurorResponse.builder()
            .jurorNumber(jurorNumber)
            .dateOfBirth(dto.getDateOfBirth())
            .title(dto.getTitle())
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .phoneNumber(dto.getPrimaryPhone())
            .altPhoneNumber(dto.getSecondaryPhone())
            .email(dto.getEmailAddress())
            .address(dto.getAddressLineOne())
            .address2(dto.getAddressLineTwo())
            .address3(dto.getAddressLineThree())
            .address4(dto.getAddressTown())
            .address5(dto.getAddressCounty())
            .postcode(dto.getAddressPostcode())
            .residency(livedConsecutive != null && livedConsecutive.isAnswer())
            .residencyDetail(livedConsecutive != null
                ?
                livedConsecutive.getDetails()
                :
                    null)
            .bail(onBail != null && onBail.isAnswer())
            .bailDetails(onBail != null
                ?
                onBail.getDetails()
                :
                    null)
            .convictions(convicted != null && convicted.isAnswer())
            .convictionsDetails(convicted != null
                ?
                convicted.getDetails()
                :
                    null)
            .mentalHealthAct(mentalHealthAct != null && mentalHealthAct.isAnswer())
            .mentalHealthActDetails(mentalHealthAct != null
                ?
                mentalHealthAct.getDetails()
                :
                    null)

            .deferralReason(deferral != null
                ?
                deferral.getReason()
                :
                    null)
            .deferralDate(deferral != null
                ?
                deferral.getDates()
                :
                    null)
            .excusalReason(excusal != null
                ?
                excusal.getReason()
                :
                    null)
            .specialNeedsArrangements(dto.getAssistanceSpecialArrangements())
            .processingComplete(Boolean.FALSE)
            .processingStatus(ProcessingStatus.TODO)
            .dateReceived(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()))
            .specialNeeds(specialNeedEntities)
            .cjsEmployments(cjsEmployerEntities)
            .version(dto.getVersion());

        // If the DTO is a third party response, add third party details to entity
        if (dto.getThirdParty() != null) {
            builder = builder
                .thirdPartyFName(dto.getThirdParty().getThirdPartyFName())
                .thirdPartyLName(dto.getThirdParty().getThirdPartyLName())
                .relationship(dto.getThirdParty().getRelationship())
                .mainPhone(dto.getThirdParty().getMainPhone())
                .otherPhone(dto.getThirdParty().getOtherPhone())
                .emailAddress(dto.getThirdParty().getEmailAddress())
                .thirdPartyReason(dto.getThirdParty().getThirdPartyReason())
                .thirdPartyOtherReason(dto.getThirdParty().getThirdPartyOtherReason())
                .jurorPhoneDetails(dto.getThirdParty().getUseJurorPhoneDetails())
                .jurorEmailDetails(dto.getThirdParty().getUseJurorEmailDetails());
        }

        // welsh language
        // lombok builder not setting defaults in JurorResponse objects. When null, force to false.
        builder.welsh(dto.getWelsh() != null
            ?
            dto.getWelsh()
            :
                Boolean.FALSE);

        // call the build method after the optional third party field are added.
        final JurorResponse entity = builder.build();

        // add urgency flags to the response if required
        urgencyService.setUrgencyFlags(entity, poolDetails);

        if (log.isTraceEnabled()) {
            log.trace("Produced: {}", entity);
        }

        return entity;
    }

    /**
     * Exception wrapping constraint violation exceptions. Specifically indicating the database was not modified.
     */
    @ResponseStatus(HttpStatus.NOT_MODIFIED)
    public static class JurorResponseAlreadyExistsException extends RuntimeException {
        public JurorResponseAlreadyExistsException(final String message, final DataIntegrityViolationException dive) {
            super(message, dive);
        }
    }
}
