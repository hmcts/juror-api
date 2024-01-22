package uk.gov.hmcts.juror.api.validation;

import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;

import java.util.Date;

public interface ResponseInspector {
    /**
     * Did a third party submit the response?.
     *
     * @param jurorResponse Response to inspect for a third party.
     * @return If the response was submitted by a third party on behalf of the Juror.
     */
    boolean isThirdPartyResponse(JurorResponse jurorResponse);

    /**
     * Does a juror response have and adjustments to the fields?.
     *
     * @param jurorResponse Response to inspect for any changes to personal information.
     * @return If there are any changes.
     */
    boolean hasAdjustments(JurorResponse jurorResponse);

    /**
     * Is this response a Welsh language response?.
     *
     * @param jurorResponse Response to inspect for welsh language.
     * @return If the response was submitted in welsh.
     */
    boolean isWelshLanguage(JurorResponse jurorResponse);

    /**
     * Get the active contact email for this response. Active contact email is based on the contact preferences if the
     * response is a third party response.
     *
     * @param jurorResponse Response to extract active contact email
     * @return Email address
     */
    String activeContactEmail(JurorResponse jurorResponse);

    /**
     * The type of unclassified juror response.  Will only detect responses of type {@link NotifyTemplateType#EXCUSAL},
     * {@link NotifyTemplateType#DEFERRAL}. Default for other types is {@link NotifyTemplateType#DISQUALIFICATION}.
     *
     * @param jurorResponse Response to inspect for type
     * @return Response type
     * @see NotifyTemplateType
     */
    NotifyTemplateType responseType(JurorResponse jurorResponse);

    /**
     * Is the DOB of the juror too old or too young?.
     *
     * @param jurorResponse Response to inspect
     * @return Disqualified?
     */
    boolean isJurorAgeDisqualified(JurorResponse jurorResponse);

    /**
     * Does the juror response indicate the juror is ineligible?.
     *
     * @param jurorResponse Response to inspect
     * @return Ineligible?
     */
    boolean isIneligible(JurorResponse jurorResponse);

    /**
     * Does the juror response request a deferral?.
     *
     * @param jurorResponse Response to inspect
     * @return Deferral?
     */
    boolean isDeferral(JurorResponse jurorResponse);

    /**
     * Does the juror response request an excusal?.
     *
     * @param jurorResponse Response to inspect
     * @return Excusal?
     */
    boolean isExcusal(JurorResponse jurorResponse);

    /**
     * Does the juror response indicate the juror is deceased?.
     *
     * @param jurorResponse Response to inspect
     * @return Deceased?
     */
    boolean isJurorDeceased(JurorResponse jurorResponse);

    /**
     * Calculate how old a juror will be on the hearing day.
     *
     * @param birthDate   Juror DOB
     * @param hearingDate Hearing date
     * @return Juror age.
     */
    int getJurorAgeAtHearingDate(Date birthDate, Date hearingDate);

    /**
     * Get the lower age for jury service.
     *
     * @return Age
     */
    int getYoungestJurorAgeAllowed();

    /**
     * Get the upper age for jury service.
     *
     * @return Age
     */
    int getTooOldJurorAge();

    /**
     * Retrieve the pool notification for weekly comms.
     *
     * @param jurorResponse Response to inspect
     * @return notification value
     */
    int getPoolNotification(JurorResponse jurorResponse);

    /**
     * Is the response to a welsh court.
     *
     * @param jurorResponse Response to inspect
     * @return welshCourt?
     */
    boolean isWelshCourt(JurorResponse jurorResponse);
}
