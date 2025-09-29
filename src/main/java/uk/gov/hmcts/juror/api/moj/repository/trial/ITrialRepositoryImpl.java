package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;


public class ITrialRepositoryImpl implements ITrialRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QTrial TRIAL = QTrial.trial;
    private static final QPanel PANEL = QPanel.panel;
    private static final QAppearance APPEARANCE = QAppearance.appearance;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJurorHistory JUROR_HISTORY = QJurorHistory.jurorHistory;

    @Override
    public <T> PaginatedList<T> getListOfTrials(TrialSearch trialSearch,
                                                Function<Trial, T> dataMapper) {
        JPAQuery<Trial> query = getQueryFactory()
            .select(TRIAL)
            .from(TRIAL)
            .where(TRIAL.courtLocation.locCode.in(SecurityUtil.getCourts()));
        trialSearch.apply(query);
        return PaginationUtil.toPaginatedList(query, trialSearch,
            TrialSearch.SortField.TRIAL_NUMBER,
            SortMethod.DESC,
            dataMapper, null);
    }

    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Override
    public List<Trial> getListOfActiveTrials(String locCode) {
        JPQLQuery<Trial> query = new JPAQuery<>(entityManager);
        query.select(TRIAL).from(TRIAL).where(TRIAL.courtLocation.locCode.eq(locCode));
        query.where(TRIAL.trialEndDate.isNull());
        return query.fetch();
    }

    @Override
    public List<Tuple> getActiveTrialsWithJurorCount(String locationCode, LocalDate attendanceDate) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(TRIAL.trialNumber,
                TRIAL.description,
                TRIAL.trialType,
                TRIAL.courtroom.description,
                TRIAL.judge.name,
                PANEL.count())
            .from(TRIAL)
            .join(PANEL)
            .on(TRIAL.eq(PANEL.trial))
            .leftJoin(APPEARANCE).on(
                APPEARANCE.jurorNumber.eq(PANEL.juror.jurorNumber)
                    .and(APPEARANCE.locCode.eq(locationCode))
                    .and(APPEARANCE.attendanceDate.eq(attendanceDate))
                    .and(APPEARANCE.attendanceAuditNumber.startsWith("J"))
                    .and(APPEARANCE.trialNumber.eq(TRIAL.trialNumber))
            )
            .where(TRIAL.courtLocation.locCode.eq(locationCode))
            .where(TRIAL.trialStartDate.loe(attendanceDate))
            .where(TRIAL.trialEndDate.isNull().or(TRIAL.trialEndDate.goe(attendanceDate)))
            //If they are a JUROR or Panelled or have an appearance with a jury confirmation audit number
            .where((PANEL.empanelledDate.isNotNull().and(PANEL.returnDate.isNull()
                                                             .or(PANEL.returnDate.after(attendanceDate))))
                .or(APPEARANCE.isNotNull())
                       .or(PANEL.empanelledDate.isNull().and(PANEL.result.isNull())))
            .groupBy(TRIAL.trialNumber, TRIAL.description, TRIAL.trialType, TRIAL.courtroom.description,
                TRIAL.judge.name)
            .fetch();
    }

    @Override
    public List<PanelListDto> getReturnedJurors(String trialNo, String locCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        List<Tuple> query = queryFactory.select(JUROR_POOL.juror.jurorNumber,
                                                JUROR_POOL.juror.firstName,
                                                JUROR_POOL.juror.lastName,
                                                JUROR_POOL.status.statusDesc)
            .from(JUROR_POOL)
            .join(PANEL)
            .on(JUROR_POOL.juror.jurorNumber.eq(PANEL.juror.jurorNumber).and(PANEL.trial.courtLocation.locCode.eq(locCode)
                                                                                 .and(PANEL.trial.trialNumber.eq(trialNo))))
            .join(JUROR_HISTORY)
            .on(JUROR_POOL.juror.jurorNumber.eq(JUROR_HISTORY.jurorNumber).and(JUROR_HISTORY.historyCode.eq(
                HistoryCodeMod.JURY_EMPANELMENT).and(JUROR_HISTORY.otherInformationRef.eq(trialNo))))
            .where(PANEL.result.eq(PanelResult.RETURNED))
            .where(JUROR_POOL.isActive.isTrue())
            .orderBy(JUROR_POOL.juror.jurorNumber.asc())
            .fetch();

        return query.stream().map(t -> new PanelListDto(
                t.get(JUROR_POOL.juror.jurorNumber),
                t.get(JUROR_POOL.juror.firstName),
                t.get(JUROR_POOL.juror.lastName),
                t.get(JUROR_POOL.status.statusDesc)
            )).toList();
    }
}
