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
 * Entity representing data for JUROR_DIGITAL.STATS_DEFERRALS.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@IdClass(StatsDeferralsKey.class)
@Table(name = "STATS_DEFERRALS", schema = "JUROR_DIGITAL")
@Builder

public class StatsDeferrals implements Serializable {

    @Id
    @NotNull
    @Column(name = "BUREAU_OR_COURT")
    private String bureauOrCourt;


    @Id
    @NotNull
    @Column(name = "EXEC_CODE")
    private String execCode;


    @Id
    @NotNull
    @Column(name = "CALENDAR_YEAR")
    private String calendarYear;


    @Id
    @NotNull
    @Column(name = "FINANCIAL_YEAR")
    private String financialYear;


    @Id
    @NotNull
    @Column(name = "WEEK")
    private String week;

    @NotNull
    @Column(name = "EXCUSAL_COUNT")
    private Integer excusalCount;


}
