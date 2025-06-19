package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QCombinedJurorResponse;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IJurorCommonResponseRepositoryModImpl implements IJurorCommonResponseRepositoryMod {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<Tuple> getJurorResponseDetailsByUsernameAndStatus(String staffLogin,
                                                                  Collection<ProcessingStatus> processingStatus,
                                                                  Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse,
                QJurorPool.jurorPool,
                QPoolRequest.poolRequest
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .where(QCombinedJurorResponse.combinedJurorResponse.staff.username.eq(staffLogin))
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.in(processingStatus))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }
        query.orderBy(QCombinedJurorResponse.combinedJurorResponse.dateReceived.asc());
        return query.fetch();
    }

    @Override
    public Map<ProcessingStatus, Long> getJurorResponseCounts(Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus,
                QCombinedJurorResponse.combinedJurorResponse.count()
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }

        return query.groupBy(QCombinedJurorResponse.combinedJurorResponse.processingStatus)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.processingStatus),
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.count())
            ));
    }


@Override
    public List<Tuple> getJurorResponseDetailsByCourtAndStatus(String locCode,
                                                                Collection<ProcessingStatus> processingStatus,
                                                                Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse,
                QJurorPool.jurorPool,
                QPoolRequest.poolRequest
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .where(QPoolRequest.poolRequest.owner.eq(locCode))
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.in(processingStatus))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNotNull())
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
