package uk.gov.hmcts.juror.api.moj.domain.letter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "REQUEST_LETT", schema = "JUROR_DIGITAL_USER")
@Getter
@Setter
public class RequestLetter extends Letter {
    @Column(name = "REQ_INFO")
    @Length(max = 210)
    private String requiredInformation;
}