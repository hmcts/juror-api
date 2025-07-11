package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

import java.time.LocalDate;
import java.util.List;

public class PanelRepositoryImpl implements IPanelRepository {
    @PersistenceContext
    EntityManager entityManager;

    private static final QPanel PANEL = QPanel.panel;
    private static final QTrial TRIAL = QTrial.trial;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QAppearance APPEARANCE = QAppearance.appearance;

    /**
     * Retrieves members that are on trial i.e. panel/jury.
     *
     * @param trialNumber  Trial number
     * @param locationCode Court location code
     *
     * @return Panel entity
     */
    @Override
    public List<Panel> retrieveMembersOnTrial(String trialNumber, String locationCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory
            .select(PANEL)
            .from(PANEL)
            .join(TRIAL).on(PANEL.trial.eq(TRIAL))
            .join(COURT_LOCATION).on(TRIAL.courtLocation.eq(COURT_LOCATION))
            .join(JUROR_POOL).on(PANEL.juror.eq(JUROR_POOL.juror))
            .where(COURT_LOCATION.locCode.eq(locationCode))
            .where(TRIAL.trialNumber.eq(trialNumber))
            .where(JUROR_POOL.pool.courtLocation.eq(COURT_LOCATION))
            .where(JUROR_POOL.isActive.isTrue())
            .where(PANEL.result.isNull().or(PANEL.result.eq(PanelResult.JUROR)))
            .where(JUROR_POOL.status.status.in(IJurorStatus.JUROR, IJurorStatus.PANEL))
            .fetch();
    }

    @Override
    public boolean isEmpanelledJuror(String jurorNumber, String locationCode, LocalDate date) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return !queryFactory
            .select(PANEL)
            .from(PANEL)
            .join(JUROR_POOL)
            .on(PANEL.juror.eq(JUROR_POOL.juror))
            .leftJoin(APPEARANCE)
            .on(PANEL.trial.trialNumber.eq(APPEARANCE.trialNumber)
                    .and(PANEL.juror.jurorNumber.eq(APPEARANCE.jurorNumber))
                    .and(APPEARANCE.attendanceDate.eq(date)))
            .where(PANEL.juror.jurorNumber.eq(jurorNumber))
            .where(JUROR_POOL.pool.courtLocation.locCode.eq(locationCode))
            .where(JUROR_POOL.isActive.isTrue())
            .where(PANEL.completed.isTrue())
            .where(PANEL.empanelledDate.isNotNull().and(PANEL.empanelledDate.loe(date)
                                                            .and(PANEL.returnDate.isNull()
                                                                     .or(PANEL.returnDate.after(date)))))
            .fetch().isEmpty();
    }

    @Override
    public long getCountPanelledJurors(String locCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        Long count = queryFactory
            .select(PANEL.count())
            .from(PANEL)
            .join(JUROR_POOL)
            .on(PANEL.juror.eq(JUROR_POOL.juror))
            .where(PANEL.completed.isFalse())
            .where(PANEL.result.isNull())
            .where(PANEL.empanelledDate.isNull())
            .where(JUROR_POOL.pool.courtLocation.locCode.eq(locCode))
            .where(JUROR_POOL.isActive.isTrue())
            .where(JUROR_POOL.status.status.eq(JurorStatusEnum.PANEL.getStatus()))
            .fetchFirst();

        return count != null ? count : 0L;
    }

}
