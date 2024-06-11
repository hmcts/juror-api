package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link PhoneLog}.
 */
@Repository
@Deprecated(forRemoval = true)
public interface PhoneLogRepository extends CrudRepository<PhoneLog, PhoneLogKey> {
    List<PhoneLog> findByJurorNumber(String jurorNumber);
}
