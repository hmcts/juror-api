package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.PendingJuror;

@Repository
public interface PendingJurorRepository extends JpaRepository<PendingJuror, String>,
    QuerydslPredicateExecutor<PendingJuror>, IPendingJurorRepository {

    @Query(value = "SELECT juror_mod.generatependingjurornumber(:locationCode)",
        nativeQuery = true)
    String generatePendingJurorNumber(@Param("locationCode") String locationCode);

}
