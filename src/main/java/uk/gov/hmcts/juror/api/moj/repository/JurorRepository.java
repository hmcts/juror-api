package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

@Repository
public interface JurorRepository extends RevisionRepository<Juror, String, Long>, IJurorRepository,
    JpaRepository<Juror, String>, QuerydslPredicateExecutor<Juror> {

    Juror findByJurorNumber(String jurorNumber);

}
