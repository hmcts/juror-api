package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository for {@link PartHist}.
 */
@Repository
@Deprecated(forRemoval = true)
public interface PartHistRepository extends CrudRepository<PartHist, PartHistKey> {

    List<PartHist> findByJurorNumberAndOwnerAndDatePartGreaterThanEqual(String juroNumber, String owner, Date datePart);

    List<PartHist> findByJurorNumberAndOwner(String juroNumber, String owner);

}
