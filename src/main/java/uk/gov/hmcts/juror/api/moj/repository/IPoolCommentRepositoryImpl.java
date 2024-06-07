package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.domain.QPoolComment;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository implementation for the PoolComment entity.
 */

@Component
@Slf4j
public class IPoolCommentRepositoryImpl implements IPoolCommentRepository {

    @PersistenceContext
    EntityManager entityManager;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QPoolComment POOL_COMMENT = QPoolComment.poolComment;

    @Override
    public List<Tuple> findPoolCommentsForLocationsAndDates(List<String> locCodes,
                                                    LocalDate dateFrom,
                                                    LocalDate dateTo) {
        JPAQuery<Tuple> queryFactory = new JPAQuery<>(entityManager);
        queryFactory.select(
                POOL_REQUEST.courtLocation.locCode.as("loc_code"),
                POOL_COMMENT.pool.poolNumber.as("pool_no"),
                POOL_COMMENT.poolComment.comment.as("comment"),
                POOL_COMMENT.numberRequested.as("no_requested")
            )
            .from(POOL_REQUEST)
            .join(POOL_COMMENT)
            .on(POOL_REQUEST.poolNumber.eq(POOL_COMMENT.pool.poolNumber))
            .where(POOL_REQUEST.courtLocation.locCode.in(locCodes))
            .where(POOL_REQUEST.returnDate.between(dateFrom, dateTo))
            .orderBy(POOL_REQUEST.courtLocation.locCode.asc(), POOL_REQUEST.poolNumber.asc());

        return queryFactory.fetch();
    }
}

