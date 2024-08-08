package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;

import java.time.LocalDate;

public interface AppearanceCreationService {
    Appearance createAppearance(String jurorNumber, LocalDate appearanceDate, CourtLocation courtLocation,
                                String poolNumber, boolean appearanceConfirmed);

    Appearance createAbsentAppearance(String jurorNumber, LocalDate attendanceDate, CourtLocation courtLocation,
                                      String poolNumber, boolean appearanceConfirmed);


    Appearance createNoneAttendanceAppearance(String jurorNumber, LocalDate nonAttendanceDate,
                                              CourtLocation courtLocation, String poolNumber,
                                              boolean appearanceConfirmed);

}
