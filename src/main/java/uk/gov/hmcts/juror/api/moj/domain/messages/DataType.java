package uk.gov.hmcts.juror.api.moj.domain.messages;

import uk.gov.hmcts.juror.api.config.Settings;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.BiFunction;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.INVALID_FORMAT;

public enum DataType {
    NONE((s, b) -> s, "N/A"),
    STRING((s, b) -> s, "N/A"),
    DATE(DataType::toDate, DataType.DATE_FROM_STRING),
    TIME(DataType::toTime, DataType.TIME_FROM_FORMAT_STRING);


    private static final String TIME_FROM_FORMAT_STRING = "HH:mm";
    private static final String TIME_TO_FORMAT_STRING = "HH:mm";

    private static final String DATE_FROM_STRING = "yyyy-MM-dd";
    private static final String DATE_TO_STRING = "dd/MM/yyyy";


    private final BiFunction<String, Boolean, String> dataConvertor;
    private final String formatText;

    DataType(BiFunction<String, Boolean, String> dataConvertor, String formatText) {
        this.dataConvertor = dataConvertor;
        this.formatText = formatText;
    }

    public String convertData(String value, boolean isWelsh) {
        try {
            return dataConvertor.apply(value, isWelsh);
        } catch (Throwable throwable) {
            throw new MojException.BusinessRuleViolation(
                "Invalid must be in format "
                    + "'" + getFormat() + "'",
                INVALID_FORMAT, null, throwable);
        }
    }

    public String getFormat() {
        return formatText;
    }

    static String toTime(String oldTime, boolean isWelsh) {
        Locale locale = getLocale(isWelsh);
        LocalTime time = LocalTime.parse(oldTime, DateTimeFormatter.ofPattern(TIME_FROM_FORMAT_STRING, locale));
        return time.format(DateTimeFormatter.ofPattern(TIME_TO_FORMAT_STRING, locale));
    }

    static String toDate(String oldDate, boolean isWelsh) {
        Locale locale = getLocale(isWelsh);
        LocalDate date = LocalDate.parse(oldDate, DateTimeFormatter.ofPattern(DATE_FROM_STRING, locale));
        return date.format(DateTimeFormatter.ofPattern(DATE_TO_STRING, locale));
    }

    private static Locale getLocale(boolean isWelsh) {
        return isWelsh ? Settings.LOCALE_WELSH : Settings.LOCALE;
    }
}
