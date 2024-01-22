package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;

import java.util.List;

@Repository
public interface JurorResponseCjsEmploymentRepositoryMod extends JpaRepository<JurorResponseCjsEmployment, Long>,
    QuerydslPredicateExecutor<JurorResponseCjsEmployment> {

    List<JurorResponseCjsEmployment> findByJurorNumber(String jurorNumber);

    JurorResponseCjsEmployment findByJurorNumberAndCjsEmployer(String jurorNumber, String employer);
}
