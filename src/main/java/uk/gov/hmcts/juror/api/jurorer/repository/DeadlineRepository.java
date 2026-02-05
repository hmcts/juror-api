package uk.gov.hmcts.juror.api.jurorer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;

import java.util.Optional;

/**
 * Repository for Deadline entity.
 *
 * Note: The deadline table contains only one row (id = 1) representing
 * the system-wide deadline for file uploads.
 */
@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, Short> {

    /**
     * Get the current system deadline.
     *
     * Since table has only one row (id = 1), this is a convenience method
     * equivalent to findById((short) 1).
     *
     * @return Optional containing the deadline if exists
     */
    default Optional<Deadline> getCurrentDeadline() {
        return findById((short)1);
    }
}
