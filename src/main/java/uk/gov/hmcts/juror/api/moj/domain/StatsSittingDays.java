package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "stats_sitting_days", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StatsSittingDays.StatsSittingDaysId.class)
@Getter
@Setter
public class StatsSittingDays implements Serializable {

    @Id
    @NotNull
    @Column(name = "service_year")
    private String serviceYear;

    @Id
    @NotNull
    @Column(name = "service_month")
    private String serviceMonth;

    @Id
    @NotNull
    @Column(name = "court_code")
    private String courtCode;

    @Id
    @NotNull
    @Column(name = "sitting_days_category")
    private String sittingDaysCategory;

    @Column(name = "court_name")
    private String courtName;

    @Column(name = "number_of_sitting_days")
    private Integer numberOfSittingDays;

    @Column(name = "number_of_jurors")
    private Integer numberOfJurors;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class StatsSittingDaysId implements Serializable {
        private String serviceYear;
        private String serviceMonth;
        private String courtCode;
        private String sittingDaysCategory;
    }
}
