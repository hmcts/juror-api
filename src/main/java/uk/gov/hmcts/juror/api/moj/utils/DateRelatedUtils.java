package uk.gov.hmcts.juror.api.moj.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
public final class DateRelatedUtils {

    private DateRelatedUtils() {

    }

    public static Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant());
    }

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }
}
