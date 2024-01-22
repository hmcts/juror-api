package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCJSEmployment;

import java.util.List;

@Repository
public interface JurorResponseCJSEmploymentRepositoryMod extends JpaRepository<JurorResponseCJSEmployment, Long>,
    QuerydslPredicateExecutor<JurorResponseCJSEmployment> {

    List<JurorResponseCJSEmployment> findByJurorNumber(String jurorNumber);

    JurorResponseCJSEmployment findByJurorNumberAndCjsEmployer(String jurorNumber, String employer);
}
