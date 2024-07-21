package uk.gov.hmcts.juror.api.moj.domain.letter.court;


import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
public class CertificateOfAttendanceLetterList extends LetterListBase {

    LocalDate startDate;

    LocalDate completionDate;

}
