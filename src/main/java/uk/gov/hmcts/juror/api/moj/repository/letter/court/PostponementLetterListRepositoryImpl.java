package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QExcusalCode;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.PostponedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class PostponementLetterListRepositoryImpl implements PostponementLetterListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QExcusalCode EXCUSAL_CODE = QExcusalCode.excusalCode;

    @Override
    public List<PostponedLetterList> findJurorsEligibleForPostponementLetter(CourtLetterSearchCriteria searchCriteria) {

        JPAQuery<Tuple> jpaQuery = buildBaseQuery();

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderQueryResults(jpaQuery, searchCriteria.includePrinted());

        List<Tuple> results = jpaQuery.fetch();

        List<PostponedLetterList> postponedLetterLists = results.stream()
            .map(tuple -> PostponedLetterList.builder()
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

        ConcurrentHashMap<String, PostponedLetterList> postponedLetterListMap = new ConcurrentHashMap<>();
        postponedLetterLists.forEach(postponedLetterList -> {
            String jurorNumber = postponedLetterList.getJurorNumber();
            if (postponedLetterListMap.containsKey(jurorNumber)) {
                PostponedLetterList existingPostponedLetterList =
                    postponedLetterListMap.get(jurorNumber);
                if (postponedLetterList.getDeferralDate()
                    .isAfter(existingPostponedLetterList.getDeferralDate())) {
                    postponedLetterListMap.put(jurorNumber, postponedLetterList);
                }
            } else {
                postponedLetterListMap.put(jurorNumber, postponedLetterList);
            }
        });
        return postponedLetterListMap.values().stream().toList();
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
                .and(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.POSTPONED_LETTER))
                .and(QJurorHistory.jurorHistory.otherInformationDate.eq(QJurorPool.jurorPool.deferralDate))
                .and(QJurorHistory.jurorHistory.dateCreatedDateOnly
                    .after(QJuror.juror.bureauTransferDate)))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)
                .and(EXCUSAL_CODE.excusalCode.code.eq(ExcusalCodeEnum.P.getCode()))
                .and(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner())));
    }

    private void filterEligibleLetterSearchCriteria(JPAQuery<Tuple> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(QJuror.juror.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(QJuror.juror.firstName.concat(" ").concat(QJuror.juror.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                QJuror.juror.postcode.trim()
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

    private void orderQueryResults(JPAQuery<Tuple> jpaQuery,
                                   boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(QJurorHistory.jurorHistory.dateCreated.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }

        jpaQuery.orderBy(QJurorPool.jurorPool.deferralDate.desc())
            .orderBy(QJuror.juror.jurorNumber.asc());
    }

    private BooleanExpression currentlyDeferred() {
        return QJurorPool.jurorPool.deferralDate.isNotNull().and(QJurorPool.jurorPool.isActive.isTrue());
    }
}
