package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

/**
 * Entity representing the view of juror response audits in the juror_digital.juror_response_aud table.
 */
@Entity
@IdClass(JurorResponseAuditKey.class)
@Table(name = "JUROR_RESPONSE_AUD", schema = "JUROR_DIGITAL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Deprecated(forRemoval = true)
//TODO: low: implement a sequence PK
public class JurorResponseAudit {
    @Id
    @Column(name = "JUROR_NUMBER")
    @Pattern.List({
        @Pattern(regexp = JUROR_NUMBER),
        @Pattern(regexp = NO_PIPES_REGEX)
    })
    @Length(max = 9)
    private String jurorNumber;

    /**
     * Date this event happened.
     */
    @Id
    @Column(name = "CHANGED")
    private Date changed;

    /**
     * The bureau user who initiated this change.
     */
    @Column(name = "LOGIN")
    @Length(max = 20)
    private String login;

    @Column(name = "OLD_PROCESSING_STATUS")
    @Enumerated(EnumType.STRING)
    private ProcessingStatus oldProcessingStatus;

    @Column(name = "NEW_PROCESSING_STATUS")
    @Enumerated(EnumType.STRING)
    private ProcessingStatus newProcessingStatus;

    /**
     * Runs before saving to the database.
     */
    @PrePersist
    void prePersist() {
        this.changed = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
    }
}
