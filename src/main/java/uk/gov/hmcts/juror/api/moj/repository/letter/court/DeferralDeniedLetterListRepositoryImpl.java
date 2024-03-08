package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralDeniedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.QDeferralDeniedLetterList;

import java.util.List;

@SuppressWarnings("PMD.LawOfDemeter")
public class DeferralDeniedLetterListRepositoryImpl implements IDeferralDeniedLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QDeferralDeniedLetterList NON_DEFERRAL_LETTER_LIST =
        QDeferralDeniedLetterList.deferralDeniedLetterList;

    @Override
    public List<DeferralDeniedLetterList> findJurorsEligibleForDeferralDeniedLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<DeferralDeniedLetterList> jpaQuery =
            queryFactory.selectFrom(NON_DEFERRAL_LETTER_LIST)
                .where(NON_DEFERRAL_LETTER_LIST.owner.equalsIgnoreCase(owner));

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderQueryResults(jpaQuery, searchCriteria.includePrinted());

        return jpaQuery.fetch();
    }


    private void filterEligibleLetterSearchCriteria(JPAQuery<DeferralDeniedLetterList> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(NON_DEFERRAL_LETTER_LIST.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(NON_DEFERRAL_LETTER_LIST.firstName.concat(" " + NON_DEFERRAL_LETTER_LIST.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                NON_DEFERRAL_LETTER_LIST.postcode.trim().equalsIgnoreCase(courtLetterSearchCriteria.postcode().trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(NON_DEFERRAL_LETTER_LIST.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (!courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(NON_DEFERRAL_LETTER_LIST.datePrinted.isNull())
                .where(NON_DEFERRAL_LETTER_LIST.rowNumber.eq(1));
        } else {
            jpaQuery.where(NON_DEFERRAL_LETTER_LIST.rowNumber.eq(1)
                .or(NON_DEFERRAL_LETTER_LIST.datePrinted.isNotNull()));
        }
    }


    private void orderQueryResults(JPAQuery<DeferralDeniedLetterList> jpaQuery, boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(NON_DEFERRAL_LETTER_LIST.datePrinted.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }

        jpaQuery.orderBy(NON_DEFERRAL_LETTER_LIST.jurorNumber.asc())
            .orderBy(NON_DEFERRAL_LETTER_LIST.refusalDate.desc());
    }
}
