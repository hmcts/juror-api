package uk.gov.hmcts.juror.api.moj.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
public class DateUtils {
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private DateUtils() {
        // empty private constructor
    }

    public static LocalDate convertToLocalDateViaMillisecond(Date dateToConvert) {
        if (dateToConvert == null) {
            return null;
        }
        return Instant.ofEpochMilli(dateToConvert.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date convertToDateViaInstant(LocalDate dateToConvert) {
        return dateToConvert == null ? null :
            Date.from(dateToConvert.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Calculate the start of the working week which a provided date falls into.
     * The start of the week is always defined as a Monday - even if it falls on a Bank-Holiday
     *
     * @param date date to be evaluated
     * @return The nearest Monday (in the past) to the provided date
     */
    public static LocalDate getStartOfWeekFromDate(LocalDate date) {
        log.debug("Enter getStartOfWeekFromDate for date {}", date);

        if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            log.debug("Provided date {} is already a Monday (start of the working week)", date);
            return date;
        }

        final int mondayIndexValue = 1;
        return date.minusDays(Math.abs(mondayIndexValue - date.getDayOfWeek().getValue()));
    }

    /**
     * Convert a String representation of a date from localised format d/M/yyyy to ISO format yyyy-MM-dd.
     *
     * @param localisedDate a String representation of a date in the format d/M/yyyy
     *                      (caters for both zero-padded and non-padded days/months)
     * @return a String representation of the date in Local ISO format (yyyy-MM-dd)
     */
    public static String convertLocalisedDateToIso(String localisedDate) {
        return LocalDate
            .from(DateTimeFormatter.ofPattern("d/M/yyyy").parse(localisedDate))
            .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Get a quantity of starting week (Monday) dates.
     * Use case for this is for getting eight weeks of start dates to create min/max bounds for returned DTO data e.g
     * . SummoningProgressResponseDTO
     *
     * @param numberOfWeeks total number of weeks wanted
     * @param startDate     the start date for calculating the number of weeks needed
     * @return A List of starting dates
     */
    public static List<LocalDate> getNumberOfStartingWeeks(int numberOfWeeks, LocalDate startDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate weekCommencing = DateUtils.getStartOfWeekFromDate(startDate);
        for (int i = 0;
             i < numberOfWeeks;
             i++) {
            dates.add(weekCommencing.plusWeeks(i));
        }
        return dates;
    }

    public static long toEpochMilli(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime fromEpochMilli(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public static long getWorkingDaysBetween(LocalDate startDate, LocalDate endDate) {
        Set<DayOfWeek> disallowedDaysOfWeek = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        return startDate.datesUntil(endDate.plusDays(1))
            .filter(localDate -> !disallowedDaysOfWeek.contains(localDate.getDayOfWeek()))
            .count();
    }
}
