package uk.gov.hmcts.juror.api.moj.utils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CourtLocationUtils {

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

}
