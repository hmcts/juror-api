package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entity representing data responses over a period of time by paper or digital.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(StatsResponseTimeKey.class)
@Immutable
@Table(name = "stats_response_times", schema = "juror_dashboard")
@Builder
public class StatsResponseTime implements Serializable {

    @NotNull
    @Id
    @Column(name = "summons_month")
    private LocalDate summonsMonth;

    @NotNull
    @Id
    @Column(name = "response_month")
    private LocalDate responseMonth;

    @NotNull
    @Id
    @Column(name = "response_period")
    private String responsePeriod;

    @NotNull
    @Id
    @Column(name = "loc_code")
    private String locCode;

    @NotNull
    @Id
    @Column(name = "response_method")
    private String responseMethod;

    @Column(name = "response_count")
    private Integer responseCount;

}
