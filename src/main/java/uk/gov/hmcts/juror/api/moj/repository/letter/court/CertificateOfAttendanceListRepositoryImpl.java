package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.CertificateOfAttendanceLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;

public class CertificateOfAttendanceListRepositoryImpl implements ICertificateOfAttendanceListRepository {

    @PersistenceContext
    EntityManager entityManager;
    private static final DateTimePath<LocalDateTime> DATE_PRINTED_EXPRESSION = QJurorHistory.jurorHistory.dateCreated;

    @Override
    @SuppressWarnings("unchecked")
    public List<CertificateOfAttendanceLetterList> findJurorsEligibleForCertificateOfAcceptanceLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {
        JPAQuery<Tuple> jpaQuery = buildBaseQuery();
        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria, owner);

        orderCertificateOfAttendanceQueryResults(jpaQuery, searchCriteria.includePrinted());

        return (List<CertificateOfAttendanceLetterList>) jpaQuery.fetch()
            .stream()
            .map(tuple -> CertificateOfAttendanceLetterList.builder()
                .owner(tuple.get(QJurorPool.jurorPool.owner))
                .poolNumber(tuple.get(QJurorPool.jurorPool.pool.poolNumber))
                .jurorNumber(tuple.get(QJurorPool.jurorPool.juror.jurorNumber))
                .firstName(tuple.get(QJurorPool.jurorPool.juror.firstName))
                .lastName(tuple.get(QJurorPool.jurorPool.juror.lastName))
                .completionDate(tuple.get(QJurorPool.jurorPool.juror.completionDate))
                .startDate(tuple.get(QJurorPool.jurorPool.pool.returnDate))
                .datePrinted(tuple.get(QJurorHistory.jurorHistory.dateCreated))
                .postcode(tuple.get(QJurorPool.jurorPool.juror.postcode))
                .isActive(Boolean.TRUE.equals(tuple.get(QJurorPool.jurorPool.isActive)))
                .status(String.valueOf(tuple.get(QJurorPool.jurorPool.status).getStatus()))
                .build()).toList();
    }

    private JPAQuery<Tuple> buildBaseQuery() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.select(
                QJurorPool.jurorPool.owner,
                QJurorPool.jurorPool.pool.poolNumber,
                QJurorPool.jurorPool.juror.jurorNumber,
                QJurorPool.jurorPool.juror.firstName,
                QJurorPool.jurorPool.juror.lastName,
                QJurorPool.jurorPool.juror.completionDate,
                QJurorPool.jurorPool.pool.returnDate,
                QJurorHistory.jurorHistory.dateCreated,
                QJurorPool.jurorPool.juror.postcode,
                QJurorPool.jurorPool.isActive,
                QJurorPool.jurorPool.status
            ).from(QJurorPool.jurorPool)
            .leftJoin(QJurorHistory.jurorHistory)
            .on(QJurorHistory.jurorHistory.jurorNumber.eq(QJurorPool.jurorPool.juror.jurorNumber)
                .and(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.CERTIFICATE_OF_RECOGNITION)))
            .where(
                QJurorPool.jurorPool.owner.ne(SecurityUtil.BUREAU_OWNER),
                QJurorPool.jurorPool.isActive.isTrue(),
                JPAExpressions.selectOne()
                    .from(QAppearance.appearance)
                    .where(QAppearance.appearance.jurorNumber.eq(QJurorPool.jurorPool.juror.jurorNumber)
                        .and(getAttendancesFilter()))
                    .exists()
            );
    }


    private BooleanExpression getAttendancesFilter() {
        return QAppearance.appearance.attendanceType.ne(AttendanceType.ABSENT)
            .and(QAppearance.appearance.noShow.isNull().or(QAppearance.appearance.noShow.isFalse()));
    }

    @Override
    public List<Appearance> getAttendances(String locCode, String jurorNumber) {
        return new JPAQueryFactory(entityManager)
            .selectFrom(QAppearance.appearance)
            .where(QAppearance.appearance.jurorNumber.eq(jurorNumber)
                    .and(QAppearance.appearance.locCode.eq(locCode))
                    .and(getAttendancesFilter()))
            .fetch();
    }

    private void filterEligibleLetterSearchCriteria(JPAQuery<Tuple> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria, String owner) {
        jpaQuery.where(QJurorPool.jurorPool.owner.eq(owner));

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(
                QJurorPool.jurorPool.juror.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(QJurorPool.jurorPool.juror.firstName.concat(" ")
                .concat(QJurorPool.jurorPool.juror.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                QJurorPool.jurorPool.juror.postcode.trim()
                    .equalsIgnoreCase(courtLetterSearchCriteria.postcode()
                        .trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(
                QJurorPool.jurorPool.pool.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (!courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(DATE_PRINTED_EXPRESSION.isNull());
        }
    }


    private void orderCertificateOfAttendanceQueryResults(JPAQuery<Tuple> jpaQuery,
                                                          boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(DATE_PRINTED_EXPRESSION.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }
        jpaQuery.orderBy(DATE_PRINTED_EXPRESSION.desc());
        jpaQuery.orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

}

