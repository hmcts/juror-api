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
import java.time.LocalDateTime;

/**
 * Read-only mapping for juror_dashboard.dbd_response_stats (JS-1050).
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
    private LocalDateTime summonsDate;

    @Column(name = "response_date")
    private LocalDateTime responseDate;

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
