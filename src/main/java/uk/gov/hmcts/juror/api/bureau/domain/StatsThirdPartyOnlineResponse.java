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
@Table(name = "stats_thirdparty_online", schema = "juror_dashboard")
@Builder
public class StatsThirdPartyOnlineResponse implements Serializable {

    @NotNull
    @Id
    @Column(name = "summons_month")
    private Date summonsMonth;

    @Column(name = "thirdparty_response_count")
    private Integer thirdPartyResponseCount;

}
