package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link PartAmendment}.
 */
@Repository
@Deprecated(forRemoval = true)
public interface PartAmendmentRepository extends CrudRepository<PartAmendment, PartAmendmentKey> {

    List<PartAmendment> findByJurorNumberAndOwner(String jurorNumber, String owner);

}
