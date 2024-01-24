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
 * Entity representing statistical data for third party online response counts.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Immutable
@Table(name = "STATS_THIRDPARTY_ONLINE", schema = "JUROR_DIGITAL")
@Builder
public class StatsThirdPartyOnlineResponse implements Serializable {

    @NotNull
    @Id
    @Column(name = "SUMMONS_MONTH")
    private Date summonsMonth;

    @Column(name = "THIRDPARTY_RESPONSE_COUNT")
    private Integer thirdPartyResponseCount;

}
