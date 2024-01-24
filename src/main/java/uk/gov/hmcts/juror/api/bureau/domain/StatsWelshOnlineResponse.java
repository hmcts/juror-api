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
@Table(name = "STATS_WELSH_ONLINE_RESPONSES", schema = "JUROR_DIGITAL")
@Builder
public class StatsWelshOnlineResponse implements Serializable {

    @NotNull
    @Id
    @Column(name = "SUMMONS_MONTH")
    private Date summonsMonth;

    @Column(name = "WELSH_RESPONSE_COUNT")
    private Integer welshResponseCount;

}
