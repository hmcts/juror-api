package uk.gov.hmcts.juror.api.bureau.service;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
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
    private final JurorResponseRepository jurorResponseRepository;
    private final PoolRepository poolRepository;

    @Autowired
    public JurorCommsSuperUrgentSendToCourtServiceImpl(
        final JurorCommsNotificationService jurorCommsNotificationService,
        final JurorResponseRepository jurorResponseRepository,
        final PoolRepository poolRepository,
        final ResponseInspector responseInspector) {
        Assert.notNull(jurorCommsNotificationService, "JurorCommsNotificationService cannot be null.");
        Assert.notNull(jurorResponseRepository, "jurorResponseRepository cannot be null.");
        Assert.notNull(poolRepository, "PoolRepository cannot be null.");
        Assert.notNull(responseInspector, "ResponseInspector cannot be null");
        this.jurorCommsNotificationService = jurorCommsNotificationService;
        this.jurorResponseRepository = jurorResponseRepository;
        this.poolRepository = poolRepository;
        this.responseInspector = responseInspector;
    }

    /**
     * Implements a specific job execution.
     * Processes entries in the Juror.pool table and sends the appropriate email notifications to
     * the juror for juror where they have been transferred to court.
     */
    @Override
    @Transactional
    public void processSuperUrgent(final String jurorId) {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Super Urgent Sent To Court Comms Processing : Started - {}", dateFormat.format(new Date()));

        final Pool poolDetails = poolRepository.findByJurorNumber(jurorId);
        final JurorResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorId);

        if (jurorResponse.getWelsh().equals(Boolean.TRUE)) {
            poolDetails.setWelsh(Boolean.TRUE);
        }
        int youngestJurorAgeAllowed = responseInspector.getYoungestJurorAgeAllowed();
        int tooOldJurorAge = responseInspector.getTooOldJurorAge();
        int age = responseInspector.getJurorAgeAtHearingDate(
            jurorResponse.getDateOfBirth(),
            poolDetails.getHearingDate()
        );

        boolean thirdPartyResponse = isThirdPartyResponse(jurorResponse);

        log.trace("Super Urgent Sent To Court Comms Service :  jurorNumber {}", poolDetails.getJurorNumber());
        try {
            if (age >= youngestJurorAgeAllowed && age < tooOldJurorAge) {
                //Email
                if (jurorResponse.getEmail() != null && !thirdPartyResponse) {
                    jurorCommsNotificationService.sendJurorComms(
                        poolDetails,
                        JurorCommsNotifyTemplateType.SU_SENT_TO_COURT,
                        null,
                        null,
                        false
                    );
                }
                //SMS
                if (jurorResponse.getPhoneNumber() != null && !thirdPartyResponse) {
                    jurorCommsNotificationService.sendJurorCommsSms(
                        poolDetails,
                        JurorCommsNotifyTemplateType.SU_SENT_TO_COURT,
                        null,
                        null,
                        true
                    );
                }
                //update regardless - stop processing next time.
                update(poolDetails);
            } else {
                log.info(
                    "Age Restriction: Juror {} is yonger than {} or older than {} on summon date",
                    poolDetails.getJurorNumber(),
                    youngestJurorAgeAllowed,
                    tooOldJurorAge
                );
            }

        } catch (JurorCommsNotificationServiceException e) {
            log.error("Unable to send super urgent sent to court comms for {} : {} {}", poolDetails.getJurorNumber(),
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
     * Updates pool notifciation.
     * @param poolDetails
     */
    private void update(Pool poolDetails) {
        log.trace("Inside update .....");
        poolDetails.setNotifications(NOTIFICATION_SENT);
        poolRepository.save(poolDetails);
        log.trace("Updating pool notification as sent ({})... ", NOTIFICATION_SENT);
    }

    private boolean isThirdPartyResponse(JurorResponse response) {
        return !ObjectUtils.isEmpty(response.getThirdPartyReason())
            && !ObjectUtils.isEmpty(response.getJurorEmailDetails())
            && !response.getJurorEmailDetails();
    }
}
