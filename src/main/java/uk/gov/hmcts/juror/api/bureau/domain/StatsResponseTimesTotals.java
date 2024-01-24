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
 * Entity VIEW representing STATS_RESPONSE_TIMES_TOTAL.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "STATS_RESPONSE_TIMES_TOTALS", schema = "JUROR_DIGITAL_USER")
@Immutable
@Builder
public class StatsResponseTimesTotals implements Serializable {

    @Id
    @Column(name = "ONLINE_RESPONSES_TOTAL")
    private Integer onlineResponsesTotal;

    @Column(name = "ALL_RESPONSES_Total")
    private Integer allResponsesTotal;


}
