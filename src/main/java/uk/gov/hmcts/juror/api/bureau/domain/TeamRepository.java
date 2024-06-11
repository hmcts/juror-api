package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Team} entities.
 */
@Repository
@Deprecated(forRemoval = true)
public interface TeamRepository extends CrudRepository<Team, Long>, QuerydslPredicateExecutor<Team> {
    Team findByTeamName(String teamName);
}
