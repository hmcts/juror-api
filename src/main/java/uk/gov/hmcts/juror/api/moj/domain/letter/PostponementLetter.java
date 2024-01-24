package uk.gov.hmcts.juror.api.moj.domain.letter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Table(name = "POSTPONE_LETTER", schema = "JUROR_DIGITAL_USER")
public class PostponementLetter extends Letter {

    @NotNull
    @Column(name = "DATE_POSTPONE")
    private LocalDate datePostpone;

}
