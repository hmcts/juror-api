package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "juror_trial", schema = "juror_mod")
@IdClass(JurorPoolId.class)
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
@Slf4j
public class JurorTrial implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "juror_number", nullable = false)
    private Juror juror;

    @Id
    @ManyToOne
    @JoinColumn(name = "pool_number", nullable = false)
    private PoolRequest pool;

    @NotNull
    @Column(name = "loc_code")
    @Length(max = 3)
    private String locCode;

    @Column(name = "trial_number")
    @Length(max = 16)
    private String trialNumber;

    @Column(name = "rand_number")
    private Integer randNumber;

    @Column(name = "date_selected")
    private LocalDate dateSelected;

    @Column(name = "completed")
    private Boolean completed;

    @Column(name = "result")
    @Length(max = 2)
    private String result;

    /**
     * Get the unique juror number from the associated juror record.
     *
     * @return 9-digit string to uniquely identify the associated juror
     */
    public String getJurorNumber() {
        return this.getJuror().getJurorNumber();
    }

    /**
     * Get the pool number from the associated pool record.
     *
     * @return 9-digit string to uniquely identify the associated pool
     */
    public String getPoolNumber() {
        return this.getPool().getPoolNumber();
    }

    /**
     * Retrieves the return date from the associated pool.
     *
     * @return LocalDate corresponding to return date.
     */
    public LocalDate getReturnDate() {
        return this.getPool().getReturnDate();
    }

    /**
     * Get the court location object where the associated pool was summoned.
     *
     * @return Court Location object related to the associated Pool
     */
    public CourtLocation getCourt() {
        return this.getPool().getCourtLocation();
    }

}
