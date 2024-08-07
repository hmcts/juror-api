package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ShowCauseLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ShowCauseLetterListRepositoryImpl implements ShowCauseLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<ShowCauseLetterList> findJurorsEligibleForShowCauseLetter(CourtLetterSearchCriteria searchCriteria,
                                                                          String owner) {

        JPAQuery<Tuple> jpaQuery = buildBaseQuery(owner);

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderQueryResults(jpaQuery, searchCriteria.includePrinted());

        List<Tuple> results = jpaQuery.fetch();

        return results.stream()
            .map(tuple -> ShowCauseLetterList.builder()
                .poolNumber(tuple.get(QPoolRequest.poolRequest.poolNumber))
                .jurorNumber(tuple.get(QJuror.juror.jurorNumber))
                .firstName(tuple.get(QJuror.juror.firstName))
                .lastName(tuple.get(QJuror.juror.lastName))
                .postcode(tuple.get(QJuror.juror.postcode))
                .status(tuple.get(QJurorStatus.jurorStatus.statusDesc))
                .absentDate(tuple.get(QAppearance.appearance.attendanceDate))
                .datePrinted(tuple.get(QJurorHistory.jurorHistory.dateCreated))
                .isActive(tuple.get(QJurorPool.jurorPool.isActive))
                .build())
            .collect(Collectors.toList());
    }

    private JPAQuery<Tuple> buildBaseQuery(String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(
                QPoolRequest.poolRequest.poolNumber,
                QJuror.juror.jurorNumber,
                QJuror.juror.firstName,
                QJuror.juror.lastName,
                QJuror.juror.postcode,
                QJurorStatus.jurorStatus.statusDesc,
                QJurorHistory.jurorHistory.dateCreated,
                QJurorPool.jurorPool.isActive,
                QAppearance.appearance.attendanceDate)
            .from(QJurorPool.jurorPool)
            .join(QJuror.juror).on(QJuror.juror.eq(QJurorPool.jurorPool.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .join(QJurorStatus.jurorStatus).on(QJurorStatus.jurorStatus.eq(QJurorPool.jurorPool.status))
            .join(QAppearance.appearance).on(QAppearance.appearance.jurorNumber.eq(QJuror.juror.jurorNumber))
            .leftJoin(QJurorHistory.jurorHistory)
            .on(QJurorHistory.jurorHistory.jurorNumber.eq(QJuror.juror.jurorNumber)
                .and(QJurorHistory.jurorHistory.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
                .and(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.SHOW_CAUSE_LETTER)))
            .where(QJurorPool.jurorPool.isActive.eq(Boolean.TRUE)
                .and(QJurorPool.jurorPool.status.status.in(List.of(IJurorStatus.RESPONDED,
                        IJurorStatus.FAILED_TO_ATTEND))
                    .and(QAppearance.appearance.noShow.eq(Boolean.TRUE))
                    .and(QAppearance.appearance.attendanceType.eq(AttendanceType.ABSENT))
                    .and(QPoolRequest.poolRequest.owner.eq(owner))));
    }

    private void orderQueryResults(JPAQuery<Tuple> jpaQuery, boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(QJurorHistory.jurorHistory.dateCreated.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }
        jpaQuery
            .orderBy(QAppearance.appearance.attendanceDate.desc())
            .orderBy(QJuror.juror.jurorNumber.asc());
    }

    private void filterEligibleLetterSearchCriteria(JPAQuery<Tuple> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(QJuror.juror.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(QJuror.juror.firstName.concat(" ").concat(QJurorPool.jurorPool.juror.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                QJuror.juror.postcode.trim()
                    .eq(courtLetterSearchCriteria.postcode().trim().toUpperCase()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(QPoolRequest.poolRequest.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (!courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(QJurorHistory.jurorHistory.dateCreated.isNull());
        }
    }
}