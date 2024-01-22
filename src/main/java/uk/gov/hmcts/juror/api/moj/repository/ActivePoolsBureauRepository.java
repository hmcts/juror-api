package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.ActivePoolsBureau;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.util.List;

@Repository
public interface ActivePoolsBureauRepository extends JpaRepository<ActivePoolsBureau, String>,
    QuerydslPredicateExecutor<PoolRequest> {

    Page<ActivePoolsBureau> findByPoolTypeInAndCourtName(List<String> poolTypes, String courtName, Pageable pageable);

    Page<ActivePoolsBureau> findByPoolTypeInAndCourtNameIn(List<String> poolTypes, List<String> courtName,
                                                           Pageable pageable);

    Page<ActivePoolsBureau> findByPoolTypeIn(List<String> poolTypes, Pageable pageable);

}
