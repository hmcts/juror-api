package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Java entity for the JUROR_DIGITAL_USER.UNIQUE_POOL view
 *
 * @since JDB-2042
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Immutable
@Table(name = "UNIQUE_POOL", schema = "JUROR_DIGITAL_USER")
@Deprecated(forRemoval = true)
public class UniquePool {

    @Id
    @NotNull
    @Column(name = "POOL_NO")
    private String poolNumber;

    @Column(name = "ADDITIONAL_SUMMONS")
    private BigDecimal additionalSummons;

    @Column(name = "ATTEND_TIME")
    private String attendTime;

    @Column(name = "LAST_UPDATE")
    private Date lastUpdate;

    @Column(name = "LOC_CODE")
    private String locCode;

    @Column(name = "NEW_REQUEST")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean newRequest;

    @NotNull
    @Column(name = "NEXT_DATE")
    private Date nextDate;

    @Column(name = "RETURN_DATE")
    private Date returnDate;
}
