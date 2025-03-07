package uk.gov.hmcts.juror.api.moj.repository.trial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.PanelId;

import java.util.List;

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

    @Query(
        value = "SELECT * FROM juror_mod.juror_trial "
            + "WHERE loc_code = ?1 AND juror_number = ?2 AND completed = true AND (result IS NULL OR result = 'J')",
        nativeQuery = true
    )
    Panel findActivePanel(String locCode, String jurorNumber);

    @Query(
        value = "SELECT * FROM juror_mod.juror_trial "
            + "WHERE loc_code in ?1 AND juror_number = ?2 AND (result IS NULL OR result = 'J')",
        nativeQuery = true
    )
    Panel findActivePanelByCourtGroup(List<String> locCode, String jurorNumber);

    long countByJurorJurorNumberAndTrialCourtLocationLocCode(String jurorNumber, String locCode);
}
