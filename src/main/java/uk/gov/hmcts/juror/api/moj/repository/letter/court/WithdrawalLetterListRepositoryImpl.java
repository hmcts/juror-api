package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.QWithdrawalLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.WithdrawalLetterList;

import java.util.List;

public class WithdrawalLetterListRepositoryImpl implements IWithdrawalLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QWithdrawalLetterList WITHDRAWAL_LETTER_LIST =
        QWithdrawalLetterList.withdrawalLetterList;

    @Override
    public List<WithdrawalLetterList> findJurorsEligibleForWithdrawalLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<WithdrawalLetterList> jpaQuery =
            queryFactory.selectFrom(WITHDRAWAL_LETTER_LIST)
                .where(WITHDRAWAL_LETTER_LIST.owner.equalsIgnoreCase(owner));

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderWithdrawalQueryResults(jpaQuery, searchCriteria.includePrinted());

        return jpaQuery.fetch();
    }

    private void filterEligibleLetterSearchCriteria(JPAQuery<WithdrawalLetterList> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(WITHDRAWAL_LETTER_LIST.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(WITHDRAWAL_LETTER_LIST.firstName.concat(WITHDRAWAL_LETTER_LIST.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                WITHDRAWAL_LETTER_LIST.postcode.trim().equalsIgnoreCase(courtLetterSearchCriteria.postcode().trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(WITHDRAWAL_LETTER_LIST.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (!courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(WITHDRAWAL_LETTER_LIST.datePrinted.isNull());
        }
    }

    private void orderWithdrawalQueryResults(JPAQuery<WithdrawalLetterList> jpaQuery,
                                                  boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(WITHDRAWAL_LETTER_LIST.datePrinted.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }

        jpaQuery.orderBy(WITHDRAWAL_LETTER_LIST.datePrinted.desc())
            .orderBy(WITHDRAWAL_LETTER_LIST.jurorNumber.asc());
    }

}
