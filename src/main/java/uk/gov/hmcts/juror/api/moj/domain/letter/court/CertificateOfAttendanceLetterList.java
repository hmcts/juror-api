package uk.gov.hmcts.juror.api.moj.domain.letter.court;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@Table(name = "court_certificate_attendance", schema = "juror_mod")
public class CertificateOfAttendanceLetterList extends LetterListBase {

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "completion_date")
    LocalDate completionDate;

}
