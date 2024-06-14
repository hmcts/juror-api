package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "juror_pool", schema = "juror_mod")
@IdClass(JurorPoolId.class)
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
@Slf4j
public class JurorPool implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "juror_number", nullable = false)
    private Juror juror;

    @Id
    @ManyToOne
    @JoinColumn(name = "pool_number", nullable = false)
    private PoolRequest pool;

    @NotNull
    @Column(name = "owner")
    @Length(max = 3)
    private String owner;

    @Column(name = "user_edtq")
    @Length(max = 30)
    private String userEdtq;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "status")
    private JurorStatus status;

    @Column(name = "times_sel")
    private Integer timesSelected;

    @Column(name = "def_date")
    private LocalDate deferralDate;

    @Length(max = 6)
    @Column(name = "location")
    private String location;

    @Column(name = "no_attendances")
    private Integer noAttendances;

    @Column(name = "no_attended")
    private Integer noAttended;

    @Column(name = "no_fta")
    private Integer failedToAttendCount;

    @Column(name = "no_awol")
    private Integer unauthorisedAbsenceCount;

    @Length(max = 4)
    @Column(name = "pool_seq")
    private String poolSequence;

    @Column(name = "edit_tag")
    private Character editTag;

    @Column(name = "next_date")
    private LocalDate nextDate;

    @Column(name = "on_call")
    private Boolean onCall;

    @Length(max = 20)
    @Column(name = "smart_card")
    private String smartCard;

    @Column(name = "was_deferred")
    private Boolean wasDeferred;

    @Length(max = 2)
    @Column(name = "deferral_code")
    private String deferralCode;

    @Column(name = "id_checked")
    private Character idChecked;

    @Column(name = "postpone")
    private Boolean postpone;

    @Column(name = "paid_cash")
    private Boolean paidCash;

    @Length(max = 9)
    @Column(name = "scan_code")
    private String scanCode;

    @Column(name = "reminder_sent")
    private Boolean reminderSent;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "juror_number", referencedColumnName = "juror_number"),
        @JoinColumn(name = "pool_number", referencedColumnName = "pool_number")

    })
    private List<Appearance> appearances = new ArrayList<>();

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

    @PrePersist
    private void prePersist() {
        dateCreated = LocalDateTime.now();
        preUpdate();
    }

    @PreUpdate
    private void preUpdate() {
        lastUpdate = LocalDateTime.now();
    }

}
