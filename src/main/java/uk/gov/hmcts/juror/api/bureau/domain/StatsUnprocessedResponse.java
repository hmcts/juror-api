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

/**
 * Entity representing statistical data for unprocessed response counts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@Table(name = "stats_unprocessed_responses", schema = "juror_dashboard")
@Builder
public class StatsUnprocessedResponse implements Serializable {

    @NotNull
    @Id
    @Column(name = "loc_code")
    private String locCode;

    @Column(name = "unprocessed_count")
    private Integer unprocessedCount;

}
