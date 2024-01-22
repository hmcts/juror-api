package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * View of active pools with the Bureau.
 */
@Entity
@Table(name = "active_pools_bureau", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Immutable
public class ActivePoolsBureau implements Serializable {

    @Id
    @NotNull
    @Column(name = "pool_no")
    @Length(max = 9)
    private String poolNumber;

    @Column(name = "jurors_requested")
    private int jurorsRequested;

    @Column(name = "confirmed_jurors")
    private int confirmedJurors;

    @Column(name = "court_name")
    private String courtName;

    @Column(name = "pool_type")
    private String poolType;

    @Column(name = "service_start_date")
    private LocalDate serviceStartDate;

}
