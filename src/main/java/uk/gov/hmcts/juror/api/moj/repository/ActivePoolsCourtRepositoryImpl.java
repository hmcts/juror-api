package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.ActivePoolsCourt;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QPoolType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Custom Repository implementation for the ActivePools at court entity.
 */
@SuppressWarnings("PMD.LawOfDemeter")
@Component
@Slf4j
public class ActivePoolsCourtRepositoryImpl implements IActivePoolsCourtRepository {

    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;
    private static final QPoolType POOL_TYPE = QPoolType.poolType1;

    @PersistenceContext
    EntityManager entityManager;

    // function to return the active pools list based on court location or date filter
    @Override
    public List<ActivePoolsCourt> findActivePools(List<String> courts, LocalDate returnDate, String sortBy,
                                                  String order,
                                                  List<String> poolTypes) {

        log.debug("Searching for active pool requests at Court");
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        BooleanExpression activeFlags = JUROR_POOL.status.status.in(Arrays.asList(3, 4));
        BooleanExpression activeFlags2 = JUROR_POOL.status.status.in(List.of(2));

        JPAQuery<Tuple> activePoolsQuery = queryFactory.select(
                POOL_REQUEST.poolNumber.as("POOL_NO"),
                POOL_REQUEST.totalNoRequired.as("POOL_CAPACITY"),
                JUROR_POOL.isActive.count().as("JURORS_IN_POOL"),
                COURT_LOCATION.name.as("COURT_NAME"),
                POOL_TYPE.description.as("POOL_TYPE"),
                POOL_REQUEST.returnDate.as("SERVICE_START_DATE")
            )
            .from(POOL_REQUEST)
            .leftJoin(JUROR_POOL)
            .on(POOL_REQUEST.eq(JUROR_POOL.pool).and(POOL_REQUEST.owner.eq(JUROR_POOL.owner)))
            .innerJoin(COURT_LOCATION)
            .on(POOL_REQUEST.courtLocation.eq(COURT_LOCATION))
            .innerJoin(POOL_TYPE)
            .on(POOL_REQUEST.poolType.eq(POOL_TYPE))
            .where(POOL_REQUEST.owner.notEqualsIgnoreCase("400"))
            .where(POOL_REQUEST.newRequest.eq('N'))
            .where(POOL_REQUEST.poolType.description.in(poolTypes))
            .where(POOL_REQUEST.nilPool.eq(false).and(POOL_REQUEST.returnDate.after(LocalDate.now()))
                .or(JUROR_POOL.isActive.isTrue().and(activeFlags.or(activeFlags2))));

        if (courts != null) {
            activePoolsQuery = activePoolsQuery.where(POOL_REQUEST.courtLocation.locCode.in(courts));
        } else if (returnDate != null) {
            activePoolsQuery = activePoolsQuery.where(POOL_REQUEST.returnDate.after(returnDate));
        } else {
            //throw an exception - can't run query without a location or date filter due to performance issues
            throw new IllegalArgumentException(
                "Unable to run active pools at court query without a location or date filter");
        }

        activePoolsQuery = activePoolsQuery
            .groupBy(POOL_REQUEST.poolNumber)
            .groupBy(POOL_REQUEST.totalNoRequired)
            .groupBy(COURT_LOCATION.name)
            .groupBy(POOL_TYPE.description)
            .groupBy(POOL_REQUEST.returnDate);

        activePoolsQuery = switch (sortBy + order) {
            case "poolNumberasc" -> activePoolsQuery.orderBy(POOL_REQUEST.poolNumber.asc());
            case "poolNumberdesc" -> activePoolsQuery.orderBy(POOL_REQUEST.poolNumber.desc());
            case "totalNumberasc" -> activePoolsQuery.orderBy(POOL_REQUEST.totalNoRequired.asc());
            case "totalNumberdesc" -> activePoolsQuery.orderBy(POOL_REQUEST.totalNoRequired.desc());
            case "jurorsInPoolasc" -> activePoolsQuery.orderBy(JUROR_POOL.isActive.count().asc());
            case "jurorsInPooldesc" -> activePoolsQuery.orderBy(JUROR_POOL.isActive.count().desc());
            case "courtNameasc" -> activePoolsQuery.orderBy(COURT_LOCATION.name.asc());
            case "courtNamedesc" -> activePoolsQuery.orderBy(COURT_LOCATION.name.desc());
            case "serviceStartDateasc" -> activePoolsQuery.orderBy(POOL_REQUEST.returnDate.asc());
            case "serviceStartDatedesc" -> activePoolsQuery.orderBy(POOL_REQUEST.returnDate.desc());
            case "poolTypeasc" -> activePoolsQuery.orderBy(POOL_TYPE.description.asc());
            case "poolTypedesc" -> activePoolsQuery.orderBy(POOL_TYPE.description.desc());
            default -> throw new IllegalArgumentException(
                "Unable to run active pools at court query without a valid sort criteria");
        };

        List<Tuple> resultList = activePoolsQuery.fetch();

        List<ActivePoolsCourt> activePoolsList = new ArrayList<>();

        for (Tuple tuple : resultList) {

            ActivePoolsCourt activePool = new ActivePoolsCourt();
            activePool.setPoolNumber(tuple.get(0, String.class));

            //need to check if there is a total capacity record as old data won't have this...
            int capacity = 0;
            if (!(tuple.get(1, Integer.class) == null)) {
                capacity = tuple.get(1, Integer.class);
            }
            activePool.setPoolCapacity(capacity);
            activePool.setJurorsInPool(tuple.get(2, Long.class));
            activePool.setCourtName(tuple.get(3, String.class));
            activePool.setPoolType(tuple.get(4, String.class));
            activePool.setServiceStartDate(tuple.get(5, LocalDate.class));

            activePoolsList.add(activePool);
        }

        return activePoolsList;

    }

}
