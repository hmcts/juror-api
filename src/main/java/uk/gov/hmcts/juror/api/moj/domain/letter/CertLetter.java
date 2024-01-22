package uk.gov.hmcts.juror.api.moj.domain.letter;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Setter;

/**
 * Entity to store certificate of attendance letters to be printed as proof of attending a jury summons.
 */
@Entity
@Setter
@Table(name = "CERT_LETTER", schema = "JUROR_DIGITAL_USER")
public class CertLetter extends Letter {

}
