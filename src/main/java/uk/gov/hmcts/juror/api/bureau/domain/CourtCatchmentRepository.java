package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link CourtCatchmentRepository} entities.
 */

@Repository
@Deprecated(forRemoval = true)
public interface CourtCatchmentRepository extends CrudRepository<CourtCatchmentEntity, String>,
    QuerydslPredicateExecutor<CourtCatchmentEntity> {
}
