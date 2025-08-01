package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPersonalDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorCommonResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.StraightThroughProcessorService;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyMergeService;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.getJurorDigitalResponse;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.getJurorPaperResponse;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.hasValueChanged;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorResponseServiceImpl implements JurorResponseService {

    private final JurorPoolRepository jurorPoolRepository;
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    private final StraightThroughProcessorService straightThroughProcessorService;
    private final JurorCommonResponseRepositoryMod jurorCommonResponseRepository;
    private final JurorResponseAuditRepositoryMod jurorResponseAuditRepositoryMod;
    private final UserRepository userRepository;
    private final SummonsReplyMergeService mergeService;

    @Override
    @Transactional
    public void updateJurorPersonalDetails(BureauJwtPayload payload, JurorPersonalDetailsDto jurorPersonalDetailsDto,
                                           String jurorNumber) {
        log.info(String.format("Juror: %s. Start updating %s response personal details by user %s", jurorNumber,
            jurorPersonalDetailsDto.getReplyMethod().getDescription(), payload.getLogin()));

        //Check if the current user has access to the Juror record
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber,
            payload.getOwner());
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        AbstractJurorResponse jurorResponse = null;

        //Get the existing juror response for the appropriate reply method and map to the pojo
        if (jurorPersonalDetailsDto.getReplyMethod().equals(ReplyMethod.PAPER)) {
            jurorResponse = getJurorPaperResponse(jurorNumber, jurorPaperResponseRepository);
        } else if (jurorPersonalDetailsDto.getReplyMethod().equals(ReplyMethod.DIGITAL)) {
            jurorResponse = getJurorDigitalResponse(jurorNumber, jurorDigitalResponseRepository);
        }

        //If changes to personal data detected, update the juror response
        if (hasSummonsReplyDataChanged(jurorResponse, jurorPersonalDetailsDto)) {
            if (jurorResponse instanceof PaperResponse) {
                jurorPaperResponseRepository.save((PaperResponse) jurorResponse);
            } else if (jurorPersonalDetailsDto.getReplyMethod().equals(ReplyMethod.DIGITAL)) {
                jurorDigitalResponseRepository.save((DigitalResponse) jurorResponse);
            }
            log.debug(String.format("Juror: %s. Finished updating %s response personal details", jurorNumber,
                jurorResponse.getReplyType().getDescription().toLowerCase()));
        } else {
            log.debug(String.format("Juror: %s. No changes identified to %s response personal details ", jurorNumber,
                jurorResponse.getReplyType().getDescription().toLowerCase()));
        }

        //Straight through processing
        processStraightThroughResponse(jurorResponse, jurorPool, payload);
    }

    @Override
    public JurorCommonResponseRepositoryMod.AbstractResponse getCommonJurorResponse(String jurorNumber) {
        return getCommonJurorResponseOptional(jurorNumber)
            .orElseThrow(() -> new MojException.NotFound("Juror response not found for juror number: " + jurorNumber,
                null));
    }

    @Override
    public Optional<JurorCommonResponseRepositoryMod.AbstractResponse> getCommonJurorResponseOptional(
        String jurorNumber) {
        return Optional.ofNullable(jurorCommonResponseRepository.findByJurorNumber(jurorNumber));
    }

    @Override
    public int getOpenSummonsRepliesCount(String courtCode) {
        log.info("Retrieving open summons replies count for court code: {}", courtCode);
        return jurorCommonResponseRepository.getOpenResponsesAtCourt(courtCode);

    }


    @Override
    public int getSummonsRepliesCount(String locCode) {
        log.info("Retrieving open summons replies count for bureau");
        return jurorCommonResponseRepository.getOpenResponsesAtBureau(locCode);
    }

    @Override
    public int getOpenSummonsRepliesFourWeeksCount(String locCode) {
        log.info("Retrieving open summons replies 4 weeks before start date: {}", locCode);
        return jurorCommonResponseRepository.getSummonsRepliesFourWeeks(locCode);
    }

    @Override
    public int getOpenSummonsRepliesStandardCount(String locCode) {
        log.info("Retrieving open standard summons replies: {}", locCode);
        return jurorCommonResponseRepository.getSummonsRepliesStandard(locCode);
    }

    @Override
    public int getOpenSummonsRepliesOverdueCount(String locCode) {
        log.info("Retrieving open overdue summons replies: {}", locCode);
        return jurorCommonResponseRepository.getSummonsRepliesUrgent(locCode);
    }

    @Override
    public int getOpenSummonsRepliesAssignedCount(String locCode) {
        log.info("Retrieving open summons replies assigned: {}", locCode);
        return jurorCommonResponseRepository.getSummonsRepliesAssigned(locCode);
    }

    @Override
    public int getOpenSummonsRepliesUnassignedCount(String locCode) {
        log.info("Retrieving open summons replies unassigned: {}", locCode);
        return jurorCommonResponseRepository.getSummonsRepliesUnassigned(locCode);
    }

    @Override
    public int getDeferredJurorsStartDateNextWeekCount(String locCode) {
        log.info("Retrieving deferred jurors start date next week count for location code: {}", locCode);
        return jurorCommonResponseRepository.getDeferredJurorsStartDateNextWeek(locCode);
    }

    @Override
    public int getPoolsNotYetSummonedCount(String locCode) {
        log.info("Retrieving pools not yet summoned count for location code: {}", locCode);
        return jurorCommonResponseRepository.getPoolsNotYetSummonedCount(locCode);
    }

    @Override
    public int getPoolsTransferringNextWeekCount(String locCode) {
        log.info("Retrieving pools transferring next week count for location code: {}", locCode);
        return jurorCommonResponseRepository.getPoolsTransferringNextWeekCount(locCode);
    }



    private boolean hasSummonsReplyDataChanged(AbstractJurorResponse jurorResponse,
                                               JurorPersonalDetailsDto jurorPersonalDetailsDto) {
        final String jurorNumber = jurorResponse.getJurorNumber();

        //Create a copy of the transient juror response pojo as will require it later to check if personal details
        // have been updated
        AbstractJurorResponse originalJurorResponse = null;
        if (jurorResponse instanceof DigitalResponse) {
            originalJurorResponse = new DigitalResponse();

        } else if (jurorResponse instanceof PaperResponse) {
            originalJurorResponse = new PaperResponse();
        }

        assert originalJurorResponse != null;
        BeanUtils.copyProperties(jurorResponse, originalJurorResponse);

        checkUpdatesToJurorPersonalDetails(jurorResponse, jurorPersonalDetailsDto, jurorNumber);
        checkUpdatesToJurorAddress(jurorResponse, jurorPersonalDetailsDto, jurorNumber);
        checkUpdatesToJurorContactDetails(jurorResponse, jurorPersonalDetailsDto, jurorNumber);
        checkUpdatesToJurorThirdParty(jurorResponse, jurorPersonalDetailsDto, jurorNumber);

        return !jurorResponse.equals(originalJurorResponse);
    }

    private void checkUpdatesToJurorPersonalDetails(AbstractJurorResponse jurorResponse,
                                                    JurorPersonalDetailsDto jurorPersonalDetailsDto,
                                                    String jurorNumber) {
        String replyMethod = jurorResponse.getReplyType().getDescription();

        //For the time being only checking DATE_OF_BIRTH (for updates to digital reply)
        if (hasValueChanged(jurorResponse.getDateOfBirth(), jurorPersonalDetailsDto.getDateOfBirth(),
            DATE_OF_BIRTH, jurorNumber, replyMethod)) {
            jurorResponse.setDateOfBirth(jurorPersonalDetailsDto.getDateOfBirth());
        }

        //TODO: PERSONAL UPDATES FOR DIGITAL
        if (jurorResponse.getReplyType().getType().equals(ReplyMethod.PAPER.getDescription())) {
            if (hasValueChanged(jurorResponse.getTitle(), jurorPersonalDetailsDto.getTitle(), TITLE,
                jurorNumber, replyMethod)) {
                jurorResponse.setTitle(jurorPersonalDetailsDto.getTitle());
            }

            if (hasValueChanged(jurorResponse.getFirstName(), jurorPersonalDetailsDto.getFirstName(),
                FIRSTNAME, jurorNumber, replyMethod)) {
                jurorResponse.setFirstName(jurorPersonalDetailsDto.getFirstName());
            }

            if (hasValueChanged(jurorResponse.getLastName(), jurorPersonalDetailsDto.getLastName(),
                LASTNAME, jurorNumber, replyMethod)) {
                jurorResponse.setLastName(jurorPersonalDetailsDto.getLastName());
            }
        }
    }

    private void checkUpdatesToJurorAddress(AbstractJurorResponse jurorResponse,
                                            JurorPersonalDetailsDto jurorPersonalDetailsDto, String jurorNumber) {
        String replyMethod = jurorResponse.getReplyType().getDescription();

        //TODO: ADDRESS UPDATES FOR DIGITAL
        if (jurorResponse.getReplyType().getType().equals(ReplyMethod.PAPER.getDescription())) {
            if (hasValueChanged(jurorResponse.getAddressLine1(), jurorPersonalDetailsDto.getAddressLineOne(),
                ADDRESS_LINE1, jurorNumber, replyMethod)) {
                jurorResponse.setAddressLine1(jurorPersonalDetailsDto.getAddressLineOne());
            }

            if (hasValueChanged(jurorResponse.getAddressLine2(), jurorPersonalDetailsDto.getAddressLineTwo(),
                ADDRESS_LINE2, jurorNumber, replyMethod)) {
                jurorResponse.setAddressLine2(jurorPersonalDetailsDto.getAddressLineTwo());
            }

            if (hasValueChanged(jurorResponse.getAddressLine3(),
                jurorPersonalDetailsDto.getAddressLineThree(), ADDRESS_LINE3, jurorNumber, replyMethod)) {
                jurorResponse.setAddressLine3(jurorPersonalDetailsDto.getAddressLineThree());
            }

            if (hasValueChanged(jurorResponse.getAddressLine4(), jurorPersonalDetailsDto.getAddressTown(),
                ADDRESS_LINE4, jurorNumber, replyMethod)) {
                jurorResponse.setAddressLine4(jurorPersonalDetailsDto.getAddressTown());
            }

            if (hasValueChanged(jurorResponse.getAddressLine5(), jurorPersonalDetailsDto.getAddressCounty(),
                ADDRESS_LINE5, jurorNumber, replyMethod)) {
                jurorResponse.setAddressLine5(jurorPersonalDetailsDto.getAddressCounty());
            }

            if (hasValueChanged(jurorResponse.getPostcode(),
                jurorPersonalDetailsDto.getAddressPostcode(), POSTCODE, jurorNumber, replyMethod)) {
                jurorResponse.setPostcode(jurorPersonalDetailsDto.getAddressPostcode());
            }
        }
    }

    private void checkUpdatesToJurorContactDetails(AbstractJurorResponse jurorResponse,
                                                   JurorPersonalDetailsDto jurorPersonalDetailsDto,
                                                   String jurorNumber) {
        String replyMethod = jurorResponse.getReplyType().getDescription();

        //TODO: CONTACT DETAILS UPDATES FOR DIGITAL
        if (jurorResponse.getReplyType().getType().equals(ReplyMethod.PAPER.getDescription())) {
            if (hasValueChanged(jurorResponse.getEmail(), jurorPersonalDetailsDto.getEmailAddress(),
                EMAIL_ADDRESS, jurorNumber, replyMethod)) {
                jurorResponse.setEmail(jurorPersonalDetailsDto.getEmailAddress());
            }

            if (hasValueChanged(jurorResponse.getPhoneNumber(),
                jurorPersonalDetailsDto.getPrimaryPhone(), PRIMARY_PHONE, jurorNumber, replyMethod)) {
                jurorResponse.setPhoneNumber(jurorPersonalDetailsDto.getPrimaryPhone());
            }

            if (hasValueChanged(jurorResponse.getAltPhoneNumber(),
                jurorPersonalDetailsDto.getSecondaryPhone(), SECONDARY_PHONE, jurorNumber, replyMethod)) {
                jurorResponse.setAltPhoneNumber(jurorPersonalDetailsDto.getSecondaryPhone());
            }
        }
    }

    private void checkUpdatesToJurorThirdParty(AbstractJurorResponse jurorResponse,
                                               JurorPersonalDetailsDto jurorPersonalDetailsDto, String jurorNumber) {
        String replyMethod = jurorResponse.getReplyType().getDescription();

        //TODO: THIRD PARTY UPDATES FOR DIGITAL
        if (jurorResponse.getReplyType().getType().equals(ReplyMethod.PAPER.getDescription())) {
            JurorPaperResponseDto.ThirdParty thirdParty = jurorPersonalDetailsDto.getThirdParty();
            if (thirdParty != null) {

                if (hasValueChanged(jurorResponse.getThirdPartyFName(), thirdParty.getThirdPartyFName(),
                                    THIRD_PARTY_FIRSTNAME, jurorNumber, replyMethod)) {
                    jurorResponse.setThirdPartyFName(thirdParty.getThirdPartyFName());
                }
                if (hasValueChanged(jurorResponse.getThirdPartyLName(), thirdParty.getThirdPartyLName(),
                                    THIRD_PARTY_LASTNAME, jurorNumber, replyMethod)) {
                    jurorResponse.setThirdPartyLName(thirdParty.getThirdPartyLName());
                }
                if (hasValueChanged(jurorResponse.getMainPhone(),thirdParty.getMainPhone(),
                                    THIRD_PARTY_MAIN_PHONE, jurorNumber, replyMethod)) {
                    jurorResponse.setMainPhone(thirdParty.getMainPhone());
                }
                if (hasValueChanged(jurorResponse.getOtherPhone(),thirdParty.getOtherPhone(),
                                    THIRD_PARTY_OTHER_PHONE, jurorNumber, replyMethod)) {
                    jurorResponse.setOtherPhone(thirdParty.getOtherPhone());
                }
                if (hasValueChanged(jurorResponse.getEmailAddress(),thirdParty.getEmailAddress(),
                                    THIRD_PARTY_EMAIL_ADDRESS, jurorNumber, replyMethod)) {
                    jurorResponse.setEmailAddress(thirdParty.getEmailAddress());
                }

                if (hasValueChanged(jurorResponse.getRelationship(), thirdParty.getRelationship(),
                                    THIRD_PARTY_RELATIONSHIP, jurorNumber, replyMethod)) {
                    jurorResponse.setRelationship(thirdParty.getRelationship());
                }

                if (hasValueChanged(jurorResponse.getThirdPartyReason(),thirdParty.getThirdPartyReason(),
                                    THIRD_PARTY_REASON, jurorNumber, replyMethod)) {
                    jurorResponse.setThirdPartyReason(thirdParty.getThirdPartyReason());
                }
                if (hasValueChanged(jurorResponse.getThirdPartyOtherReason(),thirdParty.getThirdPartyOtherReason(),
                                    THIRD_PARTY_OTHER_REASON, jurorNumber, replyMethod)) {
                    jurorResponse.setThirdPartyOtherReason(thirdParty.getThirdPartyOtherReason());
                }
                if (hasValueChanged(jurorResponse.getJurorPhoneDetails(),thirdParty.getUseJurorPhoneDetails(),
                                    String.valueOf(THIRD_PARTY_CONTACT_JUROR_BY_PHONE), jurorNumber, replyMethod)) {
                    jurorResponse.setJurorPhoneDetails(thirdParty.getUseJurorPhoneDetails());
                }
                if (hasValueChanged(jurorResponse.getJurorEmailDetails(),thirdParty.getUseJurorEmailDetails(),
                                    String.valueOf(THIRD_PARTY_CONTACT_JUROR_BY_EMAIL), jurorNumber, replyMethod)) {
                    jurorResponse.setJurorEmailDetails(thirdParty.getUseJurorEmailDetails());

                }
            }
        }
    }



    private void processStraightThroughResponse(AbstractJurorResponse jurorResponse,
                                                JurorPool jurorPool, BureauJwtPayload payload) {
        log.debug(String.format("Juror: %s. Enter juror processStraightThroughResponse", jurorPool.getJurorNumber()));
        LocalDate poolRequestReturnDate = jurorPool.getPool().getReturnDate();

        if (jurorResponse.getDateOfBirth() != null && (poolRequestReturnDate != null)) {
            if (jurorResponse.getReplyType().getType().equals(ReplyMethod.PAPER.getDescription())) {
                PaperResponse paperResponse = (PaperResponse) jurorResponse;
                if (straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(paperResponse,
                    poolRequestReturnDate,
                    jurorPool)) {
                    straightThroughProcessorService.processAgeDisqualification(paperResponse, poolRequestReturnDate,
                        jurorPool, payload);
                }
            } else if (jurorResponse.getReplyType().getType().equals(ReplyMethod.DIGITAL.getDescription())) {
                DigitalResponse jurorDigitalResponse = (DigitalResponse) jurorResponse;

                if (straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(jurorDigitalResponse,
                    poolRequestReturnDate, jurorPool)) {
                    straightThroughProcessorService.processAgeDisqualification(jurorDigitalResponse, jurorPool,
                        payload);
                }
            }
        }
        log.debug(String.format("Juror: %s. Exit juror processStraightThroughResponse", jurorPool.getJurorNumber()));
    }

    @Transactional
    private void setResponseProcessingStatusToClosed(AbstractJurorResponse jurorResponse) {
        if (jurorResponse.isClosed()) {
            return; //Closed records are static as such we should not update
        }
        jurorResponse.setProcessingStatus(jurorResponseAuditRepositoryMod, ProcessingStatus.CLOSED);
        if (jurorResponse.getStaff() == null) {
            log.info(String.format("No staff assigned to response for Juror %s", jurorResponse));
            User staff = userRepository.findByUsername(SecurityUtil.getActiveLogin());
            jurorResponse.setStaff(staff);
            log.info(String.format("Assigned current user to response for Juror %s", jurorResponse));
        }
        if (jurorResponse instanceof PaperResponse paperResponse) {
            mergeService.mergePaperResponse(paperResponse, SecurityUtil.getActiveLogin());
        } else if (jurorResponse instanceof DigitalResponse digitalResponse) {
            mergeService.mergeDigitalResponse(digitalResponse, SecurityUtil.getActiveLogin());
        }
    }

    @Override
    public void setResponseProcessingStatusToClosed(String jurorNumber) {
        AbstractJurorResponse jurorResponse = jurorDigitalResponseRepository.findByJurorNumber(jurorNumber);
        if (jurorResponse == null) {
            jurorResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        }

        if (jurorResponse != null) {
            setResponseProcessingStatusToClosed(jurorResponse);
        }
    }
}
