package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorRecordFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

/**
 * Custom Repository implementation for the Juror entity.
 */
public class JurorRepositoryImpl implements IJurorRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;

    @Override
    public Juror findByJurorNumberAndIsActiveAndCourt(String jurorNumber, boolean isActive, CourtLocation locCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .where(JUROR.jurorNumber.eq(jurorNumber))
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(isActive))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.courtLocation.eq(locCode))
            .fetchOne();
    }

    @Override
    public List<Juror> findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(List<String> jurorNumbers,
                                                                                      boolean isActive,
                                                                                      String poolNumber,
                                                                                      CourtLocation court,
                                                                                      List<Integer> status) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .where(JUROR.jurorNumber.in(jurorNumbers))
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(isActive))
            .where(JUROR_POOL.status.status.in(status))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.courtLocation.eq(court))
            .fetch();
    }

    @Override
    @SuppressWarnings("PMD.CognitiveComplexity")
    public JPAQuery<Tuple> fetchFilteredJurorRecords(JurorRecordFilterRequestQuery query) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<?> partialQuery = queryFactory.from(JUROR)
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(true));

        if (!SecurityUtil.isBureau()) {
            // If the user is not a Bureau user, filter by the courts they have access to
            partialQuery.where(JUROR_POOL.pool.courtLocation.locCode.in(SecurityUtil.getCourts()));
            partialQuery.where(JUROR_POOL.owner.eq(SecurityUtil.getActiveOwner()));
        }

        if (null != query.getJurorNumber()) {
            if (query.getJurorNumber().length() == 9) {
                partialQuery.where(JUROR.jurorNumber.eq(query.getJurorNumber()));
            } else {
                partialQuery.where(JUROR.jurorNumber.startsWith(query.getJurorNumber()));
            }
        }

        if (null != query.getJurorName()) {

            final String jurorName = query.getJurorName().trim();

            String[] names = jurorName.split(" ");

            if (names.length == 2) {
                partialQuery.where(JUROR.firstName.containsIgnoreCase(names[0])
                    .and(JUROR.lastName.containsIgnoreCase(names[1]))
                    .or(JUROR.firstName.concat(" ").concat(JUROR.lastName).containsIgnoreCase(jurorName)));
            } else if (names.length > 2) {
                // assume first name is first word and last name is the rest
                partialQuery.where((JUROR.firstName.containsIgnoreCase(names[0])
                    .and(JUROR.lastName.containsIgnoreCase(jurorName.substring(names[0].length() + 1))))
                    .or(JUROR.firstName.concat(" ").concat(JUROR.lastName).containsIgnoreCase(jurorName)));
            } else {
                partialQuery.where(JUROR.firstName.concat(" ").concat(JUROR.lastName).containsIgnoreCase(jurorName));
            }
        }

        if (null != query.getPostcode()) {
            partialQuery.where(JUROR.postcode.startsWith(DataUtils.toUppercase(query.getPostcode())));
        }

        if (null != query.getPoolNumber()) {
            if (query.getPoolNumber().length() == 9) {
                partialQuery.where(JUROR_POOL.pool.poolNumber.eq(query.getPoolNumber()));
            } else {
                partialQuery.where(JUROR_POOL.pool.poolNumber.startsWith(query.getPoolNumber()));

            }
        }


        return partialQuery.distinct().select(
            JUROR.jurorNumber,
            JUROR.firstName.concat(" ").concat(JUROR.lastName).as(JUROR_FULL_NAME),
            JUROR.email,
            JUROR.postcode,
            JUROR_POOL.pool.poolNumber,
            JUROR_POOL.pool.courtLocation.name,
            JUROR_POOL.status.statusDesc,
            JUROR_POOL.pool.courtLocation.locCode);
    }
}
