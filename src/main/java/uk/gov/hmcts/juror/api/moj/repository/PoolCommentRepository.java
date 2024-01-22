package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.PoolComment;

@Repository
public interface PoolCommentRepository extends CrudRepository<PoolComment, Long>,
    QuerydslPredicateExecutor<PoolComment> {

}
