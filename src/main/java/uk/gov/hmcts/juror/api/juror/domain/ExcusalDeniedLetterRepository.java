package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link ExcusalDeniedLetter}.
 */
@Repository
public interface ExcusalDeniedLetterRepository extends JpaRepository<ExcusalDeniedLetter, String> {
}
