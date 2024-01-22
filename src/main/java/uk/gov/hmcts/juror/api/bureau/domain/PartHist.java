package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

/**
 * Audit history of changes to legacy POOL table rows.
 *
 * @implNote The all field @Id is required as the backing table does not express a primary key at all (JPA limitation).
 */
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@IdClass(PartHistKey.class)
@Entity
@Table(name = "PART_HIST", schema = "JUROR_DIGITAL_USER")
public class PartHist implements Serializable {
    private static final String SINGLE_SPACE_CHARACTER = " ";

    /**
     * Responded status string. Human readable in log.
     *
     * @see #info
     */
    public static final String RESPONDED = "Responded";
    public static final String ADDED = "Added to New Pool";

    @Id
    @Column(name = "PART_NO")
    @Pattern.List({
        @Pattern(regexp = JUROR_NUMBER),
        @Pattern(regexp = NO_PIPES_REGEX)
    })
    @Length(max = 9)
    @NotNull
    private String jurorNumber;

    @Id
    @Column(name = "OWNER")
    @NotNull
    @Length(max = 3)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String owner;

    @Id
    @Column(name = "LAST_UPDATE")
    @Builder.Default
    private Date lastUpdate = Date.from(Instant.now());

    @Id
    @Column(name = "DATE_PART")
    @NotNull
    private Date datePart;

    @Id
    @Column(name = "HISTORY_CODE")
    @NotNull
    @Length(max = 4)
    private String historyCode;

    @Id
    @Column(name = "USER_ID")
    @NotNull
    @Length(max = 20)
    private String userId;

    @Id
    @Column(name = "OTHER_INFORMATION")
    @Length(max = 27)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String info;

    @Id
    @Column(name = "POOL_NO")
    @Length(max = 9)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String poolNumber;

    @PrePersist
    @PreUpdate
    public void prePersist() {

        if (owner == null) {
            this.owner = JurorDigitalApplication.JUROR_OWNER;
            log.trace("Defaulted owner");
        }

        if (lastUpdate == null) {
            lastUpdate = Date.from(Instant.now());
            log.trace("Defaulted lastUpdate");
        }

        if (datePart == null) {
            datePart = Date.from(Instant.now());
            log.trace("Defaulted datePart");
        }

        if (info == null) {
            info = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted info");
        }

        if (poolNumber == null) {
            poolNumber = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted poolNumber");
        }

        if (info.length() > 27) {
            info = info.substring(0, 27);
            log.trace("Truncated info");
        }
    }
}
