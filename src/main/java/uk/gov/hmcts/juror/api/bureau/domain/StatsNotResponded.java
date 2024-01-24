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
@Table(name = "STATS_NOT_RESPONDED", schema = "JUROR_DIGITAL")
@Builder
public class StatsNotResponded implements Serializable {

    @NotNull
    @Id
    @Column(name = "SUMMONS_MONTH")
    private Date summonsMonth;

    @NotNull
    @Id
    @Column(name = "LOC_CODE")
    private String locCode;

    @Column(name = "NON_RESPONSED_COUNT")
    private Integer nonResponseCount;

}
