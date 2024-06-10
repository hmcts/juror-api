package uk.gov.hmcts.juror.api.moj.repository.trial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.PanelId;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

import java.util.List;
import java.util.Set;

@Repository
public interface PanelRepository extends IPanelRepository, JpaRepository<Panel, PanelId>,
    QuerydslPredicateExecutor<Panel> {

    List<Panel> findByTrialTrialNumberAndTrialCourtLocationLocCode(String trialNumber, String locCode);

    Panel findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorJurorNumber(String trialNumber,
                                                                                String locCode,
                                                                                String jurorNumber);

    boolean existsByTrialTrialNumberAndTrialCourtLocationLocCode(String trialNumber, String locCode);


    boolean existsByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorJurorNumber(String trialNumber,
                                                                                    String locCode,
                                                                                    String poolNumber);

    Panel findByTrialCourtLocationLocCodeAndJurorJurorNumberAndCompletedAndResultIsNullOrResultIsIn(String locCode,
                                                                                      String jurorNumber,
                                                                                      boolean completed,
                                                                                      Set<PanelResult> resultSet);
}
