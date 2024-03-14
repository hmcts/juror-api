package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauBacklogAllocateRequestDto;
import uk.gov.hmcts.juror.api.bureau.exception.BureauBacklogAllocateException;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.StaffJurorResponseAuditRepositoryMod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link BureauBacklogAllocateServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BureauBacklogAllocateServiceImplTest {

    private static final Comparator<DigitalResponse> ascendingDateOrder =
        Comparator.comparing(DigitalResponse::getDateReceived);
    private static final Integer NON_URGENT_TO_ALLOCATE_TO_STAFF = 1;
    private static final Integer URGENT_TO_ALLOCATE_TO_STAFF = 2;
    private static final Integer SUPER_URGENT_TO_ALLOCATE_TO_STAFF = 3;

    @Mock
    private JurorDigitalResponseRepositoryMod responseRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private StaffJurorResponseAuditRepositoryMod auditRepo;

    private BureauBacklogAllocateServiceImpl bureauBacklogAllocateService;

    private User user1;
    private User user2;
    private User user3;

    private List<DigitalResponse> backlog;
    private List<DigitalResponse> urgentBacklog;
    private List<DigitalResponse> superUrgentBacklog;
    private List<DigitalResponse> toBeAllocated;

    @Before
    public void setUp() {
        bureauBacklogAllocateService = new BureauBacklogAllocateServiceImpl(responseRepo, userRepo, auditRepo);
        user1 = User.builder().name("Post Staff 1").username("staff1").active(true).build();
        user2 = User.builder().name("Post Staff 2").username("staff2").active(true).build();
        user3 = User.builder().name("Post Staff 3").username("staff3").active(true).build();

        doReturn(Arrays.asList(user1, user2, user3)).when(userRepo).findAllByUsernameIn(anyList());

        final LocalDateTime now = LocalDateTime.now();
        toBeAllocated = Lists.newLinkedList();

        //nonurgent backlog
        backlog = generateResponses(NON_URGENT_TO_ALLOCATE_TO_STAFF, false, false, now);
        Page<DigitalResponse> nonUrgentResponses = new PageImpl<>(backlog);

        //urgent backlog
        urgentBacklog = generateResponses(URGENT_TO_ALLOCATE_TO_STAFF, true, false, now);
        Page<DigitalResponse> urgentResponses = new PageImpl<>(urgentBacklog);

        //superUrgent backlog
        superUrgentBacklog = generateResponses(SUPER_URGENT_TO_ALLOCATE_TO_STAFF, false, true, now);
        Page<DigitalResponse> superUrgentResponses = new PageImpl<>(superUrgentBacklog);

        // If the service class doesn't use the correct parameters (for whatever reason) the tests will fall over
        // because the mock repo will return nothing


        doReturn(nonUrgentResponses).when(responseRepo).findAll(JurorResponseQueries.backlog(), PageRequest.of(
            0, NON_URGENT_TO_ALLOCATE_TO_STAFF, Sort.Direction.ASC, "dateReceived"));

        doReturn(urgentResponses).when(responseRepo).findAll(JurorResponseQueries.byStatusUrgent(), PageRequest.of(
            0, URGENT_TO_ALLOCATE_TO_STAFF, Sort.Direction.ASC, "dateReceived"));

        doReturn(superUrgentResponses).when(responseRepo).findAll(JurorResponseQueries.byStatusSuperUrgent(),
            PageRequest.of(
                0, SUPER_URGENT_TO_ALLOCATE_TO_STAFF, Sort.Direction.ASC, "dateReceived"));

        toBeAllocated.addAll(backlog);
        toBeAllocated.addAll(urgentBacklog);
        toBeAllocated.addAll(superUrgentBacklog);

    }

    /**
     * Allocates responses to staff members - happy Path.
     */
    @Test
    public void allocateReplies_happyPath() {
        //List<JurorResponse> toBeAllocated = backlog;
        bureauBacklogAllocateService.allocateBacklogReplies(BureauBacklogAllocateRequestDto.builder()
            .officerAllocations(Arrays.asList(
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(NON_URGENT_TO_ALLOCATE_TO_STAFF).urgentCount(URGENT_TO_ALLOCATE_TO_STAFF)
                    .superUrgentCount(SUPER_URGENT_TO_ALLOCATE_TO_STAFF).userId("staff1").build(),
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(NON_URGENT_TO_ALLOCATE_TO_STAFF).urgentCount(URGENT_TO_ALLOCATE_TO_STAFF)
                    .superUrgentCount(SUPER_URGENT_TO_ALLOCATE_TO_STAFF).userId("staff2").build(),
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(NON_URGENT_TO_ALLOCATE_TO_STAFF).urgentCount(URGENT_TO_ALLOCATE_TO_STAFF)
                    .superUrgentCount(SUPER_URGENT_TO_ALLOCATE_TO_STAFF).userId("staff3").build()
            )).build(), "loggedInUser");
        final LocalDateTime currentTime = LocalDateTime.now();

        // Difference will likely be < 1s, but allowing a margin of error for running on a _really_ slow
        // machine/build slave
        final LocalDate minusFiveSeconds =
            LocalDate.from(currentTime.minusSeconds(5));
        final LocalDate plusFiveSeconds =
            LocalDate.from(currentTime.plusSeconds(5));

        for (DigitalResponse testResponse : toBeAllocated) {
            if (testResponse.getStaffAssignmentDate() != null) {
                assertThat(testResponse.getStaff())
                    .describedAs("Backlog item should be assigned to a bureau officer")
                    .isNotNull();
                assertThat(testResponse.getStaffAssignmentDate())
                    .describedAs("Assignment date should be set to the time the auto-assign ran at")
                    .isNotNull()
                    .isBetween(minusFiveSeconds, plusFiveSeconds);
            }

        }
        verify(responseRepo, times(3)).saveAll(toBeAllocated);
    }


    /**
     * Tests that a BureauBacklogAllocateException is thrown if the requesting user is not provided.
     *
     * @throws Exception if the test falls over
     */
    @Test(expected = BureauBacklogAllocateException.RequestingUserIsRequired.class)
    public void allocateReplies_errorPath_requestingUserIsRequired() throws Exception {
        bureauBacklogAllocateService.allocateBacklogReplies(BureauBacklogAllocateRequestDto.builder()
            .officerAllocations(Arrays.asList(
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(NON_URGENT_TO_ALLOCATE_TO_STAFF).urgentCount(URGENT_TO_ALLOCATE_TO_STAFF)
                    .superUrgentCount(SUPER_URGENT_TO_ALLOCATE_TO_STAFF).userId("staff1").build(),
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(NON_URGENT_TO_ALLOCATE_TO_STAFF).urgentCount(URGENT_TO_ALLOCATE_TO_STAFF)
                    .superUrgentCount(SUPER_URGENT_TO_ALLOCATE_TO_STAFF).userId("staff2").build(),
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(NON_URGENT_TO_ALLOCATE_TO_STAFF).urgentCount(URGENT_TO_ALLOCATE_TO_STAFF)
                    .superUrgentCount(SUPER_URGENT_TO_ALLOCATE_TO_STAFF).userId("staff3").build()
            )).build(), null);
    }

    /**
     * Called by setup to generate the Juror Response Test/Retrieved data for each urgency category.
     *
     * @param responseCount No of responses to be allocated - requested.
     * @param urgent        urgent repsonse boolean.
     * @param superUrgent   superurgent response boolean.
     * @param now           datetime stamp.
     * @return List JurorResponses
     */
    private List<DigitalResponse> generateResponses(int responseCount, Boolean urgent, Boolean superUrgent,
                                                  LocalDateTime now) {

        List<DigitalResponse> responses = Lists.newLinkedList();
        for (int i = 0;
             i < responseCount;
             i++) {
            DigitalResponse response = new DigitalResponse();
            response.setJurorNumber(String.valueOf(i));
            response.setUrgent(urgent);
            response.setSuperUrgent(superUrgent);
            response.setDateReceived(now.minusHours(i));
            responses.add(response);
        }
        // This is the order the repository will return entries in
        responses.sort(ascendingDateOrder);
        return responses;
    }
}
