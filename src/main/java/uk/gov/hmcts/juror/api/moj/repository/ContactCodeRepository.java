package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.ContactCode;

@Repository
public interface ContactCodeRepository extends ReadOnlyRepository<ContactCode, String> {

}
