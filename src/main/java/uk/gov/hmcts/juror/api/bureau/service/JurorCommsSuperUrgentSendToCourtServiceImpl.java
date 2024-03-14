package uk.gov.hmcts.juror.api.bureau.service;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * Implementation of {@link JurorCommsSuperUrgentSendToCourtService}.
 */
@Slf4j
@Service
public class JurorCommsSuperUrgentSendToCourtServiceImpl implements JurorCommsSuperUrgentSendToCourtService {
    private static final Integer NOTIFICATION_SENT = 9;
    private final JurorCommsNotificationService jurorCommsNotificationService;
    private final ResponseInspector responseInspector;

    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepositoryMod;

    private final JurorPoolRepository jurorRepository;



    @Autowired
    public JurorCommsSuperUrgentSendToCourtServiceImpl(
       final JurorCommsNotificationService jurorCommsNotificationService,
       final JurorPoolRepository jurorRepository,
       final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepositoryMod,
       final ResponseInspector responseInspector) {
        Assert.notNull(jurorCommsNotificationService, "JurorCommsNotificationService cannot be null.");
        Assert.notNull(jurorRepository, "JurorRepository cannot be null.");
        Assert.notNull(responseInspector, "ResponseInspector cannot be null");
        Assert.notNull(jurorDigitalResponseRepositoryMod, "JurorDigitalResponseRepositoryMod cannot be null");
        this.jurorCommsNotificationService = jurorCommsNotificationService;
        this.jurorRepository = jurorRepository;
        this.jurorDigitalResponseRepositoryMod = jurorDigitalResponseRepositoryMod;
        this.responseInspector = responseInspector;
    }

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror table and sends the appropriate email notifications to
     * the juror for juror where they have been transferred to court.
     */
    @Override
    @Transactional
    public void processSuperUrgent(final String jurorId) {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Super Urgent Sent To Court Comms Processing : Started - {}", dateFormat.format(new Date()));

       // final Pool poolDetails = poolRepository.findByJurorNumber(jurorId);
        final JurorPool jurorDetails = jurorRepository.findByJurorJurorNumber(jurorId);
       // final JurorResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorId);
        final DigitalResponse digitalResponse = jurorDigitalResponseRepositoryMod.findByJurorNumber(jurorId);

        if (digitalResponse.getWelsh().equals(Boolean.TRUE)) {
            jurorDetails.getJuror().setWelsh(Boolean.TRUE);
        }
        int youngestJurorAgeAllowed = responseInspector.getYoungestJurorAgeAllowed();
        int tooOldJurorAge = responseInspector.getTooOldJurorAge();
        int age = responseInspector.getJurorAgeAtHearingDate(
            jurorDetails.getJuror().getDateOfBirth(),
            jurorDetails.getNextDate()
        );

        boolean thirdPartyResponse = isThirdPartyResponse(digitalResponse);

        log.trace("Super Urgent Sent To Court Comms Service :  jurorNumber {}", jurorDetails.getJurorNumber());
        try {
            if (age >= youngestJurorAgeAllowed && age < tooOldJurorAge) {
                //Email
                if (digitalResponse.getEmail() != null && !thirdPartyResponse) {
                    jurorCommsNotificationService.sendJurorComms(
                        jurorDetails,
                        JurorCommsNotifyTemplateType.SU_SENT_TO_COURT,
                        null,
                        null,
                        false
                    );
                }
                //SMS
                if (digitalResponse.getPhoneNumber() != null && !thirdPartyResponse) {
                    jurorCommsNotificationService.sendJurorCommsSms(
                        jurorDetails,
                        JurorCommsNotifyTemplateType.SU_SENT_TO_COURT,
                        null,
                        null,
                        true
                    );
                }
                //update regardless - stop processing next time.
                update(jurorDetails);
            } else {
                log.info(
                    "Age Restriction: Juror {} is yonger than {} or older than {} on summon date",
                    jurorDetails.getJurorNumber(),
                    youngestJurorAgeAllowed,
                    tooOldJurorAge
                );
            }

        } catch (JurorCommsNotificationServiceException e) {
            log.error("Unable to send super urgent sent to court comms for {} : {} {}", jurorDetails.getJurorNumber(),
                e.getMessage(), e.getCause().toString()
            );
        } catch (Exception e) {
            log.error("Super Urgent Sent To Court Comms Processing : Juror Comms failed : {}", e.getMessage());
            throw new JurorCommsNotificationServiceException("Super Urgent Sent To Court Comms Processing failed. "
                + e.getMessage(), e.getCause());
        }
        log.info("Super Urgent Sent To Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
    }

    /***
     * Updates juror notification.
     * @param jurorDetails
     */
    private void update(JurorPool jurorDetails) {
        log.trace("Inside update .....");
        jurorDetails.getJuror().setNotifications(NOTIFICATION_SENT);
        jurorRepository.save(jurorDetails);
        log.trace("Updating juror notification as sent ({})... ", NOTIFICATION_SENT);
    }

    private boolean isThirdPartyResponse(DigitalResponse response) {
        return !ObjectUtils.isEmpty(response.getThirdPartyReason())
            && !ObjectUtils.isEmpty(response.getJurorEmailDetails())
            && !response.getJurorEmailDetails();
    }
}
