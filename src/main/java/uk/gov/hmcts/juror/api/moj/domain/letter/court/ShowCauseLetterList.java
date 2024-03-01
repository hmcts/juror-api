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
@Table(name = "show_cause", schema = "juror_mod")
public class ShowCauseLetterList extends LetterListBase {
    @Column(name = "attendance_date") // this is the date the juror should have attended for jury service but was absent
    private LocalDate absentDate;
}