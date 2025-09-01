package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QCurrentlyDeferred;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QCombinedJurorResponse;
import uk.gov.hmcts.juror.api.moj.repository.SystemParameterRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.querydsl.jpa.JPAExpressions.select;


public class IJurorCommonResponseRepositoryModImpl implements IJurorCommonResponseRepositoryMod {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<Tuple> getJurorResponseDetailsByUsernameAndStatus(String staffLogin,
                                                                  Collection<ProcessingStatus> processingStatus,
                                                                  Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse,
                QJurorPool.jurorPool,
                QPoolRequest.poolRequest
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .where(QCombinedJurorResponse.combinedJurorResponse.staff.username.eq(staffLogin))
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.in(processingStatus))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }
        query.orderBy(QCombinedJurorResponse.combinedJurorResponse.dateReceived.asc());
        return query.fetch();
    }

    @Override
    public Map<ProcessingStatus, Long> getJurorResponseCounts(Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus,
                QCombinedJurorResponse.combinedJurorResponse.count()
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }

        return query.groupBy(QCombinedJurorResponse.combinedJurorResponse.processingStatus)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.processingStatus),
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.count())
            ));
    }




    @Override
    public List<Tuple> getJurorResponseDetailsByCourtAndStatus(String locCode,
                                                                Collection<ProcessingStatus> processingStatus,
                                                                Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse,
                QJurorPool.jurorPool,
                QPoolRequest.poolRequest
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.in(processingStatus))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNotNull())
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }
        query.orderBy(QCombinedJurorResponse.combinedJurorResponse.dateReceived.asc());
        return query.fetch();
    }

    @Override
    public Map<ProcessingStatus, Long> getJurorCourtResponseCounts(Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus,
                QCombinedJurorResponse.combinedJurorResponse.count()
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNotNull())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }

        return query.groupBy(QCombinedJurorResponse.combinedJurorResponse.processingStatus)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.processingStatus),
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.count())
            ));
    }


    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }


    @Override
    public int getOpenResponsesAtCourt(String locationCode) {
        return getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(ProcessingStatus.CLOSED))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(locationCode))
            .fetch().size();

    }

    @Override
    public int getOpenResponsesAtBureau(String locCode) {

        return getJpaQueryFactory()
            .select(QCombinedJurorResponse.combinedJurorResponse)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(
                    ProcessingStatus.CLOSED))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .fetch()
            .size();
    }

    @Override
    public int getSummonsRepliesFourWeeks(String locCode) {

        return getJpaQueryFactory()
            .select(QCombinedJurorResponse.combinedJurorResponse)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(
                    ProcessingStatus.CLOSED))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QJurorPool.jurorPool.nextDate.before(LocalDateTime.now().plusWeeks(4).toLocalDate()))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .fetch()
            .size();
    }

    @Override
    public int getSummonsRepliesStandard(String locCode) {
        return getJpaQueryFactory()
            .select(QCombinedJurorResponse.combinedJurorResponse)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(
                    ProcessingStatus.CLOSED))
            .where(QCombinedJurorResponse.combinedJurorResponse.urgent.eq(false))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .fetch()
            .size();
    }

    @Override
    public int getSummonsRepliesUrgent(String locCode) {
        return getJpaQueryFactory()
            .select(QCombinedJurorResponse.combinedJurorResponse)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(
                    ProcessingStatus.CLOSED))
            .where(QCombinedJurorResponse.combinedJurorResponse.urgent.eq(true))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .fetch()
            .size();
    }

    @Override
    public int getSummonsRepliesUnassigned(String locCode) {
        return getJpaQueryFactory()
            .select(QCombinedJurorResponse.combinedJurorResponse)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(
                    ProcessingStatus.CLOSED))
            .where(QCombinedJurorResponse.combinedJurorResponse.staff.isNull())
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .fetch()
            .size();
    }

    @Override
    public int getSummonsRepliesAssigned(String locCode) {
        return getJpaQueryFactory()
            .select(QCombinedJurorResponse.combinedJurorResponse)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(
                    ProcessingStatus.CLOSED))
            .where(QCombinedJurorResponse.combinedJurorResponse.staff.isNotNull())
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .fetch()
            .size();
    }

    @Override
    public int getDeferredJurorsStartDateNextWeek(String locCode) {
        return getJpaQueryFactory()
            .select(QCurrentlyDeferred.currentlyDeferred)
            .from(QCurrentlyDeferred.currentlyDeferred)
            .where(QCurrentlyDeferred.currentlyDeferred.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(
                QCurrentlyDeferred.currentlyDeferred.deferredTo.between(
                    LocalDateTime.now().toLocalDate(), LocalDateTime.now().plusWeeks(1).toLocalDate()))
            .fetch()
            .size();
    }

    @Override
    public int getPoolsNotYetSummonedCount(String locCode) {
        return getJpaQueryFactory()
            .select(
                QPoolRequest.poolRequest)
            .from(QPoolRequest.poolRequest)
            .leftJoin(QJurorPool.jurorPool)
            .on(QPoolRequest.poolRequest.poolNumber.eq(QJurorPool.jurorPool.pool.poolNumber))
            .where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QJurorPool.jurorPool.pool.poolNumber.isNull()).fetch().size();
    }

    @Override
    public int getPoolsTransferringNextWeekCount(String locCode) {
      //  final SystemParameterRepositoryMod systemParameterRepository;

     //   int weeksAdjustment = Integer.parseInt(systemParameterRepository.findById(7).get().getValue());
    //    String transferDay = systemParameterRepository.findById(8).get().getValue();
        // Calculate the latest return date using the same logic as stored procedure
        LocalDate effectiveDate = LocalDate.now();

        // If running before 6pm on transfer day, use previous day
        if (LocalTime.now().isBefore(LocalTime.of(18, 0)) &&
            effectiveDate.getDayOfWeek() == DayOfWeek.THURSDAY) {
            effectiveDate = effectiveDate.minusDays(1);
        }

        // Add weekday adjustment (9 days for thu->thu based on pool transfer weekday)
        effectiveDate = effectiveDate.plusDays(9);

        // Add weeks adjustment (1 week = 7 days)
        LocalDate latestReturnDate = effectiveDate.plusDays(7);

        return getJpaQueryFactory()
            .select(QPoolRequest.poolRequest)
            .from(QPoolRequest.poolRequest)
            .where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QPoolRequest.poolRequest.returnDate.loe(latestReturnDate))
            .where(QPoolRequest.poolRequest.courtLocation.locCode.eq(locCode))
            .fetch().size();
    }

   //     LocalDate weekDateBeforeTransfer = LocalDate.now().plusDays(18);
   //     return getJpaQueryFactory()
   //         .select(
   //             QPoolRequest.poolRequest)
   //         .from(QPoolRequest.poolRequest)
   //         .join(QJurorPool.jurorPool)
   //         .on(QPoolRequest.poolRequest.poolNumber.eq(QJurorPool.jurorPool.pool.poolNumber))
   //         .where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.BUREAU_OWNER))
   //         .where(QJurorPool.jurorPool.pool.poolNumber.isNotNull())
   //         .where(QPoolRequest.poolRequest.returnDate.eq(weekDateBeforeTransfer)).fetch().size();


}
