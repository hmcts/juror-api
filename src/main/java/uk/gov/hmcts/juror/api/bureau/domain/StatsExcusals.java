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
@Table(name = "STATS_EXCUSALS", schema = "JUROR_DIGITAL")
@Builder

public class StatsExcusals implements Serializable {
    @NotNull
    @Id
    @Column(name = "BUREAU_OR_COURT")
    private String bureauOrCourt;

    @NotNull
    @Id
    @Column(name = "EXEC_CODE")
    private String execCode;

    @NotNull
    @Id
    @Column(name = "CALENDAR_YEAR")
    private String calendarYear;

    @NotNull
    @Id
    @Column(name = "FINANCIAL_YEAR")
    private String financialYear;

    @NotNull
    @Id
    @Column(name = "WEEK")
    private String week;

    @NotNull
    @Column(name = "EXCUSAL_COUNT")
    private Integer excusalCount;


}
