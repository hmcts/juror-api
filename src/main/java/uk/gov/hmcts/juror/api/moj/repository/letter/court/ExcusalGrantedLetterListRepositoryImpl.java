package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QExcusalCode;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ExcusalGrantedLetterListRepositoryImpl implements IExcusalGrantedLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QExcusalCode EXCUSAL_CODE = QExcusalCode.excusalCode;

    @Override

    public List<ExcusalGrantedLetterList> findJurorsEligibleForExcusalGrantedLetter(
        CourtLetterSearchCriteria searchCriteria) {

        JPAQuery<Tuple> jpaQuery = buildBaseQuery();

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderExcusalGrantedQueryResults(jpaQuery, searchCriteria.includePrinted());

        List<Tuple> results = jpaQuery.fetch();

        List<ExcusalGrantedLetterList> excusalGrantedLetterLists = results.stream()
            .map(tuple -> ExcusalGrantedLetterList.builder()
                .poolNumber(tuple.get(QJurorPool.jurorPool.pool.poolNumber))
                .jurorNumber(tuple.get(QJuror.juror.jurorNumber))
                .firstName(tuple.get(QJuror.juror.firstName))
                .lastName(tuple.get(QJuror.juror.lastName))
                .postcode(tuple.get(QJuror.juror.postcode))
                .status(tuple.get(QJurorStatus.jurorStatus.statusDesc))
                .dateExcused(tuple.get(QJuror.juror.excusalDate))
                .reason(tuple.get(EXCUSAL_CODE.excusalCode.description))
                .datePrinted(tuple.get(QJurorHistory.jurorHistory.dateCreated))
                .isActive(tuple.get(QJurorPool.jurorPool.isActive))
                .build())
            .collect(Collectors.toList());

        ConcurrentHashMap<String, ExcusalGrantedLetterList> excusalGrantedLetterListMap = new ConcurrentHashMap<>();
        excusalGrantedLetterLists.forEach(excusalGrantedLetterList -> {
            String jurorNumber = excusalGrantedLetterList.getJurorNumber();
            if (excusalGrantedLetterListMap.containsKey(jurorNumber)) {
                ExcusalGrantedLetterList existingExcusalGrantedLetterList =
                    excusalGrantedLetterListMap.get(jurorNumber);
                if (excusalGrantedLetterList.getDateExcused()
                    .isAfter(existingExcusalGrantedLetterList.getDateExcused())) {
                    excusalGrantedLetterListMap.put(jurorNumber, existingExcusalGrantedLetterList);
                }
            } else {
                excusalGrantedLetterListMap.put(jurorNumber, excusalGrantedLetterList);
            }
        });

        return excusalGrantedLetterListMap.values().stream().toList();
    }

    private JPAQuery<Tuple> buildBaseQuery() {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(
                QJurorPool.jurorPool.pool.poolNumber,
                QJuror.juror.jurorNumber,
                QJuror.juror.firstName,
                QJuror.juror.lastName,
                QJuror.juror.postcode,
                QJurorStatus.jurorStatus.statusDesc,
                QJuror.juror.excusalDate,
                QJurorHistory.jurorHistory.dateCreated,
                QJurorPool.jurorPool.isActive,
                EXCUSAL_CODE.excusalCode.description)
            .from(QJurorPool.jurorPool)
            .join(QJuror.juror).on(QJuror.juror.eq(QJurorPool.jurorPool.juror))
            .join(QPoolRequest.poolRequest)
            .on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .join(QJurorStatus.jurorStatus).on(QJurorStatus.jurorStatus.eq(QJurorPool.jurorPool.status))
            .join(EXCUSAL_CODE.excusalCode).on(EXCUSAL_CODE.excusalCode.code.eq(QJuror.juror.excusalCode))
            .leftJoin(QJurorHistory.jurorHistory)
            .on((QJurorHistory.jurorHistory.jurorNumber.eq(QJuror.juror.jurorNumber))
                .and(QJurorHistory.jurorHistory.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
                .and(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.EXCUSED_LETTER))
                .and(QJurorHistory.jurorHistory.dateCreatedDateOnly
                    .after(QJuror.juror.bureauTransferDate)))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.EXCUSED)
                .and(QPoolRequest.poolRequest.owner.eq(SecurityUtil.getActiveOwner())));
    }

    private void filterEligibleLetterSearchCriteria(JPAQuery<Tuple> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(QJurorPool.jurorPool.juror.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(QJurorPool.jurorPool.juror.firstName.concat(" ").concat(QJurorPool.jurorPool.juror.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                QJurorPool.jurorPool.juror.postcode.trim()
                    .eq(courtLetterSearchCriteria.postcode().trim().toUpperCase()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(QJurorPool.jurorPool.pool.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(alreadyExcused().or(QJurorHistory.jurorHistory.dateCreated.isNotNull()));
        } else {
            jpaQuery.where(alreadyExcused().and(QJurorHistory.jurorHistory.dateCreated.isNull()));
        }
    }

    private void orderExcusalGrantedQueryResults(JPAQuery<Tuple> jpaQuery,
                                                 boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(QJurorHistory.jurorHistory.dateCreated.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }

        jpaQuery.orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc())
            .orderBy(QJuror.juror.excusalDate.desc());
    }

    private BooleanExpression alreadyExcused() {
        return QJuror.juror.excusalDate.isNotNull().and(QJurorPool.jurorPool.isActive.isTrue());
    }

}
