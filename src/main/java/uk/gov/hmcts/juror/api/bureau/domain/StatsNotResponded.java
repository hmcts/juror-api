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
import java.util.Date;

/**
 * Entity representing data for non responded counts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(StatsNotRespondedKey.class)
@Immutable
@Table(name = "stats_not_responded", schema = "juror_mod")
@Builder
public class StatsNotResponded implements Serializable {

    @NotNull
    @Id
    @Column(name = "summons_month")
    private Date summonsMonth;

    @NotNull
    @Id
    @Column(name = "loc_code")
    private String locCode;

    @Column(name = "non_responded_count")
    private Integer nonResponseCount;

}
