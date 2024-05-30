package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialSearch;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
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

    @Override
    public <T> PaginatedList<T> getListOfTrials(TrialSearch trialSearch,
                                                Function<Trial, T> dataMapper) {
        JPAQuery<Trial> query = getQueryFactory()
            .select(TRIAL)
            .from(TRIAL)
            .where(TRIAL.courtLocation.locCode.in(SecurityUtil.getCourts()));

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
                TRIAL.trialType.stringValue(),
                TRIAL.courtroom.description,
                TRIAL.judge.name,
                PANEL.juror.count())
            .from(TRIAL)
            .join(PANEL)
            .on(TRIAL.eq(PANEL.trial))
            .where(TRIAL.trialEndDate.isNull().and(TRIAL.courtLocation.locCode.eq(locationCode)))
            .where(TRIAL.trialStartDate.loe(attendanceDate))
            .where(PANEL.result.eq(PanelResult.JUROR))
            .groupBy(TRIAL.trialNumber, TRIAL.description, TRIAL.trialType.stringValue(), TRIAL.courtroom.description,
                TRIAL.judge.name)
            .fetch();
    }
}
