package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.CoronerPool;

import java.util.Optional;

@Repository
public interface CoronerPoolRepository extends JpaRepository<CoronerPool, String>,
    QuerydslPredicateExecutor<CoronerPool> {

    Optional<CoronerPool> findFirstByOrderByPoolNumberDesc();

}


