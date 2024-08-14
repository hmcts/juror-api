package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Voters;

@Repository
public interface VotersRepository extends JpaRepository<Voters, Voters.VotersId>,
    QuerydslPredicateExecutor<Voters> {

}
