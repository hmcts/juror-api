package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

import java.util.List;

public class PanelRepositoryImpl implements IPanelRepository {
    @PersistenceContext
    EntityManager entityManager;

    private static final QPanel PANEL = QPanel.panel;

    /** Retrieves members that are on trial i.e. panel/jury.
     *
     * @param trialNumber Trial number
     * @param locationCode Court location code
     * @return Panel entity
     */
    @Override
    public List<Panel> retrieveMembersOnTrial(String trialNumber, String locationCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory
            .select(PANEL)
            .from(PANEL)
            .where(PANEL.trial.trialNumber.eq(trialNumber))
            .where(PANEL.jurorPool.location.eq(locationCode))
            .where(PANEL.result.eq(PanelResult.JUROR)
                .and(PANEL.jurorPool.status.status.eq(IJurorStatus.JUROR))
                .or(PANEL.jurorPool.status.status.eq(IJurorStatus.PANEL)))
            .fetch();
    }
}
