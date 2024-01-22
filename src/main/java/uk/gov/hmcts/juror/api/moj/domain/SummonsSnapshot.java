package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Summons Snapshot view created to derive a moment in time snapshot of the pool information relating to when
 * the juror was first summoned e.g. this will store the original Pool Request/Court Location information that a
 * juror was summoned to.
 */
@Entity
@Table(name = "summons_snapshot", schema = "juror_mod")
@Builder
@Getter
@Immutable
@AllArgsConstructor
@NoArgsConstructor
public class SummonsSnapshot implements Serializable {

    @Id
    @Column(name = "juror_number")
    @JurorNumber
    @NotNull
    private String jurorNumber;

    @Column(name = "pool_no")
    @Length(min = 9, max = 9)
    @NotNull
    private String poolNumber;

    @Column(name = "loc_code")
    @Length(min = 3, max = 3)
    @NotNull
    private String courtLocationCode;

    @Column(name = "location_name")
    @Length(max = 40)
    @NotNull
    private String courtLocationName;

    @Column(name = "court_name")
    @Length(max = 30)
    @NotNull
    private String courtName;

    @Column(name = "service_start_date")
    @NotNull
    private LocalDate serviceStartDate;

    @Column(name = "date_created")
    @NotNull
    private LocalDateTime dateCreated;

}
