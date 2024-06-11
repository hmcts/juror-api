package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link StatsUnprocessedResponse}.
 */
@Repository
@Deprecated(forRemoval = true)
public interface StatsUnprocessedResponseRepository extends CrudRepository<StatsUnprocessedResponse, String> {
}
