package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReplyType;

public interface ReplyTypeRepository extends JpaRepository<ReplyType, String> {
}