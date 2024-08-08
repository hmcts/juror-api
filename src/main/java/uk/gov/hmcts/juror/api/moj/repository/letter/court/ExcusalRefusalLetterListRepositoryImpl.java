package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.Tuple;
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
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalRefusedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ExcusalRefusalLetterListRepositoryImpl implements IExcusalRefusalLetterListRepository {

    public static final Comparator<ExcusalRefusedLetterList> DATE_PRINT_COMPARATOR = (o1, o2) -> {
        if (o1.getDatePrinted() == null && o2.getDatePrinted() == null) {
            return 0;
        } else if (o1.getDatePrinted() == null) {
            return -1;
        } else if (o2.getDatePrinted() == null) {
            return 1;
        } else {
            return o2.getDatePrinted().compareTo(o1.getDatePrinted());
        }
    };

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<ExcusalRefusedLetterList> findJurorsEligibleForExcusalRefusalLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQuery<Tuple> jpaQuery = buildBaseQuery(owner);

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderExcusalRefusedQueryResults(jpaQuery);

        List<Tuple> results = jpaQuery.fetch();

        List<ExcusalRefusedLetterList> excusalRefusedLetterLists = results.stream().map(tuple ->
            ExcusalRefusedLetterList.builder()
                .poolNumber(tuple.get(QPoolRequest.poolRequest.poolNumber))
                .jurorNumber(tuple.get(QJuror.juror.jurorNumber))
                .firstName(tuple.get(QJuror.juror.firstName))
                .lastName(tuple.get(QJuror.juror.lastName))
                .postcode(tuple.get(QJuror.juror.postcode))
                .status(tuple.get(QJurorStatus.jurorStatus.statusDesc))
                .dateExcused(tuple.get(QJuror.juror.excusalDate))
                .reason(tuple.get(QJuror.juror.excusalCode))
                .isActive(tuple.get(QJurorPool.jurorPool.isActive))
                .build())
            .collect(Collectors.toList());

        ConcurrentHashMap<String, ExcusalRefusedLetterList>
            excusalRefusedLetterListMap = removeDuplicateJurorRecords(excusalRefusedLetterLists);

        List<String> poolNumbers =
            excusalRefusedLetterLists.stream().map(ExcusalRefusedLetterList::getPoolNumber).distinct().toList();

        // run query to get the most recent excusal refused date for each juror
        List<Tuple> printedDates = getDatePrinted(owner, poolNumbers);

        updateDatePrinted(searchCriteria, excusalRefusedLetterListMap, printedDates);

        // sort by date printed (need unprinted letters to be at the top)
        return excusalRefusedLetterListMap.values().stream().sorted(DATE_PRINT_COMPARATOR).toList();
    }

    private ConcurrentHashMap<String, ExcusalRefusedLetterList> removeDuplicateJurorRecords(
        List<ExcusalRefusedLetterList> excusalRefusedLetterLists) {
        // for each juror select the most recent excusal refused date and remove the other entries
        ConcurrentHashMap<String, ExcusalRefusedLetterList> excusalRefusedLetterListMap = new ConcurrentHashMap<>();
        excusalRefusedLetterLists.forEach(excusalRefusedLetterList -> {
            String jurorNumber = excusalRefusedLetterList.getJurorNumber();
            if (excusalRefusedLetterListMap.containsKey(jurorNumber)) {
                ExcusalRefusedLetterList existingExcusalRefusedLetterList =
                    excusalRefusedLetterListMap.get(jurorNumber);
                if (excusalRefusedLetterList.getDateExcused()
                    .isAfter(existingExcusalRefusedLetterList.getDateExcused())) {
                    excusalRefusedLetterListMap.put(jurorNumber, excusalRefusedLetterList);
                }
            } else {
                excusalRefusedLetterListMap.put(jurorNumber, excusalRefusedLetterList);
            }
        });
        return excusalRefusedLetterListMap;
    }

    private void updateDatePrinted(CourtLetterSearchCriteria searchCriteria,
                                  ConcurrentHashMap<String, ExcusalRefusedLetterList> excusalRefusedLetterListMap,
                                  List<Tuple> printedDates) {
        // update the excusal refused letter printed date for each juror
        printedDates.forEach(tuple -> {
            String jurorNumber = tuple.get(QJuror.juror.jurorNumber);
            if (excusalRefusedLetterListMap.containsKey(jurorNumber)) {
                ExcusalRefusedLetterList excusalRefusedLetterList = excusalRefusedLetterListMap.get(jurorNumber);
                LocalDateTime datePrinted = tuple.get(QJurorHistory.jurorHistory.dateCreated);
                LocalDate datePrintedDateOnly = datePrinted.toLocalDate();
                if (datePrintedDateOnly.equals(excusalRefusedLetterList.getDateExcused())
                    || datePrintedDateOnly.isAfter(excusalRefusedLetterList.getDateExcused())) {
                    if (!searchCriteria.includePrinted()) {
                        excusalRefusedLetterListMap.remove(jurorNumber);
                    } else {
                        excusalRefusedLetterList.setDatePrinted(datePrinted);
                    }
                }
            }
        });
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
                QJuror.juror.excusalCode,
                QJuror.juror.excusalDate,
                QJurorPool.jurorPool.isActive)
            .from(QJurorPool.jurorPool)
            .join(QJuror.juror).on(QJuror.juror.eq(QJurorPool.jurorPool.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .join(QJurorStatus.jurorStatus).on(QJurorStatus.jurorStatus.eq(QJurorPool.jurorPool.status))
            .leftJoin(QJurorHistory.jurorHistory)
            .on(QJurorHistory.jurorHistory.jurorNumber.eq(QJuror.juror.jurorNumber)
                .and(QJurorHistory.jurorHistory.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
                .and(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.EXCUSE_POOL_MEMBER))
                .and(QJurorHistory.jurorHistory.otherInformation.contains("Refuse Excuse"))
                .and(QJurorHistory.jurorHistory.dateCreatedDateOnly.after(QJuror.juror.bureauTransferDate)))
            .where(QJurorPool.jurorPool.isActive.isTrue()
                .and(QJuror.juror.excusalRejected.eq("Y"))
                .and(QJurorPool.jurorPool.owner.eq(owner)));
    }

    private List<Tuple> getDatePrinted(String owner, List<String> poolNumbers) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(
                QPoolRequest.poolRequest.poolNumber,
                QJuror.juror.jurorNumber,
                QJurorHistory.jurorHistory.dateCreated)
            .from(QJurorPool.jurorPool)
            .join(QJuror.juror).on(QJuror.juror.eq(QJurorPool.jurorPool.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .join(QJurorStatus.jurorStatus).on(QJurorStatus.jurorStatus.eq(QJurorPool.jurorPool.status))
            .leftJoin(QJurorHistory.jurorHistory)
            .on(QJurorHistory.jurorHistory.jurorNumber.eq(QJuror.juror.jurorNumber)
                .and(QJurorHistory.jurorHistory.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
                .and(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.NON_EXCUSED_LETTER))
                .and(QJurorHistory.jurorHistory.dateCreatedDateOnly.after(QJuror.juror.bureauTransferDate)))
            .where(QJurorPool.jurorPool.isActive.isTrue()
                .and(QJuror.juror.excusalRejected.eq("Y"))
                .and(QJurorPool.jurorPool.owner.eq(owner))
                .and(QPoolRequest.poolRequest.poolNumber.in(poolNumbers)))
            .fetch();
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
            jpaQuery.where(QPoolRequest.poolRequest.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }
    }

    private void orderExcusalRefusedQueryResults(JPAQuery<Tuple> jpaQuery) {

        jpaQuery.orderBy(QJurorHistory.jurorHistory.dateCreatedDateOnly.desc(),
            QJuror.juror.jurorNumber.asc());
    }
}
