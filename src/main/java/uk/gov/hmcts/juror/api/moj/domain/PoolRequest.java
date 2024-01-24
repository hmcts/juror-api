package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "pool", schema = "juror_mod")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class PoolRequest implements Serializable {

    private static final int DEFAULT_INTEGER = 0;
    private static final Character DEFAULT_NEW_REQUEST = 'Y';

    @Id
    @NotNull
    @Column(name = "pool_no")
    @Length(max = 9)
    private String poolNumber;

    /**
     * Composite primary key. -
     * owner in conjunction with poolNumber will identify unique records.
     * generally there will be two records per pool request,
     * one owned by the court and one owned by the central summonsing bureau.
     */
    @NotNull
    @Column(name = "owner")
    @Length(max = 3)
    private String owner;

    /**
     * Location code for the specific court the pool is being requested for.
     */
    @ManyToOne
    @JoinColumn(name = "loc_code", nullable = false)
    private CourtLocation courtLocation;

    /**
     * The date the jurors will be required to first attend court to start their service.
     */
    @NotNull
    @Column(name = "return_date")
    private LocalDate returnDate;

    /**
     * Total number of jurors requested for this pool.
     */
    @Column(name = "no_requested")
    private Integer numberRequested;

    @ManyToOne
    @JoinColumn(name = "pool_type")
    private PoolType poolType;

    @Column(name = "attend_time")
    private LocalDateTime attendTime;

    @Column(name = "new_request")
    private Character newRequest;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "additional_summons")

    private Integer additionalSummons;

    /**
     * Total number of jurors requested for this pool, before subtracting any deferrals used.
     */
    @Column(name = "total_no_required")
    private int totalNoRequired;

    @Column(name = "nil_pool")
    private boolean nilPool;

    @Column(name = "date_created", updatable = false)
    private LocalDateTime dateCreated;

    @OneToMany(mappedBy = "pool", cascade = CascadeType.REMOVE)
    private Set<PoolComment> poolComments;

    @PrePersist
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void prePersist() {
        if (newRequest == null) {
            newRequest = DEFAULT_NEW_REQUEST;
        }
        if (dateCreated == null) {
            dateCreated = LocalDateTime.now();
        }
        if (numberRequested != null && numberRequested > 0) {
            nilPool = false;
        }
        preUpdate();
    }

    @PreUpdate
    private void preUpdate() {
        lastUpdate = LocalDateTime.now();
    }

}
