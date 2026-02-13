package uk.gov.hmcts.juror.api.jurorer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.jurorer.domain.ReminderHistory;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderHistoryRepository extends JpaRepository<ReminderHistory, Long> {

    /**
     * Find all reminder history records for a specific Local Authority.
     *
     * @param laCode the Local Authority code
     * @return list of reminder history records
     */
    List<ReminderHistory> findByLaCodeOrderByTimeSentDesc(String laCode);

    /**
     * Find reminder history records sent within a date range.
     *
     * @param startDate the start date/time
     * @param endDate the end date/time
     * @return list of reminder history records
     */
    List<ReminderHistory> findByTimeSentBetweenOrderByTimeSentDesc(LocalDateTime startDate, LocalDateTime endDate);
}
