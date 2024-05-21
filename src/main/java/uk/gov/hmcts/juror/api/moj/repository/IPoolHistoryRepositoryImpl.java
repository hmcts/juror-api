package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.QPoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.dateTimeTemplate;

/**
 * Custom Repository implementation for the PoolHistory entity.
 */

@Component
@Slf4j
public class IPoolHistoryRepositoryImpl implements IPoolHistoryRepository {

    @PersistenceContext
    EntityManager entityManager;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final Expression<LocalDateTime> DATE_CREATED = POOL_REQUEST.dateCreated;

    @Override
    public List<PoolHistory> findPoolHistorySincePoolCreated(String poolNumber) {
        JPAQuery<Tuple> queryFactory = new JPAQuery<>(entityManager);
        final QPoolHistory qPoolHistory = QPoolHistory.poolHistory;

        queryFactory.select(
                qPoolHistory.poolNumber.as("POOL_NO"),
                qPoolHistory.userId.as("USER_ID"),
                qPoolHistory.historyCode.as("HISTORY_CODE"),
                qPoolHistory.historyDate.as("HISTORY_DATE"),
                qPoolHistory.otherInformation.as("OTHER_INFORMATION")
            )
            .from(qPoolHistory)
            .join(POOL_REQUEST)
            .on(qPoolHistory.poolNumber.eq(POOL_REQUEST.poolNumber))
            .where(qPoolHistory.poolNumber.eq(poolNumber))
            .where(qPoolHistory.historyDate.goe(dateTimeTemplate(LocalDateTime.class, "DATEADD(SECOND, -1, {0})",
                DATE_CREATED)))
            .orderBy(qPoolHistory.historyDate.desc());

        List<Tuple> resultList = queryFactory.fetch();
        List<PoolHistory> poolHistoryList = new ArrayList<>();

        for (Tuple tuple : resultList) {
            PoolHistory poolHistory = new PoolHistory();
            poolHistory.setPoolNumber(tuple.get(0, String.class));
            poolHistory.setUserId(tuple.get(1, String.class));
            poolHistory.setHistoryCode(tuple.get(2, HistoryCode.class));
            poolHistory.setHistoryDate(tuple.get(3, LocalDateTime.class));
            poolHistory.setOtherInformation(tuple.get(4, String.class));

            poolHistoryList.add(poolHistory);
        }

        return poolHistoryList;
    }

}

