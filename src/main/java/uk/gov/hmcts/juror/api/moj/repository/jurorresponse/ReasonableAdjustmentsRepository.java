package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;


import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;

@Repository
public interface ReasonableAdjustmentsRepository extends ReadOnlyRepository<ReasonableAdjustments, String> {


}
