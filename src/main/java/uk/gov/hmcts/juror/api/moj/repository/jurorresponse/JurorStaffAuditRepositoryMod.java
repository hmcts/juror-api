package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.StaffJurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.StaffJurorResponseAuditKeyMod;

@Repository
public interface JurorStaffAuditRepositoryMod extends CrudRepository<StaffJurorResponseAuditMod,
    StaffJurorResponseAuditKeyMod>,
    QuerydslPredicateExecutor<StaffJurorResponseAuditMod> {
}
