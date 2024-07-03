package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditModKey;

@Repository
public interface JurorResponseAuditRepositoryMod
    extends CrudRepository<JurorResponseAuditMod, JurorResponseAuditModKey> {
}
