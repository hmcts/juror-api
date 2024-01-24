package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;

import java.util.List;

@Repository
public interface JurorReasonableAdjustmentRepository extends JpaRepository<JurorReasonableAdjustment, Long>,
    QuerydslPredicateExecutor<JurorReasonableAdjustment> {

    List<JurorReasonableAdjustment> findByJurorNumber(String jurorNumber);

    JurorReasonableAdjustment findByJurorNumberAndReasonableAdjustment(String jurorNumber,
                                                                       ReasonableAdjustments reasonableAdjustment);
}
