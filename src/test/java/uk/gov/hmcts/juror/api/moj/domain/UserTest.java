package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("PMD.TooManyMethods")
class UserTest {
    @Test
    void givenBuilderWithDefaultValueThanDefaultValueIsPresent() {
        User user = User.builder().build();
        assertEquals(0, user.getLevel(), "Default level should be 0");
        assertEquals(Boolean.TRUE, user.isActive(), "Default active should be true");
    }

    @Test
    void positiveIsTeamLeaderTrue() {
        User user = new User();
        user.setRoles(Set.of(Role.TEAM_LEADER));
        assertThat(user.isTeamLeader()).isTrue();
    }

    @Test
    void positiveIsTeamLeaderFalse() {
        User user = new User();
        user.setRoles(Set.of(Role.MANAGER));
        assertThat(user.isTeamLeader()).isFalse();
    }

    @Test
    void positiveHasRoleTrue() {
        User user = new User();
        user.setRoles(Set.of(Role.MANAGER, Role.TEAM_LEADER));
        assertThat(user.hasRole(Role.TEAM_LEADER)).isTrue();
    }

    @Test
    void positiveHasRoleFalse() {
        User user = new User();
        user.setRoles(Set.of(Role.MANAGER, Role.TEAM_LEADER));
        assertThat(user.hasRole(Role.SENIOR_JUROR_OFFICER)).isFalse();
    }

    @Test
    void positiveGetLevelSjo() {
        User user = new User();
        user.setUserType(UserType.COURT);
        user.setRoles(Set.of(Role.SENIOR_JUROR_OFFICER));
        assertThat(user.getLevel()).isEqualTo(9);
    }

    @Test
    void positiveGetLevelBureauTeamLead() {
        User user = new User();
        user.setUserType(UserType.BUREAU);
        user.setRoles(Set.of(Role.TEAM_LEADER));
        assertThat(user.getLevel()).isEqualTo(1);
    }

    @Test
    void positiveGetLevelCourtUser() {
        User user = new User();
        user.setUserType(UserType.COURT);
        user.setRoles(Set.of());
        assertThat(user.getLevel()).isEqualTo(1);
    }

    @Test
    void positiveGetLevelBureauUser() {
        User user = new User();
        user.setUserType(UserType.BUREAU);
        user.setRoles(Set.of());
        assertThat(user.getLevel()).isEqualTo(0);
    }

    @Test
    void positiveGetRoles() {
        User user = new User();
        user.setRoles(Set.of(Role.MANAGER));
        assertThat(user.getRoles()).isEqualTo(Set.of(Role.MANAGER));
    }

    @Test
    void positiveGetRolesIsNull() {
        User user = new User();
        user.setRoles(null);
        assertThat(user.getRoles()).isEqualTo(Set.of());
    }

    @Test
    void positiveAddRole() {
        User user = new User();
        user.addRole(Role.TEAM_LEADER);
        assertThat(user.getRoles()).isEqualTo(Set.of(Role.TEAM_LEADER));
        user.addRole(Role.SENIOR_JUROR_OFFICER);
        assertThat(user.getRoles()).isEqualTo(Set.of(Role.TEAM_LEADER, Role.SENIOR_JUROR_OFFICER));
    }

    @Test
    void positiveGetCourts() {
        Set<CourtLocation> courtLocations = Set.of(CourtLocation.builder()
            .owner("415").build());
        User user = new User();
        user.setCourts(courtLocations);
        assertThat(user.getCourts()).isEqualTo(courtLocations);
    }

    @Test
    void positiveGetCourtsNull() {
        User user = new User();
        user.setCourts(null);
        assertThat(user.getCourts()).isEqualTo(Set.of());
    }


    @Test
    void positiveAddCourt() {
        User user = new User();
        CourtLocation courtLocation = CourtLocation.builder()
            .owner("415").build();
        user.addCourt(courtLocation);
        assertThat(user.getCourts()).isEqualTo(Set.of(courtLocation));
    }

    @Test
    void positiveAddCourtAlreadyHas() {
        User user = new User();
        CourtLocation courtLocation = CourtLocation.builder()
            .owner("415").build();
        Set<CourtLocation> courtLocations = new HashSet<>();
        courtLocations.add(courtLocation);
        user.setCourts(courtLocations);
        user.addCourt(courtLocation);
        assertThat(user.getCourts()).isEqualTo(Set.of(courtLocation));
    }

    @Test
    void positiveRemoveCourt() {
        User user = new User();
        CourtLocation courtLocation1 = CourtLocation.builder()
            .owner("415").build();
        CourtLocation courtLocation2 = CourtLocation.builder()
            .owner("400").build();
        Set<CourtLocation> courtLocations = new HashSet<>();
        courtLocations.add(courtLocation1);
        courtLocations.add(courtLocation2);
        user.setCourts(courtLocations);
        user.removeCourt(courtLocation1);
        assertThat(user.getCourts()).isEqualTo(Set.of(courtLocation2));
    }

    @Test
    void positiveRemoveCourtDoesNotHave() {
        User user = new User();
        CourtLocation courtLocation1 = CourtLocation.builder()
            .owner("415").build();
        CourtLocation courtLocation2 = CourtLocation.builder()
            .owner("400").build();
        Set<CourtLocation> courtLocations = new HashSet<>();
        courtLocations.add(courtLocation1);
        user.setCourts(courtLocations);
        user.removeCourt(courtLocation2);
        assertThat(user.getCourts()).isEqualTo(Set.of(courtLocation1));
    }


}