package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.QCurrentlyDeferred;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;

import java.util.List;

/**
 * Custom Repository implementation for the JurorPool entity.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public class ICurrentlyDeferredRepositoryImpl implements ICurrentlyDeferredRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;

    @Override
    public List<Tuple> getDeferralsByCourtLocationCode(BureauJwtPayload payload, String courtLocation) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        final QCurrentlyDeferred qCurrentlyDeferred = QCurrentlyDeferred.currentlyDeferred;
        return queryFactory.select(
                qCurrentlyDeferred.locCode.as("COURT_LOCATION"),
                qCurrentlyDeferred.jurorNumber.as("JUROR_NO"),
                JUROR.firstName.as("FIRST_NAME"),
                JUROR.lastName.as("LAST_NAME"),
                POOL_REQUEST.poolNumber.as("POOL_NO"),
                qCurrentlyDeferred.deferredTo.as("DEFER_TO")
            )
            .from(qCurrentlyDeferred)
            .join(JUROR_POOL)
            .on(qCurrentlyDeferred.jurorNumber.eq(JUROR.jurorNumber)
                .and(qCurrentlyDeferred.owner.eq(JUROR_POOL.owner)))
            .join(JUROR)
            .on(JUROR_POOL.juror.eq(JUROR))
            .join(POOL_REQUEST)
            .on(JUROR_POOL.pool.eq(POOL_REQUEST))
            .where(qCurrentlyDeferred.locCode.eq(courtLocation))
            .where(qCurrentlyDeferred.owner.eq(payload.getOwner()))
            .where(JUROR_POOL.isActive.isTrue())
            .fetch();
    }
}
