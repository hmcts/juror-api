package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;

import java.time.LocalDate;
import java.util.List;

public interface IJurorHistoryRepository {

    /**
     * Custom query method to allow a LocalDate type argument for the date created argument - the Entity uses the
     * LocalDateTime data type and JPA won't implicitly add a default time value so use this method when you want to
     * filter history events using just a specific date and a default time.
     *
     * @param jurorNumber 9 digit numeric string to uniquely identify a juror
     * @param dateCreated date argument (without the time part) to filter history events based on their created date
     *
     * @return List of history events for a given juror where the event occurred on or after the supplied date.
     */
    List<JurorHistory> findByJurorNumberAndDateCreatedGreaterThanEqual(String jurorNumber, LocalDate dateCreated);

}
