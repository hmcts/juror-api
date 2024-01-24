package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.CourtCatchmentArea;
import uk.gov.hmcts.juror.api.moj.domain.CourtCatchmentAreaId;

@Repository
public interface CourtCatchmentAreaRepository extends CrudRepository<CourtCatchmentArea, CourtCatchmentAreaId>,
    QuerydslPredicateExecutor<CourtCatchmentArea> {

}
