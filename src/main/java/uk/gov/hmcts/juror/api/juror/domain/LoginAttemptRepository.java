package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link LoginAttempt}.
 */
@Repository
public interface LoginAttemptRepository extends CrudRepository<LoginAttempt, String> {
    LoginAttempt findByUsername(String username);
}
