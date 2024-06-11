package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Deprecated(forRemoval = true)
public interface DefDeniedRepository extends CrudRepository<DefDenied, String> {
}
