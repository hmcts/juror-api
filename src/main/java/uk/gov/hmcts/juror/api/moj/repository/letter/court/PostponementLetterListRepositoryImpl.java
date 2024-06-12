package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.PostponedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.QPostponedLetterList;

import java.util.List;

@Repository
public class PostponementLetterListRepositoryImpl implements PostponementLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QPostponedLetterList POSTPONED_LETTER_LIST = QPostponedLetterList.postponedLetterList;

    @Override
    public List<PostponedLetterList> findJurorsEligibleForPostponementLetter(CourtLetterSearchCriteria searchCriteria,
                                                                             String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<PostponedLetterList> jpaQuery =
            queryFactory.selectFrom(POSTPONED_LETTER_LIST).where(POSTPONED_LETTER_LIST.owner.equalsIgnoreCase(owner));

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderQueryResults(jpaQuery, searchCriteria.includePrinted());

        return jpaQuery.fetch();
    }

    private void orderQueryResults(JPAQuery<PostponedLetterList> jpaQuery, boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(POSTPONED_LETTER_LIST.datePrinted.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }

        jpaQuery.orderBy(POSTPONED_LETTER_LIST.jurorNumber.asc())
            .orderBy(POSTPONED_LETTER_LIST.deferralDate.desc());
    }

    private void filterEligibleLetterSearchCriteria(JPAQuery<PostponedLetterList> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(POSTPONED_LETTER_LIST.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(POSTPONED_LETTER_LIST.firstName.concat(POSTPONED_LETTER_LIST.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                POSTPONED_LETTER_LIST.postcode.trim().equalsIgnoreCase(courtLetterSearchCriteria.postcode().trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(POSTPONED_LETTER_LIST.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(currentlyDeferred().or(POSTPONED_LETTER_LIST.datePrinted.isNotNull()));
        } else {
            jpaQuery.where(currentlyDeferred().and(POSTPONED_LETTER_LIST.datePrinted.isNull()));
        }
    }

    private BooleanExpression currentlyDeferred() {
        return POSTPONED_LETTER_LIST.rowNumber.eq(1).and(POSTPONED_LETTER_LIST.isActive.isTrue());
    }
}