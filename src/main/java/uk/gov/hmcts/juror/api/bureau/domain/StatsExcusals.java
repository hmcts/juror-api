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

/**
 * Entity representing data for JUROR_DIGITAL.STATS_EXCUSALS.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(StatsExcusalsKey.class)
@Immutable
@Table(name = "stats_excusals", schema = "juror_mod")
@Builder

public class StatsExcusals implements Serializable {
    @NotNull
    @Id
    @Column(name = "bureau_or_court")
    private String bureauOrCourt;

    @NotNull
    @Id
    @Column(name = "exec_code")
    private String execCode;

    @NotNull
    @Id
    @Column(name = "calendar_year")
    private String calendarYear;

    @NotNull
    @Id
    @Column(name = "financial_year")
    private String financialYear;

    @NotNull
    @Id
    @Column(name = "week")
    private String week;

    @NotNull
    @Column(name = "excusal_count")
    private Integer excusalCount;


}
