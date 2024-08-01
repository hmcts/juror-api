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
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;

import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DeferralGrantedLetterListRepositoryImpl implements IDeferralGrantedLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QExcusalCode EXCUSAL_CODE = QExcusalCode.excusalCode;

    @Override
    public List<DeferralGrantedLetterList> findJurorsEligibleForDeferralGrantedLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQuery<Tuple> jpaQuery = buildBaseQuery();

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderDeferralGrantedQueryResults(jpaQuery, searchCriteria.includePrinted());

        List<Tuple> results = jpaQuery.fetch();

        List<DeferralGrantedLetterList> deferralGrantedLetterLists = results.stream()
            .map(tuple -> DeferralGrantedLetterList.builder()
                .poolNumber(tuple.get(QJurorPool.jurorPool.pool.poolNumber))
                .jurorNumber(tuple.get(QJuror.juror.jurorNumber))
                .firstName(tuple.get(QJuror.juror.firstName))
                .lastName(tuple.get(QJuror.juror.lastName))
                .postcode(tuple.get(QJuror.juror.postcode))
                .status(tuple.get(QJurorStatus.jurorStatus.statusDesc))
                .deferralDate(tuple.get(QJurorPool.jurorPool.deferralDate))
                .deferralReason(tuple.get(EXCUSAL_CODE.excusalCode.description))
                .datePrinted(tuple.get(QJurorHistory.jurorHistory.dateCreated))
                .isActive(tuple.get(QJurorPool.jurorPool.isActive))
                .build())
            .collect(Collectors.toList());

        // for each juror select the most recent deferred date and remove the other entries
        // this is to ensure that only the most recent deferral date is returned
        ConcurrentHashMap<String, DeferralGrantedLetterList> deferralGrantedLetterListMap = new ConcurrentHashMap<>();
        deferralGrantedLetterLists.forEach(deferralGrantedLetterList -> {
            String jurorNumber = deferralGrantedLetterList.getJurorNumber();
            if (deferralGrantedLetterListMap.containsKey(jurorNumber)) {
                DeferralGrantedLetterList existingDeferralGrantedLetterList =
                    deferralGrantedLetterListMap.get(jurorNumber);
                if (deferralGrantedLetterList.getDeferralDate()
                    .isAfter(existingDeferralGrantedLetterList.getDeferralDate())) {
                    deferralGrantedLetterListMap.put(jurorNumber, deferralGrantedLetterList);
                }
            } else {
                deferralGrantedLetterListMap.put(jurorNumber, deferralGrantedLetterList);
            }
        });

        return deferralGrantedLetterListMap.values().stream().toList();
    }


    private JPAQuery<Tuple> buildBaseQuery() {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(
                QJurorPool.jurorPool.pool.poolNumber,
                QJuror.juror.jurorNumber,
                QJuror.juror.firstName,
                QJuror.juror.lastName,
                QJuror.juror.postcode,
                QJurorPool.jurorPool.deferralDate,
                QJurorStatus.jurorStatus.statusDesc,
                QJurorHistory.jurorHistory.dateCreated,
                QJurorPool.jurorPool.isActive,
                EXCUSAL_CODE.excusalCode.description)
            .from(QJurorPool.jurorPool)
            .join(QJuror.juror).on(QJuror.juror.eq(QJurorPool.jurorPool.juror))
            .join(QPoolRequest.poolRequest)
            .on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .join(QJurorStatus.jurorStatus).on(QJurorStatus.jurorStatus.eq(QJurorPool.jurorPool.status))
            .join(EXCUSAL_CODE.excusalCode).on(EXCUSAL_CODE.excusalCode.code.eq(QJurorPool.jurorPool.deferralCode))
            .leftJoin(QJurorHistory.jurorHistory)
            .on((QJurorHistory.jurorHistory.jurorNumber.eq(QJuror.juror.jurorNumber))
                .and(QJurorHistory.jurorHistory.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
                .and(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.DEFERRED_LETTER))
                .and(QJurorHistory.jurorHistory.otherInformationDate.eq(QJurorPool.jurorPool.deferralDate))
                .and(QJurorHistory.jurorHistory.dateCreatedDateOnly
                    .after(QJuror.juror.bureauTransferDate)))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)
                .and(EXCUSAL_CODE.excusalCode.code.ne(ExcusalCodeEnum.P.getCode()))
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
            jpaQuery.where(currentlyDeferred().or(QJurorHistory.jurorHistory.dateCreated.isNotNull()));
        } else {
            jpaQuery.where(currentlyDeferred().and(QJurorHistory.jurorHistory.dateCreated.isNull()));
        }
    }


    private void orderDeferralGrantedQueryResults(JPAQuery<Tuple> jpaQuery,
                                                  boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(QJurorHistory.jurorHistory.dateCreated.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }

        jpaQuery.orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc())
            .orderBy(QJurorPool.jurorPool.deferralDate.desc());
    }

    private BooleanExpression currentlyDeferred() {
        return QJurorPool.jurorPool.deferralDate.isNotNull().and(QJurorPool.jurorPool.isActive.isTrue());
    }

}
