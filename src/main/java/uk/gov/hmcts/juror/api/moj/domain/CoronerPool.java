package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * View of the legacy juror CORONER_POOL table data.
 */
@Entity
@Table(name = "coroner_pool", schema = "juror_mod")
@NoArgsConstructor
@Getter
@Setter
public class CoronerPool implements Serializable {

    @Id
    @NotNull
    @Column(name = "cor_pool_no")
    @Length(max = 9)
    private String poolNumber;

    @NotNull
    @Column(name = "cor_name")
    @Length(max = 35)
    private String name;

    @Column(name = "email")
    @NotBlank
    @Length(max = 254)
    private String email;

    @Column(name = "phone")
    @Length(max = 15)
    private String phoneNumber;

    /**
     * Location code for the specific court the pool is being requested for.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cor_court_loc")
    private CourtLocation courtLocation;

    @NotNull
    @Column(name = "cor_request_dt")
    private LocalDate requestDate;

    @NotNull
    @Column(name = "cor_service_dt")
    private LocalDate serviceDate;

    /**
     * Total number of jurors requested for this pool.
     */
    @Column(name = "cor_no_requested")
    private Integer numberRequested;

}
