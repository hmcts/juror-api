package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
 * Entity representing statistical data for welsh online response counts.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Immutable
@Table(name = "stats_welsh_online_responses", schema = "juror_dashboard")
@Builder
@Deprecated(forRemoval = true)
public class StatsWelshOnlineResponse implements Serializable {

    @NotNull
    @Id
    @Column(name = "summons_month")
    private Date summonsMonth;

    @Column(name = "welsh_response_count")
    private Integer welshResponseCount;

}
