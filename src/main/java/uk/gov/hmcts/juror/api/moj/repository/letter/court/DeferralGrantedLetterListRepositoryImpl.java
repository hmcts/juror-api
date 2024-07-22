package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.QDeferralGrantedLetterList;

import java.util.List;

public class DeferralGrantedLetterListRepositoryImpl implements IDeferralGrantedLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QDeferralGrantedLetterList DEFERRAL_LETTER_LIST =
        QDeferralGrantedLetterList.deferralGrantedLetterList;

    @Override
    public List<DeferralGrantedLetterList> findJurorsEligibleForDeferralGrantedLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<DeferralGrantedLetterList> jpaQuery =
            queryFactory.selectFrom(DEFERRAL_LETTER_LIST)
                .where(DEFERRAL_LETTER_LIST.owner.eq(owner));

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderDeferralGrantedQueryResults(jpaQuery, searchCriteria.includePrinted());

        return jpaQuery.fetch();
    }


    private void filterEligibleLetterSearchCriteria(JPAQuery<DeferralGrantedLetterList> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(DEFERRAL_LETTER_LIST.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(DEFERRAL_LETTER_LIST.firstName.concat(DEFERRAL_LETTER_LIST.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                DEFERRAL_LETTER_LIST.postcode.trim().equalsIgnoreCase(courtLetterSearchCriteria.postcode().trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(DEFERRAL_LETTER_LIST.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(currentlyDeferred().or(DEFERRAL_LETTER_LIST.datePrinted.isNotNull()));
        } else {
            jpaQuery.where(currentlyDeferred().and(DEFERRAL_LETTER_LIST.datePrinted.isNull()));
        }
    }


    private void orderDeferralGrantedQueryResults(JPAQuery<DeferralGrantedLetterList> jpaQuery,
                                                  boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(DEFERRAL_LETTER_LIST.datePrinted.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }

        jpaQuery.orderBy(DEFERRAL_LETTER_LIST.jurorNumber.asc())
            .orderBy(DEFERRAL_LETTER_LIST.deferralDate.desc());
    }

    private BooleanExpression currentlyDeferred() {
        return DEFERRAL_LETTER_LIST.rowNumber.eq(1).and(DEFERRAL_LETTER_LIST.isActive.isTrue());
    }

}
