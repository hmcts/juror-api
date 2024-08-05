package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralDeniedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DeferralDeniedLetterListRepositoryImpl implements IDeferralDeniedLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<DeferralDeniedLetterList> findJurorsEligibleForDeferralDeniedLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQuery<Tuple> jpaQuery = buildBaseQuery(owner);

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderQueryResults(jpaQuery, searchCriteria.includePrinted());

        List<Tuple> results = jpaQuery.fetch();

        List<DeferralDeniedLetterList> deferralDeniedLetterLists = results.stream()
            .map(tuple -> DeferralDeniedLetterList.builder()
                .poolNumber(tuple.get(QJurorPool.jurorPool.pool.poolNumber))
                .jurorNumber(tuple.get(QJuror.juror.jurorNumber))
                .firstName(tuple.get(QJuror.juror.firstName))
                .lastName(tuple.get(QJuror.juror.lastName))
                .postcode(tuple.get(QJuror.juror.postcode))
                .status(tuple.get(QJurorStatus.jurorStatus.statusDesc))
                .otherInformation(tuple.get(QJurorHistory.jurorHistory.otherInformation))
                .refusalDate(tuple.get(QJurorHistory.jurorHistory.dateCreatedDateOnly))
                .isActive(tuple.get(QJurorPool.jurorPool.isActive))
                .build())
            .collect(Collectors.toList());


        // for each juror select the most recent deferred date and remove the other entries
        // this is to ensure that only the most recent deferral date is returned
        ConcurrentHashMap<String, DeferralDeniedLetterList> deferralDeniedLetterListMap = new ConcurrentHashMap<>();
        deferralDeniedLetterLists.forEach(deferralDeniedLetterList -> {
            String jurorNumber = deferralDeniedLetterList.getJurorNumber();
            if (deferralDeniedLetterListMap.containsKey(jurorNumber)) {
                DeferralDeniedLetterList existingDeferralDeniedLetterList =
                    deferralDeniedLetterListMap.get(jurorNumber);
                if (deferralDeniedLetterList.getRefusalDate()
                    .isAfter(existingDeferralDeniedLetterList.getRefusalDate())) {
                    deferralDeniedLetterListMap.put(jurorNumber, deferralDeniedLetterList);
                }
            } else {
                deferralDeniedLetterListMap.put(jurorNumber, deferralDeniedLetterList);
            }
        });

        List<String> poolNumbers =
            deferralDeniedLetterLists.stream().map(DeferralDeniedLetterList::getPoolNumber).toList();

        // run query to get the most recent deferral denied date for each juror
        List<Tuple> printedDates = getPrintedDate(owner, poolNumbers);

        // update the deferral denied printed date for each juror
        printedDates.forEach(tuple -> {
            String jurorNumber = tuple.get(QJuror.juror.jurorNumber);
            if (deferralDeniedLetterListMap.containsKey(jurorNumber)) {
                DeferralDeniedLetterList deferralDeniedLetterList = deferralDeniedLetterListMap.get(jurorNumber);
                LocalDateTime datePrinted = tuple.get(QJurorHistory.jurorHistory.dateCreated);
                if (datePrinted.toLocalDate().equals(deferralDeniedLetterList.getRefusalDate())
                    || datePrinted.toLocalDate().isAfter(deferralDeniedLetterList.getRefusalDate())) {
                    if (!searchCriteria.includePrinted()) {
                        deferralDeniedLetterListMap.remove(jurorNumber);
                    } else {
                        deferralDeniedLetterList.setDatePrinted(datePrinted);
                    }
                }
            }
        });


        return deferralDeniedLetterListMap.values().stream().toList();

    }


    private JPAQuery<Tuple> buildBaseQuery(String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(
                QJurorPool.jurorPool.pool.poolNumber,
                QJuror.juror.jurorNumber,
                QJuror.juror.firstName,
                QJuror.juror.lastName,
                QJuror.juror.postcode,
                QJurorPool.jurorPool.deferralDate,
                QJurorStatus.jurorStatus.statusDesc,
                QJurorHistory.jurorHistory.dateCreatedDateOnly,
                QJurorHistory.jurorHistory.otherInformation,
                QJurorPool.jurorPool.isActive)
            .from(QJurorPool.jurorPool)
            .join(QJuror.juror).on(QJuror.juror.eq(QJurorPool.jurorPool.juror))
            .join(QPoolRequest.poolRequest)
            .on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .join(QJurorStatus.jurorStatus).on(QJurorStatus.jurorStatus.eq(QJurorPool.jurorPool.status))
            .join(QJurorHistory.jurorHistory)
            .on(QJurorHistory.jurorHistory.jurorNumber.eq(QJuror.juror.jurorNumber)
                .and(QJurorHistory.jurorHistory.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
                .and(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.DEFERRED_POOL_MEMBER))
                .and(QJurorHistory.jurorHistory.otherInformation.containsIgnoreCase("deferral denied"))
                .and(QJurorHistory.jurorHistory.dateCreatedDateOnly.after(QJuror.juror.bureauTransferDate)))
            .where(QJuror.juror.excusalRejected.eq("Z")
                .and(QJurorPool.jurorPool.isActive.isTrue())
                .and(QPoolRequest.poolRequest.owner.eq(owner)));
    }

    private List<Tuple> getPrintedDate(String owner, List<String> poolNumbers) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(
                QJurorPool.jurorPool.pool.poolNumber,
                QJuror.juror.jurorNumber,
                QJurorHistory.jurorHistory.dateCreated)
            .from(QJurorPool.jurorPool)
            .join(QJuror.juror).on(QJuror.juror.eq(QJurorPool.jurorPool.juror))
            .join(QPoolRequest.poolRequest)
            .on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .join(QJurorStatus.jurorStatus).on(QJurorStatus.jurorStatus.eq(QJurorPool.jurorPool.status))
            .join(QJurorHistory.jurorHistory)
            .on(QJurorHistory.jurorHistory.jurorNumber.eq(QJuror.juror.jurorNumber)
                .and(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.NON_DEFERRED_LETTER))
                .and(QJurorHistory.jurorHistory.dateCreatedDateOnly.after(QJuror.juror.bureauTransferDate)))
            .where(QJuror.juror.excusalRejected.eq("Z")
                .and(QJurorPool.jurorPool.isActive.isTrue())
                .and(QJurorPool.jurorPool.pool.poolNumber.in(poolNumbers))
                .and(QPoolRequest.poolRequest.owner.eq(owner)))
            .fetch();
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

    }


    private void orderQueryResults(JPAQuery<Tuple> jpaQuery, boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(QJurorHistory.jurorHistory.dateCreatedDateOnly.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }

        jpaQuery.orderBy(QJurorHistory.jurorHistory.dateCreatedDateOnly.desc(),
            QJurorPool.jurorPool.juror.jurorNumber.asc());
    }
}
