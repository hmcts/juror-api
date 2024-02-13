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
@Table(name = "court_deferral_denied", schema = "juror_mod")
public class DeferralDeniedLetterList extends LetterListBase {

    @Column(name = "refusal_date")
    private LocalDate refusalDate;

    @Column(name = "other_information")
    private String otherInformation;

}
