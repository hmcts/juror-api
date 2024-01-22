package uk.gov.hmcts.juror.api.moj.repository.trial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;

import java.util.List;

@Repository
public interface JudgeRepository extends
    JpaRepository<Judge, Long>,
    QuerydslPredicateExecutor<Judge> {

    List<Judge> findByOwner(String owner);
}
