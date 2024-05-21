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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisation_stats", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UtilisationStats.UtilisationStatsID.class)
@Getter
@Setter
public class UtilisationStats implements Serializable {

    @Id
    @Column(name = "month_start")
    @NotNull
    private LocalDate monthStart;

    @Id
    @NotNull
    @Column(name = "loc_code")
    private String locCode;

    @Column(name = "available_days")
    private int availableDays;

    @Column(name = "attendance_days")
    private int attendanceDays;

    @Column(name = "sitting_days")
    private int sittingDays;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class UtilisationStatsID implements Serializable {

        private LocalDate monthStart;
        private String locCode;

    }

}
