package uk.gov.hmcts.juror.api.moj.repository.trial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.domain.trial.TrialId;

import java.util.Optional;

public interface TrialRepository extends ITrialRepository, JpaRepository<Trial, TrialId>,
    QuerydslPredicateExecutor<Trial> {

    Optional<Trial> findByTrialNumberAndCourtLocationLocCode(String trialNumber, String locCode);

    boolean existsByTrialNumberAndCourtLocationLocCode(String trialNumber, String locCode);
    
}
