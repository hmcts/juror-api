package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity representing data responses over a period of time by paper or digital.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(StatsResponseTimeKey.class)
@Immutable
@Table(name = "STATS_RESPONSE_TIMES", schema = "JUROR_DIGITAL")
@Builder
public class StatsResponseTime implements Serializable {

    @NotNull
    @Id
    @Column(name = "SUMMONS_MONTH")
    @Temporal(TemporalType.DATE)
    private Date summonsMonth;

    @NotNull
    @Id
    @Column(name = "RESPONSE_MONTH")
    @Temporal(TemporalType.DATE)
    private Date responseMonth;

    @NotNull
    @Id
    @Column(name = "RESPONSE_PERIOD")
    private String responsePeriod;

    @NotNull
    @Id
    @Column(name = "LOC_CODE")
    private String locCode;

    @NotNull
    @Id
    @Column(name = "RESPONSE_METHOD")
    private String responseMethod;

    @Column(name = "RESPONSE_COUNT")
    private Integer responseCount;

}
