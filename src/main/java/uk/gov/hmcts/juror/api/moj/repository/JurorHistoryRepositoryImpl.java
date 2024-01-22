package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;

import java.time.LocalDate;
import java.util.List;

public class JurorHistoryRepositoryImpl implements IJurorHistoryRepository {


    @PersistenceContext
    EntityManager entityManager;

    private static final QJurorHistory JUROR_HISTORY = QJurorHistory.jurorHistory;

    /**
     * Custom query method to allow a LocalDate type argument for date created with a default time of midnight (start
     * of day) - the Entity uses the LocalDateTime data type and JPA doesn't add a default time value implicitly so
     * use this method when you want to filter history events from the beginning of a specified day.
     *
     * @param jurorNumber 9 digit numeric string to uniquely identify a juror
     * @param dateCreated date argument (without the time part) to filter history events based on their created date
     *
     * @return List of history events for a given juror where the event occurred on or after the supplied date (time
     *     part defaulted to midnight/start of day).
     */
    @Override
    public List<JurorHistory> findByJurorNumberAndDateCreatedGreaterThanEqual(String jurorNumber, LocalDate dateCreated) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.selectFrom(JUROR_HISTORY)
            .where(JUROR_HISTORY.jurorNumber.eq(jurorNumber))
            .where(JUROR_HISTORY.dateCreated.goe(dateCreated.atStartOfDay()))
            .fetch();

    }
}
