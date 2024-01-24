package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link CourtWhitelist}.
 */
@Repository
public interface CourtWhitelistRepository extends CrudRepository<CourtWhitelist, String> {
    CourtWhitelist findByLocCode(String locCode);
}
