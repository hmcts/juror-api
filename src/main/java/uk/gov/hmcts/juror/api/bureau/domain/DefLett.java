package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Entity representing deferral letter entry.
 */
@Entity
@Table(name = "DEF_LETT", schema = "JUROR_DIGITAL_USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefLett implements Serializable {
    @Id
    @Column(name = "PART_NO")
    private String partNo;

    @NotNull
    @Column(name = "OWNER")
    private String owner;

    @NotNull
    @Size(min = 1, max = 1)
    @Column(name = "EXC_CODE")
    private String excusalCode;

    @NotNull
    @Column(name = "DATE_DEF")
    private Date dateDef;

    @Size(max = 1)
    @Column(name = "PRINTED")
    private String printed;

    @Column(name = "DATE_PRINTED")
    private Date datePrinted;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        // Process dates to avoid timezone issues JDB-2993
        this.dateDef = adjustTimeOnDate(this.dateDef, false);
        this.datePrinted = adjustTimeOnDate(this.datePrinted, true);
    }

    private static Date adjustTimeOnDate(final Date date, final boolean isNullable) {
        if (isNullable && null == date) {
            return null;
        }
        return Date.from(ZonedDateTime.ofInstant(
            date.toInstant().plus(6, ChronoUnit.HOURS),
            ZoneId.systemDefault()
        ).with(LocalTime.of(6, 0)).toInstant());
    }
}
