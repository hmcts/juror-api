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
 * View of calculations over juror_pool data.
 */
@Entity
@Table(name = "pool_stats_with_pool_join", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Immutable
public class PoolStatisticsWithPoolJoin implements Serializable {

    @Id
    @NotNull
    @Column(name = "pool_number")
    @Length(max = 9)
    private String poolNumber;

    @Column(name = "return_date")
    private LocalDate returnDate;


    @Column(name = "owner")
    private String owner;

    @Column(name = "loc_code")
    private String locCode;

    @Column(name = "pool_type")
    private String poolType;

    @Column(name = "no_requested")
    private Integer numberRequested;

    @Column(name = "total_summoned")
    private int totalSummoned;

    @Column(name = "court_supply")
    private int courtSupply;

    @Column(name = "available")
    private int available;

    @Column(name = "unavailable")
    private int unavailable;

    @Column(name = "unresolved")
    private int unresolved;

}
