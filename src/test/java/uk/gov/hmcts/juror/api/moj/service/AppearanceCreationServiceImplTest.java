package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AppearanceCreationServiceImplTest {
    private static final String COURT_USER = "COURT_USER";
    private static final String JUROR_NUMBER = "041500081";
    private static final LocalDate ATTENDANCE_DATE = LocalDate.of(2026, 4, 30);
    private static final String POOL_NUMBER = "415240101";

    @Mock
    private AppearanceRepository appearanceRepository;

    @Mock
    private UserService userService;

    private AppearanceCreationServiceImpl appearanceCreationService;

    @BeforeEach
    void beforeEach() {
        TestUtils.setUpMockAuthentication("415", COURT_USER, "1", List.of("415"));
        appearanceCreationService = new AppearanceCreationServiceImpl();
    }

    @Test
    void createAppearanceLeavesVersionUnsetForHibernatePersist() {
        Appearance appearance = appearanceCreationService.createAppearance(
            JUROR_NUMBER, ATTENDANCE_DATE, courtLocation(), POOL_NUMBER, false);

        assertThat(appearance.getVersion()).isNull();
        assertThat(appearance.getCreatedBy()).isEqualTo(COURT_USER);
        assertThat(appearance.isAppearanceConfirmed()).isFalse();
        verifyNoInteractions(appearanceRepository);
    }

    @Test
    void createAbsentAppearanceLeavesVersionUnsetForHibernatePersist() {
        Appearance appearance = appearanceCreationService.createAbsentAppearance(
            JUROR_NUMBER, ATTENDANCE_DATE, courtLocation(), POOL_NUMBER, true);

        assertThat(appearance.getVersion()).isNull();
        assertThat(appearance.getNoShow()).isTrue();
        assertThat(appearance.getAttendanceType()).isEqualTo(AttendanceType.ABSENT);
        assertThat(appearance.isAppearanceConfirmed()).isTrue();
        verifyNoInteractions(appearanceRepository);
    }

    @Test
    void createNoneAttendanceAppearanceLeavesVersionUnsetForHibernatePersist() {
        Appearance appearance = appearanceCreationService.createNoneAttendanceAppearance(
            JUROR_NUMBER, ATTENDANCE_DATE, courtLocation(), POOL_NUMBER, false);

        assertThat(appearance.getVersion()).isNull();
        assertThat(appearance.getNonAttendanceDay()).isTrue();
        assertThat(appearance.getAttendanceType()).isEqualTo(AttendanceType.NON_ATTENDANCE);
        assertThat(appearance.getAppearanceStage()).isEqualTo(AppearanceStage.EXPENSE_ENTERED);
        verifyNoInteractions(appearanceRepository);
    }

    private CourtLocation courtLocation() {
        return CourtLocation.builder()
            .locCode("415")
            .build();
    }
}
