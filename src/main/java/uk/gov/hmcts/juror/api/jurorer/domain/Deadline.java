package uk.gov.hmcts.juror.api.jurorer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "deadline", schema = "juror_er")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deadline {

    @Id
    @Column(name = "id", nullable = false)
    private int id;   // fixed single-row identifier

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Column(name = "upload_start_date")
    private LocalDate uploadStartDate;

    @Column(name = "updated_by", length = 30)
    private String updatedBy;

    @Column(name = "last_updated")
    private LocalDate lastUpdated;

    /**
      * Calculate days remaining until deadline from current date.
            *
            * @return Days remaining (negative if overdue, null if no deadline set)
     */
    public Long getDaysRemaining() {
        if (deadlineDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), deadlineDate);
    }

    /**
     * Check if deadline has passed.
     *
     * @return true if current date is after deadline, false otherwise
     */
    public boolean isOverdue() {
        if (deadlineDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(deadlineDate);
    }
}

