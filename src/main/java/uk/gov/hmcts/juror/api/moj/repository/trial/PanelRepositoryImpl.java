package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

import java.util.List;

@SuppressWarnings("PMD.LawOfDemeter")
public class PanelRepositoryImpl implements IPanelRepository {
    @PersistenceContext
    EntityManager entityManager;

    private static final QPanel PANEL = QPanel.panel;
    private static final QTrial TRIAL = QTrial.trial;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;
    private static final QJuror JUROR = QJuror.juror;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;

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
            .where(PANEL.result.eq(PanelResult.JUROR))
            .where(JUROR_POOL.status.status.in(IJurorStatus.JUROR, IJurorStatus.PANEL))
            .fetch();
    }

}
