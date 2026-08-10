package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Read-only mapping for juror_dashboard.dbd_response_stats (JS-1050).
 *
 * summons_date/response_date are `date` columns (not timestamp) per the
 * current DDL, mapped as LocalDate accordingly.
 *
 * Uses the surrogate `id` column as a simple @Id rather than a composite
 * key over the six business columns - a composite id including the
 * nullable response_date column previously corrupted other field values
 * on affected rows (loc_code coming back null), which is why the DDL
 * moved to a surrogate key + separate unique constraint.
 *
 * Populated entirely by a scheduled stored procedure; never written to
 * by the application.
 */
@Entity
@Immutable
@Table(name = "dbd_response_stats", schema = "juror_dashboard")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbdResponseStats implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "summons_date")
    private LocalDate summonsDate;

    @Column(name = "response_date")
    private LocalDate responseDate;

    @Column(name = "response_period")
    private String responsePeriod;

    @Column(name = "loc_code")
    private String locCode;

    @Column(name = "response_method")
    private String responseMethod;

    @Column(name = "age_group")
    private String ageGroup;

    @Column(name = "juror_count")
    private Integer jurorCount;
}
