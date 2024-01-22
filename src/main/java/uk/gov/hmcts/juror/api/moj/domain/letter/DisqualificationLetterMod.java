package uk.gov.hmcts.juror.api.moj.domain.letter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Entity
@Setter
@Table(name = "DISQUALIFICATION_LETTER", schema = "JUROR_DIGITAL_USER")
public class DisqualificationLetterMod extends Letter {

    @NotNull
    @Length(min = 1, max = 1)
    @Column(name = "DISQ_CODE")
    private String disqCode;

    @NotNull
    @Column(name = "DATE_DISQ")
    private LocalDate dateDisq;

}
