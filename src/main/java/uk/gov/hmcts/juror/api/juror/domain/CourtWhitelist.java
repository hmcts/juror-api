package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

/**
 * Whitelist entity.  Court locations that are enabled for juror responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@Table(name = "COURT_WHITELIST", schema = "JUROR_DIGITAL")
public class CourtWhitelist implements Serializable {
    @Id
    @Column(name = "LOC_CODE")
    private String locCode;
}
