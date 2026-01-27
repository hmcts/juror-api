package uk.gov.hmcts.juror.api.jurorer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "deadline", schema = "juror_er")
public class Deadline {

    @Id
    @Column(name = "id", nullable = false)
    private Short id = 1;   // fixed single-row identifier

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Column(name = "updated_by", length = 30)
    private String updatedBy;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
}
