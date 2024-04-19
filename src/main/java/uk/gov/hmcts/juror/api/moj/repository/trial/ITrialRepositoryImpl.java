package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


@SuppressWarnings("PMD.LawOfDemeter")
public class ITrialRepositoryImpl implements ITrialRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QTrial TRIAL = QTrial.trial;
    private static final QPanel PANEL = QPanel.panel;

    private JPQLQuery<Trial> buildCommonQuery(String trialNumber, List<String> locCode, boolean isActiveFilter) {
        JPQLQuery<Trial> query = new JPAQuery<>(entityManager);

        query.select(TRIAL).from(TRIAL).where(TRIAL.courtLocation.locCode.in(locCode));

        if (isActiveFilter) {
            query.where(TRIAL.trialEndDate.isNull());
        }

        if (trialNumber != null) {
            query.where(TRIAL.trialNumber.startsWith(trialNumber));
        }
        return query;
    }

    @Override
    public List<Trial> getListOfTrialsForCourtLocations(List<String> locCode, boolean isActiveFilter,
                                                        String trialNumber, Pageable pageable) {
        Querydsl querydsl = new Querydsl(entityManager, new PathBuilderFactory().create(Trial.class));
        return querydsl.applyPagination(pageable, buildCommonQuery(trialNumber, locCode, isActiveFilter)).fetch();
    }

    @Override
    public List<Trial> getListOfActiveTrials(String locCode) {
        return buildCommonQuery(null, Collections.singletonList(locCode), true).fetch();
    }


    @Override
    public Long getTotalTrialsForCourtLocations(List<String> locCode, boolean isActiveFilter) {
        JPQLQuery<Trial> query = new JPAQuery<>(entityManager);

        query.select(TRIAL).from(TRIAL).where(TRIAL.courtLocation.locCode.in(locCode));

        if (isActiveFilter) {
            query.where(TRIAL.trialEndDate.isNull());
        }
        return query.fetchCount();
    }

    @Override
    public List<Tuple> getActiveTrialsWithJurorCount(String locationCode, LocalDate attendanceDate) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(TRIAL.trialNumber,
                TRIAL.description,
                TRIAL.trialType.stringValue(),
                TRIAL.courtroom.description,
                TRIAL.judge.name,
                PANEL.jurorPool.count())
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
