package uk.gov.hmcts.juror.api.moj.utils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysQueries;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;

import java.time.LocalDate;

import static java.time.DayOfWeek.SATURDAY;


public final class CourtLocationUtils {
    private CourtLocationUtils() {

    }

    /**
     * Lookup function to check whether a location code exists as a key in the Welsh Court Location table.
     *
     * @param welshCourtLocationRepository JPA interface to the database to generate and execute SQL queries
     * @param locCode                      3-digit numeric string used as the unique key for court locations
     * @return boolean result indicating whether the location is in the Welsh Court Location (true) or not (false)
     */
    public static boolean isWelshCourtLocation(@NotNull WelshCourtLocationRepository welshCourtLocationRepository,
                                               @NotBlank String locCode) {
        return welshCourtLocationRepository.findById(locCode).isPresent();
    }

    public static CourtLocation validateAccessToCourtLocation(String locCode, String owner,
                                                              CourtLocationRepository courtLocationRepository) {
        // validate that the user has access to the location
        CourtLocation courtLocation = RepositoryUtils.retrieveFromDatabase(locCode, courtLocationRepository);

        if (!owner.equals(courtLocation.getOwner())) {
            throw new MojException.Forbidden("Cannot access court details for this location "
                + locCode, null);
        }

        return courtLocation;
    }

    public static LocalDate getNextWorkingDay(String locCode) {
        LocalDate nextWorkingDay = LocalDate.now().plusDays(1);

        nextWorkingDay = checkWeekend(nextWorkingDay);

        // check if the day is a public holiday
        while (HolidaysQueries.isCourtHoliday(locCode, nextWorkingDay).equals(true)) {
            nextWorkingDay = nextWorkingDay.plusDays(1);
            nextWorkingDay = checkWeekend(nextWorkingDay);
        }
        return nextWorkingDay;
    }

    public static LocalDate checkWeekend(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SATURDAY:
                return date.plusDays(2);
            case SUNDAY:
                return date.plusDays(1);
            default:
                return date;
        }
    }

}
