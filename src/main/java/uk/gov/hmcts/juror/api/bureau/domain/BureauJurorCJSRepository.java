package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link BureauJurorCJS} entities.
 */
@Repository
public interface BureauJurorCJSRepository extends CrudRepository<BureauJurorCJS, Long>,
    QuerydslPredicateExecutor<BureauJurorCJS> {
    List<BureauJurorCJS> findByJurorNumber(String jurorNumber);

    // BureauJurorCJS findByEmployerAndId(String employer, String jurorNumber);

    BureauJurorCJS findByJurorNumberAndEmployer(String jurorNumber, String employer);
}
