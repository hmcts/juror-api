package uk.gov.hmcts.juror.api.moj.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class AppearanceId implements Serializable {

    private String jurorNumber;

    private LocalDate attendanceDate;

    private CourtLocation courtLocation;

    public static AppearanceId of(Appearance appearance) {
        return new AppearanceId(appearance.getJurorNumber(),
            appearance.getAttendanceDate(),
            appearance.getCourtLocation());
    }
}
