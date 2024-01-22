package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetail;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.Pool;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Methods for calculating the urgency of juror responses.
 */
public interface UrgencyService {
    /**
     * Performs {@link #flagSlaOverdueForResponse(BureauJurorDetail)} on a list of {@link BureauJurorDetail}.
     *
     * @param details details to flag
     * @return List of details with SLA overdue flagged
     */
    List<BureauJurorDetail> flagSlaOverdueFromList(List<BureauJurorDetail> details);

    /**
     * Set the SLA overdue flag on a {@link BureauJurorDetail}.
     *
     * @param bureauJurorDetail Juror response to set SLA overdue flag on
     * @return Updated juror response
     * @throws UrgencyServiceImpl.AppSettingException Failed to retrieve required constants for urgency thresholds
     */
    BureauJurorDetail flagSlaOverdueForResponse(BureauJurorDetail bureauJurorDetail);

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
    LocalDateTime addWorkingDays(LocalDateTime date, Integer workingDays);

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
     * @param response    response to set flags on
     * @param poolDetails pool details the response relates to
     */
    void setUrgencyFlags(JurorResponse response, Pool poolDetails);

    /**
     * get super urgent based on current date, added for scheduler.
     *
     * @param response
     * @param poolDetails
     * @return
     */
    boolean isUrgent(JurorResponse response, Pool poolDetails);

    /**
     * get super urgent based on current date, added for scheduler.
     *
     * @param response
     * @param poolDetails
     * @return
     */
    boolean isSuperUrgent(JurorResponse response, Pool poolDetails);


}
