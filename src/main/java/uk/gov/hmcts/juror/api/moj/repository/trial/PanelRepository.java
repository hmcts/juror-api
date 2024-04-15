package uk.gov.hmcts.juror.api.moj.repository.trial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.PanelId;

import java.util.List;

@Repository
public interface PanelRepository extends IPanelRepository, JpaRepository<Panel, PanelId>,
    QuerydslPredicateExecutor<Panel> {

    List<Panel> findByTrialTrialNumberAndTrialCourtLocationLocCode(String trialNumber, String locCode);

    Panel findByTrialTrialNumberAndJurorPoolJurorJurorNumber(String trialNumber, String jurorNumber);

    boolean existsByTrialTrialNumber(String trialNumber);

    boolean existsByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorPoolPoolPoolNumberAndJurorPoolJurorJurorNumber(
        String trialNumber, String locCode, String jurorNumber, String poolNumber);

    Panel findByTrialCourtLocationLocCodeAndJurorPoolPoolPoolNumberAndJurorPoolJurorJurorNumberAndCompleted(
        String locCode, String poolNumber, String jurorNumber, boolean completed);
}
