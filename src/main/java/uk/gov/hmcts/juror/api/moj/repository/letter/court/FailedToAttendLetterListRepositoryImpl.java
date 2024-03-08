package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.FailedToAttendLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.QFailedToAttendLetterList;

import java.util.List;

@SuppressWarnings("PMD.LawOfDemeter")
public class FailedToAttendLetterListRepositoryImpl implements IFailedToAttendLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QFailedToAttendLetterList FAIL_TO_ATTEND_LETTER_LIST =
        QFailedToAttendLetterList.failedToAttendLetterList;

    @Override
    // this method also applies to show_cause letters
    public List<FailedToAttendLetterList> findJurorsEligibleForFailedToAttendLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<FailedToAttendLetterList> jpaQuery =
            queryFactory.selectFrom(FAIL_TO_ATTEND_LETTER_LIST)
                .where(FAIL_TO_ATTEND_LETTER_LIST.owner.equalsIgnoreCase(owner));

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderQueryResults(jpaQuery, searchCriteria.includePrinted());

        return jpaQuery.fetch();
    }


    private void filterEligibleLetterSearchCriteria(JPAQuery<FailedToAttendLetterList> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(FAIL_TO_ATTEND_LETTER_LIST.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(FAIL_TO_ATTEND_LETTER_LIST.firstName.concat(" " + FAIL_TO_ATTEND_LETTER_LIST.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                FAIL_TO_ATTEND_LETTER_LIST.postcode.trim().equalsIgnoreCase(courtLetterSearchCriteria.postcode()
                    .trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(FAIL_TO_ATTEND_LETTER_LIST.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (!courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(FAIL_TO_ATTEND_LETTER_LIST.datePrinted.isNull());
        }
    }

    private void orderQueryResults(JPAQuery<FailedToAttendLetterList> jpaQuery, boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(FAIL_TO_ATTEND_LETTER_LIST.datePrinted.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }
        jpaQuery
            .orderBy(FAIL_TO_ATTEND_LETTER_LIST.absentDate.desc())
            .orderBy(FAIL_TO_ATTEND_LETTER_LIST.jurorNumber.asc());
    }
}
