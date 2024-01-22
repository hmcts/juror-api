package uk.gov.hmcts.juror.api.bureau.notify;

/**
 * Type of Notify template to be used to compose the message.
 */
public enum JurorCommsNotifyTemplateType {
    /**
     * confirmed/deferral/excusal/disqualified comms notification.
     */
    LETTER_COMMS,
    /**
     * non super urgent sent to court comms notoification.
     */
    SENT_TO_COURT,
    /**
     * super urgent sent to court comms notoification.
     */
    SU_SENT_TO_COURT,
    /**
     * weekly informational comms notification.
     */
    COMMS;

    /**
     * language token in key.
     */
    public static final String WELSH_TOKEN;

    static {
        WELSH_TOKEN = "_CY";
    }

    public static final String ENGLISH_TOKEN;

    static {
        ENGLISH_TOKEN = "_ENG";
    }

    /**
     * weekly comms token in key.
     */
    static final String[] weeklyCommsSuffixes = new String[]{"TH_", "ST_", "ND_", "RD_"};

    /**
     * type message token in key.h
     */
    public static final String EMAIL;

    static {
        EMAIL = "_EMAIL";
    }

    public static final String SMS;

    static {
        SMS = "_SMS";
    }


    /**
     * Suffix to apply to the template key name.
     */
    private String adjustSuffix;

    JurorCommsNotifyTemplateType() {
    }

    /**
     * Get weekly the juror comms template id Key for the notify service.
     *
     * @param welshLanguage      Is a Welsh language response?
     * @param weeklyNotification determines which weekly notification to send.
     * @return The key name String
     */
    public String getNotifyTemplateKey(boolean welshLanguage, int weeklyNotification) {
        if (weeklyNotification < 0 || weeklyNotification > 3) {
            return null;
        }

        final String weeklyCommsNumber = weeklyNotification + weeklyCommsSuffixes[weeklyNotification];
        final String suffix = welshLanguage
            ?
            WELSH_TOKEN
            :
                ENGLISH_TOKEN;
        return weeklyCommsNumber
            + this.name()
            + suffix;
    }


    /**
     * Get the court Comms template id Key for the notify service.
     *
     * @param welshLanguage Is a Welsh language response?
     * @param smsComms      Is this an sms comms?
     * @return The key name String
     */
    public String getNotifyTemplateKey(boolean welshLanguage, boolean smsComms) {

        final String smsCommsType = smsComms
            ?
            SMS
            :
                EMAIL;
        final String suffix = welshLanguage
            ?
            WELSH_TOKEN
            :
                ENGLISH_TOKEN;
        return this.name() + smsCommsType + suffix;
    }
}
