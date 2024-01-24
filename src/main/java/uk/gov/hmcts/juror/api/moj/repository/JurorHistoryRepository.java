package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JurorHistoryRepository extends CrudRepository<JurorHistory, Long>, IJurorHistoryRepository,
    QuerydslPredicateExecutor<JurorHistory> {

    List<JurorHistory> findByJurorNumberAndDateCreatedGreaterThanEqual(String jurorNumber, LocalDateTime dateCreated);

    List<JurorHistory> findByJurorNumber(String jurorNumber);
}
