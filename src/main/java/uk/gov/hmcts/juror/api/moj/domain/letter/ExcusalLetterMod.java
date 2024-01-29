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
@Table(name = "EXCUSAL_LETTER", schema = "JUROR_DIGITAL_USER")
public class ExcusalLetterMod extends Letter {

    @NotNull
    @Length(min = 1, max = 2)
    @Column(name = "EXC_CODE")
    private String excCode;

    @NotNull
    @Column(name = "DATE_EXCUSED")
    private LocalDate dateExcused;

}
