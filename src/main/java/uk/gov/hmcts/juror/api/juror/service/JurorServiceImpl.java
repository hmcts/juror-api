package uk.gov.hmcts.juror.api.juror.service;

import io.jsonwebtoken.lang.Collections;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorDetailDto;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.ReplyTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.ReasonableAdjustmentsRepository;
import uk.gov.hmcts.juror.api.moj.service.PoolRequestService;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of Public Juror service for public data access operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JurorServiceImpl implements JurorService {
    private final ReplyTypeRepository replyTypeRepository;

    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;
    private final JurorResponseCjsEmploymentRepositoryMod jurorResponseCjsEmploymentRepository;
    private final JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    private final JurorPoolRepository jurorRepository;
    private final UrgencyService urgencyService;
    private final PoolRequestService poolRequestService;
    private final ReasonableAdjustmentsRepository reasonableAdjustmentsRepository;


    @Override
    public JurorDetailDto getJurorByJurorNumber(final String number) {
        log.debug("Getting juror {} details", number);
        JurorPool jurorDetails = jurorRepository.findByJurorJurorNumber(number);

        JurorDetailDto.JurorDetailDtoBuilder builder = JurorDetailDto.builder();
        JurorStatus jurorStatus = new JurorStatus();
        if (jurorDetails != null) {
            builder =
                builder
                    .jurorNumber(jurorDetails.getJurorNumber())
                    .title(jurorDetails.getJuror().getTitle())
                    .firstName(jurorDetails.getJuror().getFirstName())
                    .lastName(jurorDetails.getJuror().getLastName())
                    .processingStatus(jurorStatus.getStatus())
                    .address(jurorDetails.getJuror().getAddressLine1())
                    .address2(jurorDetails.getJuror().getAddressLine2())
                    .address3(jurorDetails.getJuror().getAddressLine3())
                    .address4(jurorDetails.getJuror().getAddressLine4())
                    .address5(jurorDetails.getJuror().getAddressLine5())
                    .postcode(jurorDetails.getJuror().getPostcode())
                    .hearingDate(jurorDetails.getNextDate())
                    .locCode(jurorDetails.getCourt().getLocCode())
                    .locCourtName(jurorDetails.getCourt().getLocCourtName())
                    .courtAttendTime(DateUtils.TIME_FORMAT.format(getAttendTime(jurorDetails)))
                    .courtAddress1(jurorDetails.getCourt().getAddress1())
                    .courtAddress2(jurorDetails.getCourt().getAddress2())
                    .courtAddress3(jurorDetails.getCourt().getAddress3())
                    .courtAddress4(jurorDetails.getCourt().getAddress4())
                    .courtAddress5(jurorDetails.getCourt().getAddress5())
                    .courtPostcode(jurorDetails.getCourt().getPostcode());


        } else {
            log.debug("Pool entry not found for {}", number);
            return null;
        }

        return builder.build();
    }

    /**
     * Gets the attendance time for a summons
     * If the attend time in juror_mod.pool is populated, this value will be returned. Otherwise the 'default' attend
     * time for the court will be used.
     *
     * @param jurorDetails pool details to transform, not null
     * @return attendance time, nullable
     * @since JDB-2042
     */
    private LocalTime getAttendTime(JurorPool jurorDetails) {
        final LocalDateTime uniquePoolAttendTime =
            poolRequestService.getPoolAttendanceTime(jurorDetails.getPoolNumber());

        if (uniquePoolAttendTime != null) {
            if (log.isTraceEnabled()) {
                log.trace("Attend time is set in unique pool, using pool attend time of {}", uniquePoolAttendTime);
            }
            return uniquePoolAttendTime.toLocalTime();
        } else {
            LocalTime courtAttendTime = jurorDetails.getCourt().getCourtAttendTime();
            if (log.isTraceEnabled()) {
                log.trace("Attend time is not set in unique pool, using court attend time of {}", courtAttendTime);
            }
            return courtAttendTime;
        }
    }

    @Transactional
    @Override
    public DigitalResponse saveResponse(final JurorResponseDto responseDto) {
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
            final JurorPool jurorDetails = jurorRepository.findByJurorJurorNumber(responseDto.getJurorNumber());
            Juror juror = jurorDetails.getJuror();
            //copy fields from jurorDetails into jurorResponse as they are not supplied by the frontend!
            responseDto.setTitle(juror.getTitle());
            responseDto.setFirstName(juror.getFirstName());
            responseDto.setLastName(juror.getLastName());
            responseDto.setAddressLineOne(juror.getAddressLine1());
            responseDto.setAddressLineTwo(juror.getAddressLine2());
            responseDto.setAddressLineThree(juror.getAddressLine3());
            responseDto.setAddressTown(juror.getAddressLine4());
            responseDto.setAddressCounty(juror.getAddressLine5());
            responseDto.setAddressPostcode(juror.getPostcode());
            responseDto.setDateOfBirth(juror.getDateOfBirth());
            responseDto.setPrimaryPhone(juror.getPhoneNumber());
            responseDto.setSecondaryPhone(juror.getAltPhoneNumber());
            responseDto.setEmailAddress(juror.getEmail());
            responseDto.setReplyMethod(ReplyMethod.DIGITAL);
        }

        try {
            log.debug("Saving juror response for juror {}", responseDto.getJurorNumber());
            final DigitalResponse responseEntity = convertJurorResponseDtoToEntity(responseDto);
            final DigitalResponse savedJurorResponse = jurorResponseRepository.save(responseEntity);

            if (!Collections.isEmpty(savedJurorResponse.getCjsEmployments())) {
                jurorResponseCjsEmploymentRepository.saveAll(savedJurorResponse.getCjsEmployments());
            }
            if (!Collections.isEmpty(savedJurorResponse.getReasonableAdjustments())) {
                jurorReasonableAdjustmentRepository.saveAll(savedJurorResponse.getReasonableAdjustments());
            }


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
    public DigitalResponse convertJurorResponseDtoToEntity(JurorResponseDto dto) {
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
        final List<JurorReasonableAdjustment> reasonableAdjustmentsEntities = new ArrayList<>();
        if (dto.getReasonableAdjustments() != null) {
            dto.getReasonableAdjustments().forEach(reasonableAdjustment ->
                reasonableAdjustmentsEntities.add(
                    JurorReasonableAdjustment.builder()
                        .jurorNumber(jurorNumber)
                        .reasonableAdjustment(
                            reasonableAdjustmentsRepository.findByCode(reasonableAdjustment.getAssistanceType()))
                        .reasonableAdjustmentDetail(reasonableAdjustment.getAssistanceTypeDetails())
                        .build()
                )
            );
        }

        final List<JurorResponseCjsEmployment> cjsEmployerEntities = new ArrayList<>();
        if (dto.getCjsEmployment() != null) {
            dto.getCjsEmployment().forEach(cjsEmployment ->
                cjsEmployerEntities.add(
                    JurorResponseCjsEmployment.builder()
                        .jurorNumber(jurorNumber)
                        .cjsEmployer(cjsEmployment.getCjsEmployer())
                        .cjsEmployerDetails(cjsEmployment.getCjsEmployerDetails())
                        .build()
                )

            );
        }

        final JurorPool jurorDetails = jurorRepository.findByJurorJurorNumber(jurorNumber);
        DigitalResponse.DigitalResponseBuilder<?, ?> builder = DigitalResponse.builder()
            .jurorNumber(dto.getJurorNumber())
            .dateOfBirth(dto.getDateOfBirth())
            .title(dto.getTitle())
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .phoneNumber(dto.getPrimaryPhone())
            .altPhoneNumber(dto.getSecondaryPhone())
            .email(dto.getEmailAddress())
            .addressLine1(dto.getAddressLineOne())
            .addressLine2(dto.getAddressLineTwo())
            .addressLine3(dto.getAddressLineThree())
            .addressLine4(dto.getAddressTown())
            .addressLine5(dto.getAddressCounty())
            .postcode(DataUtils.toUppercase(dto.getAddressPostcode()))
            .residency(livedConsecutive != null && livedConsecutive.isAnswer())
            .residencyDetail(livedConsecutive != null ? livedConsecutive.getDetails() : null)
            .bail(onBail != null && onBail.isAnswer())
            .bailDetails(onBail != null ? onBail.getDetails() : null)
            .convictions(convicted != null && convicted.isAnswer())
            .convictionsDetails(convicted != null ? convicted.getDetails() : null)
            .mentalHealthAct(mentalHealthAct != null && mentalHealthAct.isAnswer())
            .mentalHealthActDetails(mentalHealthAct != null ? mentalHealthAct.getDetails() : null)
            .deferralReason(deferral != null ? deferral.getReason() : null)
            .deferralDate(deferral != null ? deferral.getDates() : null)
            .excusalReason(excusal != null ? excusal.getReason() : null)
            .reasonableAdjustmentsArrangements(dto.getAssistanceSpecialArrangements())
            .processingComplete(Boolean.FALSE)
            .processingStatus(ProcessingStatus.TODO)
            .dateReceived(LocalDateTime.now())
            .reasonableAdjustments(reasonableAdjustmentsEntities)
            .cjsEmployments(cjsEmployerEntities)
            .version(dto.getVersion())
            .replyType(
                RepositoryUtils.retrieveFromDatabase(
                    "Digital",
                    replyTypeRepository
                ));

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
        builder.welsh(dto.getWelsh() != null ? dto.getWelsh() : Boolean.FALSE);

        // call the build method after the optional third party field are added.

        final DigitalResponse entity = builder.build();
        // add urgency flags to the response if required
        urgencyService.setUrgencyFlags(entity, jurorDetails);

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
