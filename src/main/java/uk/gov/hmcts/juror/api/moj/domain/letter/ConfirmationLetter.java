package uk.gov.hmcts.juror.api.moj.domain.letter;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "CONFIRM_LETT", schema = "JUROR_DIGITAL_USER")
public class ConfirmationLetter extends Letter {

}
