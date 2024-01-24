package uk.gov.hmcts.juror.api.juror.notify;

/**
 * Type of Notify template to be used to compose the message.
 */
public enum NotifyTemplateType {
    /**
     * Acceptance (straight through). Nothing flagging response as another {@link NotifyTemplateType}
     */
    STRAIGHT_THROUGH(NotifyTemplateType.REASONABLE_ADJUSTMENTS_ALLOWED),
    /**
     * Deferral.
     */
    DEFERRAL(NotifyTemplateType.REASONABLE_ADJUSTMENTS_ALLOWED),
    /**
     * Excusal except {@link #EXCUSAL_DECEASED}! .
     */
    EXCUSAL(NotifyTemplateType.REASONABLE_ADJUSTMENTS_ALLOWED),
    /**
     * Excusal because the juror is dead.
     */
    EXCUSAL_DECEASED(!NotifyTemplateType.REASONABLE_ADJUSTMENTS_ALLOWED),
    /**
     * Disqualification due to juror being too young or too old.
     */
    DISQUALIFICATION_AGE(!NotifyTemplateType.REASONABLE_ADJUSTMENTS_ALLOWED);

    /**
     * Constant for tagging the {@link #NotifyTemplateType(boolean)} as to whether an enum value provides a
     * "with adjustments" template.
     */
    private static final boolean REASONABLE_ADJUSTMENTS_ALLOWED = true;

    /**
     * Is this enum adjustable? .
     */
    private final boolean adjustable;

    /**
     * Constant prefix for all key names.
     */
    static final String KEY_PREFIX = "NOTIFY_";

    /**
     * Welsh language token in key.
     */
    static final String WELSH_TOKEN = "CY_";

    static final String ADJUSTMENT_SUFFIX = "_ADJ";

    static final String FIRST_PERSON_TOKEN = "1ST_";

    static final String THIRD_PARTY_TOKEN = "3RD_";

    static final String INELIGIBILITY_SUFFIX = "_INEL";

    /**
     * Suffix to apply to the template key name.
     */
    private final String adjustSuffix;

    NotifyTemplateType(boolean adjust) {
        this.adjustable = adjust;
        if (adjust) {
            this.adjustSuffix = ADJUSTMENT_SUFFIX;
        } else {
            this.adjustSuffix = "";
        }
    }

    /**
     * Get the database key name.
     *
     * @param withAdjustments    Should the key name be for the with adjustments
     * @param thirdPartyResponse Is this a 3rd party response?
     * @param welshLanguage      Is a Welsh language response?
     * @param withIneligibility  Has answered negatively regarding eligibility
     * @return The key name String
     */
    public String getAppSettingKey(boolean withAdjustments, boolean thirdPartyResponse, boolean welshLanguage,
                                   boolean withIneligibility) {
        final String respondentType = thirdPartyResponse
            ?
            THIRD_PARTY_TOKEN
            :
                FIRST_PERSON_TOKEN;
        final String prefix = welshLanguage
            ?
            KEY_PREFIX.concat(WELSH_TOKEN)
            :
                KEY_PREFIX;
        return prefix
            + respondentType
            + this.name()
            + (withAdjustments
            ?
            adjustSuffix
            :
                "")
            + ((this != EXCUSAL_DECEASED && this != DISQUALIFICATION_AGE)
            && withIneligibility
            ?
            INELIGIBILITY_SUFFIX
            :
                "");
    }

    /**
     * Is this enum adjustable? .
     *
     * @return Can have reasonable adjustments.
     */
    public boolean isAdjustable() {
        return adjustable;
    }
}
