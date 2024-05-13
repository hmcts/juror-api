package uk.gov.hmcts.juror.api.bureau.service;


import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Methods for calculating the urgency of juror responses.
 */
public interface UrgencyService {
    /**
     * Performs {@link #flagSlaOverdueForResponse(uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail)} on a list of
     * {@link ModJurorDetail}.
     *
     * @param details details to flag
     * @return List of details with SLA overdue flagged
     */
    List<ModJurorDetail> flagSlaOverdueFromList(List<ModJurorDetail> details);

    /**
     * Set the SLA overdue flag on a {@link ModJurorDetail}.
     *
     * @param modJurorDetail Juror response to set SLA overdue flag on
     * @return Updated juror response
     * @throws UrgencyServiceImpl.AppSettingException Failed to retrieve required constants for urgency thresholds
     */
    ModJurorDetail flagSlaOverdueForResponse(ModJurorDetail modJurorDetail);

    boolean slaBreached(
        ProcessingStatus processingStatus,
        LocalDate dateReceived
    );

    /**
     * The friday before the friday before a date.  (The 2nd friday before the date).
     *
     * @param jurySlotDay Date to base the the calculation on.
     * @return LocalDate of the Friday of the <b>week before</b> the week before jurySlotDay.
     */
    LocalDateTime fridayCutOff(LocalDateTime jurySlotDay);

    /**
     * Add and amount of working days from a date.
     *
     * @param date        The start date
     * @param workingDays Number of working days to add
     * @return The adjusted date
     */
    LocalDate addWorkingDays(LocalDate date, Integer workingDays);

    /**
     * Subtract and amount of working days from a date.
     *
     * @param date        The start date
     * @param workingDays Number of working days to subtract
     * @return The adjusted date
     */
    LocalDateTime subtractWorkingDays(LocalDateTime date, Integer workingDays);

    /**
     * Set urgent / superUrgent flags on a juror response.
     *
     * @param response     response to set flags on
     * @param jurorDetails pool details the response relates to
     */
    void setUrgencyFlags(DigitalResponse response, JurorPool jurorDetails);

    /**
     * get super urgent based on current date, added for scheduler.
     *
     
     
     */
    boolean isUrgent(DigitalResponse response, JurorPool jurorDetails);

    /**
     * get super urgent based on current date, added for scheduler.
     *
     
     
     */
    boolean isSuperUrgent(DigitalResponse response, JurorPool jurorDetails);


}
