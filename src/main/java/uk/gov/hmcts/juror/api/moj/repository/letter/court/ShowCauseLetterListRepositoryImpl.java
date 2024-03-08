package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.QShowCauseLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ShowCauseLetterList;

import java.util.List;

@Repository
@SuppressWarnings("PMD.LawOfDemeter")
public class ShowCauseLetterListRepositoryImpl implements ShowCauseLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QShowCauseLetterList SHOW_CAUSE_LETTER_LIST = QShowCauseLetterList.showCauseLetterList;

    @Override
    public List<ShowCauseLetterList> findJurorsEligibleForShowCauseLetter(CourtLetterSearchCriteria searchCriteria,
                                                                          String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<ShowCauseLetterList> jpaQuery =
            queryFactory.selectFrom(SHOW_CAUSE_LETTER_LIST).where(SHOW_CAUSE_LETTER_LIST.owner.equalsIgnoreCase(owner));

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderQueryResults(jpaQuery, searchCriteria.includePrinted());

        return jpaQuery.fetch();
    }

    private void orderQueryResults(JPAQuery<ShowCauseLetterList> jpaQuery, boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(SHOW_CAUSE_LETTER_LIST.datePrinted.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }

        jpaQuery
            .orderBy(SHOW_CAUSE_LETTER_LIST.absentDate.desc())
            .orderBy(SHOW_CAUSE_LETTER_LIST.jurorNumber.asc());
    }

    private void filterEligibleLetterSearchCriteria(JPAQuery<ShowCauseLetterList> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(SHOW_CAUSE_LETTER_LIST.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(SHOW_CAUSE_LETTER_LIST.firstName.concat(" " + SHOW_CAUSE_LETTER_LIST.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                SHOW_CAUSE_LETTER_LIST.postcode.trim().equalsIgnoreCase(courtLetterSearchCriteria.postcode().trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(SHOW_CAUSE_LETTER_LIST.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (!courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(SHOW_CAUSE_LETTER_LIST.datePrinted.isNull());
        }
    }
}