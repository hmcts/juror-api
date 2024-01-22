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
 * Entity representing auto processed response counts.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Immutable
@Table(name = "STATS_AUTO_PROCESSED", schema = "JUROR_DIGITAL")
@Builder
public class StatsAutoProcessed implements Serializable {

    @Id
    @NotNull
    @Column(name = "PROCESSED_DATE")
    private Date processedDate;

    @Column(name = "PROCESSED_COUNT")
    private Integer processedCount;

}
