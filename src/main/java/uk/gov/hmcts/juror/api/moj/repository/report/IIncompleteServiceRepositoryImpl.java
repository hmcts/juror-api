package uk.gov.hmcts.juror.api.moj.repository.report;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository implementation for the incomplete service report.
 */
@SuppressWarnings("PMD.LawOfDemeter")
@Component
public class IIncompleteServiceRepositoryImpl implements IIncompleteServiceRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;

    @Override
    public List<Tuple> getIncompleteServiceByLocationAndDate(String courtLocation, LocalDate cutOffDate) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(
                JUROR.jurorNumber.as("juror_number"),
                JUROR.firstName.as("first_name"),
                JUROR.lastName.as("last_name"),
                JUROR_POOL.pool.poolNumber.as("pool_no"),
                JUROR_POOL.nextDate.as("next_date")
            )
            .from(JUROR)
            .join(JUROR_POOL)
            .on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber))
                .where(JUROR_POOL.nextDate.loe(cutOffDate))
                .where(JUROR_POOL.owner.eq(courtLocation))
            .where(JUROR_POOL.status.status.in(List.of(2, 3, 4)))
            .orderBy(JUROR.jurorNumber.asc())
                .fetch();
    }
}
