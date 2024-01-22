package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryType;

@Repository
public interface ContactEnquiryTypeRepository extends ReadOnlyRepository<ContactEnquiryType, ContactEnquiryCode> {

}
