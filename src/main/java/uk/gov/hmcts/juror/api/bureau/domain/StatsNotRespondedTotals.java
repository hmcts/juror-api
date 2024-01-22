package uk.gov.hmcts.juror.api.bureau.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

/**
 * Entity VIEW representing STATS_NOT_RESPONDED_TOTALS.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "STATS_NOT_RESPONDED_TOTALS", schema = "JUROR_DIGITAL_USER")
@Immutable
@Builder
public class StatsNotRespondedTotals implements Serializable {

    @Id
    @Column(name = "NOT_RESPONSED_TOTAL")
    private Integer notRespondedTotals;

}
