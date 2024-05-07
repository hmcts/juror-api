package uk.gov.hmcts.juror.api.moj.repository.staff;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.UserJurorResponseAudit;

public interface UserJurorResponseAuditRepository extends CrudRepository<UserJurorResponseAudit, Long> {
}
