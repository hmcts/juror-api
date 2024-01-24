package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;

import java.util.List;

@Repository
public interface ContactLogRepository extends JpaRepository<ContactLog, Long> {

    List<ContactLog> findByJurorNumber(String jurorNumber);

}
