package uk.gov.hmcts.juror.api.moj.domain.letter.court;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder
@Immutable
@NoArgsConstructor
@Table(name = "court_excusal_refused", schema = "juror_mod")
public class ExcusalRefusedLetterList extends LetterListBase {
    @Column(name = "date_excused")
    private LocalDate dateExcused;

    @Column(name = "excusal_reason")
    private String reason;
}
