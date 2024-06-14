package uk.gov.hmcts.juror.api.validation;

/**
 * Validation constants.
 */
public final class ValidationConstants {
    public static final String NO_PIPES_REGEX = "^$|^[^|]+$";
    public static final String POSTCODE_REGEX = "^$|(([gG][iI][rR] {0,}0[aA]{2})|(("
        + "([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y]?[0-9][0-9]?)|(([a-pr-uwyzA-PR-UWYZ][0-9][a-hjkstuwA-HJKSTUW])|"
        + "([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y][0-9][abehmnprv-yABEHMNPRV-Y]))) {0,"
        + "}[0-9][abd-hjlnp-uw-zABD-HJLNP-UW-Z]{2}))$";
    public static final String SHORT_DATE_STRING_REGEX = "^[0-3][0-9]-((0[0-9])|(1[0-2]))-[0-9]{4}$";
    public static final String PHONE_PRIMARY_REGEX = "^[0-9\\s]{8,15}|$";
    public static final String PHONE_SECONDARY_REGEX = "^(?:.{8,15})|$";
    public static final String THIRD_PARTY_PHONE_PRIMARY_REGEX = "^(?:[0-9\\s]{8,15})|$";
    public static final String THIRD_PARTY_PHONE_SECONDARY_REGEX = "^(?:.{8,15})|$";
    public static final String JUROR_NUMBER = "^\\d{9}$";
    public static final String POOL_NUMBER = "^\\d{9}$";
    public static final String TRIAL_NUMBER = "^.{0,16}$";
    public static final String LOCATION_CODE = "^\\d{3}$";
    public static final String EMAIL_ADDRESS_REGEX = "^(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\"
        + ".[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f"
        + "]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)"
        + "+[A-Za-z0-9]"
        + "(?:[A-Za-z0-9-]*[A-Za-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
        + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[A-Za-z0-9-]*[A-Za-z0-9]:"
        + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])|$";
    public static final String WHITESPACE_MATCHER = "\\s+";
    public static final String MD5_HASHCODE = "^[a-fA-F0-9]{32}+$";
    public static final String TWO_DIGIT_REGEX = "^\\d{2}$";
    public static final String FOUR_DIGIT_REGEX = "^\\d{4}$";
    public static final String PHONE_NO_REGEX = "^[04(+][0-9\\s-()]{8,14}$";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TIME_FORMAT = "HH:mm";

    private ValidationConstants() {

    }
}
