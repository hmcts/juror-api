package uk.gov.hmcts.juror.api.validation;

import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

import java.time.LocalDate;

public interface ResponseInspector {
    /**
     * Did a third party submit the response?.
     *
     * @param digitalResponse Response to inspect for a third party.
     * @return If the response was submitted by a third party on behalf of the Juror.
     */
    // boolean isThirdPartyResponse(JurorResponse jurorResponse);
    boolean isThirdPartyResponse(DigitalResponse digitalResponse);

    /**
     * Does a juror response have and adjustments to the fields?.
     *
     * @param digitalResponse Response to inspect for any changes to personal information.
     * @return If there are any changes.
     */
    boolean hasAdjustments(DigitalResponse digitalResponse);

    /**
     * Is this response a Welsh language response?.
     *
     * @param digitalResponse Response to inspect for welsh language.
     * @return If the response was submitted in welsh.
     */
    boolean isWelshLanguage(DigitalResponse digitalResponse);

    /**
     * Get the active contact email for this response. Active contact email is based on the contact preferences if the
     * response is a third party response.
     *
     * @param digitalResponse Response to extract active contact email
     * @return Email address
     */
    String activeContactEmail(DigitalResponse digitalResponse);

    /**
     * The type of unclassified juror response.  Will only detect responses of type {@link NotifyTemplateType#EXCUSAL},
     * {@link NotifyTemplateType#DEFERRAL}. Default for other types is {@link NotifyTemplateType#DISQUALIFICATION_AGE}.
     *
     * @param digitalResponse Response to inspect for type
     * @return Response type
     * @see NotifyTemplateType
     */
    NotifyTemplateType responseType(DigitalResponse digitalResponse);

    /**
     * Is the DOB of the juror too old or too young?.
     *
     * @param digitalResponse Response to inspect
     * @return Disqualified?
     */
    boolean isJurorAgeDisqualified(DigitalResponse digitalResponse);

    /**
     * Does the juror response indicate the juror is ineligible?.
     *
     * @param digitalResponse Response to inspect
     * @return Ineligible?
     */
    boolean isIneligible(DigitalResponse digitalResponse);

    /**
     * Does the juror response request a deferral?.
     *
     * @param digitalResponse Response to inspect
     * @return Deferral?
     */
    boolean isDeferral(DigitalResponse digitalResponse);

    /**
     * Does the juror response request an excusal?.
     *
     * @param digitalResponse Response to inspect
     * @return Excusal?
     */
    boolean isExcusal(DigitalResponse digitalResponse);

    /**
     * Does the juror response indicate the juror is deceased?.
     *
     * @param digitalResponse Response to inspect
     * @return Deceased?
     */
    boolean isJurorDeceased(DigitalResponse digitalResponse);

    /**
     * Calculate how old a juror will be on the hearing day.
     *
     * @param birthDate   Juror DOB
     * @param hearingDate Hearing date
     * @return Juror age.
     */
    int getJurorAgeAtHearingDate(LocalDate birthDate, LocalDate hearingDate);

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
     * @param digitalResponse Response to inspect
     * @return notification value
     */
    int getPoolNotification(DigitalResponse digitalResponse);

    /**
     * Is the response to a welsh court.
     *
     * @param digitalResponse Response to inspect
     * @return welshCourt?
     */
    boolean isWelshCourt(DigitalResponse digitalResponse);
}
