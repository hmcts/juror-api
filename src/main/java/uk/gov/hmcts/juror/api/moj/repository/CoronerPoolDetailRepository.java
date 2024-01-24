package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.CoronerPoolDetail;
import uk.gov.hmcts.juror.api.moj.domain.CoronerPoolDetailId;

import java.util.List;

@Repository
public interface CoronerPoolDetailRepository extends JpaRepository<CoronerPoolDetail, CoronerPoolDetailId>,
    QuerydslPredicateExecutor<CoronerPoolDetail> {

    List<CoronerPoolDetail> findAllByPoolNumber(String poolNumber);

    Integer countByPoolNumber(String poolNumber);
}


