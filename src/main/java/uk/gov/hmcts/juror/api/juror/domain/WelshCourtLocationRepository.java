package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link WelshCourtLocation}.
 */
@Repository
public interface WelshCourtLocationRepository extends CrudRepository<WelshCourtLocation, String> {

    WelshCourtLocation findByLocCode(String locCode);
}
