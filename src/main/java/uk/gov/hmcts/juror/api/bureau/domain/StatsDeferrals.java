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
 * Entity representing data for juror_mod.stats_deferrals.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@IdClass(StatsDeferralsKey.class)
@Table(name = "stats_deferrals", schema = "juror_dashboard")
@Builder
public class StatsDeferrals implements Serializable {

    @Id
    @NotNull
    @Column(name = "bureau_or_court")
    private String bureauOrCourt;


    @Id
    @NotNull
    @Column(name = "exc_code")
    private String execCode;


    @Id
    @NotNull
    @Column(name = "calendar_year")
    private String calendarYear;


    @Id
    @NotNull
    @Column(name = "financial_year")
    private String financialYear;

    @Id
    @NotNull
    @Column(name = "week")
    private String week;

    @NotNull
    @Column(name = "deferral_count")
    private Integer excusalCount;


}
