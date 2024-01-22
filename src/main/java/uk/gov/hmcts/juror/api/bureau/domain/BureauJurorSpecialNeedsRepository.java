package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link BureauJurorSpecialNeed} entities.
 */
@Repository
public interface BureauJurorSpecialNeedsRepository extends CrudRepository<BureauJurorSpecialNeed, Long>,
    QuerydslPredicateExecutor<BureauJurorSpecialNeed> {
    List<BureauJurorSpecialNeed> findByJurorNumber(String jurorNumber);
}
