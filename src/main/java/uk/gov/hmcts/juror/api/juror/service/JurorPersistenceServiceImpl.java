package uk.gov.hmcts.juror.api.juror.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.AppSetting;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.AppSettingRepository;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class JurorPersistenceServiceImpl implements JurorPersistenceService {
    private final JurorService jurorService;
    private final StraightThroughProcessor straightThroughProcessor;
    private final JurorNotificationService jurorNotificationService;
    private final AppSettingRepository appSettingRepository;
    private final ResponseInspector responseInspector;

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public Boolean persistJurorResponse(final JurorResponseDto responseDto) {

        final DigitalResponse savedResponse = jurorService.saveResponse(responseDto);

        // BEGIN: Straight-throughs (Should be non-urgent at this stage JDB-1862)

        log.debug("Processing Juror {} Response for a straight through", savedResponse.getJurorNumber());

        if (allowStraightThroughProcessingType(StraightThroughType.ACCEPTANCE)) {
            try {
                straightThroughProcessor.processAcceptance(savedResponse);

                log.info(
                    "Success: Processed juror {} as a straight through acceptance",
                    savedResponse.getJurorNumber()
                );
                // send notification receipt email
                sendNotificationResponse(savedResponse, NotifyTemplateType.STRAIGHT_THROUGH);
                return Boolean.TRUE;// prevent further processing of straight through due to success.
            } catch (StraightThroughProcessingServiceException stpse) {
                log.info("Failed to process Juror {} Response as a straight through acceptance: {} - {}",
                    savedResponse.getJurorNumber(), stpse.getClass().getSimpleName(), stpse.getMessage()
                );
            }
        }

        if (allowStraightThroughProcessingType(StraightThroughType.DECEASED_EXCUSAL)) {
            try {
                straightThroughProcessor.processDeceasedExcusal(savedResponse);
                log.info(
                    "Success: Processed juror {} as a straight-through deceased-excusal",
                    savedResponse.getJurorNumber()
                );
                // send notification receipt email
                sendNotificationResponse(savedResponse, NotifyTemplateType.EXCUSAL_DECEASED);
                return Boolean.TRUE;// prevent further processing of straight through due to success.
            } catch (StraightThroughProcessingServiceException stpse) {
                log.info("Failed to process Juror {} Response as a straight-through deceased-excusal: {} - {}",
                    savedResponse.getJurorNumber(), stpse.getClass().getSimpleName(), stpse.getMessage()
                );
            }
        }

        if (allowStraightThroughProcessingType(StraightThroughType.AGE_EXCUSAL)) {
            try {
                straightThroughProcessor.processAgeExcusal(savedResponse);
                log.info(
                    "Success: Processed juror {} as a straight-through age-excusal",
                    savedResponse.getJurorNumber()
                );
                // send notification receipt email
                sendNotificationResponse(savedResponse, NotifyTemplateType.DISQUALIFICATION_AGE);
                return Boolean.TRUE;// prevent further processing of straight through due to success.
            } catch (StraightThroughProcessingServiceException stpse) {
                log.info("Failed to process Juror {} Response as a straight-through age-excusal: {} - {}",
                    savedResponse.getJurorNumber(), stpse.getClass().getSimpleName(), stpse.getMessage()
                );
            }
        }

        log.debug("Juror {} Response has not qualified for a straight-through", savedResponse.getJurorNumber());
        // END: Straight-throughs


        // send notification receipt
        sendNotificationResponse(savedResponse, responseInspector.responseType(savedResponse));

        return Boolean.TRUE;
    }

    /**
     * Send response receipt notification email.
     */
    private void sendNotificationResponse(final DigitalResponse savedResponse,
                                          final NotifyTemplateType notifyTemplateType) {
        try {
            jurorNotificationService.sendResponseReceipt(savedResponse, notifyTemplateType);
        } catch (Exception e) {
            log.warn("Could not send receipt for juror {} response!", savedResponse.getJurorNumber(), e);
        }
    }

    /**
     * Allow straight through processing of specified type to proceed?.
     *
     * @return Allow straight through processing of specified type?
     */
    private boolean allowStraightThroughProcessingType(StraightThroughType straightThroughType) {
        try {
            Optional<AppSetting> optStThroTypDis = appSettingRepository.findById(straightThroughType.getDbName());
            final AppSetting straightThroughTypeDisabled = optStThroTypDis.orElse(null);
            if (null != straightThroughTypeDisabled && "true".equalsIgnoreCase(
                straightThroughTypeDisabled.getValue())) {
                // STRAIGHT_THROUGH_*TYPE*_DISABLED is set to TRUE, so it's not allowed
                if (log.isDebugEnabled()) {
                    log.debug("Straight through {} processing skipped! {}={}", straightThroughType.getReadableName(),
                        straightThroughType.getDbName(), straightThroughTypeDisabled
                    );
                }
                return false;
            } else {
                // STRAIGHT_THROUGH_*TYPE*_DISABLED entry is not found in db, so is allowed
                log.debug("Straight Through {} Processing is enabled", straightThroughType.getReadableName());
                return true;
            }
        } catch (Exception e) {
            log.warn("Failed to query application setting for Straight Through {} Processing:",
                straightThroughType.getReadableName(), e
            );
            return false;
        }
    }
}
