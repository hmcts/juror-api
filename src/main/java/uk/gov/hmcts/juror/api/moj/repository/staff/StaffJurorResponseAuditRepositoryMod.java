package uk.gov.hmcts.juror.api.moj.repository.staff;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.StaffJurorResponseAuditMod;

public interface StaffJurorResponseAuditRepositoryMod extends CrudRepository<StaffJurorResponseAuditMod,
    StaffJurorResponseAuditKeyMod>,
    QuerydslPredicateExecutor<StaffJurorResponseAuditMod> {
}
