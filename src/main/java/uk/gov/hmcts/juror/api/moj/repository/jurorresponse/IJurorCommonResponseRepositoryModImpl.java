package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QCombinedJurorResponse;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Collection;
import java.util.List;

public class IJurorCommonResponseRepositoryModImpl implements IJurorCommonResponseRepositoryMod {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<Tuple> getJurorResponseDetailsByUsernameAndStatus(String staffLogin,
                                                                  Collection<ProcessingStatus> processingStatus,
                                                                  Predicate... predicates) {
        JPAQueryFactory queryFactory = getJpaQueryFactory();

        JPAQuery<Tuple> query = queryFactory.select(
                QCombinedJurorResponse.combinedJurorResponse,
                QJuror.juror,
                QJurorPool.jurorPool,
                QPoolRequest.poolRequest
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJuror.juror)
            .on(QJuror.juror.jurorNumber.eq(QCombinedJurorResponse.combinedJurorResponse.jurorNumber))

            .join(QJurorPool.jurorPool).on(QJurorPool.jurorPool.juror.eq(QJuror.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))

            .where(QCombinedJurorResponse.combinedJurorResponse.staff.username.equalsIgnoreCase(staffLogin))
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.in(processingStatus))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }
        query.orderBy(QCombinedJurorResponse.combinedJurorResponse.dateReceived.asc());
        return query.fetch();
    }

    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
