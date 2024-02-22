package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.QExcusalGrantedLetterList;

import java.util.List;

@SuppressWarnings("PMD.LawOfDemeter")
public class ExcusalGrantedLetterListRepositoryImpl implements IExcusalGrantedLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QExcusalGrantedLetterList EXCUSAL_GRANTED_LETTER_LIST =
        QExcusalGrantedLetterList.excusalGrantedLetterList;

    @Override
    public List<ExcusalGrantedLetterList> findJurorsEligibleForExcusalGrantedLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<ExcusalGrantedLetterList> jpaQuery =
            queryFactory.selectFrom(EXCUSAL_GRANTED_LETTER_LIST)
                .where(EXCUSAL_GRANTED_LETTER_LIST.owner.equalsIgnoreCase(owner));

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderExcusalGrantedQueryResults(jpaQuery, searchCriteria.includePrinted());

        return jpaQuery.fetch();
    }


    private void filterEligibleLetterSearchCriteria(JPAQuery<ExcusalGrantedLetterList> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(EXCUSAL_GRANTED_LETTER_LIST.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(EXCUSAL_GRANTED_LETTER_LIST.firstName.concat(" " + EXCUSAL_GRANTED_LETTER_LIST.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                EXCUSAL_GRANTED_LETTER_LIST.postcode.trim().equalsIgnoreCase(courtLetterSearchCriteria.postcode()
                    .trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(EXCUSAL_GRANTED_LETTER_LIST.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (!courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(EXCUSAL_GRANTED_LETTER_LIST.datePrinted.isNull());
        }
    }

    private void orderExcusalGrantedQueryResults(JPAQuery<ExcusalGrantedLetterList> jpaQuery,
                                                  boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(EXCUSAL_GRANTED_LETTER_LIST.datePrinted.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }
        jpaQuery.orderBy(EXCUSAL_GRANTED_LETTER_LIST.datePrinted.desc());
        jpaQuery.orderBy(EXCUSAL_GRANTED_LETTER_LIST.jurorNumber.asc());
    }
}
