package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QCurrentlyDeferred;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QCombinedJurorResponse;
import uk.gov.hmcts.juror.api.moj.utils.PoolRequestUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Slf4j
public class IJurorCommonResponseRepositoryModImpl implements IJurorCommonResponseRepositoryMod {
  @PersistenceContext EntityManager entityManager;

  @Override
  public List<Tuple> getJurorResponseDetailsByUsernameAndStatus(
      String staffLogin, Collection<ProcessingStatus> processingStatus, Predicate... predicates) {
    JPAQuery<Tuple> query =
        getJpaQueryFactory()
            .select(
                QCombinedJurorResponse.combinedJurorResponse,
                QJurorPool.jurorPool,
                QPoolRequest.poolRequest)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .join(QPoolRequest.poolRequest)
            .on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .where(QCombinedJurorResponse.combinedJurorResponse.staff.username.eq(staffLogin))
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.in(processingStatus))
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
    JPAQuery<Tuple> query =
        getJpaQueryFactory()
            .select(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus,
                QCombinedJurorResponse.combinedJurorResponse.count())
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER));

    if (predicates != null && predicates.length > 0) {
      query.where(predicates);
    }

    return query
        .groupBy(QCombinedJurorResponse.combinedJurorResponse.processingStatus)
        .fetch()
        .stream()
        .collect(
            Collectors.toMap(
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.processingStatus),
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.count())));
  }

  @Override
  public List<Tuple> getJurorResponseDetailsByCourtAndStatus(
      String locCode, Collection<ProcessingStatus> processingStatus, Predicate... predicates) {
    JPAQuery<Tuple> query =
        getJpaQueryFactory()
            .select(
                QCombinedJurorResponse.combinedJurorResponse,
                QJurorPool.jurorPool,
                QPoolRequest.poolRequest)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .join(QPoolRequest.poolRequest)
            .on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.in(processingStatus))
            .where(
                QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNotNull())
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
    JPAQuery<Tuple> query =
        getJpaQueryFactory()
            .select(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus,
                QCombinedJurorResponse.combinedJurorResponse.count())
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(
                QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNotNull())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));

    if (predicates != null && predicates.length > 0) {
      query.where(predicates);
    }

    return query
        .groupBy(QCombinedJurorResponse.combinedJurorResponse.processingStatus)
        .fetch()
        .stream()
        .collect(
            Collectors.toMap(
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.processingStatus),
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.count())));
  }

  JPAQueryFactory getJpaQueryFactory() {
    return new JPAQueryFactory(entityManager);
  }

  @Override
  public int getOpenResponsesAtCourt(String locationCode) {
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
        .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()))
        .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(locationCode))
        .fetch()
        .size();
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
        .select(QCombinedJurorResponse.combinedJurorResponse)
        .from(QCombinedJurorResponse.combinedJurorResponse)
        .join(QJurorPool.jurorPool)
        .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
        .join(QCurrentlyDeferred.currentlyDeferred)
        .on(
            QCurrentlyDeferred.currentlyDeferred.jurorNumber.eq(
                QCombinedJurorResponse.combinedJurorResponse.juror.jurorNumber))
        .where(QCombinedJurorResponse.combinedJurorResponse.deferral.eq(true))
        .where(QCurrentlyDeferred.currentlyDeferred.owner.eq(SecurityUtil.BUREAU_OWNER))
        .where(
            QCurrentlyDeferred.currentlyDeferred.deferredTo.between(
                LocalDateTime.now().toLocalDate(), LocalDateTime.now().plusWeeks(1).toLocalDate()))
        .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED))
        .where(QJurorPool.jurorPool.isActive.isTrue())
        .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
        .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
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
        return getJpaQueryFactory()
            .select(
                QJurorPool.jurorPool,
                QPoolRequest.poolRequest)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .join(QPoolRequest.poolRequest)
            .on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(
                QCombinedJurorResponse.combinedJurorResponse.dateReceived
                    .between(LocalDateTime.now(), LocalDateTime.now().plusWeeks(1)))
            .fetch().size();
    }
}
