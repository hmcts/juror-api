package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link BureauJurorCjs} entities.
 */
@Repository
public interface BureauJurorCjsRepository extends CrudRepository<BureauJurorCjs, Long>,
    QuerydslPredicateExecutor<BureauJurorCjs> {
    List<BureauJurorCjs> findByJurorNumber(String jurorNumber);

    // BureauJurorCJS findByEmployerAndId(String employer, String jurorNumber);

    BureauJurorCjs findByJurorNumberAndEmployer(String jurorNumber, String employer);
}
