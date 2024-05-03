package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.request.AutoAssignRequest;
import uk.gov.hmcts.juror.api.bureau.controller.response.AutoAssignResponse;
import uk.gov.hmcts.juror.api.bureau.domain.UserQueries;
import uk.gov.hmcts.juror.api.bureau.exception.AutoAssignException;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.UserJurorResponseAudit;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.UserJurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.juror.api.bureau.service.AutoAssignmentServiceImpl.DEFAULT_CAPACITY_FALLBACK;

/**
 * Unit tests for {@link AutoAssignmentServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.LawOfDemeter")
public class AutoAssignmentServiceImplTest {

    private static final Comparator<DigitalResponse> ascendingDateOrder =
        Comparator.comparing(DigitalResponse::getDateReceived);
    private static final long FIRST_STAFF_MEMBER_ASSIGNED_INCOMPLETES = 5;
    private static final long SECOND_STAFF_MEMBER_ASSIGNED_INCOMPLETES = 7;
    private static final long FIRST_STAFF_MEMBER_URGENTS = 8;
    private static final long SECOND_STAFF_MEMBER_URGENTS = 10;

    @Mock
    private JurorDigitalResponseRepositoryMod responseRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AppSettingService appSettingService;

    @Mock
    private UserJurorResponseAuditRepository auditRepo;

    private AutoAssignmentServiceImpl autoAssignmentService;

    private User user1;
    private User user2;
    private User user3;

    private List<DigitalResponse> backlog;

    @Before
    public void setUp() {
        autoAssignmentService = new AutoAssignmentServiceImpl(responseRepo, userRepo, appSettingService, auditRepo);
        user1 = User.builder().userType(UserType.BUREAU).name("Post Staff 1").username("staff1").active(true).build();
        user2 = User.builder().userType(UserType.BUREAU).name("Post Staff 2").username("staff2").active(true).build();
        user3 = User.builder().userType(UserType.BUREAU).name("Post Staff 3").username("staff3").active(true).build();

        doReturn(Arrays.asList(user1, user2)).when(userRepo).findAll(UserQueries.activeBureauOfficers());
        doReturn(user1).when(userRepo).findByUsername("staff1");
        doReturn(user2).when(userRepo).findByUsername("staff2");
        doReturn(user3).when(userRepo).findByUsername("staff3");

        //doReturn(Arrays.asList(user1, user2, user3)).when(userRepo).findAll(StaffQueries.activeBureauOfficers());
        doReturn(Arrays.asList(user1, user2, user3)).when(userRepo)
            .findAllByUsernameIn(
                Stream.of(user1, user2, user3).map(User::getUsername).toList());

        doReturn(FIRST_STAFF_MEMBER_ASSIGNED_INCOMPLETES).when(responseRepo)
            .count(JurorResponseQueries.assignedIncompletes(user1));
        doReturn(SECOND_STAFF_MEMBER_ASSIGNED_INCOMPLETES).when(responseRepo)
            .count(JurorResponseQueries.assignedIncompletes(user2));

        doReturn(FIRST_STAFF_MEMBER_URGENTS).when(responseRepo).count(JurorResponseQueries.assignedUrgents(user1));
        doReturn(SECOND_STAFF_MEMBER_URGENTS).when(responseRepo).count(JurorResponseQueries.assignedUrgents(user2));

        final LocalDateTime now = LocalDateTime.now();
        backlog = new LinkedList<>();
        for (int i = 0; i < 180; i++) {
            DigitalResponse response = new DigitalResponse();
            response.setJurorNumber(String.valueOf(i));
            response.setDateReceived(now.minusHours(i));
            backlog.add(response);
        }

        // This is the order the repository will return entries in
        backlog.sort(ascendingDateOrder);

        // If the service class doesn't use the correct parameters (for whatever reason) the tests will fall over
        // because the mock repo will return nothing
        doReturn(backlog).when(responseRepo).findAll(JurorResponseQueries.byUnassignedTodoNonUrgent(),
            JurorResponseQueries.oldestFirst());
        doReturn((long) backlog.size()).when(responseRepo).count(JurorResponseQueries.byUnassignedTodoNonUrgent());
    }

    /**
     * Tests that the auto-assign works correctly when the capacity is equal to the size of the backlog.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @SuppressWarnings("unchecked")
    public void autoAssign_happyPath() throws Exception {
        autoAssignmentService.autoAssign(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff1").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff2").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff3").build()
            )).build(), "testUser");
        final LocalDateTime currentTime = LocalDateTime.now();

        // Difference will likely be < 1s, but allowing a margin of error for running on a _really_ slow
        // machine/build slave
        final LocalDate minusFiveSeconds =
            LocalDate.from(currentTime.minusSeconds(5));
        final LocalDate plusFiveSeconds =
            LocalDate.from(currentTime.plusSeconds(5));

        for (DigitalResponse dummyResponse : backlog) {
            assertThat(dummyResponse.getStaff())
                .describedAs("Every backlog item should be assigned to a bureau officer")
                .isNotNull();

            assertThat(dummyResponse.getStaffAssignmentDate())
                .describedAs("Assignment date should be set to the time the auto-assign ran at")
                .isNotNull()
                .isBetween(minusFiveSeconds, plusFiveSeconds);
        }

        assertThat(backlog.parallelStream().filter(r -> r.getStaff().equals(user1)).count()).isEqualTo(60);
        assertThat(backlog.parallelStream().filter(r -> r.getStaff().equals(user2)).count()).isEqualTo(60);
        assertThat(backlog.parallelStream().filter(r -> r.getStaff().equals(user3)).count()).isEqualTo(60);

        ArgumentCaptor<UserJurorResponseAudit> auditCaptor =
            ArgumentCaptor.forClass(UserJurorResponseAudit.class);
        verify(responseRepo, times(1)).saveAll(backlog);
        verify(auditRepo).saveAll(auditCaptor.capture());
        List<UserJurorResponseAudit> auditEntries = auditCaptor.getAllValues();

        for (DigitalResponse backlogItem : backlog) {

            List<UserJurorResponseAudit> itemAudit = auditEntries.parallelStream()
                .filter(audit -> audit.getJurorNumber().equals(backlogItem.getJurorNumber()))
                .collect(Collectors.toList());
            assertThat(itemAudit).hasSize(1);

            UserJurorResponseAudit audit = itemAudit.get(0);
            assertThat(audit.getAssignedBy().getUsername()).isEqualTo("testUser");
            assertThat(audit.getAssignedTo().getUsername()).isEqualTo(backlogItem.getStaff().getUsername());
        }
    }

    /**
     * Tests that auto-assign works correctly when staff members have different capacities.
     *
     * @throws Exception if the test falls over
     */
    @Test
    public void autoAssign_alternatePath_varyingCapacities() throws Exception {
        autoAssignmentService.autoAssign(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(50).login("staff1").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff2").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(70).login("staff3").build()
            )).build(), "testUser");

        assertThat(backlog.parallelStream().filter(r -> r.getStaff().equals(user1)).count()).isEqualTo(50);
        assertThat(backlog.parallelStream().filter(r -> r.getStaff().equals(user2)).count()).isEqualTo(60);
        assertThat(backlog.parallelStream().filter(r -> r.getStaff().equals(user3)).count()).isEqualTo(70);
    }

    /**
     * Tests that an AutoAssignException is thrown if we attempt to specify two different capacities for the same
     * staff member.
     *
     * @throws Exception if the test falls over
     */
    @Test(expected = AutoAssignException.DuplicateCapacityValues.class)
    public void autoAssign_errorPath_duplicateCapacityValues() throws Exception {
        autoAssignmentService.autoAssign(AutoAssignRequest.builder()
            .data(Arrays.asList(AutoAssignRequest.StaffCapacity.builder().capacity(60).login("jpowers").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(30).login("jpowers").build())).build(), "testUser");
    }

    /**
     * Tests that an AutoAssignException is thrown if not all of the logins are valid.
     *
     * @throws Exception if the test falls over
     */
    @Test(expected = AutoAssignException.MissingLogins.class)
    public void autoAssign_errorPath_invalidLogin() throws Exception {
        autoAssignmentService.autoAssign(AutoAssignRequest.builder()
            .data(Collections.singletonList(
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff4").build()
            )).build(), "testUser");
    }

    /**
     * Tests that an AutoAssignException is thrown if a capacity is given for an inactive staff member.
     *
     * @throws Exception if the test falls over
     */
    @Test(expected = AutoAssignException.IneligibleStaff.class)
    public void autoAssign_errorPath_inactiveStaffMember() throws Exception {
        user1.setActive(false);
        autoAssignmentService.autoAssign(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff1").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff2").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff3").build()
            )).build(), "testUser");
    }

    /**
     * Tests that an AutoAssignException is thrown if a capacity is given for a team leader.
     *
     * @throws Exception if the test falls over
     */
    @Test(expected = AutoAssignException.IneligibleStaff.class)
    public void autoAssign_errorPath_teamLeader() throws Exception {
        user3.setUserType(UserType.BUREAU);
        user3.addRole(Role.MANAGER);
        autoAssignmentService.autoAssign(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff1").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff2").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("staff3").build()
            )).build(), "testUser");
    }

    /**
     * Tests that the service assigns the oldest responses first if the capacity is less than the backlog.
     *
     * @throws Exception if the test falls over
     */
    @Test
    public void autoAssign_alternatePath_insufficientCapacity() throws Exception {
        autoAssignmentService.autoAssign(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(10).login("staff1").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(10).login("staff2").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(10).login("staff3").build()
            )).build(), "testUser");
        final List<DigitalResponse> assigned = backlog.stream()
            .filter(r -> r.getStaff() != null).collect(Collectors.toList());
        final List<DigitalResponse> unassigned = backlog.stream()
            .filter(r -> r.getStaff() == null).collect(Collectors.toList());

        assertThat(assigned).isSortedAccordingTo(ascendingDateOrder);

        // Should be assigned in simple order (not round-robin)
        for (int i = 0; i < 10; i++) {
            assertThat(assigned.get(i).getStaff()).isEqualTo(user1);
        }
        for (int i = 10; i < 20; i++) {
            assertThat(assigned.get(i).getStaff()).isEqualTo(user2);
        }
        for (int i = 20; i < 30; i++) {
            assertThat(assigned.get(i).getStaff()).isEqualTo(user3);
        }

        // Every assigned response should be older than every unassigned response
        for (DigitalResponse assignedResponse : assigned) {
            assertThat(unassigned)
                .describedAs("Unassigned responses should all be after assigned response date "
                    + assignedResponse.getDateReceived())
                .allMatch(r -> r.getDateReceived().isAfter(assignedResponse.getDateReceived()));
        }
    }


    /**
     * When the capacity value is set in the database, that value should be used.
     */
    @Test
    public void getAutoAssignmentData_defaultCapacitySetInDatabase() {
        final int expectedCapacity = 50;

        doReturn(expectedCapacity).when(appSettingService).getDefaultCapacity();
        final AutoAssignResponse autoAssignmentData = autoAssignmentService.getAutoAssignmentData();

        assertThat(autoAssignmentData.getData()).isNotNull().hasSize(2).extracting("login").containsOnly("staff1",
            "staff2");

        for (AutoAssignResponse.StaffCapacityResponse staffCapacity : autoAssignmentData.getData()) {
            assertThat(staffCapacity.getCapacity()).isEqualTo(expectedCapacity);

            final boolean isFirstStaffMember = "staff1".equals(staffCapacity.getLogin());

            final long expectedUrgents = isFirstStaffMember
                ?
                FIRST_STAFF_MEMBER_URGENTS
                :
                    SECOND_STAFF_MEMBER_URGENTS;
            final long expectedIncompletes = isFirstStaffMember
                ?
                FIRST_STAFF_MEMBER_ASSIGNED_INCOMPLETES
                :
                    SECOND_STAFF_MEMBER_ASSIGNED_INCOMPLETES;

            assertThat(staffCapacity.getUrgents())
                .isEqualTo(expectedUrgents);

            assertThat(staffCapacity.getIncompletes())
                .isEqualTo(expectedIncompletes);

            assertThat(staffCapacity.getName())
                .isEqualTo(isFirstStaffMember
                    ?
                    user1.getName()
                    :
                        user2.getName());

            assertThat(staffCapacity.getAllocation())
                .isEqualTo(expectedUrgents + expectedCapacity);
        }

        assertThat(autoAssignmentData.getMeta()).isNotNull();
        assertThat(autoAssignmentData.getMeta().getBacklogSize()).isEqualTo(180L);
    }

    /**
     * If the setting is not set in the database, the fallback default of 60 should be used.
     */
    @Test
    public void getAutoAssignmentData_alternatePath_defaultCapacityNotSetInDatabase() {
        doReturn(null).when(appSettingService).getDefaultCapacity();

        assertThat(autoAssignmentService.getAutoAssignmentData().getData()).isNotNull().hasSize(2)
            .extracting("capacity").containsOnly(DEFAULT_CAPACITY_FALLBACK);
    }

    /**
     * If the setting in the database is invalid, the fallback default of 60 should be used.
     */
    @Test
    public void getAutoAssignmentData_errorPath_invalidDefaultCapacitySetInDatabase() {
        doReturn(-5).when(appSettingService).getDefaultCapacity();

        assertThat(autoAssignmentService.getAutoAssignmentData().getData()).isNotNull().hasSize(2)
            .extracting("capacity").containsOnly(DEFAULT_CAPACITY_FALLBACK);
    }
}