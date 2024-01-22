package uk.gov.hmcts.juror.api.moj.repository.trial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;

@Repository
public interface CourtroomRepository extends ICourtroomRepository, JpaRepository<Courtroom, Long>,
    QuerydslPredicateExecutor<Courtroom> {
}
