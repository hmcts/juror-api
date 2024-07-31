package uk.gov.hmcts.juror.api.moj.domain.letter.court;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

@Getter
@SuperBuilder
@Immutable
@NoArgsConstructor
public class DeferralGrantedLetterList extends LetterListBase {

    @Column(name = "def_date")
    private LocalDate deferralDate;

    @Column(name = "deferral_reason")
    private String deferralReason;

}
