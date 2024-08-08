package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalRefusedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.QExcusalRefusedLetterList;

import java.util.List;

public class ExcusalRefusalLetterListRepositoryImpl implements IExcusalRefusalLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QExcusalRefusedLetterList EXCUSAL_REFUSED_LETTER_LIST =
        QExcusalRefusedLetterList.excusalRefusedLetterList;

    @Override
    public List<ExcusalRefusedLetterList> findJurorsEligibleForExcusalRefusalLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<ExcusalRefusedLetterList> jpaQuery =
            queryFactory.selectFrom(EXCUSAL_REFUSED_LETTER_LIST)
                .where(EXCUSAL_REFUSED_LETTER_LIST.owner.eq(owner));

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderExcusalRefusedQueryResults(jpaQuery, searchCriteria.includePrinted());

        return jpaQuery.fetch();
    }


    private void filterEligibleLetterSearchCriteria(JPAQuery<ExcusalRefusedLetterList> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(EXCUSAL_REFUSED_LETTER_LIST.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(EXCUSAL_REFUSED_LETTER_LIST.firstName.concat(" " + EXCUSAL_REFUSED_LETTER_LIST.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                EXCUSAL_REFUSED_LETTER_LIST.postcode.trim().equalsIgnoreCase(courtLetterSearchCriteria.postcode()
                    .trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(EXCUSAL_REFUSED_LETTER_LIST.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (!courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(EXCUSAL_REFUSED_LETTER_LIST.datePrinted.isNull());
        }
    }

    private void orderExcusalRefusedQueryResults(JPAQuery<ExcusalRefusedLetterList> jpaQuery,
                                                 boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(EXCUSAL_REFUSED_LETTER_LIST.datePrinted.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }
        jpaQuery.orderBy(EXCUSAL_REFUSED_LETTER_LIST.datePrinted.desc());
        jpaQuery.orderBy(EXCUSAL_REFUSED_LETTER_LIST.jurorNumber.asc());
    }
}
