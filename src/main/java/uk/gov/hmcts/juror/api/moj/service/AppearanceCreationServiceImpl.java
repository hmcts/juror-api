package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AppearanceCreationServiceImpl implements AppearanceCreationService {
    private final AppearanceRepository appearanceRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Appearance createAppearance(String jurorNumber, LocalDate appearanceDate, CourtLocation courtLocation,
                                       String poolNumber, boolean appearanceConfirmed) {
        return addStandardAttributes(Appearance.builder()
                .jurorNumber(jurorNumber)
                .attendanceDate(appearanceDate)
                .courtLocation(courtLocation)
                .poolNumber(poolNumber)
                .appearanceConfirmed(appearanceConfirmed),
            jurorNumber, appearanceDate, courtLocation)
            .build();
    }

    @Override
    @Transactional
    public Appearance createAbsentAppearance(String jurorNumber, LocalDate attendanceDate, CourtLocation courtLocation,
                                             String poolNumber, boolean appearanceConfirmed) {
        return addStandardAttributes(Appearance.builder()
                .jurorNumber(jurorNumber)
                .poolNumber(poolNumber)
                .attendanceDate(attendanceDate)
                .courtLocation(courtLocation)
                .noShow(Boolean.TRUE)
                .attendanceType(AttendanceType.ABSENT)
                .appearanceConfirmed(appearanceConfirmed),
            jurorNumber, attendanceDate, courtLocation)
            .build();
    }


    @Override
    @Transactional
    public Appearance createNoneAttendanceAppearance(String jurorNumber, LocalDate nonAttendanceDate,
                                                     CourtLocation courtLocation, String poolNumber,
                                                     boolean appearanceConfirmed) {
        return
            addStandardAttributes(
                Appearance.builder()
                    .jurorNumber(jurorNumber)
                    .attendanceDate(nonAttendanceDate)
                    .courtLocation(courtLocation)
                    .poolNumber(poolNumber)
                    .nonAttendanceDay(Boolean.TRUE)
                    .attendanceType(AttendanceType.NON_ATTENDANCE)
                    .appearanceStage(AppearanceStage.EXPENSE_ENTERED)
                    .isDraftExpense(true)
                    .appearanceConfirmed(appearanceConfirmed),
                jurorNumber, nonAttendanceDate, courtLocation)
                .build();
    }

    //Public for test use should only be used internally
    public Appearance.AppearanceBuilder addStandardAttributes(Appearance.AppearanceBuilder appearanceBuilder,
                                                       String jurorNumber, LocalDate appearanceDate,
                                                       CourtLocation courtLocation) {
        return appearanceBuilder
            .createdBy(SecurityUtil.getActiveLogin())
            //TODO .createdBy(userService.findUserByUsername(SecurityUtil.getActiveLogin()))
            .version(getLastVersionNumber(jurorNumber, appearanceDate, courtLocation.getLocCode()));
    }

    //Public for test use should only be used internally
    public Long getLastVersionNumber(String jurorNumber, LocalDate date, String locCode) {
        Long lastVersion = appearanceRepository.getLastVersionNumber(jurorNumber, date, locCode);
        return lastVersion == null ? null : lastVersion + 1;
    }
}
